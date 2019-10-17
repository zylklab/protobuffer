package net.zylklab.nifi.processor.sanbox;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.protobuf.ByteString;

import net.zylklab.nifi.processor.protobuffer.hadoop.util.TFRecordWriter;
import net.zylklab.nifi.processor.sanbox.auto.protobuf.Image;
import net.zylklab.nifi.processor.sanbox.auto.protobuf.ImageList;
import net.zylklab.nifi.processor.sanbox.auto.protobuf.tensorflow.BytesList;
import net.zylklab.nifi.processor.sanbox.auto.protobuf.tensorflow.Example;
import net.zylklab.nifi.processor.sanbox.auto.protobuf.tensorflow.Feature;
import net.zylklab.nifi.processor.sanbox.auto.protobuf.tensorflow.Features;
import net.zylklab.nifi.processor.sanbox.auto.protobuf.tensorflow.Int64List;

public class Test {
    private static final String BASE_PATH = "/home/gus/.keras/datasets/flower_photos";
    private static final String SER_FILE = "/home/gus/flowers/birds-and-flowers.tfrecod";
    private static final String SER_FILE_EXAMPLE = "/home/gus/flowers/flower-java.tfrecod";
    private static final String SER_FILE_PYTHON = "/home/gus/flowers/flower-python.tfrecords";

    public static void main(String[] args) throws IOException {
	Test.createExample();

	// Test.readExample();
    }

    public static void read() throws IOException {
	ImageList a = ImageList.parseFrom(new FileInputStream(SER_FILE));
	System.out.print("Records añadidos al fichero: " + a.getImageList().size());
    }

    public static void readExample() throws IOException {
	// Example a = Example.parseDelimitedFrom(new FileInputStream(SER_FILE_PYTHON));
	Example a = Example.newBuilder().mergeFrom(new FileInputStream(SER_FILE_PYTHON)).build();

	// Example.Builder builder = Example.newBuilder();
	// TextFormat.getParser().merge(str, builder);
	System.out.print("Records añadidos al fichero: " + a);

    }

    public static void createExample() throws IOException {
	long t0 = System.currentTimeMillis();
	try (FileOutputStream out = new FileOutputStream(SER_FILE_EXAMPLE)) {
	    // try (PrintWriter out = new PrintWriter(SER_FILE_EXAMPLE)) {
	    DataOutput dataoutput = new  DataOutputStream(out);
	    TFRecordWriter recordwriter =  new TFRecordWriter(dataoutput);

	    Files.walk(Paths.get(BASE_PATH)).filter(Files::isRegularFile).filter(FilesExtFunctions::hasImageExtension).forEach(new Consumer<Path>() {
		@Override
		public void accept(Path t) {
		    LABEL label = LABEL.toEnum(t.getParent().getFileName().toString());
		    try (FileInputStream content = new FileInputStream(t.toFile())) {
			Example.Builder exampleBuilder;
			Features.Builder features;
			Map<String, Feature> imageFeaturedMap;
			Feature.Builder encodedFeature;
			Feature.Builder textFeature;
			Int64List.Builder valueLabelBuilder;
			BytesList.Builder valueContentBuilder;
			BytesList.Builder valueTextBuilder;
			Feature.Builder labelFeature;
			
			exampleBuilder = Example.newBuilder();
			features = Features.newBuilder();
			imageFeaturedMap = new HashMap<String, Feature>();
			encodedFeature = Feature.newBuilder();
			textFeature = Feature.newBuilder();
			valueLabelBuilder = Int64List.newBuilder();
			valueContentBuilder = BytesList.newBuilder();
			valueTextBuilder = BytesList.newBuilder();
			labelFeature = Feature.newBuilder();
			
			valueContentBuilder.addValue(ByteString.readFrom(content));
			valueTextBuilder.addValue(ByteString.copyFrom(label.getNameAsBytes()));
			valueLabelBuilder.addValue(label.getValue());
			encodedFeature.setBytesList(valueContentBuilder.build());
			textFeature.setBytesList(valueTextBuilder.build());
			labelFeature.setInt64List(valueLabelBuilder.build());
			imageFeaturedMap.put("encoded", encodedFeature.build());
			imageFeaturedMap.put("text", textFeature.build());
			imageFeaturedMap.put("label", labelFeature.build());
			features.putAllFeature(imageFeaturedMap);
			exampleBuilder.setFeatures(features);
			Example e = exampleBuilder.build();
			recordwriter.write(e.toByteArray());
			
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    });
	    
	    long t1 = System.currentTimeMillis();
	    System.out.println("Tiempo en crear el tfrecord :: " + (t1 - t0) + "ms");
	}

    }


    public static void create() throws IOException {
	long t0 = System.currentTimeMillis();
	try (FileOutputStream output = new FileOutputStream(SER_FILE)) {
	    ImageList.Builder imageListProtosBuilder = ImageList.newBuilder();
	    Files.walk(Paths.get(BASE_PATH)).filter(Files::isRegularFile).filter(FilesExtFunctions::hasImageExtension).forEach(new Consumer<Path>() {
		@Override
		public void accept(Path t) {
		    LABEL label = LABEL.toEnum(t.getParent().getFileName().toString());
		    FileInputStream content;
		    try {
			content = new FileInputStream(t.toFile());
			Image.Builder imageProtosBuilder = Image.newBuilder();
			imageProtosBuilder.setLabel(label.getValue());
			imageProtosBuilder.setText(ByteString.copyFrom(label.getName(), "UTF-8"));
			imageProtosBuilder.setEncoded(ByteString.readFrom(content));
			Image image = imageProtosBuilder.build();
			imageListProtosBuilder.addImage(image);
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    });
	    imageListProtosBuilder.build().writeTo(output);
	}

	long t1 = System.currentTimeMillis();
	System.out.println("Tiempo en crear el tfrecord :: " + (t1 - t0) + "ms");
    }
}
