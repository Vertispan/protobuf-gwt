// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// Copyright 2025 Vertispan LLC. All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd
package com.google.protobuf.gwt;

import static com.google.protobuf.Internal.EMPTY_BYTE_BUFFER;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Lightly rewritten version of protobuf's own IterableByteBufferInputStream, which is requires
 * unsafe access to direct ByteBuffer contents, and aditionally removes the array-backed ByteBuffer
 * support (which doesn't make sense in GWT).
 */
public class IterableByteBufferInputStream extends InputStream {
    /** The {@link Iterator} with type {@link ByteBuffer} of {@code input} */
    private final Iterator<ByteBuffer> iterator;
    /** The current ByteBuffer; */
    private ByteBuffer currentByteBuffer;
    /** The number of ByteBuffers in the input data. */
    private int dataSize;
    /**
     * Current {@code ByteBuffer}'s index
     *
     * <p>If index equals dataSize, then all the data in the InputStream has been consumed
     */
    private int currentIndex;
    /** The current position for current ByteBuffer */
    private int currentByteBufferPos;

    public IterableByteBufferInputStream(Iterable<ByteBuffer> data) {
        iterator = data.iterator();
        dataSize = 0;
        for (ByteBuffer unused : data) {
            dataSize++;
        }
        currentIndex = -1;

        if (!getNextByteBuffer()) {
            currentByteBuffer = EMPTY_BYTE_BUFFER;
            currentIndex = 0;
            currentByteBufferPos = 0;
        }
    }

    private boolean getNextByteBuffer() {
        currentIndex++;
        if (!iterator.hasNext()) {
            return false;
        }
        currentByteBuffer = iterator.next();
        currentByteBufferPos = currentByteBuffer.position();
        return true;
    }

    private void updateCurrentByteBufferPos(int numberOfBytesRead) {
        currentByteBufferPos += numberOfBytesRead;
        if (currentByteBufferPos == currentByteBuffer.limit()) {
            getNextByteBuffer();
        }
    }

    public int read() throws IOException {
        if (currentIndex == dataSize) {
            return -1;
        }
        int result = currentByteBuffer.get(currentByteBufferPos) & 0xFF;
        updateCurrentByteBufferPos(1);
        return result;
    }

    public int read(byte[] output, int offset, int length) throws IOException {
        if (currentIndex == dataSize) {
            return -1;
        }
        int remaining = currentByteBuffer.limit() - currentByteBufferPos;
        if (length > remaining) {
            length = remaining;
        }
        int prevPos = currentByteBuffer.position();
        currentByteBuffer.position(currentByteBufferPos);
        currentByteBuffer.get(output, offset, length);
        currentByteBuffer.position(prevPos);
        updateCurrentByteBufferPos(length);
        return length;
    }
}
