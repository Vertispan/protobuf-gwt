// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements MapEntry messages.
 *
 * <p>In reflection API, map fields will be treated as repeated message fields and each map entry is
 * accessed as a message. This MapEntry class is used to represent these map entry messages in
 * reflection API.
 *
 * <p>Protobuf internal. Users shouldn't use this class.
 */
public final class MapEntry<K, V> extends AbstractMessage {

  private static final class Metadata<K, V> extends MapEntryLite.Metadata<K, V> {

    public final Descriptor descriptor;
    public final Parser<MapEntry<K, V>> parser;

    public Metadata(
        Descriptor descriptor,
        MapEntry<K, V> defaultInstance,
        WireFormat.FieldType keyType,
        WireFormat.FieldType valueType) {
      super(keyType, defaultInstance.key, valueType, defaultInstance.value);
      this.descriptor = descriptor;
      this.parser =
          new AbstractParser<MapEntry<K, V>>() {

            public MapEntry<K, V> parsePartialFrom(
                CodedInputStream input, ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
              return new MapEntry<K, V>(Metadata.this, input, extensionRegistry);
            }
          };
    }
  }

  private final K key;
  private final V value;
  private final Metadata<K, V> metadata;

  /** Create a default MapEntry instance. */
  private MapEntry(
      Descriptor descriptor,
      WireFormat.FieldType keyType,
      K defaultKey,
      WireFormat.FieldType valueType,
      V defaultValue) {
    this.key = defaultKey;
    this.value = defaultValue;
    this.metadata = new Metadata<K, V>(descriptor, this, keyType, valueType);
  }

  /** Create a MapEntry with the provided key and value. */
  @SuppressWarnings("unchecked")
  private MapEntry(Metadata metadata, K key, V value) {
    this.key = key;
    this.value = value;
    this.metadata = metadata;
  }

  /** Parsing constructor. */
  private MapEntry(
      Metadata<K, V> metadata, CodedInputStream input, ExtensionRegistryLite extensionRegistry)
      throws InvalidProtocolBufferException {
    try {
      this.metadata = metadata;
      Map.Entry<K, V> entry = MapEntryLite.parseEntry(input, metadata, extensionRegistry);
      this.key = entry.getKey();
      this.value = entry.getValue();
    } catch (InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (IOException e) {
      throw new InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    }
  }

  /**
   * Create a default MapEntry instance. A default MapEntry instance should be created only once for
   * each map entry message type. Generated code should store the created default instance and use
   * it later to create new MapEntry messages of the same type.
   */
  public static <K, V> MapEntry<K, V> newDefaultInstance(
      Descriptor descriptor,
      WireFormat.FieldType keyType,
      K defaultKey,
      WireFormat.FieldType valueType,
      V defaultValue) {
    return new MapEntry<K, V>(descriptor, keyType, defaultKey, valueType, defaultValue);
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  private volatile int cachedSerializedSize = -1;

  public int getSerializedSize() {
    if (cachedSerializedSize != -1) {
      return cachedSerializedSize;
    }

    int size = MapEntryLite.computeSerializedSize(metadata, key, value);
    cachedSerializedSize = size;
    return size;
  }

  public void writeTo(CodedOutputStream output) throws IOException {
    MapEntryLite.writeTo(output, metadata, key, value);
  }

  public boolean isInitialized() {
    return isInitialized(metadata, value);
  }

  public Parser<MapEntry<K, V>> getParserForType() {
    return metadata.parser;
  }

  public Builder<K, V> newBuilderForType() {
    return new Builder<K, V>(metadata);
  }

  public Builder<K, V> toBuilder() {
    return new Builder<K, V>(metadata, key, value, true, true);
  }

  public MapEntry<K, V> getDefaultInstanceForType() {
    return new MapEntry<K, V>(metadata, metadata.defaultKey, metadata.defaultValue);
  }

  public Descriptor getDescriptorForType() {
    return metadata.descriptor;
  }

  public Map<FieldDescriptor, Object> getAllFields() {
    TreeMap<FieldDescriptor, Object> result = new TreeMap<FieldDescriptor, Object>();
    for (final FieldDescriptor field : metadata.descriptor.getFields()) {
      if (hasField(field)) {
        result.put(field, getField(field));
      }
    }
    return Collections.unmodifiableMap(result);
  }

  private void checkFieldDescriptor(FieldDescriptor field) {
    if (field.getContainingType() != metadata.descriptor) {
      throw new RuntimeException(
          "Wrong FieldDescriptor \""
              + field.getFullName()
              + "\" used in message \""
              + metadata.descriptor.getFullName());
    }
  }

  public boolean hasField(FieldDescriptor field) {
    checkFieldDescriptor(field);
    ;
    // A MapEntry always contains two fields.
    return true;
  }

  public Object getField(FieldDescriptor field) {
    checkFieldDescriptor(field);
    Object result = field.getNumber() == 1 ? getKey() : getValue();
    // Convert enums to EnumValueDescriptor.
    if (field.getType() == FieldDescriptor.Type.ENUM) {
      result = field.getEnumType().findValueByNumberCreatingIfUnknown((java.lang.Integer) result);
    }
    return result;
  }

  public int getRepeatedFieldCount(FieldDescriptor field) {
    throw new RuntimeException("There is no repeated field in a map entry message.");
  }

  public Object getRepeatedField(FieldDescriptor field, int index) {
    throw new RuntimeException("There is no repeated field in a map entry message.");
  }

  public UnknownFieldSet getUnknownFields() {
    return UnknownFieldSet.getDefaultInstance();
  }

  /** Builder to create {@link MapEntry} messages. */
  public static class Builder<K, V> extends AbstractMessage.Builder<Builder<K, V>> {
    private final Metadata<K, V> metadata;
    private K key;
    private V value;
    private boolean hasKey;
    private boolean hasValue;

    private Builder(Metadata<K, V> metadata) {
      this(metadata, metadata.defaultKey, metadata.defaultValue, false, false);
    }

    private Builder(Metadata<K, V> metadata, K key, V value, boolean hasKey, boolean hasValue) {
      this.metadata = metadata;
      this.key = key;
      this.value = value;
      this.hasKey = hasKey;
      this.hasValue = hasValue;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public Builder<K, V> setKey(K key) {
      this.key = key;
      this.hasKey = true;
      return this;
    }

    public Builder<K, V> clearKey() {
      this.key = metadata.defaultKey;
      this.hasKey = false;
      return this;
    }

    public Builder<K, V> setValue(V value) {
      this.value = value;
      this.hasValue = true;
      return this;
    }

    public Builder<K, V> clearValue() {
      this.value = metadata.defaultValue;
      this.hasValue = false;
      return this;
    }

    public MapEntry<K, V> build() {
      MapEntry<K, V> result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public MapEntry<K, V> buildPartial() {
      return new MapEntry<K, V>(metadata, key, value);
    }

    public Descriptor getDescriptorForType() {
      return metadata.descriptor;
    }

    private void checkFieldDescriptor(FieldDescriptor field) {
      if (field.getContainingType() != metadata.descriptor) {
        throw new RuntimeException(
            "Wrong FieldDescriptor \""
                + field.getFullName()
                + "\" used in message \""
                + metadata.descriptor.getFullName());
      }
    }

    public Message.Builder newBuilderForField(FieldDescriptor field) {
      checkFieldDescriptor(field);
      ;
      // This method should be called for message fields and in a MapEntry
      // message only the value field can possibly be a message field.
      if (field.getNumber() != 2 || field.getJavaType() != FieldDescriptor.JavaType.MESSAGE) {
        throw new RuntimeException("\"" + field.getFullName() + "\" is not a message value field.");
      }
      return ((Message) value).newBuilderForType();
    }

    @SuppressWarnings("unchecked")
    public Builder<K, V> setField(FieldDescriptor field, Object value) {
      throw new UnsupportedOperationException("com.google.protobuf.MapEntry$Builder setField(..)");
    }

    public Builder<K, V> clearField(FieldDescriptor field) {
      checkFieldDescriptor(field);
      if (field.getNumber() == 1) {
        clearKey();
      } else {
        clearValue();
      }
      return this;
    }

    public Builder<K, V> setRepeatedField(FieldDescriptor field, int index, Object value) {
      throw new RuntimeException("There is no repeated field in a map entry message.");
    }

    public Builder<K, V> addRepeatedField(FieldDescriptor field, Object value) {
      throw new RuntimeException("There is no repeated field in a map entry message.");
    }

    public Builder<K, V> setUnknownFields(UnknownFieldSet unknownFields) {
      // Unknown fields are discarded for MapEntry message.
      return this;
    }

    public MapEntry<K, V> getDefaultInstanceForType() {
      return new MapEntry<K, V>(metadata, metadata.defaultKey, metadata.defaultValue);
    }

    public boolean isInitialized() {
      return MapEntry.isInitialized(metadata, value);
    }

    public Map<FieldDescriptor, Object> getAllFields() {
      final TreeMap<FieldDescriptor, Object> result = new TreeMap<FieldDescriptor, Object>();
      for (final FieldDescriptor field : metadata.descriptor.getFields()) {
        if (hasField(field)) {
          result.put(field, getField(field));
        }
      }
      return Collections.unmodifiableMap(result);
    }

    public boolean hasField(FieldDescriptor field) {
      checkFieldDescriptor(field);
      return field.getNumber() == 1 ? hasKey : hasValue;
    }

    public Object getField(FieldDescriptor field) {
      checkFieldDescriptor(field);
      Object result = field.getNumber() == 1 ? getKey() : getValue();
      // Convert enums to EnumValueDescriptor.
      if (field.getType() == FieldDescriptor.Type.ENUM) {
        result = field.getEnumType().findValueByNumberCreatingIfUnknown((Integer) result);
      }
      return result;
    }

    public int getRepeatedFieldCount(FieldDescriptor field) {
      throw new RuntimeException("There is no repeated field in a map entry message.");
    }

    public Object getRepeatedField(FieldDescriptor field, int index) {
      throw new RuntimeException("There is no repeated field in a map entry message.");
    }

    public UnknownFieldSet getUnknownFields() {
      return UnknownFieldSet.getDefaultInstance();
    }

    public Builder<K, V> clone() {
      return new Builder<>(metadata, key, value, hasKey, hasValue);
    }
  }

  private static <V> boolean isInitialized(Metadata metadata, V value) {
    if (metadata.valueType.getJavaType() == WireFormat.JavaType.MESSAGE) {
      return ((MessageLite) value).isInitialized();
    }
    return true;
  }

  /** Returns the metadata only for experimental runtime. */
  final Metadata<K, V> getMetadata() {
    return metadata;
  }
}
