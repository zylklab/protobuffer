package net.zylklab.nifi.protobuffer.tfrecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.schema.access.SchemaNotFoundException;
import org.apache.nifi.serialization.MalformedRecordException;
import org.apache.nifi.serialization.RecordReader;
import org.apache.nifi.serialization.RecordReaderFactory;
import org.apache.nifi.serialization.SchemaRegistryService;
import org.apache.nifi.serialization.SimpleRecordSchema;
import org.apache.nifi.serialization.record.RecordField;
import org.apache.nifi.serialization.record.RecordFieldType;
import org.apache.nifi.serialization.record.RecordSchema;
import org.apache.nifi.serialization.record.SchemaIdentifier;
import org.apache.nifi.serialization.record.StandardSchemaIdentifier;

import net.zylklab.nifi.protobuffer.tfrecord.record.ImageRecordReader;

@Tags({ "image", "parse", "record", "row", "reader" })
@CapabilityDescription("Parses image data and returns each iamge  as an separate Record object. The schema can be infferred itself, or the schema can be externalized and accessed by one of the methods offered by the 'Schema Access Strategy' property.")
public class ImageReader extends SchemaRegistryService implements RecordReaderFactory {

    private static Logger  _log = Logger.getLogger(ImageReader.class);
    
    public static final String GENERIC_IMAGE_SCHEMA_NAME = "Image";
    static final AllowableValue GENERIC_IMAGE_SCHEMA = new AllowableValue(GENERIC_IMAGE_SCHEMA_NAME, "Use Generic image Schema", "The schema will be the default image schema.");
    public static final PropertyDescriptor BUFFER_READER_SIZE = new PropertyDescriptor.Builder()
            .name("Image bytearray buffer reader")
            .description("The bytes of the byte[] buffer used to read the flowfile InputStream as ByteArrayOutputStream to convert to content field record")
            .required(true)
            .defaultValue("1024")
            .addValidator(StandardValidators.INTEGER_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor IMAGE_CONTENT_FIELD_NAME = new PropertyDescriptor.Builder()
            .name("Content Field Name for Generic Image Schema")
            .description("The name of the field wher the bytes of the image will be stored as bytes")
            .required(true)
            .defaultValue("content")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();
    
    
    
    private volatile RecordSchema recordSchema;
    private volatile Integer buffersize;
    private volatile String contentImageFieldName;
    
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> properties = new ArrayList<>(2);
        properties.add(BUFFER_READER_SIZE);
        properties.add(IMAGE_CONTENT_FIELD_NAME);
        return properties;
    }
    
    @OnEnabled
    public void onEnabled(final ConfigurationContext context) {
	_log.debug("On enabled init ::::::::::::::::::");
	this.buffersize = Integer.parseInt(context.getProperty(BUFFER_READER_SIZE).getValue());
	this.contentImageFieldName = context.getProperty(IMAGE_CONTENT_FIELD_NAME).getValue();
	final List<RecordField> fields = new ArrayList<>();
	RecordField r = new RecordField(this.contentImageFieldName, RecordFieldType.ARRAY.getArrayDataType(RecordFieldType.BYTE.getDataType()), true);
	fields.add(new RecordField(this.contentImageFieldName, r.getDataType(), true));
	SchemaIdentifier schemaIdentifier = new StandardSchemaIdentifier.Builder().name(GENERIC_IMAGE_SCHEMA_NAME).build();
	this.recordSchema = new SimpleRecordSchema(fields,schemaIdentifier);
	_log.debug("On enabled end ::::::::::::::::::");
    }
    
    @Override
    protected List<AllowableValue> getSchemaAccessStrategyValues() {
        final List<AllowableValue> allowableValues = new ArrayList<>();
        allowableValues.add(GENERIC_IMAGE_SCHEMA);
        return allowableValues;
    }
    
    @Override
    protected AllowableValue getDefaultSchemaAccessStrategy() {
        return GENERIC_IMAGE_SCHEMA;
    }
    
    @Override
    public RecordReader createRecordReader(Map<String, String> variables, InputStream in, ComponentLog logger) throws MalformedRecordException, IOException, SchemaNotFoundException {
//	BufferedInputStream reader = new BufferedInputStream(in);
//	logger.info(String.format(":::::Available bytes in the inputStream %s ",reader.available()));
//	int len;
//	while( (len = reader.read()) > 0) {
//	    logger.info(String.format(":::::Bytes read %s ",len));
//	}
//	logger.info(String.format(":::Processing a new flowfile with this recordSchema %s, InputStream %s, BufferInputStreamReader %s", this.recordSchema.toString(), in, reader));
//	return new ImageRecordReader(reader, this.recordSchema, this.buffersize, this.contentImageFieldName, logger);
	
	_log.debug("createRecordReader init ::::::::::::::::::");
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	byte[] buffer = new byte[this.buffersize];
	int len;
	while((len = in.read(buffer)) > 0) {
	    logger.info(String.format(":::::Bytes read %s ",len));
	    os.write(buffer,0, len);
	}
	RecordReader r = new ImageRecordReader(os.toByteArray(), this.recordSchema, this.buffersize, this.contentImageFieldName, logger);
	_log.debug("createRecordReader end :::::::::::::::::: "+os.toByteArray().length);
	return r;
    }
}
