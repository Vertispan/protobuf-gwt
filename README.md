# protobuf-gwt

A version of the protobuf-java that is modified to be GWT/J2CL compatible. Presently only supports binary
encoding, with a focus on CodedInputStream and CodedOutputStream, and a handful of tools that `protoc`
output requires. Not all generated protoc output will be supported automatically.

Makes use of standard OpenRewrite rules, some rules specifically written to
[simplify projects for GWT](https://github.com/vertispan/gwt-compatible-recipes/), and some custom rules just for
protobuf-gwt.

There appears to be a bug in JDT that we don't have a rewrite rule for yet, so there is a workaround in a
manual patch made to this project.

Presently only binary format is supported.

Contrast with https://github.com/google/j2cl-protobuf, which is archived and unmaintained, was J2CL only, and
only supported the text format.

## Usage

Add `com.vertispan.protobuf:protobuf-gwt` to your project dependencies. The version will be based on the
protobuf-java build being used, with an integer suffix to allow for packaging changes. For example, current
released versions:

| protobuf-java | protobuf-gwt | Description                                                                  |
| -------------- |--------------|------------------------------------------------------------------------------|
| 3.25.4 | 3.25.4-1     | Initial release, with an internal copy of StaticImpls rather than shared     |
| 3.25.4 | 3.25.4-2     | Corrected License to correctly follow protobuf-java's instead of relicensing |
| 3.25.4 | 3.25.4-3     | Restored WrappersProto types, for downstream projects that use them          |
| 3.25.8 | 3.25.8-1     | Updated to protobuf-java 3.25.8                                             |

## Building

The modified protobuf-java sources are checked in to the project both to allow us to more easily see differences
over the stock classes, and to allow for manual modifications as needed.

A fresh build will try to download the original sources, so will be missing the manual modifications - revert
changes or apply them manually as needed.

CI builds set `-Dmdep.skip=true` instead of otherwise keeping the old target/ dir or reverting changes.

## Manual changes

```patch
diff --git a/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageLite.java b/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageLite.java
index df2842a..a7a4fcb 100644
--- a/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageLite.java
+++ b/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageLite.java
@@ -964,7 +964,7 @@ public abstract class GeneratedMessageLite<
 
     protected void copyOnWriteInternal() {
       super.copyOnWriteInternal();
-      if (instance.extensions != FieldSet.emptySet()) {
+      if (instance.extensions != FieldSet.<ExtensionDescriptor>emptySet()) {
         instance.extensions = instance.extensions.clone();
       }
     }
diff --git a/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageV3.java b/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageV3.java
index 019a1ac..b23f833 100644
--- a/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageV3.java
+++ b/protobuf-gwt/src/main/java/com/google/protobuf/GeneratedMessageV3.java
@@ -1328,7 +1328,7 @@ public abstract class GeneratedMessageV3 extends AbstractMessage implements Seri
      */
     private FieldSet<FieldDescriptor> buildExtensions() {
       return extensions == null
-          ? (FieldSet<FieldDescriptor>) FieldSet.emptySet()
+          ? (FieldSet<FieldDescriptor>) FieldSet.<FieldDescriptor>emptySet()
           : extensions.buildPartial();
     }
 
```

# License

The protobuf-gwt project is BSD-3 licensed, in keeping with the protobuf-java license. See the [protobuf-java/LICENSE](LICENSE)
file for details.

The rest of the project is Apache 2.0 licensed at this time, though not yet distributed through Maven Central.
