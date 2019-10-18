import tensorflow as tf
import os
import matplotlib.pyplot as plt

#{
#   "type" : "record",
#   "namespace" : "net.zylklab.avro.tensorflow",
#   "name" : "Image",
#   "fields" : [
#      { "name" : "user" , "type" : "string" },
#      { "name" : "userId" , "type" : "int" },
#      { "name" : "message" , "type" : "string" },
#      { "name" : "timetrack" , "type" : "double"}
#   ]
#}


FEATURES_DESCRIPTION = {
    'userId': tf.FixedLenFeature([], tf.int64),
    'user': tf.FixedLenFeature([], tf.string),  
    'message': tf.FixedLenFeature([], tf.string),
    'timetrack': tf.FixedLenFeature([], tf.float32),
}
TRAIN_FILE_NIFI = "03427ccb-19a5-4628-92b0-b0d5a517f8c2.tfrecords"
FILENAME = os.path.join("/home/gus/flowers/nifi/momo", TRAIN_FILE_NIFI)

def readRecords(filename):
    records = tf.data.TFRecordDataset(
        filename,
        compression_type=None,
        buffer_size=None,
        num_parallel_reads=None
    )
    return records


def _decodeRecords(record):
    feature = tf.parse_single_example(record, FEATURES_DESCRIPTION)
    return feature['userId'], feature['user'], feature['message'], feature['timetrack']
    
def main(unused_argv):
    records = readRecords(FILENAME)
    dataset = records.map(_decodeRecords)
    dataset = dataset.shuffle(1024).batch(32).prefetch(tf.data.experimental.AUTOTUNE)

    

if __name__ == "__main__":
    tf.enable_eager_execution()
    tf.app.run()

