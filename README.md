# protobuf-gwt

Tools to unpack and rewrite the protobuf-java library into something that is roughly GWT/J2CL compatible.

Makes use of standard OpenRewrite rules, some rules specifically written to simplify projects for GWT, and
some custom rules just for protobuf-gwt.

There appears to be a bug in JDT that we don't have a rewrite rule for yet, so there is a workaround in a
manual patch made to this project.

## Usage

Add `com.vertispan.protobuf:protobuf-gwt` to your project dependencies. The version will be based on the
protobuf-java build being used, with an integer suffix to allow for packaging changes. For example, current
released versions:

| protobuf-java | protobuf-gwt |
| -------------- | ------------- |
| 3.25.4 | 3.25.4-1 |

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
