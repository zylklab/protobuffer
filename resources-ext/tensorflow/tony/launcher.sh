#!/bin/bash
cd /home/zylk/tony
export TONY_CLASSPATH=`hadoop classpath --glob`
export TONY_CLASSPATH=$TONY_CLASSPATH:flowers:flowers/*:flowers/dist:flowers/dist/*
#export HADOOP_COMMON_LIB_NATIVE_DIR=/home/gus/usr/local/hadoop/hadoop-3.1.0/lib/native
export HADOOP_COMMON_LIB_NATIVE_DIR=/usr/hdp/3.1.0.0-78/hadoop-hdfs/lib/native
#export HADOOP_HDFS_HOME=/home/gus/usr/local/hadoop/hadoop-3.1.0
export HADOOP_HDFS_HOME=/usr/hdp/3.1.0.0-78/hadoop-hdfs
#export HADOOP_OPTS="-Djava.library.path=${LD_LIBRARY_PATH} -Dhadoop.security.authentication=kerberos"
#export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export JAVA_HOME=/usr/java/default/
export KRB5CCNAME=/tmp/krb5cc_1000
export LD_LIBRARY_PATH=${JAVA_HOME}/jre/lib/amd64/server:${HADOOP_COMMON_LIB_NATIVE_DIR}:${LD_LIBRARY_PATH}
export HADOOP_OPTS="-Djava.library.path=${LD_LIBRARY_PATH} -Dhadoop.security.authentication=kerberos"
export CLASSPATH=${TONY_CLASSPATH}

echo "Starting the process ..."
TB_PORT_ENV_VAR = 'TB_PORT'
tf.flags.DEFINE_string('tfrecord_path'      , '/home/gus/Descargas/cifar/tfrecords', 'The base path where the tfrecords are stored, hdfs://namenode is valid protocol')
tf.flags.DEFINE_string('tftraining_file'    , 'train.tfrecords', 'The filename with the training data is stored as tfrecods')
tf.flags.DEFINE_string('tftesting_file'     , 'test.tfrecords', 'The filename with the test data is stored as tfrecods')
tf.flags.DEFINE_string('estimator_path'     , 'kkt', 'The working path used by the estimator to process the data')
tf.flags.DEFINE_string('export_model_path'  , './export', 'The path where the model is exported as .pb file')
tf.flags.DEFINE_boolean('plot_enabeld'  , False, 'If we want to plot the accuracy of the model, only for local training')
TASK_PARAMS="--tfrecord_path hdfs://amaterasu001.bigdata.zylk.net_8020//apps/tony/data/cifar -tftraining_file train.tfrecords --tftesting_file test.tfrecords --estimator_path kkt --export_model_path ./export --plot_enabeld False"
EXECUTE_PY_FILE = ./models/cifar10/train.py

java -cp ${TONY_CLASSPATH} com.linkedin.tony.cli.ClusterSubmitter --python_venv=./flowers/dist/tensorflow1131.zip --src_dir=./flowers/src --executes=${EXECUTE_PY_FILE} --task_params="${TASK_PARAMS}" --conf_file=./flowers/tony.xml --python_binary_path=tensorflow1131/bin/python --shell_env "LD_LIBRARY_PATH=${LD_LIBRARY_PATH} HADOOP_HDFS_HOME=${HADOOP_HDFS_HOME} HADOOP_COMMON_LIB_NATIVE_DIR=${HADOOP_COMMON_LIB_NATIVE_DIR} JAVA_HOME=${JAVA_HOME} HADOOP_OPTS=${HADOOP_OPTS} CLASSPATH=${CLASSPATH}"