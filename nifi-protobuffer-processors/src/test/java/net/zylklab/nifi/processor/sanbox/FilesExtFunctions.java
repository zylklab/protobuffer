package net.zylklab.nifi.processor.sanbox;

import java.nio.file.Path;
import java.util.function.Predicate;

public class FilesExtFunctions implements Predicate<Path> {
    
    private static FilesExtFunctions singleton;
    
    public static boolean hasImageExtension(Path t) {
	if (singleton == null) {
	    singleton =  new FilesExtFunctions();
	}
	return singleton.test(t);
    }
    
    @Override
    public boolean test(Path t) {
	int i = t.getFileName().toString().lastIndexOf('.');
	if (i > 0) {
	    String extension = t.getFileName().toString().substring(i + 1);
	    if (extension.equalsIgnoreCase("JPG"))
		return true;
	}
	return false;
    }

}
