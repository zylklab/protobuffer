## RecordWriter for TensorFlow

A RecordWriter that convert a Record or List<Record> to a tensorflow records 

A TFRecords file contains a sequence of strings with CRC32C (32-bit CRC using the Castagnoli polynomial) hashes. Each record has the format

```
uint64 length
uint32 masked_crc32_of_length
byte   data[length]
uint32 masked_crc32_of_data
```
and the records are concatenated together to produce the file. CRCs are [described](https://en.wikipedia.org/wiki/Cyclic_redundancy_check) here, and the mask of a CRC is

```
masked_crc = ((crc >> 15) | (crc << 17)) + 0xa282ead8ul
```

The structure of the TFRecord data is defined by Example and Feature proto files

**Features** there are three base Feature types:
 * bytes
 * float
 * int64
A Feature contains a Lists which may hold zero or more values. These lists are the base values BytesList, FloatList, Int64List. 

```proto
syntax = "proto3";
option cc_enable_arenas = true;
option java_outer_classname = "FeatureProtos";
option java_multiple_files = true;
option java_package = "org.tensorflow.example";
option go_package = "github.com/tensorflow/tensorflow/tensorflow/go/core/example";
package tensorflow;
message BytesList {
  repeated bytes value = 1;
}
message FloatList {
  repeated float value = 1 [packed = true];
}
message Int64List {
  repeated int64 value = 1 [packed = true];
}
message Feature {
  oneof kind {
    BytesList bytes_list = 1;
    FloatList float_list = 2;
    Int64List int64_list = 3;
  }
};
message Features {
  map<string, Feature> feature = 1;FileLookupService
};
message FeatureList {
  repeated Feature feature = 1;
};
message FeatureLists {
  map<string, FeatureList> feature_list = 1;
};
```

**Example** Protocol messages for describing input data Examples for machine learning model training or inference.
An Example is a mostly-normalized data format for storing data for training and inference.  It contains a key-value store (features); where each key (string) maps to a Feature message (which is oneof packed BytesList, FloatList, or Int64List).  This flexible and compact format allows the storage of large amounts of typed data, but requires that the data shape and use be determined by the configuration files and parsers that are used to read and write this format.  That is, the Example is mostly *not* a self-describing format.  In TensorFlow, Examples are read in row-major format, so any configuration that describes data with rank-2 or above should keep this in mind.  For example, to store an M x N matrix of Bytes, the BytesList must contain M*N bytes, with M rows of N contiguous values each.  That is, the BytesList value must store the matrix as:   .... row 0 .... .... row 1 .... // ...........  // ... row M-1 ....

**SequenceExample** Protocol message is not implemented yet  

```proto
syntax = "proto3";
import "feature.proto";
option cc_enable_arenas = true;
option java_outer_classname = "ExampleProtos";
option java_multiple_files = true;
option java_package = "org.tensorflow.example";
option go_package = "github.com/tensorflow/tensorflow/tensorflow/go/core/example";
package tensorflow;

message Example {
  Features features = 1;
};

message SequenceExample {
  Features context = 1;
  FeatureLists feature_lists = 2;
};

```

The mapper of the record field to TFRecord features follows this rules


 |Record|TensorFlow|Avro|
 |------|----------|----|
 |BOOLEAN|Not mapped|x|
 |CHOICE|Not mapped|x|
 |CHAR|Not mapped|x|
 |DATE|Not mapped|x|
 |MAP|Not mapped|x|
 |RECORD|Not mapped|x|
 |TIME|Not mapped|x|
 |TIMESTAMP|Not mapped|x|
 |BYTE|Not mapped|x|
 |ARRAY bytes[]|BytesList|Bytes|
 |ARRAY Bytes[]|BytesList|Bytes|
 |ARRAY Object[Byte]|BytesList|Bytes|
 |DOUBLE|FloatList|float|
 |FLOAT|FloatList|float|
 |BIGINT|Int64List|float|
 |INT|Int64List|float|
 |LONG|Int64List|long|
 |SHORT|Int64List|long|
 |STRING|BytesList|Bytes|

A NiFi template to test an example based on [flowers](https://storage.googleapis.com/download.tensorflow.org/example_images/flower_photos.tgz) dataset can be located at this location [**resources-ext/nifi/TFRecord-NiFi.xml**](resource-ext/nifi/TFRecord-NiFi.xml)  


###TODO List

 * Merge every data of a feature at the same list
 * Parse Arrays of data as feature list, not only single type of data


## Image Reader

A RecordReader that read the content of a image and write as record with a default schema with a unique field content of type Byte[] (avro bytes)

## FileLookupService

A FileRecordLookupService that reads the content of a file and add as a byte[] field to the original record


## NiFi Templates/Exmaples

### Read a set of images and create TFRecord
The text is extracted from the path of the image, and the lable (int) is mapped from a updata attribute processor, the content is read from the image as flowfile content

### Create a TFRecord from SQL query
The TFrecord is created from the fields of the SQL result.

### Create a TFRecord from SQL query and enrich with image as byte[]
The TFrecord is created from the fields of the SQL result and with FileLookupRecord the TFRecord is enriched with the content of the image, read from the filesystem.
 
 
## Tensorflow examples
Based on this article [Train Keras model with TensorFlow Estimators and Datasets API](https://androidkt.com/train-keras-model-with-tensorflow-estimators-and-datasets-api/) is posible to train a CNN using TFRecords stored at HDFS, and is posible to use HDFS as filesystem. There are two examples at this project, one standalone and one to use with [LinkedIn TonY project](https://github.com/linkedin/TonY/)

### Standalone keras model
The **train_simple.py** file is a python script based on the article [Train Keras model with TensorFlow Estimators and Datasets API](https://androidkt.com/train-keras-model-with-tensorflow-estimators-and-datasets-api/) modified to read and write data from/to hdfs. The Tfrecords are previuslay created from nifi flow and stored at HDFS. The params to launch the training are

 |Param|Value|Type|
 |------|----------|----|
 |CLASSPATH|hadoop classpath --glob|Environment Variable|
 |JAVA_HOME|/usr/lib/jvm/java-8-oracle|Environment Variable|
 |HADOOP_COMMON_LIB_NATIVE_DIR|/HADOOP_HOME/lib/native|Environment Variable|
 |HADOOP_HDFS_HOME|HADOOP_HOME|Environment Variable|
 |LD_LIBRARY_PATH|${JAVA_HOME}/jre/lib/amd64/server:${HADOOP_COMMON_LIB_NATIVE_DIR}:${LD_LIBRARY_PATH}|Environment Variable|
 |HADOOP_OPTS|-Djava.library.path=${LD_LIBRARY_PATH} -Dhadoop.security.authentication=kerberos|Environment Variable|
 |--tfrecord_path|hdfs://namenode:8020/apps/tony/data/cifar|Program param|
 |--estimator_path|hdfs://namenode:8020/tmp/kkt|Program param|
 |--export_model_path|hdfs://namenode:8020/tmp/export|Program param|
 

### Distributed keras model
 
The **train.py** file is a python script based on the article [Train Keras model with TensorFlow Estimators and Datasets API](https://androidkt.com/train-keras-model-with-tensorflow-estimators-and-datasets-api/) modified to read and write data from/to hdfs and to use in conjuntion with tony.xml and launcher.sh shell script.

```bash
#!/bin/bash
cd /home/zylk/tony
export TONY_CLASSPATH=`hadoop classpath --glob`
export TONY_CLASSPATH=$TONY_CLASSPATH:flowers:flowers/*:flowers/dist:flowers/dist/*
export HADOOP_COMMON_LIB_NATIVE_DIR=/usr/hdp/3.1.0.0-78/hadoop-hdfs/lib/native
export HADOOP_HDFS_HOME=/usr/hdp/3.1.0.0-78/hadoop-hdfs
export JAVA_HOME=/usr/java/default/
export KRB5CCNAME=/tmp/krb5cc_1000
export LD_LIBRARY_PATH=${JAVA_HOME}/jre/lib/amd64/server:${HADOOP_COMMON_LIB_NATIVE_DIR}:${LD_LIBRARY_PATH}
export HADOOP_OPTS="-Djava.library.path=${LD_LIBRARY_PATH} -Dhadoop.security.authentication=kerberos"
export CLASSPATH=${TONY_CLASSPATH}

echo "Starting the process ..."
TASK_PARAMS="--tfrecord_path hdfs://amaterasu001.bigdata.zylk.net:8020/apps/tony/data/cifar --estimator_path hdfs://amaterasu001.bigdata.zylk.net:8020/tmp/kkt --export_model_path hdfs://amaterasu001.bigdata.zylk.net:8020/tmp/export"
EXECUTE_PY_FILE="./models/cifar/train.py"
PYTHON_BIN="tensorflow1131-centos7/bin/python"
ENV_ZIP_FILE="venv.zip"
PYTHON_BIN="venv/bin/python"
```

Is tested with HDP 3.1 cluster
