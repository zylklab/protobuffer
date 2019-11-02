import tensorflow as tf
import os
import matplotlib.pyplot as plt

#{
#   "type" : "record",
#   "namespace" : "net.zylklab.avro.tensorflow",
#   "name" : "Image",
#   "fields" : [
#      { "name" : "content" , "type" : "bytes" },
#      { "name" : "text" , "type" : "string" },
#      { "name" : "label" , "type" : "int" }
#   ]
#}


IMG_SIZE=224
IMAGE_FEATURE_DESCRIPTION = {
    'label': tf.FixedLenFeature([], tf.int64),
    'text': tf.FixedLenFeature([], tf.string),  
    'content': tf.FixedLenFeature([], tf.string),
}

TRAIN_FILE_NIFI = "f385fdaa-1dfd-4934-a5bb-e36648cd26de.tfrecords"
FILENAME = os.path.join("/home/gus/flowers/nifi/images", TRAIN_FILE_NIFI)


def readDeprecated(filename):
    filename_queue = tf.train.string_input_producer([filename])
    reader = tf.TFRecordReader()
    file = tf.read_file(
        filename,
        name=None
    )
    return filename 

def readRecords(filename):
    records = tf.data.TFRecordDataset(
        filename,
        compression_type=None,
        buffer_size=None,
        num_parallel_reads=None
    )
    return records


def _decodeRecords(record):
    feature = tf.parse_single_example(record, IMAGE_FEATURE_DESCRIPTION)
    ##Preparo la imagen y devuelvo su label y la imagen transformada 
    image = feature['content']
    image = tf.image.decode_jpeg(image, channels=3)
    image = tf.cast(image, tf.float32)
    image = (image/127.5) - 1
    image = tf.image.resize(image, (IMG_SIZE, IMG_SIZE))
    return image, feature['label'], feature['text']
    
def main(unused_argv):
    #filedeprecated = readDeprecated(filename)
    #print(filedeprecated)
    #Cargo la lista de imagenes que está almacenada en foramto protobuffer
    records = readRecords(FILENAME)
    a = 0
    #Transformo la lista de records y construyo un dataset con una lista de elementos lista<binario de la image transformado, label>
    dataset = records.map(_decodeRecords)
    BATCH_SIZE = 32
    AUTOTUNE = tf.data.experimental.AUTOTUNE
    IMAGE_COUNT= 1 ##TODO: deberías el total de imagenes en el dataset
    ds = dataset.apply(tf.data.experimental.shuffle_and_repeat(buffer_size = IMAGE_COUNT))
    ds = dataset.shuffle(buffer_size = IMAGE_COUNT)
    ds = ds.repeat()
    ds = ds.batch(BATCH_SIZE)
    ds = ds.prefetch(buffer_size = AUTOTUNE)
    for image,label,text in ds.take(1):
        plt.title(text[3]+str(label[2]))
        plt.imshow(image[3])
        #print(text.numpy())
        #print(label.numpy())
        #print(image.numpy())
    plt.show()

if __name__ == "__main__":
    tf.enable_eager_execution()
    tf.app.run()

