// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import static com.google.protobuf.Internal.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/** A {@link ByteString} that wraps around a {@link ByteBuffer}. */
final class NioByteString extends ByteString.LeafByteString {
  private final ByteBuffer buffer;

  NioByteString(ByteBuffer buffer) {
    checkNotNull(buffer, "buffer");

    // Use native byte order for fast fixed32/64 operations.
    this.buffer = buffer.slice().order(ByteOrder.nativeOrder());
  }

  // =================================================================

  public byte byteAt(int index) {
    try {
      return buffer.get(index);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw e;
    } catch (IndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException(e.getMessage());
    }
  }

  public byte internalByteAt(int index) {
    // it isn't possible to avoid the bounds checking inside of ByteBuffer, so just use the default
    // implementation.
    return byteAt(index);
  }

  public int size() {
    return buffer.remaining();
  }

  public ByteString substring(int beginIndex, int endIndex) {
    try {
      ByteBuffer slice = slice(beginIndex, endIndex);
      return new NioByteString(slice);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw e;
    } catch (IndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException(e.getMessage());
    }
  }

  protected void copyToInternal(
      byte[] target, int sourceOffset, int targetOffset, int numberToCopy) {
    ByteBuffer slice = buffer.slice();
    Java8Compatibility.position(slice, sourceOffset);
    slice.get(target, targetOffset, numberToCopy);
  }

  public void copyTo(ByteBuffer target) {
    target.put(buffer.slice());
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(toByteArray());
  }

  boolean equalsRange(ByteString other, int offset, int length) {
    return substring(0, length).equals(other.substring(offset, offset + length));
  }

  void writeToInternal(OutputStream out, int sourceOffset, int numberToWrite) throws IOException {
    if (false) {
      // Optimized write for array-backed buffers.
      // Note that we're taking the risk that a malicious OutputStream could modify the array.
      int bufferOffset = buffer.arrayOffset() + buffer.position() + sourceOffset;
      out.write(buffer.array(), bufferOffset, numberToWrite);
      return;
    }

    ByteBufferWriter.write(slice(sourceOffset, sourceOffset + numberToWrite), out);
  }

  void writeTo(ByteOutput output) throws IOException {
    output.writeLazy(buffer.slice());
  }

  public ByteBuffer asReadOnlyByteBuffer() {
    return com.google.protobuf.gwt.StaticImpls.asReadOnlyBuffer(buffer);
  }

  public List<ByteBuffer> asReadOnlyByteBufferList() {
    return Collections.singletonList(asReadOnlyByteBuffer());
  }

  protected String toStringInternal(Charset charset) {
    final byte[] bytes;
    final int offset;
    final int length;
    if (false) {
      bytes = buffer.array();
      offset = buffer.arrayOffset() + buffer.position();
      length = buffer.remaining();
    } else {
      // TODO: Can we optimize this?
      bytes = toByteArray();
      offset = 0;
      length = bytes.length;
    }
    return new String(bytes, offset, length, charset);
  }

  public boolean isValidUtf8() {
    return Utf8.isValidUtf8(buffer);
  }

  protected int partialIsValidUtf8(int state, int offset, int length) {
    return Utf8.partialIsValidUtf8(state, buffer, offset, offset + length);
  }

  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof ByteString)) {
      return false;
    }
    ByteString otherString = ((ByteString) other);
    if (size() != otherString.size()) {
      return false;
    }
    if (size() == 0) {
      return true;
    }
    if (other instanceof NioByteString) {
      return buffer.equals(((NioByteString) other).buffer);
    }
    if (other instanceof RopeByteString) {
      return other.equals(this);
    }
    return buffer.equals(otherString.asReadOnlyByteBuffer());
  }

  protected int partialHash(int h, int offset, int length) {
    for (int i = offset; i < offset + length; i++) {
      h = h * 31 + buffer.get(i);
    }
    return h;
  }

  public InputStream newInput() {
    return new InputStream() {
      private final ByteBuffer buf = buffer.slice();

      public void mark(int readlimit) {
        Java8Compatibility.mark(buf);
      }

      public boolean markSupported() {
        return true;
      }

      public void reset() throws IOException {
        try {
          Java8Compatibility.reset(buf);
        } catch (InvalidMarkException e) {
          throw new IOException(e);
        }
      }

      public int available() throws IOException {
        return buf.remaining();
      }

      public int read() throws IOException {
        if (!buf.hasRemaining()) {
          return -1;
        }
        return buf.get() & 0xFF;
      }

      public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
          return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
      }
    };
  }

  public CodedInputStream newCodedInput() {
    return CodedInputStream.newInstance(buffer, true);
  }

  /**
   * Creates a slice of a range of this buffer.
   *
   * @param beginIndex the beginning index of the slice (inclusive).
   * @param endIndex the end index of the slice (exclusive).
   * @return the requested slice.
   */
  private ByteBuffer slice(int beginIndex, int endIndex) {
    if (beginIndex < buffer.position() || endIndex > buffer.limit() || beginIndex > endIndex) {
      throw new IllegalArgumentException(
          "Invalid indices [" + beginIndex + ", " + endIndex + "]");
    }

    ByteBuffer slice = buffer.slice();
    Java8Compatibility.position(slice, beginIndex - buffer.position());
    Java8Compatibility.limit(slice, endIndex - buffer.position());
    return slice;
  }
}
