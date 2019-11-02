from __future__ import absolute_import, division, print_function
from numpy.random import randn

import pathlib
import random

import tensorflow as tf
import numpy as np

import logging
import os
import argparse
import json
import tensorboard.program as tb_program
from tensorflow.python.client import device_lib

#tf.enable_eager_execution()

AUTOTUNE = tf.data.experimental.AUTOTUNE
TB_PORT_ENV_VAR = 'TB_PORT'
tf.flags.DEFINE_string('working_dir', '/tmp/tensorflow/mnist/working_dir', 'Directory under which events and output will be stored (in separate subdirectories).')
tf.flags.DEFINE_integer("steps", 1500, "The number of training steps to execute.")
tf.flags.DEFINE_integer("batch_size", 32, "The batch size per step.")
tf.flags.DEFINE_integer("image_size",32, "The image size")
tf.flags.DEFINE_float("learning_rate",.01,"learning rate for gradient descent, default=.001")
FLAGS = tf.flags.FLAGS

IMAGE_FEATURE_DESCRIPTION = {
    'label': tf.FixedLenFeature([], tf.int64),
    'text': tf.FixedLenFeature([], tf.string),
    'content': tf.FixedLenFeature([], tf.string),
}


def readRecords(filename):
    records = tf.data.TFRecordDataset(
        filename,
        compression_type=None,
        buffer_size=None,
        num_parallel_reads=None
    )
    return records

def start_tensorboard(logdir):
    tb = tb_program.TensorBoard()
    port = int(os.getenv(TB_PORT_ENV_VAR, 6006))
    tb.configure(logdir=logdir, port=port)
    tb.launch()
    logging.info("Starting TensorBoard with --logdir=%s" % logdir)

def _parse_image_function(example_proto):
  # Parse the input tf.Example proto using the dictionary above.
  feature=tf.parse_single_example(example_proto, IMAGE_FEATURE_DESCRIPTION)
  image = feature['content']
  image = tf.image.decode_jpeg(image, channels=3)
#   image = tf.image.resize_images(image, [224, 224])
#   image /= 255.0  # normalize to [0,1] range

  image = tf.cast(image, tf.float32)
  image = (image/127.5) - 1
  image = tf.image.resize(image, (FLAGS.image_size, FLAGS.image_size))

  #return feature['label'],feature['text'], image 
  return image, feature['label']
  
  

def print_available_devices():
    local_device_protos = device_lib.list_local_devices()
    for x in local_device_protos:
        logging.info("x.name::::::::::::::::"+str(x.name))
        logging.info("x.device_type::::::::::::::::"+str(x.device_type))
    return


def create_model(model_dir, config, learning_rate):
    #global_step = tf.train.get_or_create_global_step()
    IMG_SHAPE = (FLAGS.image_size, FLAGS.image_size, 3)
    #IMG_SHAPE = (FLAGS.image_size, FLAGS.image_size)
    VGG16_MODEL=tf.keras.applications.vgg16.VGG16(input_shape=IMG_SHAPE,include_top=False,weights='imagenet')
    NUMBER_OF_LABELS = 5
    VGG16_MODEL.trainable=True
    global_average_layer = tf.keras.layers.GlobalAveragePooling2D()
    prediction_layer = tf.keras.layers.Dense(NUMBER_OF_LABELS,activation=tf.nn.softmax)
    #VGG16_MODEL.summary()
    model = tf.keras.Sequential([
                                VGG16_MODEL,
                                global_average_layer,
                                prediction_layer
                                ])
    
    optimizer = tf.train.GradientDescentOptimizer(learning_rate=learning_rate)
    model.compile(optimizer=optimizer,loss='sparse_categorical_crossentropy',metrics=['accuracy'])
    tf.keras.backend.set_learning_phase(True)
    model.summary()
    estimator = tf.keras.estimator.model_to_estimator(keras_model=model, model_dir=model_dir, config=config)
    #return global_step, model
    return estimator

def input_fn(dataset, mode, buffer_size, batch_size):
    if mode == tf.estimator.ModeKeys.TRAIN:
        dataset = dataset.shuffle(buffer_size=buffer_size).repeat().batch(batch_size)
        dataset = dataset.prefetch(100)
    if mode in (tf.estimator.ModeKeys.EVAL, tf.estimator.ModeKeys.PREDICT):
        dataset = dataset.batch(batch_size)
        
    print(dataset)
    return dataset

def get_distribution_strategy():
    cluster_spec = os.environ.get("CLUSTER_SPEC", None)
    if cluster_spec:
        cluster_spec = json.loads(cluster_spec)
        job_index = int(os.environ["TASK_INDEX"])
        job_type = os.environ["JOB_NAME"]
        # Build cluster spec
        os.environ['TF_CONFIG'] = json.dumps({'cluster': cluster_spec,'task': {'type': job_type, 'index': job_index}})
        print('Distribution enabled: ', os.environ['TF_CONFIG'])
    else:
        print('Distribution is not enabled')


def _get_session_config_from_env_var():
    """Returns a tf.ConfigProto instance with appropriate device_filters set."""
    tf_config = json.loads(os.environ.get('TF_CONFIG', '{}'))
    print(tf_config)
    # GPU limit: TensorFlow by default allocates all GPU memory:
    # If multiple workers run in same host you may see OOM errors:
    # Use as workaround if not using Hadoop 3.1
    # Change percentage accordingly:
    # gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.25)

    if (tf_config and 'task' in tf_config and 'type' in tf_config['task'] and 'index' in tf_config['task']):
        # Master should only communicate with itself and ps.
        if tf_config['task']['type'] == 'master':
            return tf.ConfigProto(device_filters=['/job:ps', '/job:master'])
        # Worker should only communicate with itself and ps.
        elif tf_config['task']['type'] == 'worker':
            return tf.ConfigProto(#gpu_options=gpu_options,
                                  device_filters=['/job:ps','/job:worker/task:%d' % tf_config['task']['index']
                                  ])
    return None

def serving_input_fn():
    """Defines the features to be passed to the model during inference.
    Expects already tokenized and padded representation of sentences
    Returns:
      A tf.estimator.export.ServingInputReceiver
    """
    feature_placeholder = tf.placeholder(tf.float32, [None, 32 * 32, 3])
    features = feature_placeholder
    print("::::::::::::::::::::::::::"+str(feature_placeholder))
    return tf.estimator.export.TensorServingInputReceiver(features,feature_placeholder)

def train_and_evaluate():
    
    logging.info('Extracting and loading input data...')
    TRAIN_HDFS = "hdfs://amaterasu001.bigdata.zylk.net:8020/apps/tony/data/flowers/1fc8bd47-bb1f-4034-8ab2-0ac00c7f3518.tfrecords"
    image_dataset = readRecords(TRAIN_HDFS)
    BATCH_SIZE = FLAGS.batch_size
    BUFFER_SIZE = 3670 #image_size 
    dataset = image_dataset.map(_parse_image_function)
    train_steps = BUFFER_SIZE / BATCH_SIZE
    logging.info('Starting training')
    get_distribution_strategy()
    hook = tf.train.ProfilerHook(save_steps=100,output_dir=FLAGS.working_dir,show_memory=True)
    
    run_config = tf.estimator.RunConfig(
                    experimental_distribute=tf.contrib.distribute.DistributeConfig(train_distribute=tf.contrib.distribute.ParameterServerStrategy(),eval_distribute=tf.contrib.distribute.MirroredStrategy()),
                    session_config=_get_session_config_from_env_var(),
                    model_dir=FLAGS.working_dir,
                    save_summary_steps=100,
                    log_step_count_steps=100,
                    save_checkpoints_steps=500)
    estimator = create_model(model_dir=FLAGS.working_dir,config=run_config,learning_rate=FLAGS.learning_rate)
    print(estimator)
    #history = model.fit(ds,steps_per_epoch=5,epochs=20)
    
    
    train_spec = tf.estimator.TrainSpec(input_fn=lambda: input_fn(dataset=dataset,mode=tf.estimator.ModeKeys.TRAIN,buffer_size=BUFFER_SIZE, batch_size=BATCH_SIZE),max_steps=train_steps)
    print(train_spec)
    # Create EvalSpec.
#    exporter = tf.estimator.FinalExporter('exporter', serving_input_fn)
#    eval_spec = tf.estimator.EvalSpec(input_fn=lambda: input_fn(dataset=dataset, mode=tf.estimator.ModeKeys.EVAL,buffer_size=BUFFER_SIZE, batch_size=BATCH_SIZE),steps=None,name='mnist-eval',exporters=[exporter],start_delay_secs=10,throttle_secs=10)
    eval_spec = tf.estimator.EvalSpec(input_fn=lambda: input_fn(dataset=dataset, mode=tf.estimator.ModeKeys.EVAL,buffer_size=BUFFER_SIZE, batch_size=BATCH_SIZE),steps=None,name='mnist-eval')
    print(eval_spec)
    # Launch Tensorboard in a separate thread.
#    tf.gfile.MakeDirs(FLAGS.working_dir)
#    start_tensorboard(FLAGS.working_dir)
    # Start training
    tf.estimator.train_and_evaluate(estimator, train_spec, eval_spec)


if __name__ == '__main__':
    #tf.app.run()
    train_and_evaluate()
