// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import static com.google.protobuf.Internal.checkNotNull;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.Edition;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.MethodOptions;
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto;
import com.google.protobuf.DescriptorProtos.OneofOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.FileDescriptor.Syntax;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Contains a collection of classes which describe protocol message types.
 *
 * <p>Every message type has a {@link Descriptor}, which lists all its fields and other information
 * about a type. You can get a message type's descriptor by calling {@code
 * MessageType.getDescriptor()}, or (given a message object of the type) {@code
 * message.getDescriptorForType()}. Furthermore, each message is associated with a {@link
 * FileDescriptor} for a relevant {@code .proto} file. You can obtain it by calling {@code
 * Descriptor.getFile()}. A {@link FileDescriptor} contains descriptors for all the messages defined
 * in that file, and file descriptors for all the imported {@code .proto} files.
 *
 * <p>Descriptors are built from DescriptorProtos, as defined in {@code
 * google/protobuf/descriptor.proto}.
 *
 * @author kenton@google.com Kenton Varda
 */
@CheckReturnValue
public final class Descriptors {
  private static final Logger logger = Logger.getLogger(Descriptors.class.getName());
  private static final int[] EMPTY_INT_ARRAY = new int[0];
  private static final Descriptor[] EMPTY_DESCRIPTORS = new Descriptor[0];
  private static final FieldDescriptor[] EMPTY_FIELD_DESCRIPTORS = new FieldDescriptor[0];
  private static final EnumDescriptor[] EMPTY_ENUM_DESCRIPTORS = new EnumDescriptor[0];
  private static final ServiceDescriptor[] EMPTY_SERVICE_DESCRIPTORS = new ServiceDescriptor[0];
  private static final OneofDescriptor[] EMPTY_ONEOF_DESCRIPTORS = new OneofDescriptor[0];

  /**
   * Describes a {@code .proto} file, including everything defined within. That includes, in
   * particular, descriptors for all the messages and file descriptors for all other imported {@code
   * .proto} files (dependencies).
   */
  public static final class FileDescriptor extends GenericDescriptor {
    /** Convert the descriptor to its protocol message representation. */
    public FileDescriptorProto toProto() {
      return proto;
    }

    /** Get the file name. */
    public String getName() {
      return proto.getName();
    }

    /** Returns this object. */
    public FileDescriptor getFile() {
      return this;
    }

    /** Returns the same as getName(). */
    public String getFullName() {
      return proto.getName();
    }

    /**
     * Get the proto package name. This is the package name given by the {@code package} statement
     * in the {@code .proto} file, which differs from the Java package.
     */
    public String getPackage() {
      return proto.getPackage();
    }

    /** Get the {@code FileOptions}, defined in {@code descriptor.proto}. */
    public FileOptions getOptions() {
      return proto.getOptions();
    }

    /** Get a list of top-level message types declared in this file. */
    public List<Descriptor> getMessageTypes() {
      return Collections.unmodifiableList(Arrays.asList(messageTypes));
    }

    /** Get a list of top-level enum types declared in this file. */
    public List<EnumDescriptor> getEnumTypes() {
      return Collections.unmodifiableList(Arrays.asList(enumTypes));
    }

    /** Get a list of top-level services declared in this file. */
    public List<ServiceDescriptor> getServices() {
      return Collections.unmodifiableList(Arrays.asList(services));
    }

    /** Get a list of top-level extensions declared in this file. */
    public List<FieldDescriptor> getExtensions() {
      return Collections.unmodifiableList(Arrays.asList(extensions));
    }

    /** Get a list of this file's dependencies (imports). */
    public List<FileDescriptor> getDependencies() {
      return Collections.unmodifiableList(Arrays.asList(dependencies));
    }

    /** Get a list of this file's public dependencies (public imports). */
    public List<FileDescriptor> getPublicDependencies() {
      return Collections.unmodifiableList(Arrays.asList(publicDependencies));
    }

    /** The syntax of the .proto file. */
    @Deprecated
    public
    enum Syntax {
      UNKNOWN("unknown"),
      PROTO2("proto2"),
      PROTO3("proto3"),
      EDITIONS("editions");

      Syntax(String name) {
        this.name = name;
      }

      private final String name;
    }

    /** Get the syntax of the .proto file. */
    @Deprecated
    public
    Syntax getSyntax() {
      if (Syntax.PROTO3.name.equals(proto.getSyntax())) {
        return Syntax.PROTO3;
      } else if (Syntax.EDITIONS.name.equals(proto.getSyntax())) {
        return Syntax.EDITIONS;
      }
      return Syntax.PROTO2;
    }

    /** Get the edition of the .proto file. */
    public Edition getEdition() {
      return proto.getEdition();
    }

    /** Gets the name of the edition as specified in the .proto file. */
    public String getEditionName() {
      if (proto.getEdition().equals(Edition.EDITION_UNKNOWN)) {
        return "";
      }
      return proto.getEdition().name().substring("EDITION_".length());
    }

    public void copyHeadingTo(FileDescriptorProto.Builder protoBuilder) {
      protoBuilder.setName(getName()).setSyntax(getSyntax().name);
      if (!getPackage().isEmpty()) {
        protoBuilder.setPackage(getPackage());
      }

      if (getSyntax().equals(Syntax.EDITIONS)) {
        protoBuilder.setEdition(getEdition());
      }

      if (!getOptions().equals(FileOptions.getDefaultInstance())) {
        protoBuilder.setOptions(getOptions());
      }
    }

    /**
     * Find a message type in the file by name. Does not find nested types.
     *
     * @param name The unqualified type name to look for.
     * @return The message type's descriptor, or {@code null} if not found.
     */
    public Descriptor findMessageTypeByName(String name) {
      // Don't allow looking up nested types.  This will make optimization
      // easier later.
      if (name.indexOf('.') != -1) {
        return null;
      }
      final String packageName = getPackage();
      if (!packageName.isEmpty()) {
        name = packageName + '.' + name;
      }
      final GenericDescriptor result = pool.findSymbol(name);
      if (result instanceof Descriptor && result.getFile() == this) {
        return (Descriptor) result;
      } else {
        return null;
      }
    }

    /**
     * Find an enum type in the file by name. Does not find nested types.
     *
     * @param name The unqualified type name to look for.
     * @return The enum type's descriptor, or {@code null} if not found.
     */
    public EnumDescriptor findEnumTypeByName(String name) {
      // Don't allow looking up nested types.  This will make optimization
      // easier later.
      if (name.indexOf('.') != -1) {
        return null;
      }
      final String packageName = getPackage();
      if (!packageName.isEmpty()) {
        name = packageName + '.' + name;
      }
      final GenericDescriptor result = pool.findSymbol(name);
      if (result instanceof EnumDescriptor && result.getFile() == this) {
        return (EnumDescriptor) result;
      } else {
        return null;
      }
    }

    /**
     * Find a service type in the file by name.
     *
     * @param name The unqualified type name to look for.
     * @return The service type's descriptor, or {@code null} if not found.
     */
    public ServiceDescriptor findServiceByName(String name) {
      // Don't allow looking up nested types.  This will make optimization
      // easier later.
      if (name.indexOf('.') != -1) {
        return null;
      }
      final String packageName = getPackage();
      if (!packageName.isEmpty()) {
        name = packageName + '.' + name;
      }
      final GenericDescriptor result = pool.findSymbol(name);
      if (result instanceof ServiceDescriptor && result.getFile() == this) {
        return (ServiceDescriptor) result;
      } else {
        return null;
      }
    }

    /**
     * Find an extension in the file by name. Does not find extensions nested inside message types.
     *
     * @param name The unqualified extension name to look for.
     * @return The extension's descriptor, or {@code null} if not found.
     */
    public FieldDescriptor findExtensionByName(String name) {
      if (name.indexOf('.') != -1) {
        return null;
      }
      final String packageName = getPackage();
      if (!packageName.isEmpty()) {
        name = packageName + '.' + name;
      }
      final GenericDescriptor result = pool.findSymbol(name);
      if (result instanceof FieldDescriptor && result.getFile() == this) {
        return (FieldDescriptor) result;
      } else {
        return null;
      }
    }

    /**
     * Construct a {@code FileDescriptor}.
     *
     * @param proto the protocol message form of the FileDescriptort
     * @param dependencies {@code FileDescriptor}s corresponding to all of the file's dependencies
     * @throws DescriptorValidationException {@code proto} is not a valid descriptor. This can occur
     *     for a number of reasons; for instance, because a field has an undefined type or because
     *     two messages were defined with the same name.
     */
    public static FileDescriptor buildFrom(FileDescriptorProto proto, FileDescriptor[] dependencies)
        throws DescriptorValidationException {
      return buildFrom(proto, dependencies, false);
    }

    /**
     * Construct a {@code FileDescriptor}.
     *
     * @param proto the protocol message form of the FileDescriptor
     * @param dependencies {@code FileDescriptor}s corresponding to all of the file's dependencies
     * @param allowUnknownDependencies if true, non-existing dependencies will be ignored and
     *     undefined message types will be replaced with a placeholder type. Undefined enum types
     *     still cause a DescriptorValidationException.
     * @throws DescriptorValidationException {@code proto} is not a valid descriptor. This can occur
     *     for a number of reasons; for instance, because a field has an undefined type or because
     *     two messages were defined with the same name.
     */
    public static FileDescriptor buildFrom(
        FileDescriptorProto proto, FileDescriptor[] dependencies, boolean allowUnknownDependencies)
        throws DescriptorValidationException {
      // Building descriptors involves two steps:  translating and linking.
      // In the translation step (implemented by FileDescriptor's
      // constructor), we build an object tree mirroring the
      // FileDescriptorProto's tree and put all of the descriptors into the
      // DescriptorPool's lookup tables.  In the linking step, we look up all
      // type references in the DescriptorPool, so that, for example, a
      // FieldDescriptor for an embedded message contains a pointer directly
      // to the Descriptor for that message's type.  We also detect undefined
      // types in the linking step.
      DescriptorPool pool = new DescriptorPool(dependencies, allowUnknownDependencies);
      FileDescriptor result =
          new FileDescriptor(proto, dependencies, pool, allowUnknownDependencies);
      result.crossLink();
      return result;
    }

    private static byte[] latin1Cat(final String[] strings) {
      // Hack:  We can't embed a raw byte array inside generated Java code
      //   (at least, not efficiently), but we can embed Strings.  So, the
      //   protocol compiler embeds the FileDescriptorProto as a giant
      //   string literal which is passed to this function to construct the
      //   file's FileDescriptor.  The string literal contains only 8-bit
      //   characters, each one representing a byte of the FileDescriptorProto's
      //   serialized form.  So, if we convert it to bytes in ISO-8859-1, we
      //   should get the original bytes that we want.
      // Literal strings are limited to 64k, so it may be split into multiple strings.
      if (strings.length == 1) {
        return strings[0].getBytes(Internal.ISO_8859_1);
      }
      StringBuilder descriptorData = new StringBuilder();
      for (String part : strings) {
        descriptorData.append(part);
      }
      return descriptorData.toString().getBytes(Internal.ISO_8859_1);
    }

    /**
     * This method is for backward compatibility with generated code which passed an
     * InternalDescriptorAssigner.
     */
    @Deprecated
    public static void internalBuildGeneratedFileFrom(
        final String[] descriptorDataParts,
        final FileDescriptor[] dependencies,
        final InternalDescriptorAssigner descriptorAssigner) {
      final byte[] descriptorBytes = latin1Cat(descriptorDataParts);

      FileDescriptorProto proto;
      try {
        proto = FileDescriptorProto.parseFrom(descriptorBytes);
      } catch (InvalidProtocolBufferException e) {
        throw new IllegalArgumentException(
            "Failed to parse protocol buffer descriptor for generated code.", e);
      }

      final FileDescriptor result;
      try {
        // When building descriptors for generated code, we allow unknown
        // dependencies by default.
        result = buildFrom(proto, dependencies, true);
      } catch (DescriptorValidationException e) {
        throw new IllegalArgumentException(
            "Invalid embedded descriptor for \"" + proto.getName() + "\".", e);
      }

      final ExtensionRegistry registry = descriptorAssigner.assignDescriptors(result);

      if (registry != null) {
        // We must re-parse the proto using the registry.
        try {
          proto = FileDescriptorProto.parseFrom(descriptorBytes, registry);
        } catch (InvalidProtocolBufferException e) {
          throw new IllegalArgumentException(
              "Failed to parse protocol buffer descriptor for generated code.", e);
        }

        result.setProto(proto);
      }
    }

    /**
     * This method is to be called by generated code only. It is equivalent to {@code buildFrom}
     * except that the {@code FileDescriptorProto} is encoded in protocol buffer wire format.
     */
    public static FileDescriptor internalBuildGeneratedFileFrom(
        final String[] descriptorDataParts, final FileDescriptor[] dependencies) {
      final byte[] descriptorBytes = latin1Cat(descriptorDataParts);

      FileDescriptorProto proto;
      try {
        proto = FileDescriptorProto.parseFrom(descriptorBytes);
      } catch (InvalidProtocolBufferException e) {
        throw new IllegalArgumentException(
            "Failed to parse protocol buffer descriptor for generated code.", e);
      }

      try {
        // When building descriptors for generated code, we allow unknown
        // dependencies by default.
        return buildFrom(proto, dependencies, true);
      } catch (DescriptorValidationException e) {
        throw new IllegalArgumentException(
            "Invalid embedded descriptor for \"" + proto.getName() + "\".", e);
      }
    }

    /**
     * This class should be used by generated code only. When calling {@link
     * FileDescriptor#internalBuildGeneratedFileFrom}, the caller provides a callback implementing
     * this interface. The callback is called after the FileDescriptor has been constructed, in
     * order to assign all the global variables defined in the generated code which point at parts
     * of the FileDescriptor. The callback returns an ExtensionRegistry which contains any
     * extensions which might be used in the descriptor -- that is, extensions of the various
     * "Options" messages defined in descriptor.proto. The callback may also return null to indicate
     * that no extensions are used in the descriptor.
     *
     * <p>This interface is deprecated. Use the return value of internalBuildGeneratedFrom()
     * instead.
     */
    @Deprecated
    public interface InternalDescriptorAssigner {
      ExtensionRegistry assignDescriptors(FileDescriptor root);
    }

    private FileDescriptorProto proto;
    private final Descriptor[] messageTypes;
    private final EnumDescriptor[] enumTypes;
    private final ServiceDescriptor[] services;
    private final FieldDescriptor[] extensions;
    private final FileDescriptor[] dependencies;
    private final FileDescriptor[] publicDependencies;
    private final DescriptorPool pool;

    private FileDescriptor(
        final FileDescriptorProto proto,
        final FileDescriptor[] dependencies,
        final DescriptorPool pool,
        boolean allowUnknownDependencies)
        throws DescriptorValidationException {
      this.pool = pool;
      this.proto = proto;
      this.dependencies = com.google.protobuf.gwt.StaticImpls.clone(dependencies);
      HashMap<String, FileDescriptor> nameToFileMap = new HashMap<>();
      for (FileDescriptor file : dependencies) {
        nameToFileMap.put(file.getName(), file);
      }
      List<FileDescriptor> publicDependencies = new ArrayList<>();
      for (int i = 0; i < proto.getPublicDependencyCount(); i++) {
        int index = proto.getPublicDependency(i);
        if (index < 0 || index >= proto.getDependencyCount()) {
          throw new DescriptorValidationException(this, "Invalid public dependency index.");
        }
        String name = proto.getDependency(index);
        FileDescriptor file = nameToFileMap.get(name);
        if (file == null) {
          if (!allowUnknownDependencies) {
            throw new DescriptorValidationException(this, "Invalid public dependency: " + name);
          }
          // Ignore unknown dependencies.
        } else {
          publicDependencies.add(file);
        }
      }
      this.publicDependencies = new FileDescriptor[publicDependencies.size()];
      publicDependencies.toArray(this.publicDependencies);

      pool.addPackage(getPackage(), this);

      messageTypes =
          (proto.getMessageTypeCount() > 0)
              ? new Descriptor[proto.getMessageTypeCount()]
              : EMPTY_DESCRIPTORS;
      for (int i = 0; i < proto.getMessageTypeCount(); i++) {
        messageTypes[i] = new Descriptor(proto.getMessageType(i), this, null, i);
      }

      enumTypes =
          (proto.getEnumTypeCount() > 0)
              ? new EnumDescriptor[proto.getEnumTypeCount()]
              : EMPTY_ENUM_DESCRIPTORS;
      for (int i = 0; i < proto.getEnumTypeCount(); i++) {
        enumTypes[i] = new EnumDescriptor(proto.getEnumType(i), this, null, i);
      }

      services =
          (proto.getServiceCount() > 0)
              ? new ServiceDescriptor[proto.getServiceCount()]
              : EMPTY_SERVICE_DESCRIPTORS;
      for (int i = 0; i < proto.getServiceCount(); i++) {
        services[i] = new ServiceDescriptor(proto.getService(i), this, i);
      }

      extensions =
          (proto.getExtensionCount() > 0)
              ? new FieldDescriptor[proto.getExtensionCount()]
              : EMPTY_FIELD_DESCRIPTORS;
      for (int i = 0; i < proto.getExtensionCount(); i++) {
        extensions[i] = new FieldDescriptor(proto.getExtension(i), this, null, i, true);
      }
    }

    /** Create a placeholder FileDescriptor for a message Descriptor. */
    FileDescriptor(String packageName, Descriptor message) throws DescriptorValidationException {
      this.pool = new DescriptorPool(new FileDescriptor[0], true);
      this.proto =
          FileDescriptorProto.newBuilder()
              .setName(message.getFullName() + ".placeholder.proto")
              .setPackage(packageName)
              .addMessageType(message.toProto())
              .build();
      this.dependencies = new FileDescriptor[0];
      this.publicDependencies = new FileDescriptor[0];

      messageTypes = new Descriptor[] {message};
      enumTypes = EMPTY_ENUM_DESCRIPTORS;
      services = EMPTY_SERVICE_DESCRIPTORS;
      extensions = EMPTY_FIELD_DESCRIPTORS;

      pool.addPackage(packageName, this);
      pool.addSymbol(message);
    }

    /** Look up and cross-link all field types, etc. */
    private void crossLink() throws DescriptorValidationException {
      for (final Descriptor messageType : messageTypes) {
        messageType.crossLink();
      }

      for (final ServiceDescriptor service : services) {
        service.crossLink();
      }

      for (final FieldDescriptor extension : extensions) {
        extension.crossLink();
      }
    }

    /**
     * Replace our {@link FileDescriptorProto} with the given one, which is identical except that it
     * might contain extensions that weren't present in the original. This method is needed for
     * bootstrapping when a file defines custom options. The options may be defined in the file
     * itself, so we can't actually parse them until we've constructed the descriptors, but to
     * construct the descriptors we have to have parsed the descriptor protos. So, we have to parse
     * the descriptor protos a second time after constructing the descriptors.
     */
    private void setProto(final FileDescriptorProto proto) {
      this.proto = proto;

      for (int i = 0; i < messageTypes.length; i++) {
        messageTypes[i].setProto(proto.getMessageType(i));
      }

      for (int i = 0; i < enumTypes.length; i++) {
        enumTypes[i].setProto(proto.getEnumType(i));
      }

      for (int i = 0; i < services.length; i++) {
        services[i].setProto(proto.getService(i));
      }

      for (int i = 0; i < extensions.length; i++) {
        extensions[i].setProto(proto.getExtension(i));
      }
    }
  }

  // =================================================================

  /** Describes a message type. */
  public static final class Descriptor extends GenericDescriptor {
    /**
     * Get the index of this descriptor within its parent. In other words, given a {@link
     * FileDescriptor} {@code file}, the following is true:
     *
     * <pre>
     *   for all i in [0, file.getMessageTypeCount()):
     *     file.getMessageType(i).getIndex() == i
     * </pre>
     *
     * Similarly, for a {@link Descriptor} {@code messageType}:
     *
     * <pre>
     *   for all i in [0, messageType.getNestedTypeCount()):
     *     messageType.getNestedType(i).getIndex() == i
     * </pre>
     */
    public int getIndex() {
      return index;
    }

    /** Convert the descriptor to its protocol message representation. */
    public DescriptorProto toProto() {
      return proto;
    }

    /** Get the type's unqualified name. */
    public String getName() {
      return proto.getName();
    }

    /**
     * Get the type's fully-qualified name, within the proto language's namespace. This differs from
     * the Java name. For example, given this {@code .proto}:
     *
     * <pre>
     *   package foo.bar;
     *   option java_package = "com.example.protos"
     *   message Baz {}
     * </pre>
     *
     * {@code Baz}'s full name is "foo.bar.Baz".
     */
    public String getFullName() {
      return fullName;
    }

    /** Get the {@link FileDescriptor} containing this descriptor. */
    public FileDescriptor getFile() {
      return file;
    }

    /** If this is a nested type, get the outer descriptor, otherwise null. */
    public Descriptor getContainingType() {
      return containingType;
    }

    /** Get the {@code MessageOptions}, defined in {@code descriptor.proto}. */
    public MessageOptions getOptions() {
      return proto.getOptions();
    }

    /** Get a list of this message type's fields. */
    public List<FieldDescriptor> getFields() {
      return Collections.unmodifiableList(Arrays.asList(fields));
    }

    /** Get a list of this message type's oneofs. */
    public List<OneofDescriptor> getOneofs() {
      return Collections.unmodifiableList(Arrays.asList(oneofs));
    }

    /** Get a list of this message type's real oneofs. */
    public List<OneofDescriptor> getRealOneofs() {
      return Collections.unmodifiableList(Arrays.asList(oneofs).subList(0, realOneofCount));
    }

    /** Get a list of this message type's extensions. */
    public List<FieldDescriptor> getExtensions() {
      return Collections.unmodifiableList(Arrays.asList(extensions));
    }

    /** Get a list of message types nested within this one. */
    public List<Descriptor> getNestedTypes() {
      return Collections.unmodifiableList(Arrays.asList(nestedTypes));
    }

    /** Get a list of enum types nested within this one. */
    public List<EnumDescriptor> getEnumTypes() {
      return Collections.unmodifiableList(Arrays.asList(enumTypes));
    }

    /** Determines if the given field number is an extension. */
    public boolean isExtensionNumber(final int number) {
      int index = Arrays.binarySearch(extensionRangeLowerBounds, number);
      if (index < 0) {
        index = ~index - 1;
      }
      // extensionRangeLowerBounds[index] is the biggest value <= number
      return index >= 0 && number < extensionRangeUpperBounds[index];
    }

    /** Determines if the given field number is reserved. */
    public boolean isReservedNumber(final int number) {
      for (final DescriptorProto.ReservedRange range : proto.getReservedRangeList()) {
        if (range.getStart() <= number && number < range.getEnd()) {
          return true;
        }
      }
      return false;
    }

    /** Determines if the given field name is reserved. */
    public boolean isReservedName(final String name) {
      checkNotNull(name);
      for (final String reservedName : proto.getReservedNameList()) {
        if (reservedName.equals(name)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Indicates whether the message can be extended. That is, whether it has any "extensions x to
     * y" ranges declared on it.
     */
    public boolean isExtendable() {
      return !proto.getExtensionRangeList().isEmpty();
    }

    /**
     * Finds a field by name.
     *
     * @param name The unqualified name of the field (e.g. "foo"). For protocol buffer messages that
     *     follow <a
     *     href=https://developers.google.com/protocol-buffers/docs/style#message_and_field_names>Google's
     *     guidance on naming</a> this will be a snake case string, such as
     *     <pre>song_name</pre>
     *     .
     * @return The field's descriptor, or {@code null} if not found.
     */
    public FieldDescriptor findFieldByName(final String name) {
      final GenericDescriptor result = file.pool.findSymbol(fullName + '.' + name);
      if (result instanceof FieldDescriptor) {
        return (FieldDescriptor) result;
      } else {
        return null;
      }
    }

    /**
     * Finds a field by field number.
     *
     * @param number The field number within this message type.
     * @return The field's descriptor, or {@code null} if not found.
     */
    public FieldDescriptor findFieldByNumber(final int number) {
      return binarySearch(
          fieldsSortedByNumber, fieldsSortedByNumber.length, FieldDescriptor.NUMBER_GETTER, number);
    }

    /**
     * Finds a nested message type by name.
     *
     * @param name The unqualified name of the nested type such as "Foo"
     * @return The types's descriptor, or {@code null} if not found.
     */
    public Descriptor findNestedTypeByName(final String name) {
      final GenericDescriptor result = file.pool.findSymbol(fullName + '.' + name);
      if (result instanceof Descriptor) {
        return (Descriptor) result;
      } else {
        return null;
      }
    }

    /**
     * Finds a nested enum type by name.
     *
     * @param name The unqualified name of the nested type such as "Foo"
     * @return The types's descriptor, or {@code null} if not found.
     */
    public EnumDescriptor findEnumTypeByName(final String name) {
      final GenericDescriptor result = file.pool.findSymbol(fullName + '.' + name);
      if (result instanceof EnumDescriptor) {
        return (EnumDescriptor) result;
      } else {
        return null;
      }
    }

    private final int index;
    private DescriptorProto proto;
    private final String fullName;
    private final FileDescriptor file;
    private final Descriptor containingType;
    private final Descriptor[] nestedTypes;
    private final EnumDescriptor[] enumTypes;
    private final FieldDescriptor[] fields;
    private final FieldDescriptor[] fieldsSortedByNumber;
    private final FieldDescriptor[] extensions;
    private final OneofDescriptor[] oneofs;
    private final int realOneofCount;

    private final int[] extensionRangeLowerBounds;
    private final int[] extensionRangeUpperBounds;

    // Used to create a placeholder when the type cannot be found.
    Descriptor(final String fullname) throws DescriptorValidationException {
      String name = fullname;
      String packageName = "";
      int pos = fullname.lastIndexOf('.');
      if (pos != -1) {
        name = fullname.substring(pos + 1);
        packageName = fullname.substring(0, pos);
      }
      this.index = 0;
      this.proto =
          DescriptorProto.newBuilder()
              .setName(name)
              .addExtensionRange(
                  DescriptorProto.ExtensionRange.newBuilder().setStart(1).setEnd(536870912).build())
              .build();
      this.fullName = fullname;
      this.containingType = null;

      this.nestedTypes = EMPTY_DESCRIPTORS;
      this.enumTypes = EMPTY_ENUM_DESCRIPTORS;
      this.fields = EMPTY_FIELD_DESCRIPTORS;
      this.fieldsSortedByNumber = EMPTY_FIELD_DESCRIPTORS;
      this.extensions = EMPTY_FIELD_DESCRIPTORS;
      this.oneofs = EMPTY_ONEOF_DESCRIPTORS;
      this.realOneofCount = 0;

      // Create a placeholder FileDescriptor to hold this message.
      this.file = new FileDescriptor(packageName, this);

      extensionRangeLowerBounds = new int[] {1};
      extensionRangeUpperBounds = new int[] {536870912};
    }

    private Descriptor(
        final DescriptorProto proto,
        final FileDescriptor file,
        final Descriptor parent,
        final int index)
        throws DescriptorValidationException {
      this.index = index;
      this.proto = proto;
      fullName = computeFullName(file, parent, proto.getName());
      this.file = file;
      containingType = parent;

      oneofs =
          (proto.getOneofDeclCount() > 0)
              ? new OneofDescriptor[proto.getOneofDeclCount()]
              : EMPTY_ONEOF_DESCRIPTORS;
      for (int i = 0; i < proto.getOneofDeclCount(); i++) {
        oneofs[i] = new OneofDescriptor(proto.getOneofDecl(i), file, this, i);
      }

      nestedTypes =
          (proto.getNestedTypeCount() > 0)
              ? new Descriptor[proto.getNestedTypeCount()]
              : EMPTY_DESCRIPTORS;
      for (int i = 0; i < proto.getNestedTypeCount(); i++) {
        nestedTypes[i] = new Descriptor(proto.getNestedType(i), file, this, i);
      }

      enumTypes =
          (proto.getEnumTypeCount() > 0)
              ? new EnumDescriptor[proto.getEnumTypeCount()]
              : EMPTY_ENUM_DESCRIPTORS;
      for (int i = 0; i < proto.getEnumTypeCount(); i++) {
        enumTypes[i] = new EnumDescriptor(proto.getEnumType(i), file, this, i);
      }

      fields =
          (proto.getFieldCount() > 0)
              ? new FieldDescriptor[proto.getFieldCount()]
              : EMPTY_FIELD_DESCRIPTORS;
      for (int i = 0; i < proto.getFieldCount(); i++) {
        fields[i] = new FieldDescriptor(proto.getField(i), file, this, i, false);
      }
      this.fieldsSortedByNumber =
          (proto.getFieldCount() > 0) ? com.google.protobuf.gwt.StaticImpls.clone(fields) : EMPTY_FIELD_DESCRIPTORS;

      extensions =
          (proto.getExtensionCount() > 0)
              ? new FieldDescriptor[proto.getExtensionCount()]
              : EMPTY_FIELD_DESCRIPTORS;
      for (int i = 0; i < proto.getExtensionCount(); i++) {
        extensions[i] = new FieldDescriptor(proto.getExtension(i), file, this, i, true);
      }

      for (int i = 0; i < proto.getOneofDeclCount(); i++) {
        oneofs[i].fields = new FieldDescriptor[oneofs[i].getFieldCount()];
        oneofs[i].fieldCount = 0;
      }
      for (int i = 0; i < proto.getFieldCount(); i++) {
        OneofDescriptor oneofDescriptor = fields[i].getContainingOneof();
        if (oneofDescriptor != null) {
          oneofDescriptor.fields[oneofDescriptor.fieldCount++] = fields[i];
        }
      }

      int syntheticOneofCount = 0;
      for (OneofDescriptor oneof : this.oneofs) {
        if (oneof.isSynthetic()) {
          syntheticOneofCount++;
        } else {
          if (syntheticOneofCount > 0) {
            throw new DescriptorValidationException(this, "Synthetic oneofs must come last.");
          }
        }
      }
      this.realOneofCount = this.oneofs.length - syntheticOneofCount;

      file.pool.addSymbol(this);

      // NOTE: The defined extension ranges are guaranteed to be disjoint.
      if (proto.getExtensionRangeCount() > 0) {
        extensionRangeLowerBounds = new int[proto.getExtensionRangeCount()];
        extensionRangeUpperBounds = new int[proto.getExtensionRangeCount()];
        int i = 0;
        for (final DescriptorProto.ExtensionRange range : proto.getExtensionRangeList()) {
          extensionRangeLowerBounds[i] = range.getStart();
          extensionRangeUpperBounds[i] = range.getEnd();
          i++;
        }
        // Since the ranges are disjoint, sorting these independently must still produce the correct
        // order.
        Arrays.sort(extensionRangeLowerBounds);
        Arrays.sort(extensionRangeUpperBounds);
      } else {
        extensionRangeLowerBounds = EMPTY_INT_ARRAY;
        extensionRangeUpperBounds = EMPTY_INT_ARRAY;
      }
    }

    /** Look up and cross-link all field types, etc. */
    private void crossLink() throws DescriptorValidationException {
      for (final Descriptor nestedType : nestedTypes) {
        nestedType.crossLink();
      }

      for (final FieldDescriptor field : fields) {
        field.crossLink();
      }
      Arrays.sort(fieldsSortedByNumber);
      validateNoDuplicateFieldNumbers();

      for (final FieldDescriptor extension : extensions) {
        extension.crossLink();
      }
    }

    private void validateNoDuplicateFieldNumbers() throws DescriptorValidationException {
      for (int i = 0; i + 1 < fieldsSortedByNumber.length; i++) {
        FieldDescriptor old = fieldsSortedByNumber[i];
        FieldDescriptor field = fieldsSortedByNumber[i + 1];
        if (old.getNumber() == field.getNumber()) {
          throw new DescriptorValidationException(
              field,
              "Field number "
                  + field.getNumber()
                  + " has already been used in \""
                  + field.getContainingType().getFullName()
                  + "\" by field \""
                  + old.getName()
                  + "\".");
        }
      }
    }

    /** See {@link FileDescriptor#setProto}. */
    private void setProto(final DescriptorProto proto) {
      this.proto = proto;

      for (int i = 0; i < nestedTypes.length; i++) {
        nestedTypes[i].setProto(proto.getNestedType(i));
      }

      for (int i = 0; i < oneofs.length; i++) {
        oneofs[i].setProto(proto.getOneofDecl(i));
      }

      for (int i = 0; i < enumTypes.length; i++) {
        enumTypes[i].setProto(proto.getEnumType(i));
      }

      for (int i = 0; i < fields.length; i++) {
        fields[i].setProto(proto.getField(i));
      }

      for (int i = 0; i < extensions.length; i++) {
        extensions[i].setProto(proto.getExtension(i));
      }
    }
  }

  // =================================================================

  /** Describes a field of a message type. */
  public static final class FieldDescriptor extends GenericDescriptor
      implements Comparable<FieldDescriptor>, FieldSet.FieldDescriptorLite<FieldDescriptor> {
    private static final NumberGetter<FieldDescriptor> NUMBER_GETTER =
        new NumberGetter<FieldDescriptor>() {
          public int getNumber(FieldDescriptor fieldDescriptor) {
            return fieldDescriptor.getNumber();
          }
        };

    /**
     * Get the index of this descriptor within its parent.
     *
     * @see Descriptors.Descriptor#getIndex()
     */
    public int getIndex() {
      return index;
    }

    /** Convert the descriptor to its protocol message representation. */
    public FieldDescriptorProto toProto() {
      return proto;
    }

    /** Get the field's unqualified name. */
    public String getName() {
      return proto.getName();
    }

    /** Get the field's number. */
    public int getNumber() {
      return proto.getNumber();
    }

    /**
     * Get the field's fully-qualified name.
     *
     * @see Descriptors.Descriptor#getFullName()
     */
    public String getFullName() {
      return fullName;
    }

    /** Get the JSON name of this field. */
    public String getJsonName() {
      String result = jsonName;
      if (result != null) {
        return result;
      } else if (proto.hasJsonName()) {
        return jsonName = proto.getJsonName();
      } else {
        return jsonName = fieldNameToJsonName(proto.getName());
      }
    }

    /**
     * Get the field's java type. This is just for convenience. Every {@code
     * FieldDescriptorProto.Type} maps to exactly one Java type.
     */
    public JavaType getJavaType() {
      return type.getJavaType();
    }

    /** For internal use only. */
    public WireFormat.JavaType getLiteJavaType() {
      return getLiteType().getJavaType();
    }

    /** Get the {@code FileDescriptor} containing this descriptor. */
    public FileDescriptor getFile() {
      return file;
    }

    /** Get the field's declared type. */
    public Type getType() {
      return type;
    }

    /** For internal use only. */
    public WireFormat.FieldType getLiteType() {
      return table[type.ordinal()];
    }

    /** For internal use only. */
    public boolean needsUtf8Check() {
      if (type != Type.STRING) {
        return false;
      }
      if (getContainingType().getOptions().getMapEntry()) {
        // Always enforce strict UTF-8 checking for map fields.
        return true;
      }
      if (getFile().getSyntax() == Syntax.PROTO3) {
        return true;
      }
      return getFile().getOptions().getJavaStringCheckUtf8();
    }

    public boolean isMapField() {
      return getType() == Type.MESSAGE
          && isRepeated()
          && getMessageType().getOptions().getMapEntry();
    }

    // I'm pretty sure values() constructs a new array every time, since there
    // is nothing stopping the caller from mutating the array.  Therefore we
    // make a static copy here.
    private static final WireFormat.FieldType[] table = WireFormat.FieldType.values();

    /** Is this field declared required? */
    public boolean isRequired() {
      return proto.getLabel() == FieldDescriptorProto.Label.LABEL_REQUIRED;
    }

    /** Is this field declared optional? */
    public boolean isOptional() {
      return proto.getLabel() == FieldDescriptorProto.Label.LABEL_OPTIONAL;
    }

    /** Is this field declared repeated? */
    public boolean isRepeated() {
      return proto.getLabel() == FieldDescriptorProto.Label.LABEL_REPEATED;
    }

    /**
     * Does this field have the {@code [packed = true]} option or is this field packable in proto3
     * and not explicitly set to unpacked?
     */
    public boolean isPacked() {
      if (!isPackable()) {
        return false;
      }
      if (getFile().getSyntax() == FileDescriptor.Syntax.PROTO2) {
        return getOptions().getPacked();
      } else {
        return !getOptions().hasPacked() || getOptions().getPacked();
      }
    }

    /** Can this field be packed? That is, is it a repeated primitive field? */
    public boolean isPackable() {
      return isRepeated() && getLiteType().isPackable();
    }

    /** Returns true if the field had an explicitly-defined default value. */
    public boolean hasDefaultValue() {
      return proto.hasDefaultValue();
    }

    /**
     * Returns the field's default value. Valid for all types except for messages and groups. For
     * all other types, the object returned is of the same class that would returned by
     * Message.getField(this).
     */
    public Object getDefaultValue() {
      if (getJavaType() == JavaType.MESSAGE) {
        throw new UnsupportedOperationException(
            "FieldDescriptor.getDefaultValue() called on an embedded message field.");
      }
      return defaultValue;
    }

    /** Get the {@code FieldOptions}, defined in {@code descriptor.proto}. */
    public FieldOptions getOptions() {
      return proto.getOptions();
    }

    /** Is this field an extension? */
    public boolean isExtension() {
      return proto.hasExtendee();
    }

    /**
     * Get the field's containing type. For extensions, this is the type being extended, not the
     * location where the extension was defined. See {@link #getExtensionScope()}.
     */
    public Descriptor getContainingType() {
      return containingType;
    }

    /** Get the field's containing oneof. */
    public OneofDescriptor getContainingOneof() {
      return containingOneof;
    }

    /** Get the field's containing oneof, only if non-synthetic. */
    public OneofDescriptor getRealContainingOneof() {
      return containingOneof != null && !containingOneof.isSynthetic() ? containingOneof : null;
    }

    /**
     * Returns true if this field was syntactically written with "optional" in the .proto file.
     * Excludes singular proto3 fields that do not have a label.
     */
    @Deprecated
    public
    boolean hasOptionalKeyword() {
      return isProto3Optional
          || (file.getSyntax() == Syntax.PROTO2 && isOptional() && getContainingOneof() == null);
    }

    /**
     * Returns true if this field tracks presence, ie. does the field distinguish between "unset"
     * and "present with default value."
     *
     * <p>This includes required, optional, and oneof fields. It excludes maps, repeated fields, and
     * singular proto3 fields without "optional".
     *
     * <p>For fields where hasPresence() == true, the return value of msg.hasField() is semantically
     * meaningful.
     */
    public boolean hasPresence() {
      if (isRepeated()) {
        return false;
      }
      return getType() == Type.MESSAGE
          || getType() == Type.GROUP
          || getContainingOneof() != null
          || file.getSyntax() == Syntax.PROTO2;
    }

    /**
     * For extensions defined nested within message types, gets the outer type. Not valid for
     * non-extension fields. For example, consider this {@code .proto} file:
     *
     * <pre>
     *   message Foo {
     *     extensions 1000 to max;
     *   }
     *   extend Foo {
     *     optional int32 baz = 1234;
     *   }
     *   message Bar {
     *     extend Foo {
     *       optional int32 moo = 4321;
     *     }
     *   }
     * </pre>
     *
     * Both {@code baz}'s and {@code moo}'s containing type is {@code Foo}. However, {@code baz}'s
     * extension scope is {@code null} while {@code moo}'s extension scope is {@code Bar}.
     */
    public Descriptor getExtensionScope() {
      if (!isExtension()) {
        throw new UnsupportedOperationException(
            "This field is not an extension. (" + fullName + ")");
      }
      return extensionScope;
    }

    /** For embedded message and group fields, gets the field's type. */
    public Descriptor getMessageType() {
      if (getJavaType() != JavaType.MESSAGE) {
        throw new UnsupportedOperationException(
            "This field is not of message type. (" + fullName + ")");
      }
      return messageType;
    }

    /** For enum fields, gets the field's type. */
    public EnumDescriptor getEnumType() {
      if (getJavaType() != JavaType.ENUM) {
        throw new UnsupportedOperationException(
            "This field is not of enum type. (" + fullName + ")");
      }
      return enumType;
    }

    /**
     * Determines if the given enum field is treated as closed based on legacy non-conformant
     * behavior.
     *
     * <p>Conformant behavior determines closedness based on the enum and can be queried using
     * {@code EnumDescriptor.isClosed()}.
     *
     * <p>Some runtimes currently have a quirk where non-closed enums are treated as closed when
     * used as the type of fields defined in a `syntax = proto2;` file. This quirk is not present in
     * all runtimes; as of writing, we know that:
     *
     * <ul>
     *   <li>C++, Java, and C++-based Python share this quirk.
     *   <li>UPB and UPB-based Python do not.
     *   <li>PHP and Ruby treat all enums as open regardless of declaration.
     * </ul>
     *
     * <p>Care should be taken when using this function to respect the target runtime's enum
     * handling quirks.
     */
    public boolean legacyEnumFieldTreatedAsClosed() {
      return getType() == Type.ENUM && getFile().getSyntax() == Syntax.PROTO2;
    }

    /**
     * Compare with another {@code FieldDescriptor}. This orders fields in "canonical" order, which
     * simply means ascending order by field number. {@code other} must be a field of the same type.
     * That is, {@code getContainingType()} must return the same {@code Descriptor} for both fields.
     *
     * @return negative, zero, or positive if {@code this} is less than, equal to, or greater than
     *     {@code other}, respectively
     */
    public int compareTo(final FieldDescriptor other) {
      if (other.containingType != containingType) {
        throw new IllegalArgumentException(
            "FieldDescriptors can only be compared to other FieldDescriptors "
                + "for fields of the same message type.");
      }
      return getNumber() - other.getNumber();
    }

    public String toString() {
      return getFullName();
    }

    private final int index;

    private FieldDescriptorProto proto;
    private final String fullName;
    private String jsonName;
    private final FileDescriptor file;
    private final Descriptor extensionScope;
    private final boolean isProto3Optional;

    // Possibly initialized during cross-linking.
    private Type type;
    private Descriptor containingType;
    private Descriptor messageType;
    private OneofDescriptor containingOneof;
    private EnumDescriptor enumType;
    private Object defaultValue;

    public enum Type {
      DOUBLE(JavaType.DOUBLE),
      FLOAT(JavaType.FLOAT),
      INT64(JavaType.LONG),
      UINT64(JavaType.LONG),
      INT32(JavaType.INT),
      FIXED64(JavaType.LONG),
      FIXED32(JavaType.INT),
      BOOL(JavaType.BOOLEAN),
      STRING(JavaType.STRING),
      GROUP(JavaType.MESSAGE),
      MESSAGE(JavaType.MESSAGE),
      BYTES(JavaType.BYTE_STRING),
      UINT32(JavaType.INT),
      ENUM(JavaType.ENUM),
      SFIXED32(JavaType.INT),
      SFIXED64(JavaType.LONG),
      SINT32(JavaType.INT),
      SINT64(JavaType.LONG);

      // Private copy to avoid repeated allocations from calls to values() in valueOf().
      private static final Type[] types = values();

      Type(JavaType javaType) {
        this.javaType = javaType;
      }

      private final JavaType javaType;

      public FieldDescriptorProto.Type toProto() {
        return FieldDescriptorProto.Type.forNumber(ordinal() + 1);
      }

      public JavaType getJavaType() {
        return javaType;
      }

      public static Type valueOf(final FieldDescriptorProto.Type type) {
        return types[type.getNumber() - 1];
      }
    }

    static {
      // Refuse to init if someone added a new declared type.
      if (Type.types.length != FieldDescriptorProto.Type.values().length) {
        throw new RuntimeException(
            "descriptor.proto has a new declared type but Descriptors.java wasn't updated.");
      }
    }

    public enum JavaType {
      INT(0),
      LONG(0L),
      FLOAT(0F),
      DOUBLE(0D),
      BOOLEAN(false),
      STRING(""),
      BYTE_STRING(ByteString.EMPTY),
      ENUM(null),
      MESSAGE(null);

      JavaType(final Object defaultDefault) {
        this.defaultDefault = defaultDefault;
      }

      /**
       * The default default value for fields of this type, if it's a primitive type. This is meant
       * for use inside this file only, hence is private.
       */
      private final Object defaultDefault;
    }

    // This method should match exactly with the ToJsonName() function in C++
    // descriptor.cc.
    private static String fieldNameToJsonName(String name) {
      final int length = name.length();
      StringBuilder result = new StringBuilder(length);
      boolean isNextUpperCase = false;
      for (int i = 0; i < length; i++) {
        char ch = name.charAt(i);
        if (ch == '_') {
          isNextUpperCase = true;
        } else if (isNextUpperCase) {
          // This closely matches the logic for ASCII characters in:
          // http://google3/google/protobuf/descriptor.cc?l=249-251&rcl=228891689
          if ('a' <= ch && ch <= 'z') {
            ch = (char) (ch - 'a' + 'A');
          }
          result.append(ch);
          isNextUpperCase = false;
        } else {
          result.append(ch);
        }
      }
      return result.toString();
    }

    private FieldDescriptor(
        final FieldDescriptorProto proto,
        final FileDescriptor file,
        final Descriptor parent,
        final int index,
        final boolean isExtension)
        throws DescriptorValidationException {
      this.index = index;
      this.proto = proto;
      fullName = computeFullName(file, parent, proto.getName());
      this.file = file;

      if (proto.hasType()) {
        type = Type.valueOf(proto.getType());
      }

      isProto3Optional = proto.getProto3Optional();

      if (getNumber() <= 0) {
        throw new DescriptorValidationException(this, "Field numbers must be positive integers.");
      }

      if (isExtension) {
        if (!proto.hasExtendee()) {
          throw new DescriptorValidationException(
              this, "FieldDescriptorProto.extendee not set for extension field.");
        }
        containingType = null; // Will be filled in when cross-linking
        if (parent != null) {
          extensionScope = parent;
        } else {
          extensionScope = null;
        }

        if (proto.hasOneofIndex()) {
          throw new DescriptorValidationException(
              this, "FieldDescriptorProto.oneof_index set for extension field.");
        }
        containingOneof = null;
      } else {
        if (proto.hasExtendee()) {
          throw new DescriptorValidationException(
              this, "FieldDescriptorProto.extendee set for non-extension field.");
        }
        containingType = parent;

        if (proto.hasOneofIndex()) {
          if (proto.getOneofIndex() < 0
              || proto.getOneofIndex() >= parent.toProto().getOneofDeclCount()) {
            throw new DescriptorValidationException(
                this,
                "FieldDescriptorProto.oneof_index is out of range for type " + parent.getName());
          }
          containingOneof = parent.getOneofs().get(proto.getOneofIndex());
          containingOneof.fieldCount++;
        } else {
          containingOneof = null;
        }
        extensionScope = null;
      }

      file.pool.addSymbol(this);
    }

    /** Look up and cross-link all field types, etc. */
    private void crossLink() throws DescriptorValidationException {
      if (proto.hasExtendee()) {
        final GenericDescriptor extendee =
            file.pool.lookupSymbol(
                proto.getExtendee(), this, DescriptorPool.SearchFilter.TYPES_ONLY);
        if (!(extendee instanceof Descriptor)) {
          throw new DescriptorValidationException(
              this, '\"' + proto.getExtendee() + "\" is not a message type.");
        }
        containingType = (Descriptor) extendee;

        if (!getContainingType().isExtensionNumber(getNumber())) {
          throw new DescriptorValidationException(
              this,
              '\"'
                  + getContainingType().getFullName()
                  + "\" does not declare "
                  + getNumber()
                  + " as an extension number.");
        }
      }

      if (proto.hasTypeName()) {
        final GenericDescriptor typeDescriptor =
            file.pool.lookupSymbol(
                proto.getTypeName(), this, DescriptorPool.SearchFilter.TYPES_ONLY);

        if (!proto.hasType()) {
          // Choose field type based on symbol.
          if (typeDescriptor instanceof Descriptor) {
            type = Type.MESSAGE;
          } else if (typeDescriptor instanceof EnumDescriptor) {
            type = Type.ENUM;
          } else {
            throw new DescriptorValidationException(
                this, '\"' + proto.getTypeName() + "\" is not a type.");
          }
        }

        if (getJavaType() == JavaType.MESSAGE) {
          if (!(typeDescriptor instanceof Descriptor)) {
            throw new DescriptorValidationException(
                this, '\"' + proto.getTypeName() + "\" is not a message type.");
          }
          messageType = (Descriptor) typeDescriptor;

          if (proto.hasDefaultValue()) {
            throw new DescriptorValidationException(this, "Messages can't have default values.");
          }
        } else if (getJavaType() == JavaType.ENUM) {
          if (!(typeDescriptor instanceof EnumDescriptor)) {
            throw new DescriptorValidationException(
                this, '\"' + proto.getTypeName() + "\" is not an enum type.");
          }
          enumType = (EnumDescriptor) typeDescriptor;
        } else {
          throw new DescriptorValidationException(this, "Field with primitive type has type_name.");
        }
      } else {
        if (getJavaType() == JavaType.MESSAGE || getJavaType() == JavaType.ENUM) {
          throw new DescriptorValidationException(
              this, "Field with message or enum type missing type_name.");
        }
      }

      // Only repeated primitive fields may be packed.
      if (proto.getOptions().getPacked() && !isPackable()) {
        throw new DescriptorValidationException(
            this, "[packed = true] can only be specified for repeated primitive fields.");
      }

      // We don't attempt to parse the default value until here because for
      // enums we need the enum type's descriptor.
      if (proto.hasDefaultValue()) {
        if (isRepeated()) {
          throw new DescriptorValidationException(
              this, "Repeated fields cannot have default values.");
        }

        try {
          switch (getType()) {
            case INT32:
            case SINT32:
            case SFIXED32:
              defaultValue = TextFormat.parseInt32(proto.getDefaultValue());
              break;
            case UINT32:
            case FIXED32:
              defaultValue = TextFormat.parseUInt32(proto.getDefaultValue());
              break;
            case INT64:
            case SINT64:
            case SFIXED64:
              defaultValue = TextFormat.parseInt64(proto.getDefaultValue());
              break;
            case UINT64:
            case FIXED64:
              defaultValue = TextFormat.parseUInt64(proto.getDefaultValue());
              break;
            case FLOAT:
              if (proto.getDefaultValue().equals("inf")) {
                defaultValue = Float.POSITIVE_INFINITY;
              } else if (proto.getDefaultValue().equals("-inf")) {
                defaultValue = Float.NEGATIVE_INFINITY;
              } else if (proto.getDefaultValue().equals("nan")) {
                defaultValue = Float.NaN;
              } else {
                defaultValue = Float.valueOf(proto.getDefaultValue());
              }
              break;
            case DOUBLE:
              if (proto.getDefaultValue().equals("inf")) {
                defaultValue = Double.POSITIVE_INFINITY;
              } else if (proto.getDefaultValue().equals("-inf")) {
                defaultValue = Double.NEGATIVE_INFINITY;
              } else if (proto.getDefaultValue().equals("nan")) {
                defaultValue = Double.NaN;
              } else {
                defaultValue = Double.valueOf(proto.getDefaultValue());
              }
              break;
            case BOOL:
              defaultValue = Boolean.valueOf(proto.getDefaultValue());
              break;
            case STRING:
              defaultValue = proto.getDefaultValue();
              break;
            case BYTES:
              try {
                defaultValue = TextFormat.unescapeBytes(proto.getDefaultValue());
              } catch (TextFormat.InvalidEscapeSequenceException e) {
                throw new DescriptorValidationException(
                    this, "Couldn't parse default value: " + e.getMessage(), e);
              }
              break;
            case ENUM:
              defaultValue = enumType.findValueByName(proto.getDefaultValue());
              if (defaultValue == null) {
                throw new DescriptorValidationException(
                    this, "Unknown enum default value: \"" + proto.getDefaultValue() + '\"');
              }
              break;
            case MESSAGE:
            case GROUP:
              throw new DescriptorValidationException(this, "Message type had default value.");
          }
        } catch (NumberFormatException e) {
          throw new DescriptorValidationException(
              this, "Could not parse default value: \"" + proto.getDefaultValue() + '\"', e);
        }
      } else {
        // Determine the default default for this field.
        if (isRepeated()) {
          defaultValue = Collections.emptyList();
        } else {
          switch (getJavaType()) {
            case ENUM:
              // We guarantee elsewhere that an enum type always has at least
              // one possible value.
              defaultValue = enumType.getValues().get(0);
              break;
            case MESSAGE:
              defaultValue = null;
              break;
            default:
              defaultValue = getJavaType().defaultDefault;
              break;
          }
        }
      }

      if (containingType != null && containingType.getOptions().getMessageSetWireFormat()) {
        if (isExtension()) {
          if (!isOptional() || getType() != Type.MESSAGE) {
            throw new DescriptorValidationException(
                this, "Extensions of MessageSets must be optional messages.");
          }
        } else {
          throw new DescriptorValidationException(
              this, "MessageSets cannot have fields, only extensions.");
        }
      }
    }

    /** See {@link FileDescriptor#setProto}. */
    private void setProto(final FieldDescriptorProto proto) {
      this.proto = proto;
    }

    /** For internal use only. This is to satisfy the FieldDescriptorLite interface. */
    public MessageLite.Builder internalMergeFrom(MessageLite.Builder to, MessageLite from) {
      // FieldDescriptors are only used with non-lite messages so we can just
      // down-cast and call mergeFrom directly.
      return ((Message.Builder) to).mergeFrom((Message) from);
    }
  }

  // =================================================================

  /** Describes an enum type. */
  public static final class EnumDescriptor extends GenericDescriptor
      implements Internal.EnumLiteMap<EnumValueDescriptor> {
    /**
     * Get the index of this descriptor within its parent.
     *
     * @see Descriptors.Descriptor#getIndex()
     */
    public int getIndex() {
      return index;
    }

    /** Convert the descriptor to its protocol message representation. */
    public EnumDescriptorProto toProto() {
      return proto;
    }

    /** Get the type's unqualified name. */
    public String getName() {
      return proto.getName();
    }

    /**
     * Get the type's fully-qualified name.
     *
     * @see Descriptors.Descriptor#getFullName()
     */
    public String getFullName() {
      return fullName;
    }

    /** Get the {@link FileDescriptor} containing this descriptor. */
    public FileDescriptor getFile() {
      return file;
    }

    /**
     * Determines if the given enum is closed.
     *
     * <p>Closed enum means that it:
     *
     * <ul>
     *   <li>Has a fixed set of values, rather than being equivalent to an int32.
     *   <li>Encountering values not in this set causes them to be treated as unknown fields.
     *   <li>The first value (i.e., the default) may be nonzero.
     * </ul>
     *
     * <p>WARNING: Some runtimes currently have a quirk where non-closed enums are treated as closed
     * when used as the type of fields defined in a `syntax = proto2;` file. This quirk is not
     * present in all runtimes; as of writing, we know that:
     *
     * <ul>
     *   <li> C++, Java, and C++-based Python share this quirk.
     *   <li> UPB and UPB-based Python do not.
     *   <li> PHP and Ruby treat all enums as open regardless of declaration.
     * </ul>
     *
     * <p>Care should be taken when using this function to respect the target runtime's enum
     * handling quirks.
     */
    public boolean isClosed() {
      return getFile().getSyntax() != Syntax.PROTO3;
    }

    /** If this is a nested type, get the outer descriptor, otherwise null. */
    public Descriptor getContainingType() {
      return containingType;
    }

    /** Get the {@code EnumOptions}, defined in {@code descriptor.proto}. */
    public EnumOptions getOptions() {
      return proto.getOptions();
    }

    /** Get a list of defined values for this enum. */
    public List<EnumValueDescriptor> getValues() {
      return Collections.unmodifiableList(Arrays.asList(values));
    }

    /** Determines if the given field number is reserved. */
    public boolean isReservedNumber(final int number) {
      for (final EnumDescriptorProto.EnumReservedRange range : proto.getReservedRangeList()) {
        if (range.getStart() <= number && number <= range.getEnd()) {
          return true;
        }
      }
      return false;
    }

    /** Determines if the given field name is reserved. */
    public boolean isReservedName(final String name) {
      checkNotNull(name);
      for (final String reservedName : proto.getReservedNameList()) {
        if (reservedName.equals(name)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Find an enum value by name.
     *
     * @param name the unqualified name of the value such as "FOO"
     * @return the value's descriptor, or {@code null} if not found
     */
    public EnumValueDescriptor findValueByName(final String name) {
      final GenericDescriptor result = file.pool.findSymbol(fullName + '.' + name);
      if (result instanceof EnumValueDescriptor) {
        return (EnumValueDescriptor) result;
      } else {
        return null;
      }
    }

    /**
     * Find an enum value by number. If multiple enum values have the same number, this returns the
     * first defined value with that number.
     *
     * @param number The value's number.
     * @return the value's descriptor, or {@code null} if not found.
     */
    public EnumValueDescriptor findValueByNumber(final int number) {
      return binarySearch(
          valuesSortedByNumber, distinctNumbers, EnumValueDescriptor.NUMBER_GETTER, number);
    }

    private static class UnknownEnumValueReference extends WeakReference<EnumValueDescriptor> {
      private final int number;

      private UnknownEnumValueReference(int number, EnumValueDescriptor descriptor) {
        super(descriptor);
        this.number = number;
      }
    }

    /**
     * Get the enum value for a number. If no enum value has this number, construct an
     * EnumValueDescriptor for it.
     */
    public EnumValueDescriptor findValueByNumberCreatingIfUnknown(final int number) {
      EnumValueDescriptor result = findValueByNumber(number);
      if (result != null) {
        return result;
      }
      // The number represents an unknown enum value.
      synchronized (this) {
        if (cleanupQueue == null) {
          cleanupQueue = new ReferenceQueue<>();
          unknownValues = new HashMap<>();
        } else {
          while (true) {
            UnknownEnumValueReference toClean = (UnknownEnumValueReference) cleanupQueue.poll();
            if (toClean == null) {
              break;
            }
            unknownValues.remove(toClean.number);
          }
        }

        // There are two ways we can be missing a value: it wasn't in the map, or the reference
        // has been GC'd.  (It may even have been GC'd since we cleaned up the references a few
        // lines of code ago.)  So get out the reference, if it's still present...
        WeakReference<EnumValueDescriptor> reference = unknownValues.get(number);
        result = (reference == null) ? null : reference.get();

        if (result == null) {
          result = new EnumValueDescriptor(this, number);
          unknownValues.put(number, new UnknownEnumValueReference(number, result));
        }
      }
      return result;
    }

    // Used in tests only.
    int getUnknownEnumValueDescriptorCount() {
      return unknownValues.size();
    }

    private final int index;
    private EnumDescriptorProto proto;
    private final String fullName;
    private final FileDescriptor file;
    private final Descriptor containingType;
    private final EnumValueDescriptor[] values;
    private final EnumValueDescriptor[] valuesSortedByNumber;
    private final int distinctNumbers;
    private Map<Integer, WeakReference<EnumValueDescriptor>> unknownValues = null;
    private ReferenceQueue<EnumValueDescriptor> cleanupQueue = null;

    private EnumDescriptor(
        final EnumDescriptorProto proto,
        final FileDescriptor file,
        final Descriptor parent,
        final int index)
        throws DescriptorValidationException {
      this.index = index;
      this.proto = proto;
      fullName = computeFullName(file, parent, proto.getName());
      this.file = file;
      containingType = parent;

      if (proto.getValueCount() == 0) {
        // We cannot allow enums with no values because this would mean there
        // would be no valid default value for fields of this type.
        throw new DescriptorValidationException(this, "Enums must contain at least one value.");
      }

      values = new EnumValueDescriptor[proto.getValueCount()];
      for (int i = 0; i < proto.getValueCount(); i++) {
        values[i] = new EnumValueDescriptor(proto.getValue(i), file, this, i);
      }
      valuesSortedByNumber = com.google.protobuf.gwt.StaticImpls.clone(values);
      Arrays.sort(valuesSortedByNumber, EnumValueDescriptor.BY_NUMBER);
      // deduplicate
      int j = 0;
      for (int i = 1; i < proto.getValueCount(); i++) {
        EnumValueDescriptor oldValue = valuesSortedByNumber[j];
        EnumValueDescriptor newValue = valuesSortedByNumber[i];
        if (oldValue.getNumber() != newValue.getNumber()) {
          valuesSortedByNumber[++j] = newValue;
        }
      }
      this.distinctNumbers = j + 1;
      Arrays.fill(valuesSortedByNumber, distinctNumbers, proto.getValueCount(), null);

      file.pool.addSymbol(this);
    }

    /** See {@link FileDescriptor#setProto}. */
    private void setProto(final EnumDescriptorProto proto) {
      this.proto = proto;

      for (int i = 0; i < values.length; i++) {
        values[i].setProto(proto.getValue(i));
      }
    }
  }

  // =================================================================

  /**
   * Describes one value within an enum type. Note that multiple defined values may have the same
   * number. In generated Java code, all values with the same number after the first become aliases
   * of the first. However, they still have independent EnumValueDescriptors.
   */
  public static final class EnumValueDescriptor extends GenericDescriptor
      implements Internal.EnumLite {
    static final Comparator<EnumValueDescriptor> BY_NUMBER =
        new Comparator<EnumValueDescriptor>() {
          public int compare(EnumValueDescriptor o1, EnumValueDescriptor o2) {
            return Integer.valueOf(o1.getNumber()).compareTo(o2.getNumber());
          }
        };

    static final NumberGetter<EnumValueDescriptor> NUMBER_GETTER =
        new NumberGetter<EnumValueDescriptor>() {
          public int getNumber(EnumValueDescriptor enumValueDescriptor) {
            return enumValueDescriptor.getNumber();
          }
        };

    /**
     * Get the index of this descriptor within its parent.
     *
     * @see Descriptors.Descriptor#getIndex()
     */
    public int getIndex() {
      return index;
    }

    /** Convert the descriptor to its protocol message representation. */
    public EnumValueDescriptorProto toProto() {
      return proto;
    }

    /** Get the value's unqualified name. */
    public String getName() {
      return proto.getName();
    }

    /** Get the value's number. */
    public int getNumber() {
      return proto.getNumber();
    }

    public String toString() {
      return proto.getName();
    }

    /**
     * Get the value's fully-qualified name.
     *
     * @see Descriptors.Descriptor#getFullName()
     */
    public String getFullName() {
      return fullName;
    }

    /** Get the {@link FileDescriptor} containing this descriptor. */
    public FileDescriptor getFile() {
      return type.file;
    }

    /** Get the value's enum type. */
    public EnumDescriptor getType() {
      return type;
    }

    /** Get the {@code EnumValueOptions}, defined in {@code descriptor.proto}. */
    public EnumValueOptions getOptions() {
      return proto.getOptions();
    }

    private final int index;
    private EnumValueDescriptorProto proto;
    private final String fullName;
    private final EnumDescriptor type;

    private EnumValueDescriptor(
        final EnumValueDescriptorProto proto,
        final FileDescriptor file,
        final EnumDescriptor parent,
        final int index)
        throws DescriptorValidationException {
      this.index = index;
      this.proto = proto;
      type = parent;

      fullName = parent.getFullName() + '.' + proto.getName();

      file.pool.addSymbol(this);
    }

    // Create an unknown enum value.
    private EnumValueDescriptor(final EnumDescriptor parent, final Integer number) {
      String name = "UNKNOWN_ENUM_VALUE_" + parent.getName() + "_" + number;
      EnumValueDescriptorProto proto =
          EnumValueDescriptorProto.newBuilder().setName(name).setNumber(number).build();
      this.index = -1;
      this.proto = proto;
      this.type = parent;
      this.fullName = parent.getFullName() + '.' + proto.getName();

      // Don't add this descriptor into pool.
    }

    /** See {@link FileDescriptor#setProto}. */
    private void setProto(final EnumValueDescriptorProto proto) {
      this.proto = proto;
    }
  }

  // =================================================================

  /** Describes a service type. */
  public static final class ServiceDescriptor extends GenericDescriptor {
    /**
     * Get the index of this descriptor within its parent. * @see Descriptors.Descriptor#getIndex()
     */
    public int getIndex() {
      return index;
    }

    /** Convert the descriptor to its protocol message representation. */
    public ServiceDescriptorProto toProto() {
      return proto;
    }

    /** Get the type's unqualified name. */
    public String getName() {
      return proto.getName();
    }

    /**
     * Get the type's fully-qualified name.
     *
     * @see Descriptors.Descriptor#getFullName()
     */
    public String getFullName() {
      return fullName;
    }

    /** Get the {@link FileDescriptor} containing this descriptor. */
    public FileDescriptor getFile() {
      return file;
    }

    /** Get the {@code ServiceOptions}, defined in {@code descriptor.proto}. */
    public ServiceOptions getOptions() {
      return proto.getOptions();
    }

    /** Get a list of methods for this service. */
    public List<MethodDescriptor> getMethods() {
      return Collections.unmodifiableList(Arrays.asList(methods));
    }

    /**
     * Find a method by name.
     *
     * @param name the unqualified name of the method such as "Foo"
     * @return the method's descriptor, or {@code null} if not found
     */
    public MethodDescriptor findMethodByName(final String name) {
      final GenericDescriptor result = file.pool.findSymbol(fullName + '.' + name);
      if (result instanceof MethodDescriptor) {
        return (MethodDescriptor) result;
      } else {
        return null;
      }
    }

    private final int index;
    private ServiceDescriptorProto proto;
    private final String fullName;
    private final FileDescriptor file;
    private MethodDescriptor[] methods;

    private ServiceDescriptor(
        final ServiceDescriptorProto proto, final FileDescriptor file, final int index)
        throws DescriptorValidationException {
      this.index = index;
      this.proto = proto;
      fullName = computeFullName(file, null, proto.getName());
      this.file = file;

      methods = new MethodDescriptor[proto.getMethodCount()];
      for (int i = 0; i < proto.getMethodCount(); i++) {
        methods[i] = new MethodDescriptor(proto.getMethod(i), file, this, i);
      }

      file.pool.addSymbol(this);
    }

    private void crossLink() throws DescriptorValidationException {
      for (final MethodDescriptor method : methods) {
        method.crossLink();
      }
    }

    /** See {@link FileDescriptor#setProto}. */
    private void setProto(final ServiceDescriptorProto proto) {
      this.proto = proto;

      for (int i = 0; i < methods.length; i++) {
        methods[i].setProto(proto.getMethod(i));
      }
    }
  }

  // =================================================================

  /** Describes one method within a service type. */
  public static final class MethodDescriptor extends GenericDescriptor {
    /**
     * Get the index of this descriptor within its parent. * @see Descriptors.Descriptor#getIndex()
     */
    public int getIndex() {
      return index;
    }

    /** Convert the descriptor to its protocol message representation. */
    public MethodDescriptorProto toProto() {
      return proto;
    }

    /** Get the method's unqualified name. */
    public String getName() {
      return proto.getName();
    }

    /**
     * Get the method's fully-qualified name.
     *
     * @see Descriptors.Descriptor#getFullName()
     */
    public String getFullName() {
      return fullName;
    }

    /** Get the {@link FileDescriptor} containing this descriptor. */
    public FileDescriptor getFile() {
      return file;
    }

    /** Get the method's service type. */
    public ServiceDescriptor getService() {
      return service;
    }

    /** Get the method's input type. */
    public Descriptor getInputType() {
      return inputType;
    }

    /** Get the method's output type. */
    public Descriptor getOutputType() {
      return outputType;
    }

    /** Get whether or not the inputs are streaming. */
    public boolean isClientStreaming() {
      return proto.getClientStreaming();
    }

    /** Get whether or not the outputs are streaming. */
    public boolean isServerStreaming() {
      return proto.getServerStreaming();
    }

    /** Get the {@code MethodOptions}, defined in {@code descriptor.proto}. */
    public MethodOptions getOptions() {
      return proto.getOptions();
    }

    private final int index;
    private MethodDescriptorProto proto;
    private final String fullName;
    private final FileDescriptor file;
    private final ServiceDescriptor service;

    // Initialized during cross-linking.
    private Descriptor inputType;
    private Descriptor outputType;

    private MethodDescriptor(
        final MethodDescriptorProto proto,
        final FileDescriptor file,
        final ServiceDescriptor parent,
        final int index)
        throws DescriptorValidationException {
      this.index = index;
      this.proto = proto;
      this.file = file;
      service = parent;

      fullName = parent.getFullName() + '.' + proto.getName();

      file.pool.addSymbol(this);
    }

    private void crossLink() throws DescriptorValidationException {
      final GenericDescriptor input =
          getFile()
              .pool
              .lookupSymbol(proto.getInputType(), this, DescriptorPool.SearchFilter.TYPES_ONLY);
      if (!(input instanceof Descriptor)) {
        throw new DescriptorValidationException(
            this, '\"' + proto.getInputType() + "\" is not a message type.");
      }
      inputType = (Descriptor) input;

      final GenericDescriptor output =
          getFile()
              .pool
              .lookupSymbol(proto.getOutputType(), this, DescriptorPool.SearchFilter.TYPES_ONLY);
      if (!(output instanceof Descriptor)) {
        throw new DescriptorValidationException(
            this, '\"' + proto.getOutputType() + "\" is not a message type.");
      }
      outputType = (Descriptor) output;
    }

    /** See {@link FileDescriptor#setProto}. */
    private void setProto(final MethodDescriptorProto proto) {
      this.proto = proto;
    }
  }

  // =================================================================

  private static String computeFullName(
      final FileDescriptor file, final Descriptor parent, final String name) {
    if (parent != null) {
      return parent.getFullName() + '.' + name;
    }

    final String packageName = file.getPackage();
    if (!packageName.isEmpty()) {
      return packageName + '.' + name;
    }

    return name;
  }

  // =================================================================

  /**
   * All descriptors implement this to make it easier to implement tools like {@code
   * DescriptorPool}.
   */
  public abstract static class GenericDescriptor {

    // Private constructor to prevent subclasses outside of com.google.protobuf.Descriptors
    private GenericDescriptor() {}

    public abstract Message toProto();

    public abstract String getName();

    public abstract String getFullName();

    public abstract FileDescriptor getFile();
  }

  /** Thrown when building descriptors fails because the source DescriptorProtos are not valid. */
  public static class DescriptorValidationException extends Exception {
    private static final long serialVersionUID = 5750205775490483148L;

    /** Gets the full name of the descriptor where the error occurred. */
    public String getProblemSymbolName() {
      return name;
    }

    /** Gets the protocol message representation of the invalid descriptor. */
    public Message getProblemProto() {
      return proto;
    }

    /** Gets a human-readable description of the error. */
    public String getDescription() {
      return description;
    }

    private final String name;
    private final Message proto;
    private final String description;

    private DescriptorValidationException(
        final GenericDescriptor problemDescriptor, final String description) {
      super(problemDescriptor.getFullName() + ": " + description);

      // Note that problemDescriptor may be partially uninitialized, so we
      // don't want to expose it directly to the user.  So, we only provide
      // the name and the original proto.
      name = problemDescriptor.getFullName();
      proto = problemDescriptor.toProto();
      this.description = description;
    }

    private DescriptorValidationException(
        final GenericDescriptor problemDescriptor,
        final String description,
        final Throwable cause) {
      this(problemDescriptor, description);
      initCause(cause);
    }

    private DescriptorValidationException(
        final FileDescriptor problemDescriptor, final String description) {
      super(problemDescriptor.getName() + ": " + description);

      // Note that problemDescriptor may be partially uninitialized, so we
      // don't want to expose it directly to the user.  So, we only provide
      // the name and the original proto.
      name = problemDescriptor.getName();
      proto = problemDescriptor.toProto();
      this.description = description;
    }
  }

  // =================================================================

  /**
   * A private helper class which contains lookup tables containing all the descriptors defined in a
   * particular file.
   */
  private static final class DescriptorPool {

    /** Defines what subclass of descriptors to search in the descriptor pool. */
    enum SearchFilter {
      TYPES_ONLY,
      AGGREGATES_ONLY,
      ALL_SYMBOLS
    }

    DescriptorPool(final FileDescriptor[] dependencies, boolean allowUnknownDependencies) {
      this.dependencies =
          Collections.newSetFromMap(
              new IdentityHashMap<FileDescriptor, Boolean>(dependencies.length));
      this.allowUnknownDependencies = allowUnknownDependencies;

      for (Descriptors.FileDescriptor dependency : dependencies) {
        this.dependencies.add(dependency);
        importPublicDependencies(dependency);
      }

      for (final FileDescriptor dependency : this.dependencies) {
        try {
          addPackage(dependency.getPackage(), dependency);
        } catch (DescriptorValidationException e) {
          // Can't happen, because addPackage() only fails when the name
          // conflicts with a non-package, but we have not yet added any
          // non-packages at this point.
          throw new AssertionError(e);
        }
      }
    }

    /** Find and put public dependencies of the file into dependencies set. */
    private void importPublicDependencies(final FileDescriptor file) {
      for (FileDescriptor dependency : file.getPublicDependencies()) {
        if (dependencies.add(dependency)) {
          importPublicDependencies(dependency);
        }
      }
    }

    private final Set<FileDescriptor> dependencies;
    private final boolean allowUnknownDependencies;

    private final Map<String, GenericDescriptor> descriptorsByName = new HashMap<>();

    /** Find a generic descriptor by fully-qualified name. */
    GenericDescriptor findSymbol(final String fullName) {
      return findSymbol(fullName, SearchFilter.ALL_SYMBOLS);
    }

    /**
     * Find a descriptor by fully-qualified name and given option to only search valid field type
     * descriptors.
     */
    GenericDescriptor findSymbol(final String fullName, final SearchFilter filter) {
      GenericDescriptor result = descriptorsByName.get(fullName);
      if (result != null) {
        if ((filter == SearchFilter.ALL_SYMBOLS)
            || ((filter == SearchFilter.TYPES_ONLY) && isType(result))
            || ((filter == SearchFilter.AGGREGATES_ONLY) && isAggregate(result))) {
          return result;
        }
      }

      for (final FileDescriptor dependency : dependencies) {
        result = dependency.pool.descriptorsByName.get(fullName);
        if (result != null) {
          if ((filter == SearchFilter.ALL_SYMBOLS)
              || ((filter == SearchFilter.TYPES_ONLY) && isType(result))
              || ((filter == SearchFilter.AGGREGATES_ONLY) && isAggregate(result))) {
            return result;
          }
        }
      }

      return null;
    }

    /** Checks if the descriptor is a valid type for a message field. */
    boolean isType(GenericDescriptor descriptor) {
      return (descriptor instanceof Descriptor) || (descriptor instanceof EnumDescriptor);
    }

    /** Checks if the descriptor is a valid namespace type. */
    boolean isAggregate(GenericDescriptor descriptor) {
      return (descriptor instanceof Descriptor)
          || (descriptor instanceof EnumDescriptor)
          || (descriptor instanceof PackageDescriptor)
          || (descriptor instanceof ServiceDescriptor);
    }

    /**
     * Look up a type descriptor by name, relative to some other descriptor. The name may be
     * fully-qualified (with a leading '.'), partially-qualified, or unqualified. C++-like name
     * lookup semantics are used to search for the matching descriptor.
     */
    GenericDescriptor lookupSymbol(
        final String name,
        final GenericDescriptor relativeTo,
        final DescriptorPool.SearchFilter filter)
        throws DescriptorValidationException {

      GenericDescriptor result;
      String fullname;
      if (name.startsWith(".")) {
        // Fully-qualified name.
        fullname = name.substring(1);
        result = findSymbol(fullname, filter);
      } else {
        // If "name" is a compound identifier, we want to search for the
        // first component of it, then search within it for the rest.
        // If name is something like "Foo.Bar.baz", and symbols named "Foo" are
        // defined in multiple parent scopes, we only want to find "Bar.baz" in
        // the innermost one.  E.g., the following should produce an error:
        //   message Bar { message Baz {} }
        //   message Foo {
        //     message Bar {
        //     }
        //     optional Bar.Baz baz = 1;
        //   }
        // So, we look for just "Foo" first, then look for "Bar.baz" within it
        // if found.
        final int firstPartLength = name.indexOf('.');
        final String firstPart;
        if (firstPartLength == -1) {
          firstPart = name;
        } else {
          firstPart = name.substring(0, firstPartLength);
        }

        // We will search each parent scope of "relativeTo" looking for the
        // symbol.
        final StringBuilder scopeToTry = new StringBuilder(relativeTo.getFullName());

        while (true) {
          // Chop off the last component of the scope.
          final int dotpos = scopeToTry.lastIndexOf(".");
          if (dotpos == -1) {
            fullname = name;
            result = findSymbol(name, filter);
            break;
          } else {
            scopeToTry.setLength(dotpos + 1);

            // Append firstPart and try to find
            scopeToTry.append(firstPart);
            result = findSymbol(scopeToTry.toString(), DescriptorPool.SearchFilter.AGGREGATES_ONLY);

            if (result != null) {
              if (firstPartLength != -1) {
                // We only found the first part of the symbol.  Now look for
                // the whole thing.  If this fails, we *don't* want to keep
                // searching parent scopes.
                scopeToTry.setLength(dotpos + 1);
                scopeToTry.append(name);
                result = findSymbol(scopeToTry.toString(), filter);
              }
              fullname = scopeToTry.toString();
              break;
            }

            // Not found.  Remove the name so we can try again.
            scopeToTry.setLength(dotpos);
          }
        }
      }

      if (result == null) {
        if (allowUnknownDependencies && filter == SearchFilter.TYPES_ONLY) {
          logger.warning(
              "The descriptor for message type \""
                  + name
                  + "\" cannot be found and a placeholder is created for it");
          // We create a dummy message descriptor here regardless of the
          // expected type. If the type should be message, this dummy
          // descriptor will work well and if the type should be enum, a
          // DescriptorValidationException will be thrown later. In either
          // case, the code works as expected: we allow unknown message types
          // but not unknown enum types.
          result = new Descriptor(fullname);
          // Add the placeholder file as a dependency so we can find the
          // placeholder symbol when resolving other references.
          this.dependencies.add(result.getFile());
          return result;
        } else {
          throw new DescriptorValidationException(relativeTo, '\"' + name + "\" is not defined.");
        }
      } else {
        return result;
      }
    }

    /**
     * Adds a symbol to the symbol table. If a symbol with the same name already exists, throws an
     * error.
     */
    void addSymbol(final GenericDescriptor descriptor) throws DescriptorValidationException {
      validateSymbolName(descriptor);

      final String fullName = descriptor.getFullName();

      final GenericDescriptor old = descriptorsByName.put(fullName, descriptor);
      if (old != null) {
        descriptorsByName.put(fullName, old);

        if (descriptor.getFile() == old.getFile()) {
          final int dotpos = fullName.lastIndexOf('.');
          if (dotpos == -1) {
            throw new DescriptorValidationException(
                descriptor, '\"' + fullName + "\" is already defined.");
          } else {
            throw new DescriptorValidationException(
                descriptor,
                '\"'
                    + fullName.substring(dotpos + 1)
                    + "\" is already defined in \""
                    + fullName.substring(0, dotpos)
                    + "\".");
          }
        } else {
          throw new DescriptorValidationException(
              descriptor,
              '\"'
                  + fullName
                  + "\" is already defined in file \""
                  + old.getFile().getName()
                  + "\".");
        }
      }
    }

    /**
     * Represents a package in the symbol table. We use PackageDescriptors just as placeholders so
     * that someone cannot define, say, a message type that has the same name as an existing
     * package.
     */
    private static final class PackageDescriptor extends GenericDescriptor {
      public Message toProto() {
        return file.toProto();
      }

      public String getName() {
        return name;
      }

      public String getFullName() {
        return fullName;
      }

      public FileDescriptor getFile() {
        return file;
      }

      PackageDescriptor(final String name, final String fullName, final FileDescriptor file) {
        this.file = file;
        this.fullName = fullName;
        this.name = name;
      }

      private final String name;
      private final String fullName;
      private final FileDescriptor file;
    }

    /**
     * Adds a package to the symbol tables. If a package by the same name already exists, that is
     * fine, but if some other kind of symbol exists under the same name, an exception is thrown. If
     * the package has multiple components, this also adds the parent package(s).
     */
    void addPackage(final String fullName, final FileDescriptor file)
        throws DescriptorValidationException {
      final int dotpos = fullName.lastIndexOf('.');
      final String name;
      if (dotpos == -1) {
        name = fullName;
      } else {
        addPackage(fullName.substring(0, dotpos), file);
        name = fullName.substring(dotpos + 1);
      }

      final GenericDescriptor old =
          descriptorsByName.put(fullName, new PackageDescriptor(name, fullName, file));
      if (old != null) {
        descriptorsByName.put(fullName, old);
        if (!(old instanceof PackageDescriptor)) {
          throw new DescriptorValidationException(
              file,
              '\"'
                  + name
                  + "\" is already defined (as something other than a "
                  + "package) in file \""
                  + old.getFile().getName()
                  + "\".");
        }
      }
    }

    /**
     * Verifies that the descriptor's name is valid. That is, it contains only letters, digits, and
     * underscores, and does not start with a digit.
     */
    static void validateSymbolName(final GenericDescriptor descriptor)
        throws DescriptorValidationException {
      final String name = descriptor.getName();
      if (name.length() == 0) {
        throw new DescriptorValidationException(descriptor, "Missing name.");
      }

      // Non-ASCII characters are not valid in protobuf identifiers, even
      // if they are letters or digits.
      // The first character must be a letter or '_'.
      // Subsequent characters may be letters, numbers, or digits.
      for (int i = 0; i < name.length(); i++) {
        final char c = name.charAt(i);
        if (('a' <= c && c <= 'z')
            || ('A' <= c && c <= 'Z')
            || (c == '_')
            || ('0' <= c && c <= '9' && i > 0)) {
          // Valid
          continue;
        }
        throw new DescriptorValidationException(
            descriptor, '\"' + name + "\" is not a valid identifier.");
      }
    }
  }

  /** Describes a oneof of a message type. */
  public static final class OneofDescriptor extends GenericDescriptor {
    /** Get the index of this descriptor within its parent. */
    public int getIndex() {
      return index;
    }

    public String getName() {
      return proto.getName();
    }

    public FileDescriptor getFile() {
      return file;
    }

    public String getFullName() {
      return fullName;
    }

    public Descriptor getContainingType() {
      return containingType;
    }

    public int getFieldCount() {
      return fieldCount;
    }

    public OneofOptions getOptions() {
      return proto.getOptions();
    }

    /** Get a list of this message type's fields. */
    public List<FieldDescriptor> getFields() {
      return Collections.unmodifiableList(Arrays.asList(fields));
    }

    public FieldDescriptor getField(int index) {
      return fields[index];
    }

    public OneofDescriptorProto toProto() {
      return proto;
    }

    @Deprecated
    public
    boolean isSynthetic() {
      return fields.length == 1 && fields[0].isProto3Optional;
    }

    private void setProto(final OneofDescriptorProto proto) {
      this.proto = proto;
    }

    private OneofDescriptor(
        final OneofDescriptorProto proto,
        final FileDescriptor file,
        final Descriptor parent,
        final int index) {
      this.proto = proto;
      fullName = computeFullName(file, parent, proto.getName());
      this.file = file;
      this.index = index;

      containingType = parent;
      fieldCount = 0;
    }

    private final int index;
    private OneofDescriptorProto proto;
    private final String fullName;
    private final FileDescriptor file;

    private Descriptor containingType;
    private int fieldCount;
    private FieldDescriptor[] fields;
  }

  private static <T> T binarySearch(T[] array, int size, NumberGetter<T> getter, int number) {
    int left = 0;
    int right = size - 1;

    while (left <= right) {
      int mid = (left + right) / 2;
      T midValue = array[mid];
      int midValueNumber = getter.getNumber(midValue);
      if (number < midValueNumber) {
        right = mid - 1;
      } else if (number > midValueNumber) {
        left = mid + 1;
      } else {
        return midValue;
      }
    }
    return null;
  }

  private interface NumberGetter<T> {
    int getNumber(T t);
  }
}
