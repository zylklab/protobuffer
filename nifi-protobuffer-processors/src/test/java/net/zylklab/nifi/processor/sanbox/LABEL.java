package net.zylklab.nifi.processor.sanbox;

import java.io.UnsupportedEncodingException;

public enum LABEL {
    DAISY("daisy",0),
    DANDELION("dandelion",1),
    ROSES("roses",2),
    SUNFLOWERS("sunflowers",3),
    TULIPANS("tulips",4);
    
    String name;
    long value;
    private LABEL(String name, long value) {
	this.name = name;
	this.value = value;
    }
    public long getValue() {
	return this.value;
    }
   
    public String getName() {
	return this.name;
    }
    
    public byte[] getNameAsBytes() throws UnsupportedEncodingException {
	return this.name.getBytes("UTF-8");
    }
    
    public static LABEL toEnum(String name) {
	LABEL label = null;
	for (LABEL l : LABEL.values()) {
	    if(l.getName().equals(name)) {
		label = l;
		break;
	    }
	}
	return label;
    }
}
