package com.google.protobuf;

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
}
