// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Equivalent to {@link ExtensionRegistry} but supports only "lite" types.
 *
 * <p>If all of your types are lite types, then you only need to use {@code ExtensionRegistryLite}.
 * Similarly, if all your types are regular types, then you only need {@link ExtensionRegistry}.
 * Typically it does not make sense to mix the two, since if you have any regular types in your
 * program, you then require the full runtime and lose all the benefits of the lite runtime, so you
 * might as well make all your types be regular types. However, in some cases (e.g. when depending
 * on multiple third-party libraries where one uses lite types and one uses regular), you may find
 * yourself wanting to mix the two. In this case things get more complicated.
 *
 * <p>There are three factors to consider: Whether the type being extended is lite, whether the
 * embedded type (in the case of a message-typed extension) is lite, and whether the extension
 * itself is lite. Since all three are declared in different files, they could all be different.
 * Here are all the combinations and which type of registry to use:
 *
 * <pre>
 *   Extended type     Inner type    Extension         Use registry
 *   =======================================================================
 *   lite              lite          lite              ExtensionRegistryLite
 *   lite              regular       lite              ExtensionRegistry
 *   regular           regular       regular           ExtensionRegistry
 *   all other combinations                            not supported
 * </pre>
 *
 * <p>Note that just as regular types are not allowed to contain lite-type fields, they are also not
 * allowed to contain lite-type extensions. This is because regular types must be fully accessible
 * via reflection, which in turn means that all the inner messages must also support reflection. On
 * the other hand, since regular types implement the entire lite interface, there is no problem with
 * embedding regular types inside lite types.
 *
 * @author kenton@google.com Kenton Varda
 */
public class ExtensionRegistryLite {

  // Set true to enable lazy parsing feature for MessageSet.
  //
  // TODO: Now we use a global flag to control whether enable lazy
  // parsing feature for MessageSet, which may be too crude for some
  // applications. Need to support this feature on smaller granularity.
  private static volatile boolean eagerlyParseMessageSets = false;

  // short circuit the ExtensionRegistryFactory via assumevalues trickery
  @SuppressWarnings("JavaOptionalSuggestions")
  private static boolean doFullRuntimeInheritanceCheck = true;

  // Visible for testing.
  static final String EXTENSION_CLASS_NAME = "com.google.protobuf.Extension";

  public static boolean isEagerlyParseMessageSets() {
    return eagerlyParseMessageSets;
  }

  public static void setEagerlyParseMessageSets(boolean isEagerlyParse) {
    eagerlyParseMessageSets = isEagerlyParse;
  }

  /**
   * Construct a new, empty instance.
   *
   * <p>This may be an {@code ExtensionRegistry} if the full (non-Lite) proto libraries are
   * available.
   */
  public static ExtensionRegistryLite newInstance() {
    return doFullRuntimeInheritanceCheck
        ? ExtensionRegistryFactory.create()
        : new ExtensionRegistryLite();
  }

  private static volatile ExtensionRegistryLite emptyRegistry;

  /**
   * Get the unmodifiable singleton empty instance of either ExtensionRegistryLite or {@code
   * ExtensionRegistry} (if the full (non-Lite) proto libraries are available).
   */
  public static ExtensionRegistryLite getEmptyRegistry() {
    if (!doFullRuntimeInheritanceCheck) {
      return EMPTY_REGISTRY_LITE;
    }
    ExtensionRegistryLite result = emptyRegistry;
    if (result == null) {
      synchronized (ExtensionRegistryLite.class) {
        result = emptyRegistry;
        if (result == null) {
          result = emptyRegistry = ExtensionRegistryFactory.createEmpty();
        }
      }
    }
    return result;
  }

  /** Returns an unmodifiable view of the registry. */
  public ExtensionRegistryLite getUnmodifiable() {
    return new ExtensionRegistryLite(this);
  }

  /**
   * Find an extension by containing type and field number.
   *
   * @return Information about the extension if found, or {@code null} otherwise.
   */
  @SuppressWarnings("unchecked")
  public <ContainingType extends MessageLite>
      GeneratedMessageLite.GeneratedExtension<ContainingType, ?> findLiteExtensionByNumber(
          final ContainingType containingTypeDefaultInstance, final int fieldNumber) {
    return (GeneratedMessageLite.GeneratedExtension<ContainingType, ?>)
        extensionsByNumber.get(new ObjectIntPair(containingTypeDefaultInstance, fieldNumber));
  }

  // =================================================================
  // Private stuff.

  // Constructors are package-private so that ExtensionRegistry can subclass
  // this.

  ExtensionRegistryLite() {
    this.extensionsByNumber =
        new HashMap<ObjectIntPair, GeneratedMessageLite.GeneratedExtension<?, ?>>();
  }

  static final ExtensionRegistryLite EMPTY_REGISTRY_LITE = new ExtensionRegistryLite(true);

  ExtensionRegistryLite(ExtensionRegistryLite other) {
    if (other == EMPTY_REGISTRY_LITE) {
      this.extensionsByNumber = Collections.emptyMap();
    } else {
      this.extensionsByNumber = Collections.unmodifiableMap(other.extensionsByNumber);
    }
  }

  private final Map<ObjectIntPair, GeneratedMessageLite.GeneratedExtension<?, ?>>
      extensionsByNumber;

  ExtensionRegistryLite(boolean empty) {
    this.extensionsByNumber = Collections.emptyMap();
  }

  /** A (Object, int) pair, used as a map key. */
  private static final class ObjectIntPair {
    private final Object object;
    private final int number;

    ObjectIntPair(final Object object, final int number) {
      this.object = object;
      this.number = number;
    }

    public int hashCode() {
      return System.identityHashCode(object) * ((1 << 16) - 1) + number;
    }

    public boolean equals(final Object obj) {
      if (!(obj instanceof ObjectIntPair)) {
        return false;
      }
      final ObjectIntPair other = (ObjectIntPair) obj;
      return object == other.object && number == other.number;
    }
  }
}
