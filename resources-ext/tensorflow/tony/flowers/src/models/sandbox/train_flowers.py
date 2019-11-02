from __future__ import absolute_import, division, print_function
from numpy.random import randn

import pathlib
import random

import tensorflow as tf
import numpy as np



#tf.enable_eager_execution()

AUTOTUNE = tf.data.experimental.AUTOTUNE

def readRecords(filename):
    records = tf.data.TFRecordDataset(
        filename,
        compression_type=None,
        buffer_size=None,
        num_parallel_reads=None
    )
    return records


IMG_SIZE=32
IMAGE_FEATURE_DESCRIPTION = {
    'label': tf.FixedLenFeature([], tf.int64),
    'text': tf.FixedLenFeature([], tf.string),
    'content': tf.FixedLenFeature([], tf.string),
}

def _parse_image_function(example_proto):
  # Parse the input tf.Example proto using the dictionary above.
  feature=tf.parse_single_example(example_proto, IMAGE_FEATURE_DESCRIPTION)
  image = feature['content']
  image = tf.image.decode_jpeg(image, channels=3)
#   image = tf.image.resize_images(image, [224, 224])
#   image /= 255.0  # normalize to [0,1] range

  image = tf.cast(image, tf.float32)
  image = (image/127.5) - 1
  image = tf.image.resize(image, (IMG_SIZE, IMG_SIZE))

  #return feature['label'],feature['text'], image 
  return image, feature['label']

def main(_):
        #records = readRecords(FILENAME)
        #image_dataset = tf.data.TFRecordDataset('flower.tfrecords')
        #TRAIN_HDFS = "hdfs://amaterasu001.bigdata.zylk.net:8020/apps/tony/data/flowers/1fc8bd47-bb1f-4034-8ab2-0ac00c7f3518.tfrecords"
        #image_dataset = readRecords(TRAIN_HDFS)
        TRAIN_LOCALFS = "/home/gus/flowers/nifi/images/1fc8bd47-bb1f-4034-8ab2-0ac00c7f3518.tfrecords"
        image_dataset = readRecords(TRAIN_LOCALFS)
        BATCH_SIZE = 32
        BUFFER_SIZE = 3670 #image_size 
        dataset = image_dataset.map(_parse_image_function)
        
        ds = dataset.apply(tf.data.experimental.shuffle_and_repeat(buffer_size=BUFFER_SIZE))
        ds = dataset.shuffle(buffer_size=BUFFER_SIZE)
        ds = ds.repeat()
        ds = ds.batch(BATCH_SIZE)
        ds = ds.prefetch(buffer_size=AUTOTUNE)
        
        # for image,label,text in ds.take(1):
        #   plt.title(text.numpy())
        #   plt.imshow(image)
        
        IMG_SHAPE = (IMG_SIZE, IMG_SIZE, 3)
        VGG16_MODEL=tf.keras.applications.vgg16.VGG16(input_shape=IMG_SHAPE,include_top=False,weights='imagenet')
        NUMBER_OF_LABELS = 5
        
        VGG16_MODEL.trainable=False
        global_average_layer = tf.keras.layers.GlobalAveragePooling2D()
        prediction_layer = tf.keras.layers.Dense(NUMBER_OF_LABELS,activation=tf.nn.softmax)
        VGG16_MODEL.summary()
        model = tf.keras.Sequential([VGG16_MODEL,global_average_layer,prediction_layer])
        base_learning_rate = 0.0001
        model.compile(optimizer=tf.keras.optimizers.RMSprop(lr=base_learning_rate),loss='sparse_categorical_crossentropy',metrics=['accuracy'])
        model.summary()
        history = model.fit(ds,steps_per_epoch=5,epochs=20)

if __name__ == '__main__':
    tf.app.run()

