// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: example.proto

package org.tensorflow.example;

public interface SequenceExampleOrBuilder extends
    // @@protoc_insertion_point(interface_extends:tensorflow.SequenceExample)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional .tensorflow.Features context = 1;</code>
   */
  boolean hasContext();
  /**
   * <code>optional .tensorflow.Features context = 1;</code>
   */
  org.tensorflow.example.Features getContext();
  /**
   * <code>optional .tensorflow.Features context = 1;</code>
   */
  org.tensorflow.example.FeaturesOrBuilder getContextOrBuilder();

  /**
   * <code>optional .tensorflow.FeatureLists feature_lists = 2;</code>
   */
  boolean hasFeatureLists();
  /**
   * <code>optional .tensorflow.FeatureLists feature_lists = 2;</code>
   */
  org.tensorflow.example.FeatureLists getFeatureLists();
  /**
   * <code>optional .tensorflow.FeatureLists feature_lists = 2;</code>
   */
  org.tensorflow.example.FeatureListsOrBuilder getFeatureListsOrBuilder();
}
