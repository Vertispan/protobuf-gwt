package com.google.protobuf.gwt;

import java.nio.ByteBuffer;

/**
 * Like UnsafeUtil, except no use of Unsafe - many static calls to UnsafeUtil can be simply
 * retargetted to this class without additional rewrites.
 * <p>
 * It may be wise in the future to simplify this down to just putByte, and rely on replacing
 * the other methods calls with constants, plus better unused import cleanup to remove the
 * addressOffset reference in Utf8.
 */
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
