package net.zylklab.nifi.protobuffer.tfrecord.record;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.serialization.AbstractRecordSetWriter;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordSchema;

import net.zylklab.nifi.processor.protobuffer.hadoop.util.TFRecordWriter;
import net.zylklab.nifi.protobuffer.tfrecord.utils.TFRecordConfig;
import net.zylklab.nifi.protobuffer.tfrecord.utils.TFRecordUtils;

public class WriteTFRecordResult extends AbstractRecordSetWriter {

    private final RecordSchema schema;
    private final ComponentLog componentLogger;
    private final DataOutput dataOutput;
    private final TFRecordWriter writer; 
    private static Logger  _log = Logger.getLogger(WriteTFRecordResult.class);
    
    public WriteTFRecordResult(final RecordSchema recordSchema, final OutputStream out, final TFRecordConfig config, final ComponentLog componentLogger) throws IOException {
        super(out);
        this.schema = recordSchema;
        this.componentLogger = componentLogger;
        componentLogger.debug(String.format("Constructor of WriteTFRecordResult"));
        this.dataOutput = new DataOutputStream(out);
        this.writer = new TFRecordWriter(this.dataOutput);
    }

    @Override
    protected Map<String, String> writeRecord(final Record record) throws IOException {
        //final GenericRecord genericRecord = AvroTypeUtil.createAvroRecord(record, schema);
	componentLogger.debug(String.format("Writing nifi-record to the output flowfile as TFRecord proto"));
	TFRecordUtils.processRecords(record, this.writer, this.schema, this.componentLogger);
        return Collections.emptyMap();
    }

    @Override
    public void close() throws IOException {
	super.close();
    }

    @Override
    public String getMimeType() {
        return "application/protobuf";
    }
}
