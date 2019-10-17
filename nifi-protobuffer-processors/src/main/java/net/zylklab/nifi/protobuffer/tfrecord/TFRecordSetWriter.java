package net.zylklab.nifi.protobuffer.tfrecord;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.schema.access.SchemaNotFoundException;
import org.apache.nifi.serialization.RecordSetWriter;
import org.apache.nifi.serialization.RecordSetWriterFactory;
import org.apache.nifi.serialization.SchemaRegistryService;
import org.apache.nifi.serialization.record.RecordSchema;

import net.zylklab.nifi.protobuffer.tfrecord.record.WriteTFRecordResult;
import net.zylklab.nifi.protobuffer.tfrecord.utils.TFRecordConfig;

@Tags({ "tensorflow", "result", "set", "writer", "serializer", "tfrecord", "record", "protobuffer", "recordset", "row" })
@CapabilityDescription("Writes the contents of a RecordSet in TensorFlow Record format (Example).")
public class TFRecordSetWriter extends SchemaRegistryService implements RecordSetWriterFactory {

    private static Logger  _log = Logger.getLogger(TFRecordSetWriter.class);
    
    @Override
    public RecordSetWriter createWriter(ComponentLog logger, RecordSchema recordSchema, OutputStream out) throws SchemaNotFoundException, IOException {
	_log.debug("createWriter ..........................");
	final TFRecordConfig config = new TFRecordConfig();
	logger.debug("Creating the TFWriter");
	return new WriteTFRecordResult(recordSchema, out, config, logger);
    }

}
