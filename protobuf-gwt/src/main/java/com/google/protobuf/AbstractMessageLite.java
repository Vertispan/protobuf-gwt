// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import static com.google.protobuf.Internal.checkNotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A partial implementation of the {@link MessageLite} interface which implements as many methods of
 * that interface as possible in terms of other methods.
 *
 * @author kenton@google.com Kenton Varda
 */
public abstract class AbstractMessageLite<
        MessageType extends AbstractMessageLite<MessageType, BuilderType>,
        BuilderType extends AbstractMessageLite.Builder<MessageType, BuilderType>>
    implements MessageLite {
  protected int memoizedHashCode = 0;

  public ByteString toByteString() {
    try {
      final ByteString.CodedBuilder out = ByteString.newCodedBuilder(getSerializedSize());
      writeTo(out.getCodedOutput());
      return out.build();
    } catch (IOException e) {
      throw new RuntimeException(getSerializingExceptionMessage("ByteString"), e);
    }
  }

  public byte[] toByteArray() {
    try {
      final byte[] result = new byte[getSerializedSize()];
      final CodedOutputStream output = CodedOutputStream.newInstance(result);
      writeTo(output);
      output.checkNoSpaceLeft();
      return result;
    } catch (IOException e) {
      throw new RuntimeException(getSerializingExceptionMessage("byte array"), e);
    }
  }

  public void writeTo(final OutputStream output) throws IOException {
    final int bufferSize = CodedOutputStream.computePreferredBufferSize(getSerializedSize());
    final CodedOutputStream codedOutput = CodedOutputStream.newInstance(output, bufferSize);
    writeTo(codedOutput);
    codedOutput.flush();
  }

  public void writeDelimitedTo(final OutputStream output) throws IOException {
    final int serialized = getSerializedSize();
    final int bufferSize =
        CodedOutputStream.computePreferredBufferSize(
            CodedOutputStream.computeUInt32SizeNoTag(serialized) + serialized);
    final CodedOutputStream codedOutput = CodedOutputStream.newInstance(output, bufferSize);
    codedOutput.writeUInt32NoTag(serialized);
    writeTo(codedOutput);
    codedOutput.flush();
  }

  // We'd like these to be abstract but some folks are extending this class directly. They shouldn't
  // be doing that and they should feel bad.
  int getMemoizedSerializedSize() {
    throw new UnsupportedOperationException();
  }

  void setMemoizedSerializedSize(int size) {
    throw new UnsupportedOperationException();
  }

  int getSerializedSize(
          Schema schema) {
    int memoizedSerializedSize = getMemoizedSerializedSize();
    if (memoizedSerializedSize == -1) {
      memoizedSerializedSize = schema.getSerializedSize(this);
      setMemoizedSerializedSize(memoizedSerializedSize);
    }
    return memoizedSerializedSize;
  }

  /** Package private helper method for AbstractParser to create UninitializedMessageException. */
  UninitializedMessageException newUninitializedMessageException() {
    return new UninitializedMessageException(this);
  }

  private String getSerializingExceptionMessage(String target) {
    return "Serializing "
        + getClass().getName()
        + " to a "
        + target
        + " threw an IOException (should never happen).";
  }

  protected static void checkByteStringIsUtf8(ByteString byteString)
      throws IllegalArgumentException {
    if (!byteString.isValidUtf8()) {
      throw new IllegalArgumentException("Byte string is not UTF-8.");
    }
  }

  // For binary compatibility
  @Deprecated
  protected static <T> void addAll(final Iterable<T> values, final Collection<? super T> list) {
    Builder.addAll(values, (List) list);
  }

  protected static <T> void addAll(final Iterable<T> values, final List<? super T> list) {
    Builder.addAll(values, list);
  }

  /** Interface for an enum which signifies which field in a {@code oneof} was specified. */
  protected interface InternalOneOfEnum {
    /**
     * Retrieves the field number of the field which was set in this {@code oneof}, or {@code 0} if
     * none were.
     */
    int getNumber();
  }

  /**
   * A partial implementation of the {@link Message.Builder} interface which implements as many
   * methods of that interface as possible in terms of other methods.
   */
  @SuppressWarnings("unchecked")
  public abstract static class Builder<
          MessageType extends AbstractMessageLite<MessageType, BuilderType>,
          BuilderType extends Builder<MessageType, BuilderType>>
      implements MessageLite.Builder {
    // The compiler produces an error if this is not declared explicitly.
    public abstract BuilderType clone();

    public BuilderType mergeFrom(final CodedInputStream input) throws IOException {
      return mergeFrom(input, ExtensionRegistryLite.getEmptyRegistry());
    }

    // Re-defined here for return type covariance.
    public abstract BuilderType mergeFrom(
        final CodedInputStream input, final ExtensionRegistryLite extensionRegistry)
        throws IOException;

    public BuilderType mergeFrom(final ByteString data) throws InvalidProtocolBufferException {
      try {
        final CodedInputStream input = data.newCodedInput();
        mergeFrom(input);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(getReadingExceptionMessage("ByteString"), e);
      }
    }

    public BuilderType mergeFrom(
        final ByteString data, final ExtensionRegistryLite extensionRegistry)
        throws InvalidProtocolBufferException {
      try {
        final CodedInputStream input = data.newCodedInput();
        mergeFrom(input, extensionRegistry);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(getReadingExceptionMessage("ByteString"), e);
      }
    }

    public BuilderType mergeFrom(final byte[] data) throws InvalidProtocolBufferException {
      return mergeFrom(data, 0, data.length);
    }

    public BuilderType mergeFrom(final byte[] data, final int off, final int len)
        throws InvalidProtocolBufferException {
      try {
        final CodedInputStream input = CodedInputStream.newInstance(data, off, len);
        mergeFrom(input);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(getReadingExceptionMessage("byte array"), e);
      }
    }

    public BuilderType mergeFrom(final byte[] data, final ExtensionRegistryLite extensionRegistry)
        throws InvalidProtocolBufferException {
      return mergeFrom(data, 0, data.length, extensionRegistry);
    }

    public BuilderType mergeFrom(
        final byte[] data,
        final int off,
        final int len,
        final ExtensionRegistryLite extensionRegistry)
        throws InvalidProtocolBufferException {
      try {
        final CodedInputStream input = CodedInputStream.newInstance(data, off, len);
        mergeFrom(input, extensionRegistry);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(getReadingExceptionMessage("byte array"), e);
      }
    }

    public BuilderType mergeFrom(final InputStream input) throws IOException {
      final CodedInputStream codedInput = CodedInputStream.newInstance(input);
      mergeFrom(codedInput);
      codedInput.checkLastTagWas(0);
      return (BuilderType) this;
    }

    public BuilderType mergeFrom(
        final InputStream input, final ExtensionRegistryLite extensionRegistry) throws IOException {
      final CodedInputStream codedInput = CodedInputStream.newInstance(input);
      mergeFrom(codedInput, extensionRegistry);
      codedInput.checkLastTagWas(0);
      return (BuilderType) this;
    }

    /**
     * An InputStream implementations which reads from some other InputStream but is limited to a
     * particular number of bytes. Used by mergeDelimitedFrom(). This is intentionally
     * package-private so that UnknownFieldSet can share it.
     */
    static final class LimitedInputStream extends FilterInputStream {
      private int limit;

      LimitedInputStream(InputStream in, int limit) {
        super(in);
        this.limit = limit;
      }

      public int available() throws IOException {
        return Math.min(super.available(), limit);
      }

      public int read() throws IOException {
        if (limit <= 0) {
          return -1;
        }
        final int result = super.read();
        if (result >= 0) {
          --limit;
        }
        return result;
      }

      public int read(final byte[] b, final int off, int len) throws IOException {
        if (limit <= 0) {
          return -1;
        }
        len = Math.min(len, limit);
        final int result = super.read(b, off, len);
        if (result >= 0) {
          limit -= result;
        }
        return result;
      }

      public long skip(final long n) throws IOException {
        // because we take the minimum of an int and a long, result is guaranteed to be
        // less than or equal to Integer.MAX_INT so this cast is safe
        int result = (int) super.skip(Math.min(n, limit));
        if (result >= 0) {
          // if the superclass adheres to the contract for skip, this condition is always true
          limit -= result;
        }
        return result;
      }
    }

    public boolean mergeDelimitedFrom(
        final InputStream input, final ExtensionRegistryLite extensionRegistry) throws IOException {
      final int firstByte = input.read();
      if (firstByte == -1) {
        return false;
      }
      final int size = CodedInputStream.readRawVarint32(firstByte, input);
      final InputStream limitedInput = new LimitedInputStream(input, size);
      mergeFrom(limitedInput, extensionRegistry);
      return true;
    }

    public boolean mergeDelimitedFrom(final InputStream input) throws IOException {
      return mergeDelimitedFrom(input, ExtensionRegistryLite.getEmptyRegistry());
    }

    protected abstract BuilderType internalMergeFrom(MessageType message);

    private String getReadingExceptionMessage(String target) {
      return "Reading "
          + getClass().getName()
          + " from a "
          + target
          + " threw an IOException (should never happen).";
    }

    // We check nulls as we iterate to avoid iterating over values twice.
    private static <T> void addAllCheckingNulls(Iterable<T> values, List<? super T> list) {
      if (list instanceof ArrayList && values instanceof Collection) {
        ((ArrayList<T>) list).ensureCapacity(list.size() + ((Collection<T>) values).size());
      }
      int begin = list.size();
      for (T value : values) {
        if (value == null) {
          // encountered a null value so we must undo our modifications prior to throwing
          String message = "Element at index " + (list.size() - begin) + " is null.";
          for (int i = list.size() - 1; i >= begin; i--) {
            list.remove(i);
          }
          throw new NullPointerException(message);
        }
        list.add(value);
      }
    }

    /** Construct an UninitializedMessageException reporting missing fields in the given message. */
    protected static UninitializedMessageException newUninitializedMessageException(
        MessageLite message) {
      return new UninitializedMessageException(message);
    }

    // For binary compatibility.
    @Deprecated
    protected static <T> void addAll(final Iterable<T> values, final Collection<? super T> list) {
      addAll(values, (List<T>) list);
    }

    /**
     * Adds the {@code values} to the {@code list}. This is a helper method used by generated code.
     * Users should ignore it.
     *
     * @throws NullPointerException if {@code values} or any of the elements of {@code values} is
     *     null.
     */
    protected static <T> void addAll(final Iterable<T> values, final List<? super T> list) {
      checkNotNull(values);
      if (values instanceof LazyStringList) {
        // For StringOrByteStringLists, check the underlying elements to avoid
        // forcing conversions of ByteStrings to Strings.
        // TODO: Could we just prohibit nulls in all protobuf lists and get rid of this? Is
        // if even possible to hit this condition as all protobuf methods check for null first,
        // right?
        List<?> lazyValues = ((LazyStringList) values).getUnderlyingElements();
        LazyStringList lazyList = (LazyStringList) list;
        int begin = list.size();
        for (Object value : lazyValues) {
          if (value == null) {
            // encountered a null value so we must undo our modifications prior to throwing
            String message = "Element at index " + (lazyList.size() - begin) + " is null.";
            for (int i = lazyList.size() - 1; i >= begin; i--) {
              lazyList.remove(i);
            }
            throw new NullPointerException(message);
          }
          if (value instanceof ByteString) {
            lazyList.add((ByteString) value);
          } else {
            lazyList.add((String) value);
          }
        }
      } else {
        if (values instanceof PrimitiveNonBoxingCollection) {
          list.addAll((Collection<T>) values);
        } else {
          addAllCheckingNulls(values, list);
        }
      }
    }
  }
}
