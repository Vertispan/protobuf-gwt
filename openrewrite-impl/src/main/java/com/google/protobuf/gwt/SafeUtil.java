package com.google.protobuf.gwt;

import java.nio.ByteBuffer;

public class SafeUtil {

    public static boolean hasUnsafeArrayOperations() {
        return false;
    }

    public static boolean hasUnsafeByteBufferOperations() {
        return false;
    }

    public static void putByte(byte[] buffer, long position, byte value) {
        buffer[(int) position] = value;
    }

    public static int getByte(byte[] bytes, long position) {
        return bytes[(int) position];
    }

    public static long addressOffset(ByteBuffer buffer) {
        // OpenRewrite doesn't prune this import correctly, so we just leave the method unimplemented.s
        throw new UnsupportedOperationException("addressOffset");
    }

    public static ByteBuffer wrap(byte[] array, int offset, int length) {
        ByteBuffer bb = ByteBuffer.allocate(length);
        bb.put(array, offset, length);
        return bb;
    }
    public static ByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }


    public static int floatToRawIntBits(float value) {
        return Float.floatToIntBits(value);
    }
}
