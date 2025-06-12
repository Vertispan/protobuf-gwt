//package com.example.gwt.protobuf;
//
//import org.openrewrite.ExecutionContext;
//import org.openrewrite.NlsRewrite;
//import org.openrewrite.Recipe;
//import org.openrewrite.TreeVisitor;
//import org.openrewrite.java.JavaParser;
//import org.openrewrite.java.JavaTemplate;
//import org.openrewrite.java.JavaVisitor;
//import org.openrewrite.java.tree.J;
//import org.openrewrite.java.tree.JavaType;
//
///**
// * Fixes two usages of FieldSet.emptySet() by adding explicit type parameters.
// */
//public class JdtTypeInferenceFix extends Recipe {
//    private static final JavaTemplate GENERATED_MESSAGE_V3_TEMPLATE = JavaTemplate.builder("(com.google.protobuf.FieldSet<com.google.protobuf.Descriptors.FieldDescriptor>) com.google.protobuf.FieldSet.emptySet()")
//            .imports("com.google.protobuf.FieldSet", "com.google.protobuf.Descriptors.FieldDescriptor")
//            .javaParser(JavaParser.fromJavaVersion())
//            .build();
//    private static final JavaTemplate GENERATED_MESSAGE_V3_TEMPLATE_WITH_TYPE = JavaTemplate.builder("FieldSet.<#{any()}>emptySet()")
//            .imports("com.google.protobuf.FieldSet")
//            .javaParser(JavaParser.fromJavaVersion())
//            .build();
//    @NlsRewrite.DisplayName
//    @Override
//    public String getDisplayName() {
//        return "";
//    }
//
//    @NlsRewrite.Description
//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    @Override
//    public TreeVisitor<?, ExecutionContext> getVisitor() {
//        return new JavaVisitor<>() {
//            private JavaType typeToParameterize;
//
//            @Override
//            public J visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
//                if (classDecl.getType() != null && classDecl.getType().getFullyQualifiedName().startsWith("com.google.protobuf.GeneratedMessageV3")) {
//                    // Only examine classes that we need to fix
//                    return super.visitClassDeclaration(classDecl, executionContext);
//                }
//                return classDecl;
//            }
//
//            @Override
//            public J visitTypeCast(J.TypeCast typeCast, ExecutionContext executionContext) {
//                if (typeCast.getType() instanceof JavaType.Parameterized) {
//                    JavaType.Parameterized parameterizedType = (JavaType.Parameterized) typeCast.getType();
//                    if (parameterizedType.toString().equals("FieldSet<FieldDescriptor>")) {
//                        // Continue, this is probably our bug
//                        typeToParameterize = parameterizedType;
//                        return super.visitTypeCast(typeCast, executionContext);
//                    }
//                }
//                return typeCast;
//            }
//
//            @Override
//            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
//                if (method.toString().equals("FieldSet.emptySet()")) {
//                    if (typeToParameterize != null) {
//                        // We have a type to parameterize, so use the template with explicit type parameters
//                        return GENERATED_MESSAGE_V3_TEMPLATE_WITH_TYPE.apply(getCursor(), method.getCoordinates().replace(), typeToParameterize);
//                    }
//                }
//                return super.visitMethodInvocation(method, executionContext);
//            }
//        };
//    }
//}
