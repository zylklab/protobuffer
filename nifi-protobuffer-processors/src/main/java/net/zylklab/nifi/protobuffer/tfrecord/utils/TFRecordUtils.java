package net.zylklab.nifi.protobuffer.tfrecord.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordField;
import org.apache.nifi.serialization.record.RecordFieldType;
import org.apache.nifi.serialization.record.RecordSchema;
import org.apache.nifi.serialization.record.util.DataTypeUtils;
import org.tensorflow.example.BytesList;
import org.tensorflow.example.Example;
import org.tensorflow.example.Feature;
import org.tensorflow.example.Features;
import org.tensorflow.example.FloatList;
import org.tensorflow.example.Int64List;

import com.google.protobuf.ByteString;

import net.zylklab.nifi.processor.protobuffer.hadoop.util.TFRecordWriter;

public class TFRecordUtils {

    private static Logger _log = Logger.getLogger(TFRecordUtils.class);

    public static final PropertyDescriptor ENABLE_DICTIONARY_ENCODING = new PropertyDescriptor.Builder().name("enable-dictionary-encoding").displayName("Enable Dictionary Encoding").description("Specifies whether dictionary encoding should be enabled for the TFRecord writer")
	    .allowableValues("true", "false").build();

    public static final PropertyDescriptor ENABLE_VALIDATION = new PropertyDescriptor.Builder().name("enable-validation").displayName("Enable Validation").description("Specifies whether validation should be enabled for the TFRecord writer").allowableValues("true", "false")
	    .build();

    /**
     * Creates a ParquetConfig instance from the given PropertyContext.
     *
     * @param context   the PropertyContext from a component
     * @param variables an optional set of variables to evaluate EL against, may be
     *                  null
     * @return the ParquetConfig
     */
    public static TFRecordConfig createTensorFlowConfig(final PropertyContext context, final Map<String, String> variables) {
	final TFRecordConfig config = new TFRecordConfig();

	if (context.getProperty(ENABLE_DICTIONARY_ENCODING).isSet()) {
	    final boolean enableDictionaryEncoding = context.getProperty(ENABLE_DICTIONARY_ENCODING).asBoolean();
	    config.setEnableDictionaryEncoding(enableDictionaryEncoding);
	}

	if (context.getProperty(ENABLE_VALIDATION).isSet()) {
	    final boolean enableValidation = context.getProperty(ENABLE_VALIDATION).asBoolean();
	    config.setEnableValidation(enableValidation);
	}

	return config;
    }

    private static byte[] toPrimitive(Byte[] originalArray) {
	byte[] bytes = new byte[originalArray.length];
	for (int i = 0; i < originalArray.length; i++) {
	    bytes[i] = originalArray[i].byteValue();
	}
	return bytes;
    }

    private static byte[] toPrimitive(Object[] originalArray) {
	byte[] bytes = new byte[originalArray.length];
	for (int i = 0; i < originalArray.length; i++) {
	    bytes[i] = ((Byte)originalArray[i]).byteValue();
	}
	return bytes;
    }

    private static Example convert2Example(Record record, RecordSchema schema, ComponentLog componentLogger) throws UnsupportedEncodingException {
	_log.info("Convert NifiRecord to TF Example proto");
	componentLogger.debug(String.format("Convert NifiRecord to TF Example proto"));
	Features.Builder features = Features.newBuilder();
	Feature.Builder feature = null;
	for (final RecordField field : schema.getFields()) {

	    final String fieldName = field.getFieldName();
	    final RecordFieldType type = field.getDataType().getFieldType();
	    final Object value = record.getValue(field);
	    componentLogger.debug(String.format("Writing the fieldname %s of type %s", fieldName, type.toString()));
	    _log.info(String.format("Writing the fieldname %s of type %s", fieldName, type.toString()));
	    switch (type) {
	    case BOOLEAN:
	    case CHOICE:
	    case CHAR:
	    case DATE:
	    case MAP:
	    case RECORD:
	    case TIME:
	    case TIMESTAMP:
	    case BYTE:
		componentLogger.warn(String.format("Unsupporte data type conversion %s at field %s with value %s", type.toString(), fieldName, value));
		break;

	    case ARRAY:
		final byte[] javaBytesValue;
		if (value instanceof byte[]) {
		    javaBytesValue = (byte[]) value;
		    _log.info(String.format("Content type byte[] detected the size is %s, and the field name is %s ",javaBytesValue.length, fieldName));
		    componentLogger.debug(String.format("Content type byte[] detected the size is %s, and the field name is %s ",javaBytesValue.length, fieldName));
		} else if (value instanceof Byte[]) {
		    javaBytesValue = toPrimitive(((Byte[]) value));
		    _log.info(String.format("Content type Byte[] detected the size is %s, and the field name is %s ",javaBytesValue.length, fieldName));
		    componentLogger.debug(String.format("Content type Byte[] detected the size is %s, and the field name is %s ",javaBytesValue.length, fieldName));
		} else if (value instanceof Object[] && ((Object[]) value)[0] instanceof Byte) {
		    javaBytesValue = toPrimitive(((Object[]) value));
		    _log.info(String.format("Content type Object[Byte] detected the size is %s, and the field name is %s ",javaBytesValue.length, fieldName));
		    componentLogger.debug(String.format("Content type Object[Byte] detected the size is %s, and the field name is %s ",javaBytesValue.length, fieldName));
		} else {
		    Object[] a = (Object[]) value;
		    componentLogger.debug(String.format("Unsupporte data type conversion %s at field %s with value type: (%s) - subtype: (%s)  only Arrays of bytes are valid array types", type.toString(), fieldName, value.getClass().getSimpleName(), a[0].getClass().getSimpleName()));
		    _log.warn(String.format("Unsupporte data type conversion %s at field %s with value type: (%s) - subtype: (%s)  only Arrays of bytes are valid array types", type.toString(), fieldName, value.getClass().getSimpleName(), a[0].getClass().getSimpleName()));
		    break;
		}
		feature = Feature.newBuilder();
		BytesList.Builder tfBvalue = BytesList.newBuilder();
		tfBvalue.addValue(ByteString.copyFrom(javaBytesValue));
		feature.setBytesList(tfBvalue.build());
		features.putFeature(fieldName, feature.build());
		break;

	    case DOUBLE:
		Double javaDoubleValue = DataTypeUtils.toDouble(value, fieldName);
		componentLogger.debug(String.format("Content type Double detected the size is %s, and the field name is %s ",javaDoubleValue, fieldName));
		_log.info(String.format("Content type Double detected the size is %s, and the field name is %s ",javaDoubleValue, fieldName));
		feature = Feature.newBuilder();
		FloatList.Builder tfDoubleValue = FloatList.newBuilder();
		tfDoubleValue.addValue(javaDoubleValue.floatValue());
		feature.setFloatList(tfDoubleValue.build());
		features.putFeature(fieldName, feature.build());
		break;

	    case FLOAT:
		Float javaFloatValue = DataTypeUtils.toFloat(value, fieldName);
		componentLogger.debug(String.format("Content type Float detected the size is %s, and the field name is %s ",javaFloatValue, fieldName));
		_log.info(String.format("Content type Float detected the size is %s, and the field name is %s ",javaFloatValue, fieldName));
		feature = Feature.newBuilder();
		FloatList.Builder tfFloatValue = FloatList.newBuilder();
		tfFloatValue.addValue(javaFloatValue.floatValue());
		feature.setFloatList(tfFloatValue.build());
		features.putFeature(fieldName, feature.build());
		break;

	    case BIGINT:
		BigInteger javaBigIntValue = DataTypeUtils.toBigInt(value, fieldName);
		componentLogger.debug(String.format("Content type BIGINT detected the size is %s, and the field name is %s ",javaBigIntValue, fieldName));
		_log.info(String.format("Content type BIGINT detected the size is %s, and the field name is %s ",javaBigIntValue, fieldName));
		feature = Feature.newBuilder();
		Int64List.Builder tfBigIntValue = Int64List.newBuilder();
		tfBigIntValue.addValue(javaBigIntValue.longValue());
		feature.setInt64List(tfBigIntValue.build());
		features.putFeature(fieldName, feature.build());
		break;
	    case INT:
		Integer javaIntValue = DataTypeUtils.toInteger(value, fieldName);
		componentLogger.debug(String.format("Content type INT detected the size is %s, and the field name is %s ",javaIntValue, fieldName));
		_log.info(String.format("Content type INT detected the size is %s, and the field name is %s ",javaIntValue, fieldName));
		feature = Feature.newBuilder();
		Int64List.Builder tfIntValue = Int64List.newBuilder();
		tfIntValue.addValue(javaIntValue.longValue());
		feature.setInt64List(tfIntValue.build());
		features.putFeature(fieldName, feature.build());
		break;
	    case LONG:
		Long javaLongValue = DataTypeUtils.toLong(value, fieldName);
		componentLogger.debug(String.format("Content type LONG detected the size is %s, and the field name is %s ",javaLongValue, fieldName));
		_log.info(String.format("Content type LONG detected the size is %s, and the field name is %s ",javaLongValue, fieldName));
		feature = Feature.newBuilder();
		Int64List.Builder tfLongValue = Int64List.newBuilder();
		tfLongValue.addValue(javaLongValue.longValue());
		feature.setInt64List(tfLongValue.build());
		features.putFeature(fieldName, feature.build());
		break;

	    case SHORT:
		Short javaShortValue = DataTypeUtils.toShort(value, fieldName);
		componentLogger.debug(String.format("Content type SHORT detected the size is %s, and the field name is %s ",javaShortValue, fieldName));
		_log.info(String.format("Content type SHORT detected the size is %s, and the field name is %s ",javaShortValue, fieldName));
		feature = Feature.newBuilder();
		Int64List.Builder tfShortValue = Int64List.newBuilder();
		tfShortValue.addValue(javaShortValue.longValue());
		feature.setInt64List(tfShortValue.build());
		features.putFeature(fieldName, feature.build());
		break;

	    case STRING:
		String javaStringValue = DataTypeUtils.toString(value, fieldName);
		componentLogger.debug(String.format("Content type STRING detected the size is %s, and the field name is %s ",javaStringValue, fieldName));
		_log.info(String.format("Content type STRING detected the size is %s, and the field name is %s ",javaStringValue, fieldName));
		feature = Feature.newBuilder();
		BytesList.Builder tfSvalue = BytesList.newBuilder();
		tfSvalue.addValue(ByteString.copyFrom(javaStringValue.getBytes("UTF-8")));
		feature.setBytesList(tfSvalue.build());
		features.putFeature(fieldName, feature.build());
		break;
	    default:
		break;
	    }
	}
	Example.Builder example = Example.newBuilder();
	example.setFeatures(features.build());
	return example.build();
    }

    public static void processRecords(Record record, TFRecordWriter writer, RecordSchema schema, ComponentLog componentLogger) throws IOException {
	Example example = convert2Example(record, schema, componentLogger);
	writer.write(example.toByteArray());
    }
}
