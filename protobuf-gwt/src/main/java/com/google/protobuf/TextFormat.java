// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import com.google.protobuf.Descriptors.FieldDescriptor;
import java.io.IOException;

/**
 * Provide text parsing and formatting support for proto2 instances. The implementation largely
 * follows text_format.cc.
 *
 * @author wenboz@google.com Wenbo Zhu
 * @author kenton@google.com Kenton Varda
 */
public final class TextFormat {

  /**
   * Generates a human readable form of this message, useful for debugging and other purposes, with
   * no newline characters. This is just a trivial wrapper around {@link
   * TextFormat.Printer#shortDebugString(MessageOrBuilder)}.
   *
   * @deprecated Use {@code printer().emittingSingleLine(true).printToString(MessageOrBuilder)}
   */
  @Deprecated
  public static String shortDebugString(final MessageOrBuilder message) {
    throw new UnsupportedOperationException("shortDebugString");
  }

  /**
   * Outputs a textual representation of the value of an unknown field.
   *
   * @param tag the field's tag number
   * @param value the value of the field
   * @param output the output to which to append the formatted value
   * @throws ClassCastException if the value is not appropriate for the given field descriptor
   * @throws IOException if there is an exception writing to the output
   */
  public static void printUnknownFieldValue(
      final int tag, final Object value, final Appendable output) throws IOException {
    throw new UnsupportedOperationException("printUnknownFieldValue");
  }

  /** Printer instance which escapes non-ASCII characters. */
  public static Printer printer() {
    throw new UnsupportedOperationException("printer");
  }

  /** Printer instance which escapes non-ASCII characters and prints in the debug format. */
  public static Printer debugFormatPrinter() {
    throw new UnsupportedOperationException("debugFormatPrinter");
  }

  /** Printer instance which escapes non-ASCII characters and prints in the debug format. */
  public static Printer defaultFormatPrinter() {
    throw new UnsupportedOperationException("defaultFormatPrinter");
  }

  /** Helper class for converting protobufs to text. */
  public static final class Printer {

    static Printer getOutputModePrinter() {
      throw new UnsupportedOperationException("getOutputModePrinter");
    }

    /**
     * A list of the public APIs that output human-readable text from a message. A higher-level API
     * must be larger than any lower-level APIs it calls under the hood, e.g
     * DEBUG_MULTILINE.compareTo(PRINTER_PRINT_TO_STRING) > 0. The inverse is not necessarily true.
     */
    static enum FieldReporterLevel {
      REPORT_ALL(0),
      TEXT_GENERATOR(1),
      PRINT(2),
      PRINTER_PRINT_TO_STRING(3),
      TEXTFORMAT_PRINT_TO_STRING(4),
      PRINT_UNICODE(5),
      SHORT_DEBUG_STRING(6),
      LEGACY_MULTILINE(7),
      LEGACY_SINGLE_LINE(8),
      DEBUG_MULTILINE(9),
      DEBUG_SINGLE_LINE(10),
      ABSTRACT_TO_STRING(11),
      ABSTRACT_BUILDER_TO_STRING(12),
      ABSTRACT_MUTABLE_TO_STRING(13),
      REPORT_NONE(14);
      private final int index;

      FieldReporterLevel(int index) {
        this.index = index;
      }
    }

    /**
     * Return a new Printer instance with the specified escape mode.
     *
     * @param escapeNonAscii If true, the new Printer will escape non-ASCII characters (this is the
     *     default behavior. If false, the new Printer will print non-ASCII characters as is. In
     *     either case, the new Printer still escapes newlines and quotes in strings.
     * @return a new Printer that clones all other configurations from the current {@link Printer},
     *     with the escape mode set to the given parameter.
     */
    public Printer escapingNonAscii(boolean escapeNonAscii) {
      throw new UnsupportedOperationException("escapingNonAscii");
    }

    /**
     * Creates a new {@link Printer} using the given typeRegistry. The new Printer clones all other
     * configurations from the current {@link Printer}.
     *
     * @throws IllegalArgumentException if a registry is already set.
     */
    public Printer usingTypeRegistry(TypeRegistry typeRegistry) {
      throw new UnsupportedOperationException("usingTypeRegistry");
    }

    /**
     * Creates a new {@link Printer} using the given extensionRegistry. The new Printer clones all
     * other configurations from the current {@link Printer}.
     *
     * @throws IllegalArgumentException if a registry is already set.
     */
    public Printer usingExtensionRegistry(ExtensionRegistryLite extensionRegistry) {
      throw new UnsupportedOperationException("usingExtensionRegistry");
    }

    /**
     * Return a new Printer instance that outputs a redacted and unstable format suitable for
     * debugging.
     *
     * @param enablingSafeDebugFormat If true, the new Printer will redact all proto fields that are
     *     marked by a debug_redact=true option, and apply an unstable prefix to the output.
     * @return a new Printer that clones all other configurations from the current {@link Printer},
     *     with the enablingSafeDebugFormat mode set to the given parameter.
     */
    Printer enablingSafeDebugFormat(boolean enablingSafeDebugFormat) {
      throw new UnsupportedOperationException("enablingSafeDebugFormat");
    }

    /**
     * Return a new Printer instance that outputs primitive repeated fields in short notation
     *
     * @param useShortRepeatedPrimitives If true, repeated fields with a primitive type are printed
     *     using the short hand notation with comma-delimited field values in square brackets.
     * @return a new Printer that clones all other configurations from the current {@link Printer},
     *     with the useShortRepeatedPrimitives mode set to the given parameter.
     */
    public Printer usingShortRepeatedPrimitives(boolean useShortRepeatedPrimitives) {
      throw new UnsupportedOperationException("usingShortRepeatedPrimitives");
    }

    /**
     * Return a new Printer instance with the specified line formatting status.
     *
     * @param singleLine If true, the new Printer will output no newline characters.
     * @return a new Printer that clones all other configurations from the current {@link Printer},
     *     with the singleLine mode set to the given parameter.
     */
    public Printer emittingSingleLine(boolean singleLine) {
      throw new UnsupportedOperationException("emittingSingleLine");
    }

    void setSensitiveFieldReportingLevel(FieldReporterLevel level) {
      throw new UnsupportedOperationException("setSensitiveFieldReportingLevel");
    }

    /**
     * Outputs a textual representation of the Protocol Message supplied into the parameter output.
     * (This representation is the new version of the classic "ProtocolPrinter" output from the
     * original Protocol Buffer system)
     */
    public void print(final MessageOrBuilder message, final Appendable output) throws IOException {
      throw new UnsupportedOperationException("print");
    }

    void print(final MessageOrBuilder message, final Appendable output, FieldReporterLevel level)
        throws IOException {
      throw new UnsupportedOperationException("print");
    }

    /** Outputs a textual representation of {@code fields} to {@code output}. */
    public void print(final UnknownFieldSet fields, final Appendable output) throws IOException {
      throw new UnsupportedOperationException("print");
    }

    public String printFieldToString(final FieldDescriptor field, final Object value) {
      throw new UnsupportedOperationException("printFieldToString");
    }

    public void printField(final FieldDescriptor field, final Object value, final Appendable output)
        throws IOException {
      throw new UnsupportedOperationException("printField");
    }

    /**
     * Outputs a textual representation of the value of given field value.
     *
     * @param field the descriptor of the field
     * @param value the value of the field
     * @param output the output to which to append the formatted value
     * @throws ClassCastException if the value is not appropriate for the given field descriptor
     * @throws IOException if there is an exception writing to the output
     */
    public void printFieldValue(
        final FieldDescriptor field, final Object value, final Appendable output)
        throws IOException {
      throw new UnsupportedOperationException("printFieldValue");
    }

    /** Like {@code print()}, but writes directly to a {@code String} and returns it. */
    public String printToString(final MessageOrBuilder message) {
      throw new UnsupportedOperationException("printToString");
    }

    String printToString(final MessageOrBuilder message, FieldReporterLevel level) {
      throw new UnsupportedOperationException("printToString");
    }

    /** Like {@code print()}, but writes directly to a {@code String} and returns it. */
    public String printToString(final UnknownFieldSet fields) {
      throw new UnsupportedOperationException("printToString");
    }

    /**
     * Generates a human readable form of this message, useful for debugging and other purposes,
     * with no newline characters.
     *
     * @deprecated Use {@code this.emittingSingleLine(true).printToString(MessageOrBuilder)}
     */
    @Deprecated
    public String shortDebugString(final MessageOrBuilder message) {
      throw new UnsupportedOperationException("shortDebugString");
    }

    /**
     * Generates a human readable form of the field, useful for debugging and other purposes, with
     * no newline characters.
     *
     * @deprecated Use {@code this.emittingSingleLine(true).printFieldToString(FieldDescriptor,
     *     Object)}
     */
    @Deprecated
    @InlineMe(replacement = "this.emittingSingleLine(true).printFieldToString(field, value)")
    public String shortDebugString(final FieldDescriptor field, final Object value) {
      throw new UnsupportedOperationException("shortDebugString");
    }

    /**
     * Generates a human readable form of the unknown fields, useful for debugging and other
     * purposes, with no newline characters.
     *
     * @deprecated Use {@code this.emittingSingleLine(true).printToString(UnknownFieldSet)}
     */
    @Deprecated
    @InlineMe(replacement = "this.emittingSingleLine(true).printToString(fields)")
    public String shortDebugString(final UnknownFieldSet fields) {
      throw new UnsupportedOperationException("shortDebugString");
    }
  }

  /**
   * Outputs a textual representation of the Protocol Message supplied into the parameter output.
   * (This representation is the new version of the classic "ProtocolPrinter" output from the
   * original Protocol Buffer system)
   *
   * @deprecated Use {@code printer().print(MessageOrBuilder, Appendable)}
   */
  @Deprecated
  @InlineMe(
      replacement = "TextFormat.printer().print(message, output)",
      imports = "com.google.protobuf.TextFormat")
  public static void print(final MessageOrBuilder message, final Appendable output)
      throws IOException {
    throw new UnsupportedOperationException("print");
  }

  /**
   * Same as {@code print()}, except that non-ASCII characters are not escaped.
   *
   * @deprecated Use {@code printer().escapingNonAscii(false).print(MessageOrBuilder, Appendable)}
   */
  @Deprecated
  public static void printUnicode(final MessageOrBuilder message, final Appendable output)
      throws IOException {
    throw new UnsupportedOperationException("printUnicode");
  }

  /**
   * Like {@code print()}, but writes directly to a {@code String} and returns it.
   *
   * @deprecated Use {@code message.toString()}
   */
  @Deprecated
  public static String printToString(final MessageOrBuilder message) {
    throw new UnsupportedOperationException("printToString");
  }

  /**
   * Same as {@code printToString()}, except that non-ASCII characters in string type fields are not
   * escaped in backslash+octals.
   *
   * @deprecated Use {@code printer().escapingNonAscii(false).printToString(MessageOrBuilder)}
   */
  @Deprecated
  public static String printToUnicodeString(final MessageOrBuilder message) {
    throw new UnsupportedOperationException("printToUnicodeString");
  }

  /**
   * Outputs a textual representation of the value of given field value.
   *
   * @deprecated Use {@code printer().printFieldValue(FieldDescriptor, Object, Appendable)}
   * @param field the descriptor of the field
   * @param value the value of the field
   * @param output the output to which to append the formatted value
   * @throws ClassCastException if the value is not appropriate for the given field descriptor
   * @throws IOException if there is an exception writing to the output
   */
  @Deprecated
  @InlineMe(
      replacement = "TextFormat.printer().printFieldValue(field, value, output)",
      imports = "com.google.protobuf.TextFormat")
  public static void printFieldValue(
      final FieldDescriptor field, final Object value, final Appendable output) throws IOException {
    throw new UnsupportedOperationException("printFieldValue");
  }

  /** Convert an unsigned 32-bit integer to a string. */
  public static String unsignedToString(final int value) {
    throw new UnsupportedOperationException("unsignedToString");
  }

  /** Convert an unsigned 64-bit integer to a string. */
  public static String unsignedToString(final long value) {
    throw new UnsupportedOperationException("unsignedToString");
  }

  /** Thrown when parsing an invalid text format message. */
  public static class ParseException extends IOException {
    private static final long serialVersionUID = 3196188060225107702L;

    private final int line;
    private final int column;

    /** Create a new instance, with -1 as the line and column numbers. */
    public ParseException(final String message) {
      this(-1, -1, message);
    }

    /**
     * Create a new instance
     *
     * @param line the line number where the parse error occurred, using 1-offset.
     * @param column the column number where the parser error occurred, using 1-offset.
     */
    public ParseException(final int line, final int column, final String message) {
      super(Integer.toString(line) + ":" + column + ": " + message);
      this.line = line;
      this.column = column;
    }

    /**
     * Return the line where the parse exception occurred, or -1 when none is provided. The value is
     * specified as 1-offset, so the first line is line 1.
     */
    public int getLine() {
      return line;
    }

    /**
     * Return the column where the parse exception occurred, or -1 when none is provided. The value
     * is specified as 1-offset, so the first line is line 1.
     */
    public int getColumn() {
      return column;
    }
  }

  /**
   * Return a {@link Parser} instance which can parse text-format messages. The returned instance is
   * thread-safe.
   */
  public static Parser getParser() {
    throw new UnsupportedOperationException("getParser");
  }

  /** Parse a text-format message from {@code input} and merge the contents into {@code builder}. */
  public static void merge(final Readable input, final Message.Builder builder) throws IOException {
    throw new UnsupportedOperationException("merge");
  }

  /** Parse a text-format message from {@code input} and merge the contents into {@code builder}. */
  public static void merge(final CharSequence input, final Message.Builder builder)
      throws ParseException {
    throw new UnsupportedOperationException("merge");
  }

  /**
   * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
   * Extensions will be recognized if they are registered in {@code extensionRegistry}.
   */
  public static void merge(
      final Readable input,
      final ExtensionRegistry extensionRegistry,
      final Message.Builder builder)
      throws IOException {
    throw new UnsupportedOperationException("merge");
  }

  /**
   * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
   * Extensions will be recognized if they are registered in {@code extensionRegistry}.
   */
  public static void merge(
      final CharSequence input,
      final ExtensionRegistry extensionRegistry,
      final Message.Builder builder)
      throws ParseException {
    throw new UnsupportedOperationException("merge");
  }

  /**
   * Parser for text-format proto2 instances. This class is thread-safe. The implementation largely
   * follows google/protobuf/text_format.cc.
   *
   * <p>Use {@link TextFormat#getParser()} to obtain the default parser, or {@link Builder} to
   * control the parser behavior.
   */
  public static class Parser {

    /**
     * Determines if repeated values for non-repeated fields and oneofs are permitted. For example,
     * given required/optional field "foo" and a oneof containing "baz" and "moo":
     *
     * <ul>
     *   <li>"foo: 1 foo: 2"
     *   <li>"baz: 1 moo: 2"
     *   <li>merging "foo: 2" into a proto in which foo is already set, or
     *   <li>merging "moo: 2" into a proto in which baz is already set.
     * </ul>
     */
    public enum SingularOverwritePolicy {
      /**
       * Later values are merged with earlier values. For primitive fields or conflicting oneofs,
       * the last value is retained.
       */
      ALLOW_SINGULAR_OVERWRITES,
      /** An error is issued. */
      FORBID_SINGULAR_OVERWRITES
    }

    /** Returns a new instance of {@link Builder}. */
    public static Builder newBuilder() {
      throw new UnsupportedOperationException("newBuilder");
    }

    /** Builder that can be used to obtain new instances of {@link Parser}. */
    public static class Builder {

      /**
       * Sets the TypeRegistry for resolving Any. If this is not set, TextFormat will not be able to
       * parse Any unless Any is write as bytes.
       *
       * @throws IllegalArgumentException if a registry is already set.
       */
      public Builder setTypeRegistry(TypeRegistry typeRegistry) {
        throw new UnsupportedOperationException("setTypeRegistry");
      }

      /**
       * Set whether this parser will allow unknown fields. By default, an exception is thrown if an
       * unknown field is encountered. If this is set, the parser will only log a warning. Allow
       * unknown fields will also allow unknown extensions.
       *
       * <p>Use of this parameter is discouraged which may hide some errors (e.g. spelling error on
       * field name).
       */
      public Builder setAllowUnknownFields(boolean allowUnknownFields) {
        throw new UnsupportedOperationException("setAllowUnknownFields");
      }

      /**
       * Set whether this parser will allow unknown extensions. By default, an exception is thrown
       * if unknown extension is encountered. If this is set true, the parser will only log a
       * warning. Allow unknown extensions does not mean allow normal unknown fields.
       */
      public Builder setAllowUnknownExtensions(boolean allowUnknownExtensions) {
        throw new UnsupportedOperationException("setAllowUnknownExtensions");
      }

      /** Sets parser behavior when a non-repeated field appears more than once. */
      public Builder setSingularOverwritePolicy(SingularOverwritePolicy p) {
        throw new UnsupportedOperationException("setSingularOverwritePolicy");
      }

      public Builder setParseInfoTreeBuilder(TextFormatParseInfoTree.Builder parseInfoTreeBuilder) {
        throw new UnsupportedOperationException("setParseInfoTreeBuilder");
      }

      /**
       * Set the maximum recursion limit that the parser will allow. If the depth of the message
       * exceeds this limit then the parser will stop and throw an exception.
       */
      public Builder setRecursionLimit(int recursionLimit) {
        throw new UnsupportedOperationException("setRecursionLimit");
      }

      public Parser build() {
        throw new UnsupportedOperationException("build");
      }
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     */
    public void merge(final Readable input, final Message.Builder builder) throws IOException {
      throw new UnsupportedOperationException("merge");
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     */
    public void merge(final CharSequence input, final Message.Builder builder)
        throws ParseException {
      throw new UnsupportedOperationException("merge");
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     * Extensions will be recognized if they are registered in {@code extensionRegistry}.
     */
    public void merge(
        final Readable input,
        final ExtensionRegistry extensionRegistry,
        final Message.Builder builder)
        throws IOException {
      throw new UnsupportedOperationException("merge");
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     * Extensions will be recognized if they are registered in {@code extensionRegistry}.
     */
    public void merge(
        final CharSequence input,
        final ExtensionRegistry extensionRegistry,
        final Message.Builder builder)
        throws ParseException {
      throw new UnsupportedOperationException("merge");
    }
  }

  // =================================================================
  // Utility functions
  //
  // Some of these methods are package-private because Descriptors.java uses
  // them.

  /**
   * Escapes bytes in the format used in protocol buffer text format, which is the same as the
   * format used for C string literals. All bytes that are not printable 7-bit ASCII characters are
   * escaped, as well as backslash, single-quote, and double-quote characters. Characters for which
   * no defined short-hand escape sequence is defined will be escaped using 3-digit octal sequences.
   */
  public static String escapeBytes(ByteString input) {
    throw new UnsupportedOperationException("escapeBytes");
  }

  /** Like {@link #escapeBytes(ByteString)}, but used for byte array. */
  public static String escapeBytes(byte[] input) {
    throw new UnsupportedOperationException("escapeBytes");
  }

  /**
   * Un-escape a byte sequence as escaped using {@link #escapeBytes(ByteString)}. Two-digit hex
   * escapes (starting with "\x") are also recognized.
   */
  public static ByteString unescapeBytes(CharSequence charString)
      throws InvalidEscapeSequenceException {
    throw new UnsupportedOperationException("unescapeBytes");
  }

  /**
   * Thrown by {@link TextFormat#unescapeBytes} and {@link TextFormat#unescapeText} when an invalid
   * escape sequence is seen.
   */
  public static class InvalidEscapeSequenceException extends IOException {
    private static final long serialVersionUID = -8164033650142593304L;

    InvalidEscapeSequenceException(final String description) {
      super(description);
    }
  }

  /**
   * Like {@link #escapeBytes(ByteString)}, but escapes a text string. Non-ASCII characters are
   * first encoded as UTF-8, then each byte is escaped individually as a 3-digit octal escape. Yes,
   * it's weird.
   */
  static String escapeText(final String input) {
    throw new UnsupportedOperationException("escapeText");
  }

  /** Escape double quotes and backslashes in a String for emittingUnicode output of a message. */
  public static String escapeDoubleQuotesAndBackslashes(final String input) {
    throw new UnsupportedOperationException("escapeDoubleQuotesAndBackslashes");
  }

  /**
   * Un-escape a text string as escaped using {@link #escapeText(String)}. Two-digit hex escapes
   * (starting with "\x") are also recognized.
   */
  static String unescapeText(final String input) throws InvalidEscapeSequenceException {
    throw new UnsupportedOperationException("unescapeText");
  }

  /**
   * Parse a 32-bit signed integer from the text. Unlike the Java standard {@code
   * Integer.parseInt()}, this function recognizes the prefixes "0x" and "0" to signify hexadecimal
   * and octal numbers, respectively.
   */
  static int parseInt32(final String text) throws NumberFormatException {
    throw new UnsupportedOperationException("parseInt32");
  }

  /**
   * Parse a 32-bit unsigned integer from the text. Unlike the Java standard {@code
   * Integer.parseInt()}, this function recognizes the prefixes "0x" and "0" to signify hexadecimal
   * and octal numbers, respectively. The result is coerced to a (signed) {@code int} when returned
   * since Java has no unsigned integer type.
   */
  static int parseUInt32(final String text) throws NumberFormatException {
    throw new UnsupportedOperationException("parseUInt32");
  }

  /**
   * Parse a 64-bit signed integer from the text. Unlike the Java standard {@code
   * Integer.parseInt()}, this function recognizes the prefixes "0x" and "0" to signify hexadecimal
   * and octal numbers, respectively.
   */
  static long parseInt64(final String text) throws NumberFormatException {
    throw new UnsupportedOperationException("parseInt64");
  }

  /**
   * Parse a 64-bit unsigned integer from the text. Unlike the Java standard {@code
   * Integer.parseInt()}, this function recognizes the prefixes "0x" and "0" to signify hexadecimal
   * and octal numbers, respectively. The result is coerced to a (signed) {@code long} when returned
   * since Java has no unsigned long type.
   */
  static long parseUInt64(final String text) throws NumberFormatException {
    throw new UnsupportedOperationException("parseUInt64");
  }
}
