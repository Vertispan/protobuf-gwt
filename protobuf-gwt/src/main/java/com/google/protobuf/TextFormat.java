// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import com.google.protobuf.Descriptors.FieldDescriptor;
import java.io.IOException;
import java.util.List;

/**
 * Provide text parsing and formatting support for proto2 instances. The implementation largely
 * follows text_format.cc.
 *
 * @author wenboz@google.com Wenbo Zhu
 * @author kenton@google.com Kenton Varda
 */
public final class TextFormat {

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
   * Outputs a textual representation of {@code fields} to {@code output}.
   *
   * @deprecated Use {@code printer().print(UnknownFieldSet, Appendable)}
   */
  @Deprecated
  public static void print(final UnknownFieldSet fields, final Appendable output)
      throws IOException {
    throw new UnsupportedOperationException("print");
  }

  /**
   * Same as {@code print()}, except that non-ASCII characters are not escaped.
   *
   * @deprecated Use {@code printer().escapingNonAscii(false).print(MessageOrBuilder, Appendable)}
   */
  @Deprecated
  @InlineMe(
      replacement = "TextFormat.printer().escapingNonAscii(false).print(message, output)",
      imports = "com.google.protobuf.TextFormat")
  public static void printUnicode(final MessageOrBuilder message, final Appendable output)
      throws IOException {
    throw new UnsupportedOperationException("printUnicode");
  }

  /**
   * Same as {@code print()}, except that non-ASCII characters are not escaped.
   *
   * @deprecated Use {@code printer().escapingNonAscii(false).print(UnknownFieldSet, Appendable)}
   */
  @Deprecated
  public static void printUnicode(final UnknownFieldSet fields, final Appendable output)
      throws IOException {
    throw new UnsupportedOperationException("printUnicode");
  }

  /**
   * Generates a human readable form of this message, useful for debugging and other purposes, with
   * no newline characters. This is just a trivial wrapper around {@link
   * TextFormat.Printer#shortDebugString(MessageOrBuilder)}.
   */
  public static String shortDebugString(final MessageOrBuilder message) {
    throw new UnsupportedOperationException("shortDebugString");
  }

  /**
   * Generates a human readable form of the field, useful for debugging and other purposes, with
   * no newline characters.
   *
   * @deprecated Use {@code printer().shortDebugString(FieldDescriptor, Object)}
   */
  @Deprecated
  public static String shortDebugString(final FieldDescriptor field, final Object value) {
    throw new UnsupportedOperationException("shortDebugString");
  }

  /**
   * Generates a human readable form of the unknown fields, useful for debugging and other
   * purposes, with no newline characters.
   *
   * @deprecated Use {@code printer().shortDebugString(UnknownFieldSet)}
   */
  @Deprecated
  public static String shortDebugString(final UnknownFieldSet fields) {
    throw new UnsupportedOperationException("shortDebugString");
  }

  /**
   * Like {@code print()}, but writes directly to a {@code String} and returns it.
   *
   * @deprecated Use {@code message.toString()}
   */
  @Deprecated
  @InlineMe(
      replacement = "TextFormat.printer().printToString(message)",
      imports = "com.google.protobuf.TextFormat")
  public static String printToString(final MessageOrBuilder message) {
    throw new UnsupportedOperationException("printToString");
  }

  /**
   * Like {@code print()}, but writes directly to a {@code String} and returns it.
   *
   * @deprecated Use {@link UnknownFieldSet#toString()}
   */
  @Deprecated
  public static String printToString(final UnknownFieldSet fields) {
    throw new UnsupportedOperationException("printToString");
  }

  /**
   * Same as {@code printToString()}, except that non-ASCII characters in string type fields are not
   * escaped in backslash+octals.
   *
   * @deprecated Use {@code printer().escapingNonAscii(false).printToString(MessageOrBuilder)}
   */
  @Deprecated
  @InlineMe(
      replacement = "TextFormat.printer().escapingNonAscii(false).printToString(message)",
      imports = "com.google.protobuf.TextFormat")
  public static String printToUnicodeString(final MessageOrBuilder message) {
    throw new UnsupportedOperationException("printToUnicodeString");
  }

  /**
   * Same as {@code printToString()}, except that non-ASCII characters in string type fields are
   * not escaped in backslash+octals.
   *
   * @deprecated Use {@code printer().escapingNonAscii(false).printToString(UnknownFieldSet)}
   */
  @Deprecated
  public static String printToUnicodeString(final UnknownFieldSet fields) {
    throw new UnsupportedOperationException("printToUnicodeString");
  }

  /** @deprecated Use {@code printer().printField(FieldDescriptor, Object, Appendable)} */
  @Deprecated
  public static void printField(
      final FieldDescriptor field, final Object value, final Appendable output)
      throws IOException {
    throw new UnsupportedOperationException("printField");
  }

  /** @deprecated Use {@code printer().printFieldToString(FieldDescriptor, Object)} */
  @Deprecated
  public static String printFieldToString(final FieldDescriptor field, final Object value) {
    throw new UnsupportedOperationException("printFieldToString");
  }

  /**
   * Outputs a unicode textual representation of the value of given field value.
   *
   * <p>Same as {@code printFieldValue()}, except that non-ASCII characters in string type fields
   * are not escaped in backslash+octals.
   *
   * @deprecated Use {@code printer().escapingNonAscii(false).printFieldValue(FieldDescriptor,
   *     Object, Appendable)}
   * @param field the descriptor of the field
   * @param value the value of the field
   * @param output the output to which to append the formatted value
   * @throws ClassCastException if the value is not appropriate for the given field descriptor
   * @throws IOException if there is an exception writing to the output
   */
  @Deprecated
  public static void printUnicodeFieldValue(
      final FieldDescriptor field, final Object value, final Appendable output)
      throws IOException {
    throw new UnsupportedOperationException("printUnicodeFieldValue");
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

  /** Helper class for converting protobufs to text. */
  public static final class Printer {

    // Printer instance which escapes non-ASCII characters.
    private static final Printer DEFAULT = new Printer(true, TypeRegistry.getEmptyTypeRegistry());

    /** Whether to escape non ASCII characters with backslash and octal. */
    private final boolean escapeNonAscii;

    private final TypeRegistry typeRegistry;

    private Printer(boolean escapeNonAscii, TypeRegistry typeRegistry) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
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
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /**
     * Creates a new {@link Printer} using the given typeRegistry. The new Printer clones all other
     * configurations from the current {@link Printer}.
     *
     * @throws IllegalArgumentException if a registry is already set.
     */
    public Printer usingTypeRegistry(TypeRegistry typeRegistry) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /**
     * Outputs a textual representation of the Protocol Message supplied into the parameter output.
     * (This representation is the new version of the classic "ProtocolPrinter" output from the
     * original Protocol Buffer system)
     */
    public void print(final MessageOrBuilder message, final Appendable output) throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /** Outputs a textual representation of {@code fields} to {@code output}. */
    public void print(final UnknownFieldSet fields, final Appendable output) throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private void print(final MessageOrBuilder message, final TextGenerator generator)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /**
     * Attempt to print the 'google.protobuf.Any' message in a human-friendly format. Returns false
     * if the message isn't a valid 'google.protobuf.Any' message (in which case the message should
     * be rendered just like a regular message to help debugging).
     */
    private boolean printAny(final MessageOrBuilder message, final TextGenerator generator)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    public String printFieldToString(final FieldDescriptor field, final Object value) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    public void printField(final FieldDescriptor field, final Object value, final Appendable output)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private void printField(
        final FieldDescriptor field, final Object value, final TextGenerator generator)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
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
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private void printFieldValue(
        final FieldDescriptor field, final Object value, final TextGenerator generator)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /** Like {@code print()}, but writes directly to a {@code String} and returns it. */
    public String printToString(final MessageOrBuilder message) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }
    /** Like {@code print()}, but writes directly to a {@code String} and returns it. */
    public String printToString(final UnknownFieldSet fields) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /**
     * Generates a human readable form of this message, useful for debugging and other purposes,
     * with no newline characters.
     */
    public String shortDebugString(final MessageOrBuilder message) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /**
     * Generates a human readable form of the field, useful for debugging and other purposes, with
     * no newline characters.
     */
    public String shortDebugString(final FieldDescriptor field, final Object value) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    /**
     * Generates a human readable form of the unknown fields, useful for debugging and other
     * purposes, with no newline characters.
     */
    public String shortDebugString(final UnknownFieldSet fields) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private static void printUnknownFieldValue(
        final int tag, final Object value, final TextGenerator generator) throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private void printMessage(final MessageOrBuilder message, final TextGenerator generator)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private void printSingleField(
        final FieldDescriptor field, final Object value, final TextGenerator generator)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private static void printUnknownFields(
        final UnknownFieldSet unknownFields, final TextGenerator generator) throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }

    private static void printUnknownField(
        final int number, final int wireType, final List<?> values, final TextGenerator generator)
        throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$Printer *(..)");
    }
  }

  /** Convert an unsigned 32-bit integer to a string. */
  public static String unsignedToString(final int value) {
    throw new UnsupportedOperationException("unsignedToString");
  }

  /** Convert an unsigned 64-bit integer to a string. */
  public static String unsignedToString(final long value) {
    throw new UnsupportedOperationException("unsignedToString");
  }

  /** An inner class for writing text to the output stream. */
  private static final class TextGenerator {
    private final Appendable output;
    private final StringBuilder indent = new StringBuilder();
    private final boolean singleLineMode;
    // While technically we are "at the start of a line" at the very beginning of the output, all
    // we would do in response to this is emit the (zero length) indentation, so it has no effect.
    // Setting it false here does however suppress an unwanted leading space in single-line mode.
    private boolean atStartOfLine = false;

    private TextGenerator(final Appendable output, boolean singleLineMode) {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$TextGenerator *(..)");
    }

    /**
     * Indent text by two spaces. After calling Indent(), two spaces will be inserted at the
     * beginning of each line of text. Indent() may be called multiple times to produce deeper
     * indents.
     */
    public void indent() {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$TextGenerator *(..)");
    }

    /** Reduces the current indent level by two spaces, or crashes if the indent level is zero. */
    public void outdent() {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$TextGenerator *(..)");
    }

    /**
     * Print text to the output stream. Bare newlines are never expected to be passed to this
     * method; to indicate the end of a line, call "eol()".
     */
    public void print(final CharSequence text) throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$TextGenerator *(..)");
    }

    /**
     * Signifies reaching the "end of the current line" in the output. In single-line mode, this
     * does not result in a newline being emitted, but ensures that a separating space is written
     * before the next output.
     */
    public void eol() throws IOException {
      throw new UnsupportedOperationException("com.google.protobuf.TextFormat$TextGenerator *(..)");
    }
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
