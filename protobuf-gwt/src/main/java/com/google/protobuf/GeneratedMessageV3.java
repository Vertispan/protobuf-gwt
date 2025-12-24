// Protocol Buffers - Google's data interchange format
// Copyright 2024 Google LLC.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Internal.BooleanList;
import com.google.protobuf.Internal.DoubleList;
import com.google.protobuf.Internal.FloatList;
import com.google.protobuf.Internal.IntList;
import com.google.protobuf.Internal.LongList;

/**
 * Stub for GeneratedMessageV3 wrapping GeneratedMessage for compatibility with older gencode.
 *
 * <p>Extends GeneratedMessage.ExtendableMessage instead of GeneratedMessage to allow "multiple
 * inheritance" for GeneratedMessageV3.ExtendableMessage subclass.
 *
 * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
 *     (5.x). Users should update gencode to >= 4.26.x which uses GeneratedMessage instead.
 */
@Deprecated
public abstract class GeneratedMessageV3
    extends GeneratedMessage.ExtendableMessage<GeneratedMessageV3> {
  private static final long serialVersionUID = 1L;

  @Deprecated
  protected GeneratedMessageV3() {
    super();
  }

  @Deprecated
  protected GeneratedMessageV3(Builder<?> builder) {
    super(builder);
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which no longer uses this.
   */
  protected static IntList newIntList() {
    return new IntArrayList();
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which no longer uses this.
   */
  protected static LongList newLongList() {
    return new LongArrayList();
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which no longer uses this.
   */
  protected static FloatList newFloatList() {
    return new FloatArrayList();
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which no longer uses this.
   */
  protected static DoubleList newDoubleList() {
    return new DoubleArrayList();
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which no longer uses this.
   */
  protected static BooleanList newBooleanList() {
    return new BooleanArrayList();
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which uses makeMutableCopy() instead.
   */
  @Deprecated
  protected static IntList mutableCopy(IntList list) {
    return makeMutableCopy(list);
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which uses makeMutableCopy() instead.
   */
  @Deprecated
  protected static LongList mutableCopy(LongList list) {
    return makeMutableCopy(list);
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which uses makeMutableCopy() instead.
   */
  @Deprecated
  protected static FloatList mutableCopy(FloatList list) {
    return makeMutableCopy(list);
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which uses makeMutableCopy() instead.
   */
  @Deprecated
  protected static DoubleList mutableCopy(DoubleList list) {
    return makeMutableCopy(list);
  }

  /* @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which uses makeMutableCopy() instead.
   */
  @Deprecated
  protected static BooleanList mutableCopy(BooleanList list) {
    return makeMutableCopy(list);
  }

  /* Overrides abstract GeneratedMessage.internalGetFieldAccessorTable().
   *
   * @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which uses
   * GeneratedMessage.internalGetFieldAccessorTable() instead.
   */
  @Deprecated
  protected FieldAccessorTable internalGetFieldAccessorTable() {
    throw new UnsupportedOperationException("Should be overridden in gencode.");
  }

  /**
   * Stub for GeneratedMessageV3.UnusedPrivateParameter for compatibility with older gencode.
   *
   * @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   *     (5.x). Users should update gencode to >= 4.26.x which uses
   *     GeneratedMessage.UnusedPrivateParameter instead.
   */
  @Deprecated
  protected static final class UnusedPrivateParameter {
    static final UnusedPrivateParameter INSTANCE = new UnusedPrivateParameter();

    private UnusedPrivateParameter() {}
  }

  /* Stub for method overridden from old generated code

  * @deprecated This method is deprecated, and slated for removal in the next Java breaking change
  *     (5.x). Users should update gencode to >= 4.26.x which overrides
  *     GeneratedMessage.newInstance() instead.
  */
  @Deprecated
  @SuppressWarnings({"unused"})
  protected Object newInstance(UnusedPrivateParameter unused) {
    throw new UnsupportedOperationException("This method must be overridden by the subclass.");
  }

  @Deprecated
  protected interface BuilderParent extends AbstractMessage.BuilderParent {}

  @Deprecated
  protected abstract Message.Builder newBuilderForType(BuilderParent parent);

  /* Removed from GeneratedMessage in
   * https://github.com/protocolbuffers/protobuf/commit/787447430fc9a69c071393e85a380b664d261ab4
   *
   * @deprecated This method is deprecated, and slated for removal in the next Java breaking change
   * (5.x). Users should update gencode to >= 4.26.x which no longer uses this method.
   */
  @Deprecated
  protected Message.Builder newBuilderForType(final AbstractMessage.BuilderParent parent) {
    return newBuilderForType(
        new BuilderParent() {
          public void markDirty() {
            parent.markDirty();
          }
        });
  }

  /**
   * Stub for GeneratedMessageV3.Builder wrapping GeneratedMessage.Builder for compatibility with
   * older gencode.
   *
   * <p>Extends GeneratedMessage.ExtendableBuilder instead of GeneratedMessage.Builder to allow
   * "multiple inheritance" for GeneratedMessageV3.ExtendableBuilder subclass.
   *
   * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
   *     (5.x). Users should update gencode to >= 4.26.x which uses GeneratedMessage.Builder
   *     instead.
   */
  @Deprecated
  public abstract static class Builder<BuilderT extends Builder<BuilderT>>
      extends GeneratedMessage.ExtendableBuilder<GeneratedMessageV3, BuilderT> {

    private BuilderParentImpl meAsParent;

    @Deprecated
    protected Builder() {
      super(null);
    }

    @Deprecated
    protected Builder(BuilderParent builderParent) {
      super(builderParent);
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.clone() instead. */
    @Deprecated
    public BuilderT clone() {
      return super.clone();
    }

    /* Stub for method overridden from old generated code
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.setField() instead. */
    @Deprecated
    public BuilderT clear() {
      return super.clear();
    }

    /* Overrides abstract GeneratedMessage.Builder.internalGetFieldAccessorTable().
     *
     * @deprecated This method is deprecated, and slated for removal in the next Java breaking
     * change (5.x). Users should update gencode to >= 4.26.x which overrides
     * GeneratedMessage.Builder.internalGetFieldAccessorTable() instead.
     */
    @Deprecated
    protected FieldAccessorTable internalGetFieldAccessorTable() {
      throw new UnsupportedOperationException("Should be overridden in gencode.");
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.setField() instead. */
    @Deprecated
    public BuilderT setField(final FieldDescriptor field, final Object value) {
      return super.setField(field, value);
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.clearField() instead. */
    @Deprecated
    public BuilderT clearField(final FieldDescriptor field) {
      return super.clearField(field);
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.clearOneof() instead. */
    @Deprecated
    public BuilderT clearOneof(final OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.setRepeatedField() instead. */
    @Deprecated
    public BuilderT setRepeatedField(
        final FieldDescriptor field, final int index, final Object value) {
      return super.setRepeatedField(field, index, value);
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.addRepeatedField() instead. */
    @Deprecated
    public BuilderT addRepeatedField(final FieldDescriptor field, final Object value) {
      return super.addRepeatedField(field, value);
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.setUnknownFields() instead. */
    @Deprecated
    public BuilderT setUnknownFields(final UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    /* Stub for method overridden from old generated code removed in
     * https://github.com/protocolbuffers/protobuf/commit/7bff169d32710b143951ec6ce2c4ea9a56e2ad24
     *
     * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
     *     (5.x). Users should update gencode to >= 4.26.x which overrides
     *     GeneratedMessage.Builder.mergeUnknownFields() instead. */
    @Deprecated
    public BuilderT mergeUnknownFields(final UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    @Deprecated
    private class BuilderParentImpl implements BuilderParent {
      public void markDirty() {
        onChanged();
      }
    }

    /* Returns GeneratedMessageV3.Builder.BuilderParent instead of
     * GeneratedMessage.Builder.BuilderParent.
     *
     * @deprecated This method is deprecated, and slated for removal in the next Java breaking change
     * (5.x). Users should update gencode to >= 4.26.x which uses
     * GeneratedMessage.Builder.getParentForChildren() instead.
     */
    @Deprecated
    protected BuilderParent getParentForChildren() {
      if (meAsParent == null) {
        meAsParent = new BuilderParentImpl();
      }
      return meAsParent;
    }
  }

  /**
   * Stub for GeneratedMessageV3.FieldAccessorTable wrapping GeneratedMessage.FieldAccessorTable for
   * compatibility with older gencode.
   *
   * @deprecated This class is deprecated, and slated for removal in the next Java breaking change
   *     (5.x). Users should update gencode to >= 4.26.x which uses
   *     GeneratedMessage.FieldAccessorTable instead.
   */
  @Deprecated
  public static final class FieldAccessorTable extends GeneratedMessage.FieldAccessorTable {

    @Deprecated
    public FieldAccessorTable(
        final Descriptor descriptor,
        final String[] camelCaseNames,
        final Class<? extends GeneratedMessageV3> messageClass,
        final Class<? extends Builder<?>> builderClass) {
      super(descriptor, camelCaseNames, messageClass, builderClass);
    }

    @Deprecated
    public FieldAccessorTable(final Descriptor descriptor, final String[] camelCaseNames) {
      super(descriptor, camelCaseNames);
    }

    /* Returns GeneratedMessageV3.FieldAccessorTable instead of GeneratedMessage.FieldAccessorTable.
     *
     * @deprecated This method is deprecated, and slated for removal in the next Java breaking
     * change (5.x). Users should update gencode to >= 4.26.x which uses
     * GeneratedMessage.ensureFieldAccessorsInitialized() instead.
     */
    @Deprecated
    public FieldAccessorTable ensureFieldAccessorsInitialized(
        Class<? extends GeneratedMessage> messageClass,
        Class<? extends GeneratedMessage.Builder<?>> builderClass) {
      throw new UnsupportedOperationException("com.google.protobuf.GeneratedMessageV3.FieldAccessorTable ensureFieldAccessorsInitialized(..)");
    }
  }
}
