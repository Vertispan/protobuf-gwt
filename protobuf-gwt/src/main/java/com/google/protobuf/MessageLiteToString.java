// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import com.google.protobuf.gwt.StaticImpls;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.gwtproject.nio.Numbers;

/** Helps generate {@link String} representations of {@link MessageLite} protos. */
final class MessageLiteToString {

  private static final String LIST_SUFFIX = "List";
  private static final String BUILDER_LIST_SUFFIX = "OrBuilderList";
  private static final String MAP_SUFFIX = "Map";
  private static final String BYTES_SUFFIX = "Bytes";
  private static final char[] INDENT_BUFFER = new char[80];

  static {
    Arrays.fill(INDENT_BUFFER, ' ');
  }

  private MessageLiteToString() {
    // Classes which are not intended to be instantiated should be made non-instantiable with a
    // private constructor. This includes utility classes (classes with only static members).
  }

  /**
   * Returns a {@link String} representation of the {@link MessageLite} object. The first line of
   * the {@code String} representation includes a comment string to uniquely identify
   * the object instance. This acts as an indicator that this should not be relied on for
   * comparisons.
   */
  static String toString(MessageLite messageLite, String commentString) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("# ").append(commentString);
    reflectivePrintWithIndent(messageLite, buffer, 0);
    return buffer.toString();
  }

  /**
   * Reflectively prints the {@link MessageLite} to the buffer at given {@code indent} level.
   *
   * @param buffer the buffer to write to
   * @param indent the number of spaces to indent the proto by
   */
  private static void reflectivePrintWithIndent(
      MessageLite messageLite, StringBuilder buffer, int indent) {
    throw new UnsupportedOperationException("com.google.protobuf.MessageLiteToString reflectivePrintWithIndent(..)");
  }

  private static boolean isDefaultValue(Object o) {
    if (o instanceof Boolean) {
      return !((Boolean) o);
    }
    if (o instanceof Integer) {
      return ((Integer) o) == 0;
    }
    if (o instanceof Float) {
      return StaticImpls.floatToRawIntBits((Float) o) == 0;
    }
    if (o instanceof Double) {
      return Numbers.doubleToRawLongBits((Double) o) == 0;
    }
    if (o instanceof String) {
      return o.equals("");
    }
    if (o instanceof ByteString) {
      return o.equals(ByteString.EMPTY);
    }
    if (o instanceof MessageLite) { // Can happen in oneofs.
      return o == ((MessageLite) o).getDefaultInstanceForType();
    }
    if (o instanceof java.lang.Enum<?>) { // Catches oneof enums.
      return ((java.lang.Enum<?>) o).ordinal() == 0;
    }

    return false;
  }

  /**
   * Formats a text proto field.
   *
   * <p>For use by generated code only.
   *
   * @param buffer the buffer to write to
   * @param indent the number of spaces the proto should be indented by
   * @param name the field name (in PascalCase)
   * @param object the object value of the field
   */
  static void printField(StringBuilder buffer, int indent, String name, Object object) {
    if (object instanceof List<?>) {
      List<?> list = (List<?>) object;
      for (Object entry : list) {
        printField(buffer, indent, name, entry);
      }
      return;
    }
    if (object instanceof Map<?, ?>) {
      Map<?, ?> map = (Map<?, ?>) object;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        printField(buffer, indent, name, entry);
      }
      return;
    }

    buffer.append('\n');
    indent(indent, buffer);
    buffer.append(pascalCaseToSnakeCase(name));

    if (object instanceof String) {
      buffer.append(": \"").append(TextFormatEscaper.escapeText((String) object)).append('"');
    } else if (object instanceof ByteString) {
      buffer.append(": \"").append(TextFormatEscaper.escapeBytes((ByteString) object)).append('"');
    } else if (object instanceof GeneratedMessageLite) {
      buffer.append(" {");
      reflectivePrintWithIndent((GeneratedMessageLite<?, ?>) object, buffer, indent + 2);
      buffer.append("\n");
      indent(indent, buffer);
      buffer.append("}");
    } else if (object instanceof Map.Entry<?, ?>) {
      buffer.append(" {");
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
      printField(buffer, indent + 2, "key", entry.getKey());
      printField(buffer, indent + 2, "value", entry.getValue());
      buffer.append("\n");
      indent(indent, buffer);
      buffer.append("}");
    } else {
      buffer.append(": ").append(object);
    }
  }

  private static void indent(int indent, StringBuilder buffer) {
    while (indent > 0) {
      int partialIndent = indent;
      if (partialIndent > INDENT_BUFFER.length) {
        partialIndent = INDENT_BUFFER.length;
      }
      buffer.append(INDENT_BUFFER, 0, partialIndent);
      indent -= partialIndent;
    }
  }

  private static String pascalCaseToSnakeCase(String pascalCase) {
    if (pascalCase.isEmpty()) {
      return pascalCase;
    }

    StringBuilder builder = new StringBuilder();
    builder.append(Character.toLowerCase(pascalCase.charAt(0)));
    for (int i = 1; i < pascalCase.length(); i++) {
      char ch = pascalCase.charAt(i);
      if (Character.isUpperCase(ch)) {
        builder.append("_");
      }
      builder.append(Character.toLowerCase(ch));
    }
    return builder.toString();
  }
}
