package net.zylklab.nifi.processor.sanbox;

import java.nio.ByteBuffer;

public class ByteUtils {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public static byte[] longToBytes(long x) {
	buffer.putLong(0, x);
	return buffer.array();
    }
    
    public static byte[] intToBytes(int x) {
	buffer.putInt(0, x);
	return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
	buffer.put(bytes, 0, bytes.length);
	buffer.flip();// need flip
	return buffer.getLong();
    }

    public static byte[] reverse(byte[] array) {
	if (array == null) {
	    return array;
	}
	byte[] reverse = new byte[array.length];
	System.arraycopy(array, 0, reverse, 0, array.length);
	int i = 0;
	int j = reverse.length - 1;
	byte tmp;
	while (j > i) {
	    tmp = reverse[j];
	    reverse[j] = reverse[i];
	    reverse[i] = tmp;
	    j--;
	    i++;
	}
	return reverse;
    }

}
