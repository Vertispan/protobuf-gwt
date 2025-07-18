// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

/**
 * RawMessageInfo stores the same amount of information as {@link MessageInfo} but in a more compact
 * format.
 */
@CheckReturnValue
final class RawMessageInfo implements MessageInfo {
  private static final int IS_PROTO2_BIT = 0x1;
  private static final int IS_EDITION_BIT = 0x4;

  private final MessageLite defaultInstance;

  /**
   * The compact format packs everything in a String object and a Object[] array. The String object
   * is encoded with field number, field type, hasbits offset, oneof index, etc., whereas the
   * Object[] array contains field references, class references, instance references, etc.
   *
   * <p>The String object encodes a sequence of integers into UTF-16 characters. For each int, it
   * will be encoding into 1 to 3 UTF-16 characters depending on its unsigned value:
   *
   * <ul>
   *   <li>1 char: [c1: 0x0000 - 0xD7FF] = int of the same value.
   *   <li>2 chars: [c1: 0xE000 - 0xFFFF], [c2: 0x0000 - 0xD7FF] = (c2 << 13) | (c1 & 0x1FFF)
   *   <li>3 chars: [c1: 0xE000 - 0xFFFF], [c2: 0xE000 - 0xFFFF], [c3: 0x0000 - 0xD7FF] = (c3 << 26)
   *       | ((c2 & 0x1FFF) << 13) | (c1 & 0x1FFF)
   * </ul>
   *
   * <p>Note that we don't use UTF-16 surrogate pairs [0xD800 - 0xDFFF] because they have to come in
   * pairs to form a valid UTF-16char sequence and don't help us encode values more efficiently.
   *
   * <p>The integer sequence encoded in the String object has the following layout:
   *
   * <ul>
   *   <li>[0]: flags, flags & 0x1 = is proto2?, flags & 0x2 = is message?, flags & 0x4 = is
   *       edition?
   *   <li>[1]: field count, if 0, this is the end of the integer sequence and the corresponding
   *       Object[] array should be null.
   *   <li>[2]: oneof count
   *   <li>[3]: hasbits count, how many hasbits integers are generated.
   *   <li>[4]: min field number
   *   <li>[5]: max field number
   *   <li>[6]: total number of entries need to allocate
   *   <li>[7]: map field count
   *   <li>[8]: repeated field count, this doesn't include map fields.
   *   <li>[9]: size of checkInitialized array
   *   <li>[...]: field entries
   * </ul>
   *
   * <p>Each field entry starts with a field number and the field type:
   *
   * <ul>
   *   <li>[0]: field number
   *   <li>[1]: field type with extra bits:
   *       <ul>
   *         <li>v & 0xFF = field type as defined in the FieldType class
   *         <li>v & 0x0100 = is required?
   *         <li>v & 0x0200 = is checkUtf8?
   *         <li>v & 0x0400 = needs isInitialized check?
   *         <li>v & 0x0800 = is enum field or map field enum value with legacy closedness?
   *         <li>v & 0x1000 = supports presence checking?
   *       </ul>
   * </ul>
   *
   * If the (singular) field supports presence checking:
   *
   * <ul>
   *   <li>[2]: hasbits offset
   * </ul>
   *
   * If the field is in an oneof:
   *
   * <ul>
   *   <li>[2]: oneof index
   * </ul>
   *
   * For other types, the field entry only has field number and field type.
   *
   * <p>The Object[] array has 3 sections:
   *
   * <ul>
   *   <li>---- oneof section ----
   *       <ul>
   *         <li>[0]: value field for oneof 1.
   *         <li>[1]: case field for oneof 1.
   *         <li>...
   *         <li>[.]: value field for oneof n.
   *         <li>[.]: case field for oneof n.
   *       </ul>
   *   <li>---- hasbits section ----
   *       <ul>
   *         <li>[.]: hasbits field 1
   *         <li>[.]: hasbits field 2
   *         <li>...
   *         <li>[.]: hasbits field n
   *       </ul>
   *   <li>---- field section ----
   *       <ul>
   *         <li>[...]: field entries
   *       </ul>
   * </ul>
   *
   * <p>In the Object[] array, field entries are ordered in the same way as field entries in the
   * String object. The size of each entry is determined by the field type.
   *
   * <ul>
   *   <li>Oneof field:
   *       <ul>
   *         <li>Oneof message field:
   *             <ul>
   *               <li>[0]: message class reference.
   *             </ul>
   *         <li>Oneof enum fieldin proto2:
   *             <ul>
   *               <li>[0]: EnumLiteMap
   *             </ul>
   *         <li>For all other oneof fields, field entry in the Object[] array is empty.
   *       </ul>
   *   <li>Repeated message field:
   *       <ul>
   *         <li>[0]: field reference
   *         <li>[1]: message class reference
   *       </ul>
   *   <li>Proto2 singular/repeated enum field:
   *       <ul>
   *         <li>[0]: field reference
   *         <li>[1]: EnumLiteMap
   *       </ul>
   *   <li>Map field with a proto2 enum value:
   *       <ul>
   *         <li>[0]: field reference
   *         <li>[1]: map default entry instance
   *         <li>[2]: EnumLiteMap
   *       </ul>
   *   <li>Map field with other value types:
   *       <ul>
   *         <li>[0]: field reference
   *         <li>[1]: map default entry instance
   *       </ul>
   *   <li>All other field type:
   *       <ul>
   *         <li>[0]: field reference
   *       </ul>
   * </ul>
   *
   * <p>In order to read the field info from this compact format, a reader needs to progress through
   * the String object and the Object[] array simultaneously.
   */
  private final String info;

  private final Object[] objects;
  private final int flags;

  RawMessageInfo(MessageLite defaultInstance, String info, Object[] objects) {
    this.defaultInstance = defaultInstance;
    this.info = info;
    this.objects = objects;
    int position = 0;
    int value = (int) info.charAt(position++);
    if (value < 0xD800) {
      flags = value;
    } else {
      int result = value & 0x1FFF;
      int shift = 13;
      while ((value = info.charAt(position++)) >= 0xD800) {
        result |= (value & 0x1FFF) << shift;
        shift += 13;
      }
      flags = result | (value << shift);
    }
  }

  String getStringInfo() {
    return info;
  }

  Object[] getObjects() {
    return objects;
  }

  public MessageLite getDefaultInstance() {
    return defaultInstance;
  }

  public ProtoSyntax getSyntax() {
    if ((flags & IS_PROTO2_BIT) != 0) {
      return ProtoSyntax.PROTO2;
    } else if ((flags & IS_EDITION_BIT) == 0x4) {
      return ProtoSyntax.EDITIONS;
    } else {
      return ProtoSyntax.PROTO3;
    }
  }

  public boolean isMessageSetWireFormat() {
    return (flags & 0x2) == 0x2;
  }
}
