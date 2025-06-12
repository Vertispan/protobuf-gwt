// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import static com.google.protobuf.ExtensionRegistryLite.EMPTY_REGISTRY_LITE;

/**
 * A factory object to create instances of {@link ExtensionRegistryLite}.
 *
 * <p>This factory detects (via reflection) if the full (non-Lite) protocol buffer libraries are
 * available, and if so, the instances returned are actually {@link ExtensionRegistry}.
 */
final class ExtensionRegistryFactory {

  static final String FULL_REGISTRY_CLASS_NAME = "com.google.protobuf.ExtensionRegistry";

  /* Visible for Testing
  @Nullable */
  static final Class<?> EXTENSION_REGISTRY_CLASS = null;

  /** Construct a new, empty instance. */
  public static ExtensionRegistryLite create() {
    ExtensionRegistryLite result = null;

    return result != null ? result : new ExtensionRegistryLite();
  }

  /** Get the unmodifiable singleton empty instance. */
  public static ExtensionRegistryLite createEmpty() {
    ExtensionRegistryLite result = null;

    return result != null ? result : EMPTY_REGISTRY_LITE;
  }
}
