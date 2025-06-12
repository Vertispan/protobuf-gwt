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
}
