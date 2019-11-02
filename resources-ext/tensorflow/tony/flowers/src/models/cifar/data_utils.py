import os
from os import listdir
from os.path import join

import tensorflow as tf

def dataset_input_fn(filenames, num_epochs, image_size):
    dataset = tf.data.TFRecordDataset(filenames)

    def parser(record):
        featdef = {
            'image': tf.FixedLenFeature(shape=[], dtype=tf.string),
            'labels': tf.FixedLenFeature(shape=[], dtype=tf.string),
        }

        example = tf.parse_single_example(record, featdef)
        im = tf.decode_raw(example['image'], tf.float32)
        im = tf.reshape(im, (32, 32, 3))
        #im = tf.reshape(im, (image_size, image_size, 3))
        lbl = tf.decode_raw(example['labels'], tf.float32)
        return im, lbl

    dataset = dataset.map(parser)
    dataset = dataset.shuffle(buffer_size=10000)
    dataset = dataset.batch(128)
    dataset = dataset.repeat(num_epochs)
    iterator = dataset.make_one_shot_iterator()
    features, labels = iterator.get_next()

    return features, labels