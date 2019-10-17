package net.zylklab.nifi.protobuffer.tfrecord.record;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.serialization.MalformedRecordException;
import org.apache.nifi.serialization.RecordReader;
import org.apache.nifi.serialization.record.MapRecord;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordSchema;

public class ImageRecordReader implements RecordReader {

    private static Logger _log = Logger.getLogger(ImageRecordReader.class);
    private final Integer buffersize;
    private final String contentFieldName;
    private final ComponentLog logger;
    private RecordSchema schema;
    private final byte[] content;
    private boolean isread;

    public ImageRecordReader(byte[] content, RecordSchema schema, Integer buffersize, String contentFieldName, ComponentLog logger) throws IOException, MalformedRecordException {
	_log.info("ImageRecordReader init:::::::::::::::");
	this.buffersize = buffersize;
	this.logger = logger;
	this.logger.info(String.format("ImageRecordReader created with Content %s, RecordSechema %s, BufferSize %s, ContentFiledName %s", content, schema, buffersize, contentFieldName));
	this.contentFieldName = contentFieldName;
	this.schema = schema;
	this.content = content;
	this.isread = false;
	_log.info("ImageRecordReader end:::::::::::::::");
    }

    @Override
    public Record nextRecord(boolean coerceTypes, boolean dropUnknownFields) throws IOException, MalformedRecordException {
	_log.info("nextRecord init:::::::::::::::");
	Record r = null;
	if(!this.isread) {
        	this.logger.info(String.format("nextRecord 0:::::::::: %s, %s", this.contentFieldName, this.content.length));
        	final Map<String, Object> imageMap = new HashMap<>(1);
        	imageMap.put(this.contentFieldName, this.content);
        	this.logger.info(String.format("nextRecord 1:::::::::: %s, %s", this.contentFieldName, this.content.length));
        	r = new MapRecord(schema, imageMap);
        	this.logger.info(String.format("nextRecord 2:::::::::: %s, %s", this.contentFieldName, this.content.length));
        	_log.info("nextRecord end::::::::::::::: "+this.content.length);
        	this.isread = true;
	}
	return r;
    }

    @Override
    public RecordSchema getSchema() throws MalformedRecordException {
	return schema;
    }

    @Override
    public void close() throws IOException {
	
    }
}
