package net.zylklab.nifi.protobuffer.lookup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.lookup.LookupFailureException;
import org.apache.nifi.lookup.RecordLookupService;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.serialization.SimpleRecordSchema;
import org.apache.nifi.serialization.record.MapRecord;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordField;
import org.apache.nifi.serialization.record.RecordFieldType;
import org.apache.nifi.serialization.record.RecordSchema;
import org.apache.nifi.serialization.record.SchemaIdentifier;
import org.apache.nifi.serialization.record.StandardSchemaIdentifier;
import org.apache.nifi.util.StringUtils;

@Tags({ "lookup", "cache", "enrich", "join", "image", "reloadable", "key", "value", "record" })
@CapabilityDescription("By file name lookup service. When the lookup key is found in the filesysteme the file content are returned as a Record. The contend will be encoded as Byte[]")
public class FileLookupService extends AbstractControllerService implements RecordLookupService {

    private static final String KEY = "key";
    private static final Set<String> REQUIRED_KEYS = Collections.unmodifiableSet(Stream.of(KEY).collect(Collectors.toSet()));
    public static final String GENERIC_IMAGE_SCHEMA_NAME = "Image";

    private static final PropertyDescriptor FILE_BASE_PATH = new PropertyDescriptor.Builder().name("file-base-path").displayName("File search base path").description("A base path to use as prefix of the key to complete the file read").required(true)
	    .addValidator(StandardValidators.FILE_EXISTS_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY).build();
    
    public static final PropertyDescriptor IMAGE_CONTENT_FIELD_NAME = new PropertyDescriptor.Builder()
            .name("Content Field Name for Generic Image Schema")
            .description("The name of the field wher the bytes of the image will be stored as bytes")
            .required(true)
            .defaultValue("content")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();

    private List<PropertyDescriptor> properties;
    private volatile String basePath;
    private volatile String contentImageFieldName;
    private volatile RecordSchema recordSchema;

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
	return properties;
    }

    @Override
    protected void init(final ControllerServiceInitializationContext context) throws InitializationException {
	final List<PropertyDescriptor> properties = new ArrayList<>();
	properties.add(FILE_BASE_PATH);
	properties.add(IMAGE_CONTENT_FIELD_NAME);
	this.properties = Collections.unmodifiableList(properties);
    }

    @OnEnabled
    public void onEnabled(final ConfigurationContext context) throws InitializationException, IOException {
	this.basePath = context.getProperty(FILE_BASE_PATH).evaluateAttributeExpressions().getValue();
	final List<RecordField> fields = new ArrayList<>();
	RecordField r = new RecordField(this.contentImageFieldName, RecordFieldType.ARRAY.getArrayDataType(RecordFieldType.BYTE.getDataType()), true);
	fields.add(new RecordField(this.contentImageFieldName, r.getDataType(), true));
	SchemaIdentifier schemaIdentifier = new StandardSchemaIdentifier.Builder().name(GENERIC_IMAGE_SCHEMA_NAME).build();
	this.recordSchema = new SimpleRecordSchema(fields,schemaIdentifier);
    }

    @Override
    public Optional<Record> lookup(final Map<String, Object> coordinates) throws LookupFailureException {
	final String key = (String) coordinates.get(KEY);
	if (StringUtils.isBlank(key)) {
	    return Optional.empty();
	}
	// Read the file from FS
	String fileNameAndPath = this.basePath + "/" + key;
	Path path = Paths.get(fileNameAndPath);
	Record r = null;
	byte[] data;
	try {
	    data = Files.readAllBytes(path);
	    final Map<String, Object> imageMap = new HashMap<>(1);
	    imageMap.put("content", data);
	    r = new MapRecord(this.recordSchema, imageMap);
	} catch (IOException e) {
	    throw new LookupFailureException(e.getMessage(),e);
	}
	return  Optional.of(r);
    }
    

    @Override
    public Set<String> getRequiredKeys() {
	return REQUIRED_KEYS;
    }
}
