package com.google.protobuf.gwt;

import com.google.protobuf.Descriptors;

import java.nio.ByteBuffer;
import java.util.TreeMap;

/**
 * Replacement implementations for methods that aren't supported in GWT. These are both
 * for instance methods and static methods - the instance methods must be "effectively final"
 * and have their first argument be the "this" instance.
 */
public class StaticImpls {
    public static ByteBuffer wrap(byte[] array, int offset, int length) {
        ByteBuffer bb = ByteBuffer.allocate(length);
        bb.put(array);
        bb.position(offset);
        bb.limit(offset + length);
        return bb;
    }

    public static ByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public static int floatToRawIntBits(float value) {
        return Float.floatToIntBits(value);
    }

    public static byte[] clone(byte[] arr) {
        // Copy the array using arraycopy
        if (arr == null) {
            return null;
        }
        byte[] copy = new byte[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);
        return copy;
    }
    public static Descriptors.FieldDescriptor[] clone(Descriptors.FieldDescriptor[] arr) {
        // Copy the array using arraycopy
        if (arr == null) {
            return null;
        }
        Descriptors.FieldDescriptor[] copy = new Descriptors.FieldDescriptor[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);
        return copy;
    }
    public static Descriptors.FileDescriptor[] clone(Descriptors.FileDescriptor[] arr) {
        // Copy the array using arraycopy
        if (arr == null) {
            return null;
        }
        Descriptors.FileDescriptor[] copy = new Descriptors.FileDescriptor[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);
        return copy;
    }
    public static Descriptors.EnumValueDescriptor[] clone(Descriptors.EnumValueDescriptor[] arr) {
        // Copy the array using arraycopy
        if (arr == null) {
            return null;
        }
        Descriptors.EnumValueDescriptor[] copy = new Descriptors.EnumValueDescriptor[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);
        return copy;
    }

    public static <K, V> TreeMap<K, V> clone(TreeMap<K, V> map) {
        // Copy the map using a new TreeMap
        if (map == null) {
            return null;
        }
        return new TreeMap<>(map);
    }

    public static ByteBuffer asReadOnlyBuffer(ByteBuffer buffer) {
        // GWT does not support read-only buffers, so we return a copy
        ByteBuffer readOnlyBuffer = ByteBuffer.allocate(buffer.capacity());
        readOnlyBuffer.put(buffer);
        readOnlyBuffer.flip(); // Prepare the buffer for reading
        return readOnlyBuffer;
    }
}
