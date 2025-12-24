// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
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
import com.google.protobuf.Internal.ProtobufList;
import com.google.protobuf.gwt.SafeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * All generated protocol message classes extend this class. This class implements most of the
 * Message and Builder interfaces using Java reflection. Users can ignore this class and pretend
 * that generated messages implement the Message interface directly.
 *
 * @author kenton@google.com Kenton Varda
 */
public abstract class GeneratedMessage extends AbstractMessage implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * For testing. Allows a test to disable the optimization that avoids using field builders for
   * nested messages until they are requested. By disabling this optimization, existing tests can be
   * reused to test the field builders.
   */
  protected static boolean alwaysUseFieldBuilders = false;

  /**
   * For use by generated code only.
   *
   * <p>TODO: mark this private and final (breaking change)
   */
  protected UnknownFieldSet unknownFields;

  protected GeneratedMessage() {
    unknownFields = UnknownFieldSet.getDefaultInstance();
  }

  protected GeneratedMessage(Builder<?> builder) {
    unknownFields = builder.getUnknownFields();
  }

  /** TODO: Remove this unnecessary intermediate implementation of this method. */
  public Parser<? extends GeneratedMessage> getParserForType() {
    throw new UnsupportedOperationException("This is supposed to be overridden by subclasses.");
  }

  /**
   * TODO: Stop using SingleFieldBuilder and remove this setting
   *
   * @see #setAlwaysUseFieldBuildersForTesting(boolean)
   */
  static void enableAlwaysUseFieldBuildersForTesting() {
    setAlwaysUseFieldBuildersForTesting(true);
  }

  /**
   * For testing. Allows a test to disable/re-enable the optimization that avoids using field
   * builders for nested messages until they are requested. By disabling this optimization, existing
   * tests can be reused to test the field builders. See {@link RepeatedFieldBuilder} and {@link
   * SingleFieldBuilder}.
   *
   * <p>TODO: Stop using SingleFieldBuilder and remove this setting
   */
  static void setAlwaysUseFieldBuildersForTesting(boolean useBuilders) {
    alwaysUseFieldBuilders = useBuilders;
  }

  /**
   * Get the FieldAccessorTable for this type. We can't have the message class pass this in to the
   * constructor because of bootstrapping trouble with DescriptorProtos.
   */
  protected abstract FieldAccessorTable internalGetFieldAccessorTable();

  public Descriptor getDescriptorForType() {
    return internalGetFieldAccessorTable().descriptor;
  }

  /**
   * TODO: This method should be removed. It enables parsing directly into an
   * "immutable" message. Have to leave it for now to support old gencode.
   *
   * @deprecated use newBuilder().mergeFrom() instead
   */
  @Deprecated
  protected void mergeFromAndMakeImmutableInternal(
      CodedInputStream input, ExtensionRegistryLite extensionRegistry)
      throws InvalidProtocolBufferException {
    Schema<GeneratedMessage> schema = Protobuf.getInstance().schemaFor(this);
    try {
      schema.mergeFrom(this, CodedInputStreamReader.forCodedInput(input), extensionRegistry);
    } catch (InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (IOException e) {
      throw new InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    }
    schema.makeImmutable(this);
  }

  /**
   * Internal helper to return a modifiable map containing all the fields. The returned Map is
   * modifiable so that the caller can add additional extension fields to implement {@link
   * #getAllFields()}.
   *
   * @param getBytesForString whether to generate ByteString for string fields
   */
  private Map<FieldDescriptor, Object> getAllFieldsMutable(boolean getBytesForString) {
    final TreeMap<FieldDescriptor, Object> result = new TreeMap<>();
    final FieldAccessorTable fieldAccessorTable = internalGetFieldAccessorTable();

    final Descriptor descriptor = fieldAccessorTable.descriptor;
    final List<FieldDescriptor> fields = descriptor.getFields();

    for (int i = 0; i < fields.size(); i++) {
      FieldDescriptor field = fields.get(i);
      final OneofDescriptor oneofDescriptor = field.getContainingOneof();

      /*
       * If the field is part of a Oneof, then at maximum one field in the Oneof is set
       * and it is not repeated. There is no need to iterate through the others.
       */
      if (oneofDescriptor != null) {
        // Skip other fields in the Oneof we know are not set
        i += oneofDescriptor.getFieldCount() - 1;
        if (!hasOneof(oneofDescriptor)) {
          // If no field is set in the Oneof, skip all the fields in the Oneof
          continue;
        }
        // Get the pointer to the only field which is set in the Oneof
        field = getOneofFieldDescriptor(oneofDescriptor);
      } else {
        // If we are not in a Oneof, we need to check if the field is set and if it is repeated
        if (field.isRepeated()) {
          final List<?> value = (List<?>) getField(field);
          if (!value.isEmpty()) {
            result.put(field, value);
          }
          continue;
        }
        if (!hasField(field)) {
          continue;
        }
      }
      // Add the field to the map
      if (getBytesForString && field.getJavaType() == FieldDescriptor.JavaType.STRING) {
        result.put(field, getFieldRaw(field));
      } else {
        result.put(field, getField(field));
      }
    }
    return result;
  }

  // TODO: compute this at {@code build()} time in the Builder class.
  public boolean isInitialized() {
    for (final FieldDescriptor field : getDescriptorForType().getFields()) {
      // Check that all required fields are present.
      if (field.isRequired()) {
        if (!hasField(field)) {
          return false;
        }
      }
      // Check that embedded messages are initialized.
      if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
        if (field.isRepeated()) {
          @SuppressWarnings("unchecked")
          final List<Message> messageList = (List<Message>) getField(field);
          for (final Message element : messageList) {
            if (!element.isInitialized()) {
              return false;
            }
          }
        } else {
          if (hasField(field) && !((Message) getField(field)).isInitialized()) {
            return false;
          }
        }
      }
    }

    return true;
  }

  public Map<FieldDescriptor, Object> getAllFields() {
    return Collections.unmodifiableMap(getAllFieldsMutable(/* getBytesForString= */ false));
  }

  /**
   * Returns a collection of all the fields in this message which are set and their corresponding
   * values. A singular ("required" or "optional") field is set iff hasField() returns true for that
   * field. A "repeated" field is set iff getRepeatedFieldCount() is greater than zero. The values
   * are exactly what would be returned by calling {@link #getFieldRaw(Descriptors.FieldDescriptor)}
   * for each field. The map is guaranteed to be a sorted map, so iterating over it will return
   * fields in order by field number.
   */
  Map<FieldDescriptor, Object> getAllFieldsRaw() {
    return Collections.unmodifiableMap(getAllFieldsMutable(/* getBytesForString= */ true));
  }

  public boolean hasOneof(final OneofDescriptor oneof) {
    return internalGetFieldAccessorTable().getOneof(oneof).has(this);
  }

  public FieldDescriptor getOneofFieldDescriptor(final OneofDescriptor oneof) {
    return internalGetFieldAccessorTable().getOneof(oneof).get(this);
  }

  public boolean hasField(final FieldDescriptor field) {
    return internalGetFieldAccessorTable().getField(field).has(this);
  }

  public Object getField(final FieldDescriptor field) {
    return internalGetFieldAccessorTable().getField(field).get(this);
  }

  /**
   * Obtains the value of the given field, or the default value if it is not set. For primitive
   * fields, the boxed primitive value is returned. For enum fields, the EnumValueDescriptor for the
   * value is returned. For embedded message fields, the sub-message is returned. For repeated
   * fields, a java.util.List is returned. For present string fields, a ByteString is returned
   * representing the bytes that the field contains.
   */
  Object getFieldRaw(final FieldDescriptor field) {
    return internalGetFieldAccessorTable().getField(field).getRaw(this);
  }

  public int getRepeatedFieldCount(final FieldDescriptor field) {
    return internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
  }

  public Object getRepeatedField(final FieldDescriptor field, final int index) {
    return internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
  }

  // TODO: This method should be final.
  public UnknownFieldSet getUnknownFields() {
    return unknownFields;
  }

  // TODO: This should go away when Schema classes cannot modify immutable
  // GeneratedMessage objects anymore.
  void setUnknownFields(UnknownFieldSet unknownFields) {
    this.unknownFields = unknownFields;
  }

  /**
   * Called by subclasses to parse an unknown field.
   *
   * <p>TODO remove this method
   *
   * @return {@code true} unless the tag is an end-group tag.
   */
  protected boolean parseUnknownField(
      CodedInputStream input,
      UnknownFieldSet.Builder unknownFields,
      ExtensionRegistryLite extensionRegistry,
      int tag)
      throws IOException {
    if (input.shouldDiscardUnknownFields()) {
      return input.skipField(tag);
    }
    return unknownFields.mergeFieldFrom(tag, input);
  }

  /**
   * Delegates to parseUnknownField. This method is obsolete, but we must retain it for
   * compatibility with older generated code.
   *
   * <p>TODO remove this method
   */
  protected boolean parseUnknownFieldProto3(
      CodedInputStream input,
      UnknownFieldSet.Builder unknownFields,
      ExtensionRegistryLite extensionRegistry,
      int tag)
      throws IOException {
    return parseUnknownField(input, unknownFields, extensionRegistry, tag);
  }

  /** Used by generated code. */
  @SuppressWarnings("ProtoParseWithRegistry")
  protected static <M extends Message> M parseWithIOException(Parser<M> parser, InputStream input)
      throws IOException {
    try {
      return parser.parseFrom(input);
    } catch (InvalidProtocolBufferException e) {
      throw e.unwrapIOException();
    }
  }

  /** Used by generated code. */
  protected static <M extends Message> M parseWithIOException(
      Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
    try {
      return parser.parseFrom(input, extensions);
    } catch (InvalidProtocolBufferException e) {
      throw e.unwrapIOException();
    }
  }

  /** Used by generated code. */
  @SuppressWarnings("ProtoParseWithRegistry")
  protected static <M extends Message> M parseWithIOException(
      Parser<M> parser, CodedInputStream input) throws IOException {
    try {
      return parser.parseFrom(input);
    } catch (InvalidProtocolBufferException e) {
      throw e.unwrapIOException();
    }
  }

  /** Used by generated code. */
  protected static <M extends Message> M parseWithIOException(
      Parser<M> parser, CodedInputStream input, ExtensionRegistryLite extensions)
      throws IOException {
    try {
      return parser.parseFrom(input, extensions);
    } catch (InvalidProtocolBufferException e) {
      throw e.unwrapIOException();
    }
  }

  /** Used by generated code. */
  @SuppressWarnings("ProtoParseWithRegistry")
  protected static <M extends Message> M parseDelimitedWithIOException(
      Parser<M> parser, InputStream input) throws IOException {
    try {
      return parser.parseDelimitedFrom(input);
    } catch (InvalidProtocolBufferException e) {
      throw e.unwrapIOException();
    }
  }

  /** Used by generated code. */
  protected static <M extends Message> M parseDelimitedWithIOException(
      Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
    try {
      return parser.parseDelimitedFrom(input, extensions);
    } catch (InvalidProtocolBufferException e) {
      throw e.unwrapIOException();
    }
  }

  protected static boolean canUseUnsafe() {
    return SafeUtil.hasUnsafeArrayOperations() && SafeUtil.hasUnsafeByteBufferOperations();
  }

  protected static IntList emptyIntList() {
    return IntArrayList.emptyList();
  }

  protected static LongList emptyLongList() {
    return LongArrayList.emptyList();
  }

  protected static FloatList emptyFloatList() {
    return FloatArrayList.emptyList();
  }

  protected static DoubleList emptyDoubleList() {
    return DoubleArrayList.emptyList();
  }

  protected static BooleanList emptyBooleanList() {
    return BooleanArrayList.emptyList();
  }

  protected static <ListT extends ProtobufList<?>> ListT makeMutableCopy(ListT list) {
    return makeMutableCopy(list, 0);
  }

  @SuppressWarnings("unchecked") // Guaranteed by proto runtime.
  protected static <ListT extends ProtobufList<?>> ListT makeMutableCopy(
      ListT list, int minCapacity) {
    int size = list.size();
    if (minCapacity <= size) {
      minCapacity = size * 2;
    }
    if (minCapacity <= 0) {
      minCapacity = AbstractProtobufList.DEFAULT_CAPACITY;
    }

    return (ListT) list.mutableCopyWithCapacity(minCapacity);
  }

  @SuppressWarnings("unchecked") // The empty list can be safely cast
  protected static <T> ProtobufList<T> emptyList(Class<T> elementType) {
    return (ProtobufList<T>) ProtobufArrayList.emptyList();
  }

  public void writeTo(final CodedOutputStream output) throws IOException {
    MessageReflection.writeMessageTo(this, getAllFieldsRaw(), output, false);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) {
      return size;
    }

    memoizedSize = MessageReflection.getSerializedSize(
        this, getAllFieldsRaw());
    return memoizedSize;
  }

  /**
   * This class is used to make a generated protected method inaccessible from user's code (e.g.,
   * the {@link #newInstance} method below). When this class is used as a parameter's type in a
   * generated protected method, the method is visible to user's code in the same package, but since
   * the constructor of this class is private to protobuf runtime, user's code can't obtain an
   * instance of this class and as such can't actually make a method call on the protected method.
   */
  protected static final class UnusedPrivateParameter {
    static final UnusedPrivateParameter INSTANCE = new UnusedPrivateParameter();

    private UnusedPrivateParameter() {}
  }

  /** Creates a new instance of this message type. Overridden in the generated code. */
  @SuppressWarnings({"unused"})
  protected Object newInstance(UnusedPrivateParameter unused) {
    throw new UnsupportedOperationException("This method must be overridden by the subclass.");
  }

  /** Builder class for {@link GeneratedMessage}. */
  @SuppressWarnings("unchecked")
  public abstract static class Builder<BuilderT extends Builder<BuilderT>>
      extends AbstractMessage.Builder<BuilderT> {

    private BuilderParent builderParent;

    private BuilderParentImpl meAsParent;

    // Indicates that we've built a message and so we are now obligated
    // to dispatch dirty invalidations. See GeneratedMessage.BuilderListener.
    private boolean isClean;

    /**
     * This field holds either an {@link UnknownFieldSet} or {@link UnknownFieldSet.Builder}.
     *
     * <p>We use an object because it should only be one or the other of those things at a time and
     * Object is the only common base. This also saves space.
     *
     * <p>Conversions are lazy: if {@link #setUnknownFields} is called, this will contain {@link
     * UnknownFieldSet}. If unknown fields are merged into this builder, the current {@link
     * UnknownFieldSet} will be converted to a {@link UnknownFieldSet.Builder} and left that way
     * until either {@link #setUnknownFields} or {@link #buildPartial} or {@link #build} is called.
     */
    private Object unknownFieldsOrBuilder = UnknownFieldSet.getDefaultInstance();

    protected Builder() {}

    protected Builder(BuilderParent builderParent) {
      this.builderParent = builderParent;
    }

    void dispose() {
      builderParent = null;
    }

    /** Called by the subclass when a message is built. */
    protected void onBuilt() {
      if (builderParent != null) {
        markClean();
      }
    }

    /**
     * Called by the subclass or a builder to notify us that a message was built and may be cached
     * and therefore invalidations are needed.
     */
    protected void markClean() {
      this.isClean = true;
    }

    /**
     * Gets whether invalidations are needed
     *
     * @return whether invalidations are needed
     */
    protected boolean isClean() {
      return isClean;
    }

    public BuilderT clone() {
      BuilderT builder = (BuilderT) getDefaultInstanceForType().newBuilderForType();
      builder.mergeFrom(buildPartial());
      return builder;
    }

    /**
     * Called by the initialization and clear code paths to allow subclasses to reset any of their
     * builtin fields back to the initial values.
     */
    public BuilderT clear() {
      unknownFieldsOrBuilder = UnknownFieldSet.getDefaultInstance();
      onChanged();
      return (BuilderT) this;
    }

    /**
     * Get the FieldAccessorTable for this type. We can't have the message class pass this in to the
     * constructor because of bootstrapping trouble with DescriptorProtos.
     */
    protected abstract FieldAccessorTable internalGetFieldAccessorTable();

    public Descriptor getDescriptorForType() {
      return internalGetFieldAccessorTable().descriptor;
    }

    public Map<FieldDescriptor, Object> getAllFields() {
      return Collections.unmodifiableMap(getAllFieldsMutable());
    }

    /** Internal helper which returns a mutable map. */
    private Map<FieldDescriptor, Object> getAllFieldsMutable() {
      final TreeMap<FieldDescriptor, Object> result = new TreeMap<>();
      final FieldAccessorTable fieldAccessorTable = internalGetFieldAccessorTable();
      final Descriptor descriptor = fieldAccessorTable.descriptor;
      final List<FieldDescriptor> fields = descriptor.getFields();

      for (int i = 0; i < fields.size(); i++) {
        FieldDescriptor field = fields.get(i);
        final OneofDescriptor oneofDescriptor = field.getContainingOneof();

        /*
         * If the field is part of a Oneof, then at maximum one field in the Oneof is set
         * and it is not repeated. There is no need to iterate through the others.
         */
        if (oneofDescriptor != null) {
          // Skip other fields in the Oneof we know are not set
          i += oneofDescriptor.getFieldCount() - 1;
          if (!hasOneof(oneofDescriptor)) {
            // If no field is set in the Oneof, skip all the fields in the Oneof
            continue;
          }
          // Get the pointer to the only field which is set in the Oneof
          field = getOneofFieldDescriptor(oneofDescriptor);
        } else {
          // If we are not in a Oneof, we need to check if the field is set and if it is repeated
          if (field.isRepeated()) {
            final List<?> value = (List<?>) getField(field);
            if (!value.isEmpty()) {
              result.put(field, value);
            }
            continue;
          }
          if (!hasField(field)) {
            continue;
          }
        }
        // Add the field to the map
        result.put(field, getField(field));
      }
      return result;
    }

    public Message.Builder newBuilderForField(final FieldDescriptor field) {
      return internalGetFieldAccessorTable().getField(field).newBuilder();
    }

    public Message.Builder getFieldBuilder(final FieldDescriptor field) {
      return internalGetFieldAccessorTable().getField(field).getBuilder(this);
    }

    public Message.Builder getRepeatedFieldBuilder(final FieldDescriptor field, int index) {
      return internalGetFieldAccessorTable().getField(field).getRepeatedBuilder(this, index);
    }

    public boolean hasOneof(final OneofDescriptor oneof) {
      return internalGetFieldAccessorTable().getOneof(oneof).has(this);
    }

    public FieldDescriptor getOneofFieldDescriptor(final OneofDescriptor oneof) {
      return internalGetFieldAccessorTable().getOneof(oneof).get(this);
    }

    public boolean hasField(final FieldDescriptor field) {
      return internalGetFieldAccessorTable().getField(field).has(this);
    }

    public Object getField(final FieldDescriptor field) {
      Object object = internalGetFieldAccessorTable().getField(field).get(this);
      if (field.isRepeated()) {
        // The underlying list object is still modifiable at this point.
        // Make sure not to expose the modifiable list to the caller.
        return Collections.unmodifiableList((List<?>) object);
      } else {
        return object;
      }
    }

    public BuilderT setField(final FieldDescriptor field, final Object value) {
      internalGetFieldAccessorTable().getField(field).set(this, value);
      return (BuilderT) this;
    }

    public BuilderT clearField(final FieldDescriptor field) {
      internalGetFieldAccessorTable().getField(field).clear(this);
      return (BuilderT) this;
    }

    public BuilderT clearOneof(final OneofDescriptor oneof) {
      internalGetFieldAccessorTable().getOneof(oneof).clear(this);
      return (BuilderT) this;
    }

    public int getRepeatedFieldCount(final FieldDescriptor field) {
      return internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
    }

    public Object getRepeatedField(final FieldDescriptor field, final int index) {
      return internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
    }

    public BuilderT setRepeatedField(
        final FieldDescriptor field, final int index, final Object value) {
      internalGetFieldAccessorTable().getField(field).setRepeated(this, index, value);
      return (BuilderT) this;
    }

    public BuilderT addRepeatedField(final FieldDescriptor field, final Object value) {
      internalGetFieldAccessorTable().getField(field).addRepeated(this, value);
      return (BuilderT) this;
    }

    private BuilderT setUnknownFieldsInternal(final UnknownFieldSet unknownFields) {
      unknownFieldsOrBuilder = unknownFields;
      onChanged();
      return (BuilderT) this;
    }

    public BuilderT setUnknownFields(final UnknownFieldSet unknownFields) {
      return setUnknownFieldsInternal(unknownFields);
    }

    /**
     * This method is obsolete, but we must retain it for compatibility with older generated code.
     */
    protected BuilderT setUnknownFieldsProto3(final UnknownFieldSet unknownFields) {
      return setUnknownFieldsInternal(unknownFields);
    }

    public BuilderT mergeUnknownFields(final UnknownFieldSet unknownFields) {
      if (UnknownFieldSet.getDefaultInstance().equals(unknownFields)) {
        return (BuilderT) this;
      }

      if (UnknownFieldSet.getDefaultInstance().equals(unknownFieldsOrBuilder)) {
        unknownFieldsOrBuilder = unknownFields;
        onChanged();
        return (BuilderT) this;
      }

      getUnknownFieldSetBuilder().mergeFrom(unknownFields);
      onChanged();
      return (BuilderT) this;
    }

    public boolean isInitialized() {
      for (final FieldDescriptor field : getDescriptorForType().getFields()) {
        // Check that all required fields are present.
        if (field.isRequired()) {
          if (!hasField(field)) {
            return false;
          }
        }
        // Check that embedded messages are initialized.
        if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
          if (field.isRepeated()) {
            @SuppressWarnings("unchecked")
            final List<Message> messageList = (List<Message>) getField(field);
            for (final Message element : messageList) {
              if (!element.isInitialized()) {
                return false;
              }
            }
          } else {
            if (hasField(field) && !((Message) getField(field)).isInitialized()) {
              return false;
            }
          }
        }
      }
      return true;
    }

    public final UnknownFieldSet getUnknownFields() {
      if (unknownFieldsOrBuilder instanceof UnknownFieldSet) {
        return (UnknownFieldSet) unknownFieldsOrBuilder;
      } else {
        return ((UnknownFieldSet.Builder) unknownFieldsOrBuilder).buildPartial();
      }
    }

    /**
     * Called by generated subclasses to parse an unknown field.
     *
     * @return {@code true} unless the tag is an end-group tag.
     */
    protected boolean parseUnknownField(
        CodedInputStream input, ExtensionRegistryLite extensionRegistry, int tag)
        throws IOException {
      if (input.shouldDiscardUnknownFields()) {
        return input.skipField(tag);
      }
      return getUnknownFieldSetBuilder().mergeFieldFrom(tag, input);
    }

    /** Called by generated subclasses to add to the unknown field set. */
    protected final void mergeUnknownLengthDelimitedField(int number, ByteString bytes) {
      getUnknownFieldSetBuilder().mergeLengthDelimitedField(number, bytes);
    }

    /** Called by generated subclasses to add to the unknown field set. */
    protected final void mergeUnknownVarintField(int number, int value) {
      getUnknownFieldSetBuilder().mergeVarintField(number, value);
    }

    protected UnknownFieldSet.Builder getUnknownFieldSetBuilder() {
      if (unknownFieldsOrBuilder instanceof UnknownFieldSet) {
        unknownFieldsOrBuilder = ((UnknownFieldSet) unknownFieldsOrBuilder).toBuilder();
      }
      onChanged();
      return (UnknownFieldSet.Builder) unknownFieldsOrBuilder;
    }

    protected void setUnknownFieldSetBuilder(UnknownFieldSet.Builder builder) {
      unknownFieldsOrBuilder = builder;
      onChanged();
    }

    /**
     * Implementation of {@link BuilderParent} for giving to our children. This small inner class
     * makes it so we don't publicly expose the BuilderParent methods.
     */
    private class BuilderParentImpl implements BuilderParent {

      public void markDirty() {
        onChanged();
      }
    }

    /**
     * Gets the {@link BuilderParent} for giving to our children.
     *
     * @return The builder parent for our children.
     */
    protected BuilderParent getParentForChildren() {
      if (meAsParent == null) {
        meAsParent = new BuilderParentImpl();
      }
      return meAsParent;
    }

    /**
     * Called when a builder or one of its nested children has changed and any parent should be
     * notified of its invalidation.
     */
    protected final void onChanged() {
      if (isClean && builderParent != null) {
        builderParent.markDirty();

        // Don't keep dispatching invalidations until build is called again.
        isClean = false;
      }
    }

    /**
     * Gets the map field with the given field number. This method should be overridden in the
     * generated message class if the message contains map fields.
     *
     * <p>Unlike other field types, reflection support for map fields can't be implemented based on
     * generated public API because we need to access a map field as a list in reflection API but
     * the generated API only allows us to access it as a map. This method returns the underlying
     * map field directly and thus enables us to access the map field as a list.
     */
    @SuppressWarnings("unused")
    protected MapFieldReflectionAccessor internalGetMapFieldReflection(int fieldNumber) {
      return internalGetMapField(fieldNumber);
    }

    /** TODO: Remove, exists for compatibility with generated code. */
    @Deprecated
    @SuppressWarnings({"unused", "rawtypes"})
    protected MapField internalGetMapField(int fieldNumber) {
      // Note that we can't use descriptor names here because this method will
      // be called when descriptor is being initialized.
      throw new IllegalArgumentException("No map fields found in " + getClass().getName());
    }

    /** Like {@link #internalGetMapFieldReflection} but return a mutable version. */
    @SuppressWarnings("unused")
    protected MapFieldReflectionAccessor internalGetMutableMapFieldReflection(int fieldNumber) {
      return internalGetMutableMapField(fieldNumber);
    }

    /** TODO: Remove, exists for compatibility with generated code. */
    @Deprecated
    @SuppressWarnings({"unused", "rawtypes"})
    protected MapField internalGetMutableMapField(int fieldNumber) {
      // Note that we can't use descriptor names here because this method will
      // be called when descriptor is being initialized.
      throw new IllegalArgumentException("No map fields found in " + getClass().getName());
    }
  }

  // =================================================================
  // Extensions-related stuff

  /** Extends {@link MessageOrBuilder} with extension-related functions. */
  public interface ExtendableMessageOrBuilder<MessageT extends ExtendableMessage<MessageT>>
      extends MessageOrBuilder {
    // Re-define for return type covariance.
    Message getDefaultInstanceForType();

    /** Get the value of an extension. */
    <T> T getExtension(ExtensionLite<? extends MessageT, T> extension);

    /** Get one element of a repeated extension. */
    <T> T getExtension(ExtensionLite<? extends MessageT, List<T>> extension, int index);
  }

  /**
   * Generated message classes for message types that contain extension ranges subclass this.
   *
   * <p>This class implements type-safe accessors for extensions. They implement all the same
   * operations that you can do with normal fields -- e.g. "has", "get", and "getCount" -- but for
   * extensions. The extensions are identified using instances of the class {@link
   * GeneratedExtension}; the protocol compiler generates a static instance of this class for every
   * extension in its input. Through the magic of generics, all is made type-safe.
   *
   * <p>For example, imagine you have the {@code .proto} file:
   *
   * <pre>
   * option java_class = "MyProto";
   *
   * message Foo {
   *   extensions 1000 to max;
   * }
   *
   * extend Foo {
   *   optional int32 bar;
   * }
   * </pre>
   *
   * <p>Then you might write code like:
   *
   * <pre>
   * MyProto.Foo foo = getFoo();
   * int i = foo.getExtension(MyProto.bar);
   * </pre>
   *
   * <p>See also {@link ExtendableBuilder}.
   */
  public abstract static class ExtendableMessage<MessageT extends ExtendableMessage<MessageT>>
      extends GeneratedMessage implements ExtendableMessageOrBuilder<MessageT> {

    private static final long serialVersionUID = 1L;

    private final FieldSet<FieldDescriptor> extensions;

    protected ExtendableMessage() {
      this.extensions = FieldSet.newFieldSet();
    }

    protected ExtendableMessage(ExtendableBuilder<MessageT, ?> builder) {
      super(builder);
      this.extensions = builder.buildExtensions();
    }

    private void verifyExtensionContainingType(final Extension<? extends MessageT, ?> extension) {
      if (extension.getDescriptor().getContainingType() != getDescriptorForType()) {
        // This can only happen if someone uses unchecked operations.
        throw new IllegalArgumentException(
            "Extension is for type \""
                + extension.getDescriptor().getContainingType().getFullName()
                + "\" which does not match message type \""
                + getDescriptorForType().getFullName()
                + "\".");
      }
    }

    /** Get the value of an extension. */
    @SuppressWarnings("unchecked")
    public final <T> T getExtension(final ExtensionLite<? extends MessageT, T> extensionLite) {
      Extension<MessageT, T> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      FieldDescriptor descriptor = extension.getDescriptor();
      final Object value = extensions.getField(descriptor);
      T result = null;
      if (value == null) {
        if (descriptor.isRepeated()) {
          result = (T) ProtobufArrayList.emptyList();
        } else if (descriptor.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
          result = (T) extension.getMessageDefaultInstance();
        } else {
          result = (T) extension.fromReflectionType(descriptor.getDefaultValue());
        }
      } else {
        result = (T) extension.fromReflectionType(value);
      }

      // If the lazy field is corrupted, we need to invalidate the memoized size in case the
      // corrupted message data was replaced with an empty ByteString and yet a previous serialized
      // size was memoized.
      if (extensions.lazyFieldCorrupted(descriptor)) {
        setMemoizedSerializedSize(-1);
      }
      return result;
    }

    /** Get one element of a repeated extension. */
    @SuppressWarnings("unchecked")
    public final <T> T getExtension(
        final ExtensionLite<? extends MessageT, List<T>> extensionLite, final int index) {
      Extension<MessageT, List<T>> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      FieldDescriptor descriptor = extension.getDescriptor();
      return (T)
          extension.singularFromReflectionType(extensions.getRepeatedField(descriptor, index));
    }

    /** Called by subclasses to check if all extensions are initialized. */
    protected boolean extensionsAreInitialized() {
      return extensions.isInitialized();
    }

    // TODO: compute this in the builder at {@code build()} time.
    public boolean isInitialized() {
      return super.isInitialized() && extensionsAreInitialized();
    }

    /**
     * Used by subclasses to serialize extensions. Extension ranges may be interleaved with field
     * numbers, but we must write them in canonical (sorted by field number) order.
     * ExtensionSerializer helps us write individual ranges of extensions at once.
     */
    protected interface ExtensionSerializer {
      public void writeUntil(final int end, final CodedOutputStream output) throws IOException;
    }

    /** No-op implementation that writes nothing, for messages with no extensions. */
    private static final class NoOpExtensionSerializer implements ExtensionSerializer {
      // Singleton instance so we can avoid allocating a new one for each message serialization.
      private static final NoOpExtensionSerializer INSTANCE = new NoOpExtensionSerializer();

      public void writeUntil(final int end, final CodedOutputStream output) {
        // no-op
      }
    }

    /**
     * ExtensionSerializer that writes extensions from the FieldSet, for messages with extensions.
     */
    protected class ExtensionWriter implements ExtensionSerializer {
      // Imagine how much simpler this code would be if Java iterators had
      // a way to get the next element without advancing the iterator.

      private final Iterator<Map.Entry<FieldDescriptor, Object>> iter = extensions.iterator();
      private Map.Entry<FieldDescriptor, Object> next;
      private final boolean messageSetWireFormat;

      // TODO: Should be marked private in v5.x.x once GeneratedMessageV3 is removed.
      protected ExtensionWriter(final boolean messageSetWireFormat) {
        if (iter.hasNext()) {
          next = iter.next();
        }
        this.messageSetWireFormat = messageSetWireFormat;
      }

      public void writeUntil(final int end, final CodedOutputStream output) throws IOException {
        while (next != null && next.getKey().getNumber() < end) {
          FieldDescriptor descriptor = next.getKey();
          if (messageSetWireFormat
              && descriptor.getLiteJavaType() == WireFormat.JavaType.MESSAGE
              && !descriptor.isRepeated()) {
            if (next instanceof LazyField.LazyEntry<?>) {
              output.writeRawMessageSetExtension(
                  descriptor.getNumber(),
                  ((LazyField.LazyEntry<?>) next).getField().toByteString());
            } else {
              output.writeMessageSetExtension(descriptor.getNumber(), (Message) next.getValue());
            }
          } else {
            // TODO: Taken care of following code, it may cause
            // problem when we use LazyField for normal fields/extensions.
            // Due to the optional field can be duplicated at the end of
            // serialized bytes, which will make the serialized size change
            // after lazy field parsed. So when we use LazyField globally,
            // we need to change the following write method to write cached
            // bytes directly rather than write the parsed message.
            FieldSet.writeField(descriptor, next.getValue(), output);
          }
          if (iter.hasNext()) {
            next = iter.next();
          } else {
            next = null;
          }
        }
      }
    }

    /**
     * For compatibility with older gencode.
     *
     * <p>TODO Remove this in the next breaking release.
     *
     * @deprecated Use {@link newExtensionSerializer()} instead.
     */
    @Deprecated
    protected ExtensionWriter newExtensionWriter() {
      return new ExtensionWriter(false);
    }

    protected ExtensionSerializer newExtensionSerializer() {
      // Avoid allocation in the common case of no extensions.
      if (extensions.isEmpty()) {
        return NoOpExtensionSerializer.INSTANCE;
      }
      return new ExtensionWriter(false);
    }

    // TODO: Remove, replace with newMessageSetExtensionSerializer().
    protected ExtensionWriter newMessageSetExtensionWriter() {
      return new ExtensionWriter(true);
    }

    protected ExtensionSerializer newMessageSetExtensionSerializer() {
      // Avoid allocation in the common case of no extensions.
      if (extensions.isEmpty()) {
        return NoOpExtensionSerializer.INSTANCE;
      }
      return new ExtensionWriter(true);
    }

    /** Called by subclasses to compute the size of extensions. */
    protected int extensionsSerializedSize() {
      return extensions.getSerializedSize();
    }

    protected int extensionsSerializedSizeAsMessageSet() {
      return extensions.getMessageSetSerializedSize();
    }

    // ---------------------------------------------------------------
    // Reflection

    protected Map<FieldDescriptor, Object> getExtensionFields() {
      return extensions.getAllFields();
    }

    public Map<FieldDescriptor, Object> getAllFields() {
      final Map<FieldDescriptor, Object> result =
          super.getAllFieldsMutable(/* getBytesForString= */ false);
      result.putAll(getExtensionFields());
      return Collections.unmodifiableMap(result);
    }

    public Map<FieldDescriptor, Object> getAllFieldsRaw() {
      final Map<FieldDescriptor, Object> result =
          super.getAllFieldsMutable(/* getBytesForString= */ false);
      result.putAll(getExtensionFields());
      return Collections.unmodifiableMap(result);
    }

    public boolean hasField(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        return extensions.hasField(field);
      } else {
        return super.hasField(field);
      }
    }

    public Object getField(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        final Object value = extensions.getField(field);
        if (value == null) {
          if (field.isRepeated()) {
            return Collections.emptyList();
          } else if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
            // Lacking an ExtensionRegistry, we have no way to determine the
            // extension's real type, so we return a DynamicMessage.
            return DynamicMessage.getDefaultInstance(field.getMessageType());
          } else {
            return field.getDefaultValue();
          }
        } else {
          return value;
        }
      } else {
        return super.getField(field);
      }
    }

    public int getRepeatedFieldCount(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        return extensions.getRepeatedFieldCount(field);
      } else {
        return super.getRepeatedFieldCount(field);
      }
    }

    public Object getRepeatedField(final FieldDescriptor field, final int index) {
      if (field.isExtension()) {
        verifyContainingType(field);
        return extensions.getRepeatedField(field, index);
      } else {
        return super.getRepeatedField(field, index);
      }
    }

    private void verifyContainingType(final FieldDescriptor field) {
      if (field.getContainingType() != getDescriptorForType()) {
        throw new IllegalArgumentException("FieldDescriptor does not match message type.");
      }
    }
  }

  /**
   * Generated message builders for message types that contain extension ranges subclass this.
   *
   * <p>This class implements type-safe accessors for extensions. They implement all the same
   * operations that you can do with normal fields -- e.g. "get", "set", and "add" -- but for
   * extensions. The extensions are identified using instances of the class {@link
   * GeneratedExtension}; the protocol compiler generates a static instance of this class for every
   * extension in its input. Through the magic of generics, all is made type-safe.
   *
   * <p>For example, imagine you have the {@code .proto} file:
   *
   * <pre>
   * option java_class = "MyProto";
   *
   * message Foo {
   *   extensions 1000 to max;
   * }
   *
   * extend Foo {
   *   optional int32 bar;
   * }
   * </pre>
   *
   * <p>Then you might write code like:
   *
   * <pre>
   * MyProto.Foo foo =
   *   MyProto.Foo.newBuilder()
   *     .setExtension(MyProto.bar, 123)
   *     .build();
   * </pre>
   *
   * <p>See also {@link ExtendableMessage}.
   */
  @SuppressWarnings("unchecked")
  public abstract static class ExtendableBuilder<
          MessageT extends ExtendableMessage<MessageT>,
          BuilderT extends ExtendableBuilder<MessageT, BuilderT>>
      extends Builder<BuilderT> implements ExtendableMessageOrBuilder<MessageT> {

    private FieldSet.Builder<FieldDescriptor> extensions;

    protected ExtendableBuilder() {}

    protected ExtendableBuilder(BuilderParent parent) {
      super(parent);
    }

    // For immutable message conversion.
    void internalSetExtensionSet(FieldSet<FieldDescriptor> extensions) {
      this.extensions = FieldSet.Builder.fromFieldSet(extensions);
    }

    public BuilderT clear() {
      extensions = null;
      return super.clear();
    }

    private void ensureExtensionsIsMutable() {
      if (extensions == null) {
        extensions = FieldSet.newBuilder();
      }
    }

    private void verifyExtensionContainingType(final Extension<MessageT, ?> extension) {
      if (extension.getDescriptor().getContainingType() != getDescriptorForType()) {
        // This can only happen if someone uses unchecked operations.
        throw new IllegalArgumentException(
            "Extension is for type \""
                + extension.getDescriptor().getContainingType().getFullName()
                + "\" which does not match message type \""
                + getDescriptorForType().getFullName()
                + "\".");
      }
    }

    /** Get the value of an extension. */
    public final <T> T getExtension(final ExtensionLite<? extends MessageT, T> extensionLite) {
      Extension<MessageT, T> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      FieldDescriptor descriptor = extension.getDescriptor();
      final Object value = extensions == null ? null : extensions.getField(descriptor);
      if (value == null) {
        if (descriptor.isRepeated()) {
          return (T) Collections.emptyList();
        } else if (descriptor.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
          return (T) extension.getMessageDefaultInstance();
        } else {
          return (T) extension.fromReflectionType(descriptor.getDefaultValue());
        }
      } else {
        return (T) extension.fromReflectionType(value);
      }
    }

    /** Get one element of a repeated extension. */
    public final <T> T getExtension(
        final ExtensionLite<? extends MessageT, List<T>> extensionLite, final int index) {
      Extension<MessageT, List<T>> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      FieldDescriptor descriptor = extension.getDescriptor();
      if (extensions == null) {
        throw new IndexOutOfBoundsException();
      }
      return (T)
          extension.singularFromReflectionType(extensions.getRepeatedField(descriptor, index));
    }

    /** Set the value of an extension. */
    public final <T> BuilderT setExtension(
        final ExtensionLite<? extends MessageT, T> extensionLite, final T value) {
      Extension<MessageT, T> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      ensureExtensionsIsMutable();
      final FieldDescriptor descriptor = extension.getDescriptor();
      extensions.setField(descriptor, extension.toReflectionType(value));
      onChanged();
      return (BuilderT) this;
    }

    /** Set the value of one element of a repeated extension. */
    public final <T> BuilderT setExtension(
        final ExtensionLite<? extends MessageT, List<T>> extensionLite,
        final int index,
        final T value) {
      Extension<MessageT, List<T>> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      ensureExtensionsIsMutable();
      final FieldDescriptor descriptor = extension.getDescriptor();
      extensions.setRepeatedField(descriptor, index, extension.singularToReflectionType(value));
      onChanged();
      return (BuilderT) this;
    }

    /** Append a value to a repeated extension. */
    public final <T> BuilderT addExtension(
        final ExtensionLite<? extends MessageT, List<T>> extensionLite, final T value) {
      Extension<MessageT, List<T>> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      ensureExtensionsIsMutable();
      final FieldDescriptor descriptor = extension.getDescriptor();
      extensions.addRepeatedField(descriptor, extension.singularToReflectionType(value));
      onChanged();
      return (BuilderT) this;
    }

    /** Clear an extension. */
    public final <T> BuilderT clearExtension(
        final ExtensionLite<? extends MessageT, T> extensionLite) {
      Extension<MessageT, T> extension = checkNotLite(extensionLite);

      verifyExtensionContainingType(extension);
      ensureExtensionsIsMutable();
      extensions.clearField(extension.getDescriptor());
      onChanged();
      return (BuilderT) this;
    }

    /** Called by subclasses to check if all extensions are initialized. */
    protected boolean extensionsAreInitialized() {
      return extensions == null || extensions.isInitialized();
    }

    /**
     * Called by the build code path to create a copy of the extensions for building the message.
     */
    private FieldSet<FieldDescriptor> buildExtensions() {
      return extensions == null
          ? (FieldSet<FieldDescriptor>) FieldSet.emptySet()
          : extensions.buildPartial();
    }

    public boolean isInitialized() {
      return super.isInitialized() && extensionsAreInitialized();
    }

    // ---------------------------------------------------------------
    // Reflection

    public Map<FieldDescriptor, Object> getAllFields() {
      final Map<FieldDescriptor, Object> result = super.getAllFieldsMutable();
      if (extensions != null) {
        result.putAll(extensions.getAllFields());
      }
      return Collections.unmodifiableMap(result);
    }

    public Object getField(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        final Object value = extensions == null ? null : extensions.getField(field);
        if (value == null) {
          if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
            // Lacking an ExtensionRegistry, we have no way to determine the
            // extension's real type, so we return a DynamicMessage.
            return DynamicMessage.getDefaultInstance(field.getMessageType());
          } else {
            return field.getDefaultValue();
          }
        } else {
          return value;
        }
      } else {
        return super.getField(field);
      }
    }

    public Message.Builder getFieldBuilder(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        if (field.getJavaType() != FieldDescriptor.JavaType.MESSAGE) {
          throw new UnsupportedOperationException(
              "getFieldBuilder() called on a non-Message type.");
        }
        ensureExtensionsIsMutable();
        final Object value = extensions.getFieldAllowBuilders(field);
        if (value == null) {
          Message.Builder builder = DynamicMessage.newBuilder(field.getMessageType());
          extensions.setField(field, builder);
          onChanged();
          return builder;
        } else {
          if (value instanceof Message.Builder) {
            return (Message.Builder) value;
          } else if (value instanceof Message) {
            Message.Builder builder = ((Message) value).toBuilder();
            extensions.setField(field, builder);
            onChanged();
            return builder;
          } else {
            throw new UnsupportedOperationException(
                "getRepeatedFieldBuilder() called on a non-Message type.");
          }
        }
      } else {
        return super.getFieldBuilder(field);
      }
    }

    public int getRepeatedFieldCount(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        return extensions == null ? 0 : extensions.getRepeatedFieldCount(field);
      } else {
        return super.getRepeatedFieldCount(field);
      }
    }

    public Object getRepeatedField(final FieldDescriptor field, final int index) {
      if (field.isExtension()) {
        verifyContainingType(field);
        if (extensions == null) {
          throw new IndexOutOfBoundsException();
        }
        return extensions.getRepeatedField(field, index);
      } else {
        return super.getRepeatedField(field, index);
      }
    }

    public Message.Builder getRepeatedFieldBuilder(final FieldDescriptor field, final int index) {
      if (field.isExtension()) {
        verifyContainingType(field);
        ensureExtensionsIsMutable();
        if (field.getJavaType() != FieldDescriptor.JavaType.MESSAGE) {
          throw new UnsupportedOperationException(
              "getRepeatedFieldBuilder() called on a non-Message type.");
        }
        final Object value = extensions.getRepeatedFieldAllowBuilders(field, index);
        if (value instanceof Message.Builder) {
          return (Message.Builder) value;
        } else if (value instanceof Message) {
          Message.Builder builder = ((Message) value).toBuilder();
          extensions.setRepeatedField(field, index, builder);
          onChanged();
          return builder;
        } else {
          throw new UnsupportedOperationException(
              "getRepeatedFieldBuilder() called on a non-Message type.");
        }
      } else {
        return super.getRepeatedFieldBuilder(field, index);
      }
    }

    public boolean hasField(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        return extensions != null && extensions.hasField(field);
      } else {
        return super.hasField(field);
      }
    }

    public BuilderT setField(final FieldDescriptor field, final Object value) {
      if (field.isExtension()) {
        verifyContainingType(field);
        ensureExtensionsIsMutable();
        extensions.setField(field, value);
        onChanged();
        return (BuilderT) this;
      } else {
        return super.setField(field, value);
      }
    }

    public BuilderT clearField(final FieldDescriptor field) {
      if (field.isExtension()) {
        verifyContainingType(field);
        ensureExtensionsIsMutable();
        extensions.clearField(field);
        onChanged();
        return (BuilderT) this;
      } else {
        return super.clearField(field);
      }
    }

    public BuilderT setRepeatedField(
        final FieldDescriptor field, final int index, final Object value) {
      if (field.isExtension()) {
        verifyContainingType(field);
        ensureExtensionsIsMutable();
        extensions.setRepeatedField(field, index, value);
        onChanged();
        return (BuilderT) this;
      } else {
        return super.setRepeatedField(field, index, value);
      }
    }

    public BuilderT addRepeatedField(final FieldDescriptor field, final Object value) {
      if (field.isExtension()) {
        verifyContainingType(field);
        ensureExtensionsIsMutable();
        extensions.addRepeatedField(field, value);
        onChanged();
        return (BuilderT) this;
      } else {
        return super.addRepeatedField(field, value);
      }
    }

    public Message.Builder newBuilderForField(final FieldDescriptor field) {
      if (field.isExtension()) {
        return DynamicMessage.newBuilder(field.getMessageType());
      } else {
        return super.newBuilderForField(field);
      }
    }

    // TODO: Should be marked final in v5.x.x once GeneratedMessageV3 is removed.
    protected void mergeExtensionFields(final ExtendableMessage<?> other) {
      if (other.extensions != null) {
        ensureExtensionsIsMutable();
        extensions.mergeFrom(other.extensions);
        onChanged();
      }
    }

    protected boolean parseUnknownField(
        CodedInputStream input, ExtensionRegistryLite extensionRegistry, int tag)
        throws IOException {
      ensureExtensionsIsMutable();
      return MessageReflection.mergeFieldFrom(
          input,
          input.shouldDiscardUnknownFields() ? null : getUnknownFieldSetBuilder(),
          extensionRegistry,
          getDescriptorForType(),
          new MessageReflection.ExtensionBuilderAdapter(extensions),
          tag);
    }

    private void verifyContainingType(final FieldDescriptor field) {
      if (field.getContainingType() != getDescriptorForType()) {
        throw new IllegalArgumentException("FieldDescriptor does not match message type.");
      }
    }
  }

  // -----------------------------------------------------------------

  /**
   * Gets the descriptor for an extension. The implementation depends on whether the extension is
   * scoped in the top level of a file or scoped in a Message.
   */
  interface ExtensionDescriptorRetriever {
    FieldDescriptor getDescriptor();
  }

  /** For use by generated code only. */
  public static <ContainingT extends Message, T>
      GeneratedExtension<ContainingT, T> newFileScopedGeneratedExtension(
          final Class<?> singularType, final Message defaultInstance) {
    throw new UnsupportedOperationException("com.google.protobuf.GeneratedMessage newFileScopedGeneratedExtension(..)");
  }

  /**
   * Type used to represent generated extensions. The protocol compiler generates a static singleton
   * instance of this class for each extension.
   *
   * <p>For example, imagine you have the {@code .proto} file:
   *
   * <pre>
   * option java_class = "MyProto";
   *
   * message Foo {
   *   extensions 1000 to max;
   * }
   *
   * extend Foo {
   *   optional int32 bar;
   * }
   * </pre>
   *
   * <p>Then, {@code MyProto.Foo.bar} has type {@code GeneratedExtension<MyProto.Foo, Integer>}.
   *
   * <p>In general, users should ignore the details of this type, and simply use these static
   * singletons as parameters to the extension accessors defined in {@link ExtendableMessage} and
   * {@link ExtendableBuilder}.
   */
  public static class GeneratedExtension<ContainingT extends Message, T>
      extends Extension<ContainingT, T> {

    // We can't always initialize the descriptor of a GeneratedExtension when
    // we first construct it due to initialization order difficulties (namely,
    // the descriptor may not have been constructed yet, since it is often
    // constructed by the initializer of a separate module).
    //
    // In the case of nested extensions, we initialize the
    // ExtensionDescriptorRetriever with an instance that uses the scoping
    // Message's default instance to retrieve the extension's descriptor.
    //
    // In the case of non-nested extensions, we initialize the
    // ExtensionDescriptorRetriever to null and rely on the outer class's static
    // initializer to call internalInit() after the descriptor has been parsed.
    GeneratedExtension(
        ExtensionDescriptorRetriever descriptorRetriever,
        Class<?> singularType,
        Message messageDefaultInstance,
        ExtensionType extensionType) {
      throw new UnsupportedOperationException("GeneratedExtension");
    }

    /** For use by generated code only. */
    public void internalInit(final FieldDescriptor descriptor) {
      throw new UnsupportedOperationException("internalInit");
    }

    public FieldDescriptor getDescriptor() {
      throw new UnsupportedOperationException("getDescriptor");
    }

    /**
     * If the extension is an embedded message or group, returns the default instance of the
     * message.
     */
    public Message getMessageDefaultInstance() {
      throw new UnsupportedOperationException("getMessageDefaultInstance");
    }

    protected ExtensionType getExtensionType() {
      throw new UnsupportedOperationException("getExtensionType");
    }

    /**
     * Convert from the type used by the reflection accessors to the type used by native accessors.
     * E.g., for enums, the reflection accessors use EnumValueDescriptors but the native accessors
     * use the generated enum type.
     */
    protected Object fromReflectionType(final Object value) {
      throw new UnsupportedOperationException("fromReflectionType");
    }

    /**
     * Like {@link #fromReflectionType(Object)}, but if the type is a repeated type, this converts a
     * single element.
     */
    protected Object singularFromReflectionType(final Object value) {
      throw new UnsupportedOperationException("singularFromReflectionType");
    }

    /**
     * Convert from the type used by the native accessors to the type used by reflection accessors.
     * E.g., for enums, the reflection accessors use EnumValueDescriptors but the native accessors
     * use the generated enum type.
     */
    protected Object toReflectionType(final Object value) {
      throw new UnsupportedOperationException("toReflectionType");
    }

    /**
     * Like {@link #toReflectionType(Object)}, but if the type is a repeated type, this converts a
     * single element.
     */
    protected Object singularToReflectionType(final Object value) {
      throw new UnsupportedOperationException("singularToReflectionType");
    }

    public int getNumber() {
      throw new UnsupportedOperationException("getNumber");
    }

    public WireFormat.FieldType getLiteType() {
      throw new UnsupportedOperationException("getLiteType");
    }

    public boolean isRepeated() {
      throw new UnsupportedOperationException("isRepeated");
    }

    @SuppressWarnings("unchecked")
    public T getDefaultValue() {
      throw new UnsupportedOperationException("getDefaultValue");
    }
  }

  /**
   * Gets the map field with the given field number. This method should be overridden in the
   * generated message class if the message contains map fields.
   *
   * <p>Unlike other field types, reflection support for map fields can't be implemented based on
   * generated public API because we need to access a map field as a list in reflection API but the
   * generated API only allows us to access it as a map. This method returns the underlying map
   * field directly and thus enables us to access the map field as a list.
   */
  @SuppressWarnings("unused")
  protected MapFieldReflectionAccessor internalGetMapFieldReflection(int fieldNumber) {
    return internalGetMapField(fieldNumber);
  }

  /** TODO: Remove, exists for compatibility with generated code. */
  @Deprecated
  @SuppressWarnings({"rawtypes", "unused"})
  protected MapField internalGetMapField(int fieldNumber) {
    // Note that we can't use descriptor names here because this method will
    // be called when descriptor is being initialized.
    throw new IllegalArgumentException("No map fields found in " + getClass().getName());
  }

  /**
   * Users should ignore this class. This class provides the implementation with access to the
   * fields of a message object using Java reflection.
   */
  // TODO: Should be marked final in v5.x.x once GeneratedMessageV3 is removed.
  public static class FieldAccessorTable {

    /**
     * Construct a FieldAccessorTable for a particular message class. Only one FieldAccessorTable
     * should ever be constructed per class.
     *
     * @param descriptor The type's descriptor.
     * @param camelCaseNames The camelcase names of all fields in the message. These are used to
     *     derive the accessor method names.
     * @param messageClass The message type.
     * @param builderClass The builder type.
     */
    public FieldAccessorTable(
        final Descriptor descriptor,
        final String[] camelCaseNames,
        final Class<? extends GeneratedMessage> messageClass,
        final Class<? extends Builder<?>> builderClass) {
      throw new UnsupportedOperationException("com.google.protobuf.GeneratedMessage$FieldAccessorTable *(..)");
    }

    /**
     * Construct a FieldAccessorTable for a particular message class without initializing
     * FieldAccessors.
     */
    public FieldAccessorTable(final Descriptor descriptor, final String[] camelCaseNames) {
      throw new UnsupportedOperationException("com.google.protobuf.GeneratedMessage$FieldAccessorTable *(..)");
    }

    /**
     * Ensures the field accessors are initialized. This method is thread-safe.
     *
     * @param messageClass The message type.
     * @param builderClass The builder type.
     * @return this
     */
    public FieldAccessorTable ensureFieldAccessorsInitialized(
        Class<? extends GeneratedMessage> messageClass, Class<? extends Builder<?>> builderClass) {
      throw new UnsupportedOperationException("com.google.protobuf.GeneratedMessage$FieldAccessorTable *(..)");
    }

    private final Descriptor descriptor;
    private final FieldAccessor[] fields;
    private String[] camelCaseNames;
    private final OneofAccessor[] oneofs;
    private volatile boolean initialized;

    /** Get the FieldAccessor for a particular field. */
    private FieldAccessor getField(final FieldDescriptor field) {
      throw new UnsupportedOperationException("com.google.protobuf.GeneratedMessage$FieldAccessorTable *(..)");
    }

    /** Get the OneofAccessor for a particular oneof. */
    private OneofAccessor getOneof(final OneofDescriptor oneof) {
      throw new UnsupportedOperationException("com.google.protobuf.GeneratedMessage$FieldAccessorTable *(..)");
    }

    /**
     * Abstract interface that provides access to a single field. This is implemented differently
     * depending on the field type and cardinality.
     */
    private interface FieldAccessor {
      Object get(GeneratedMessage message);

      Object get(GeneratedMessage.Builder<?> builder);

      Object getRaw(GeneratedMessage message);

      void set(Builder<?> builder, Object value);

      Object getRepeated(GeneratedMessage message, int index);

      Object getRepeated(GeneratedMessage.Builder<?> builder, int index);

      void setRepeated(Builder<?> builder, int index, Object value);

      void addRepeated(Builder<?> builder, Object value);

      boolean has(GeneratedMessage message);

      boolean has(GeneratedMessage.Builder<?> builder);

      int getRepeatedCount(GeneratedMessage message);

      int getRepeatedCount(GeneratedMessage.Builder<?> builder);

      void clear(Builder<?> builder);

      Message.Builder newBuilder();

      Message.Builder getBuilder(GeneratedMessage.Builder<?> builder);

      Message.Builder getRepeatedBuilder(GeneratedMessage.Builder<?> builder, int index);
    }

    /** OneofAccessor provides access to a single oneof. */
    private static interface OneofAccessor {
      public boolean has(final GeneratedMessage message);

      public boolean has(GeneratedMessage.Builder<?> builder);

      public FieldDescriptor get(final GeneratedMessage message);

      public FieldDescriptor get(GeneratedMessage.Builder<?> builder);

      public void clear(final Builder<?> builder);
    }
  }

  /**
   * Checks that the {@link Extension} is non-Lite and returns it as a {@link GeneratedExtension}.
   */
  private static <MessageT extends ExtendableMessage<MessageT>, T>
      Extension<MessageT, T> checkNotLite(ExtensionLite<? extends MessageT, T> extension) {
    if (extension.isLite()) {
      throw new IllegalArgumentException("Expected non-lite extension.");
    }

    return (Extension<MessageT, T>) extension;
  }

  protected static boolean isStringEmpty(final Object value) {
    if (value instanceof String) {
      return ((String) value).isEmpty();
    } else {
      return ((ByteString) value).isEmpty();
    }
  }

  protected static int computeStringSize(final int fieldNumber, final Object value) {
    if (value instanceof String) {
      return CodedOutputStream.computeStringSize(fieldNumber, (String) value);
    } else {
      return CodedOutputStream.computeBytesSize(fieldNumber, (ByteString) value);
    }
  }

  protected static int computeStringSizeNoTag(final Object value) {
    if (value instanceof String) {
      return CodedOutputStream.computeStringSizeNoTag((String) value);
    } else {
      return CodedOutputStream.computeBytesSizeNoTag((ByteString) value);
    }
  }

  protected static void writeString(
      CodedOutputStream output, final int fieldNumber, final Object value) throws IOException {
    if (value instanceof String) {
      output.writeString(fieldNumber, (String) value);
    } else {
      output.writeBytes(fieldNumber, (ByteString) value);
    }
  }

  protected static void writeStringNoTag(CodedOutputStream output, final Object value)
      throws IOException {
    if (value instanceof String) {
      output.writeStringNoTag((String) value);
    } else {
      output.writeBytesNoTag((ByteString) value);
    }
  }

  protected static <V> void serializeIntegerMapTo(
      CodedOutputStream out,
      MapField<Integer, V> field,
      MapEntry<Integer, V> defaultEntry,
      int fieldNumber)
      throws IOException {
    Map<Integer, V> m = field.getMap();
    if (!out.isSerializationDeterministic()) {
      serializeMapTo(out, m, defaultEntry, fieldNumber);
      return;
    }
    // Sorting the unboxed keys and then look up the values during serialization is 2x faster
    // than sorting map entries with a custom comparator directly.
    int[] keys = new int[m.size()];
    int index = 0;
    for (int k : m.keySet()) {
      keys[index++] = k;
    }
    Arrays.sort(keys);
    for (int key : keys) {
      out.writeMessage(
          fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
    }
  }

  protected static <V> void serializeLongMapTo(
      CodedOutputStream out,
      MapField<Long, V> field,
      MapEntry<Long, V> defaultEntry,
      int fieldNumber)
      throws IOException {
    Map<Long, V> m = field.getMap();
    if (!out.isSerializationDeterministic()) {
      serializeMapTo(out, m, defaultEntry, fieldNumber);
      return;
    }

    long[] keys = new long[m.size()];
    int index = 0;
    for (long k : m.keySet()) {
      keys[index++] = k;
    }
    Arrays.sort(keys);
    for (long key : keys) {
      out.writeMessage(
          fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
    }
  }

  protected static <V> void serializeStringMapTo(
      CodedOutputStream out,
      MapField<String, V> field,
      MapEntry<String, V> defaultEntry,
      int fieldNumber)
      throws IOException {
    Map<String, V> m = field.getMap();
    if (!out.isSerializationDeterministic()) {
      serializeMapTo(out, m, defaultEntry, fieldNumber);
      return;
    }

    // Sorting the String keys and then look up the values during serialization is 25% faster than
    // sorting map entries with a custom comparator directly.
    String[] keys = new String[m.size()];
    keys = m.keySet().toArray(keys);
    Arrays.sort(keys);
    for (String key : keys) {
      out.writeMessage(
          fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
    }
  }

  protected static <V> void serializeBooleanMapTo(
      CodedOutputStream out,
      MapField<Boolean, V> field,
      MapEntry<Boolean, V> defaultEntry,
      int fieldNumber)
      throws IOException {
    Map<Boolean, V> m = field.getMap();
    if (!out.isSerializationDeterministic()) {
      serializeMapTo(out, m, defaultEntry, fieldNumber);
      return;
    }
    maybeSerializeBooleanEntryTo(out, m, defaultEntry, fieldNumber, false);
    maybeSerializeBooleanEntryTo(out, m, defaultEntry, fieldNumber, true);
  }

  private static <V> void maybeSerializeBooleanEntryTo(
      CodedOutputStream out,
      Map<Boolean, V> m,
      MapEntry<Boolean, V> defaultEntry,
      int fieldNumber,
      boolean key)
      throws IOException {
    if (m.containsKey(key)) {
      out.writeMessage(
          fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
    }
  }

  /** Serialize the map using the iteration order. */
  private static <K, V> void serializeMapTo(
      CodedOutputStream out, Map<K, V> m, MapEntry<K, V> defaultEntry, int fieldNumber)
      throws IOException {
    for (Map.Entry<K, V> entry : m.entrySet()) {
      out.writeMessage(
          fieldNumber,
          defaultEntry
              .newBuilderForType()
              .setKey(entry.getKey())
              .setValue(entry.getValue())
              .build());
    }
  }
}
