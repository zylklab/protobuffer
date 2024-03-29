/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package net.zylklab.nifi.processor.protobuffer.hadoop.io;

import org.apache.hadoop.conf.Configuration;

public class TFRecordIOConf {
  static int getBufferSize(Configuration conf) {
    return conf.getInt("io.file.buffer.size", 4096);
  }

  static boolean getDoCrc32Check(Configuration conf) {
    return conf.getBoolean("tensorflow.read.crc32check", true);
  }
}
