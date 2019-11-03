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
#TASK_PARAMS="--tfrecord_path hdfs://amaterasu001.bigdata.zylk.net_8020/apps/tony/data/cifar -tftraining_file train.tfrecords --tftesting_file test.tfrecords --estimator_path kkt --export_model_path ./export --plot_enabled False"
TASK_PARAMS="--tfrecord_path hdfs://amaterasu001.bigdata.zylk.net:8020/apps/tony/data/cifar"
EXECUTE_PY_FILE="./models/cifar/train.py"
#ENV_ZIP_FILE="tensorflow1131-centos7.zip"
PYTHON_BIN="tensorflow1131-centos7/bin/python"
ENV_ZIP_FILE="venv.zip"
PYTHON_BIN="venv/bin/python"

java -cp ${TONY_CLASSPATH} com.linkedin.tony.cli.ClusterSubmitter --python_venv=./python-envs/${ENV_ZIP_FILE} --src_dir=./flowers/src --executes=${EXECUTE_PY_FILE} --task_params="${TASK_PARAMS}" --conf_file=./flowers/tony.xml --python_binary_path=${PYTHON_BIN} --shell_env "LD_LIBRARY_PATH=${LD_LIBRARY_PATH} HADOOP_HDFS_HOME=${HADOOP_HDFS_HOME} HADOOP_COMMON_LIB_NATIVE_DIR=${HADOOP_COMMON_LIB_NATIVE_DIR} JAVA_HOME=${JAVA_HOME} HADOOP_OPTS=${HADOOP_OPTS} CLASSPATH=${CLASSPATH}"