package com.example.gwt.protobuf.codedinputstream;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class CodedInputStreamNewInstanceIterable extends Recipe {
    private static final String cisClassName = "com.google.protobuf.CodedInputStream";
    private static final MethodMatcher matcher = new MethodMatcher(
            cisClassName + " newInstance(java.lang.Iterable*)");

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Simplify CodedInputStream.newInstance(Iterable) and friends";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Removes the isArray check and unsafe nio implementation, and only copies the bytes and reads as a plain array";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            private J.ClassDeclaration cisClassDecl;
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                if (classDecl.getType() != null && classDecl.getType().getFullyQualifiedName().equals(cisClassName)) {
                    cisClassDecl = classDecl;
                    return super.visitClassDeclaration(classDecl, executionContext);
                }
                return classDecl;
            }

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                if (matcher.matches(method, cisClassDecl)) {
                    String iterable = method.getParameters().size() == 1 ? "input" : "bufs";
                    final JavaTemplate replacementTemplate = JavaTemplate
                            .builder("return newInstance(new IterableByteBufferInputStream(" + iterable + ");\n")
                            .imports("com.google.protobuf.IterableByteBufferInputStream")
                            .build();

                    return replacementTemplate.apply(getCursor(), method.getCoordinates().replaceBody());
                }
                return method;
            }
        };
    }
}
