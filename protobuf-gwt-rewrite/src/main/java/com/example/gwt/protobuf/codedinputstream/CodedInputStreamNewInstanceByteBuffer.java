package com.example.gwt.protobuf.codedinputstream;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class CodedInputStreamNewInstanceByteBuffer extends Recipe {
    private static final String cisClassName = "com.google.protobuf.CodedInputStream";
    private static final MethodMatcher matcher = new MethodMatcher(
            cisClassName + " newInstance(java.nio.ByteBuffer,boolean)");

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Simplify CodedInputStream.newInstance(ByteBuffer)";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Removes the isArray check and unsafe nio implementation, and only copies the bytes and reads as a plain array";
    }

    @Override
    public boolean causesAnotherCycle() {
        return true;
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
                    return super.visitMethodDeclaration(method, executionContext);
                }
                return method;
            }

            @Override
            public J.If visitIf(J.If iff, ExecutionContext executionContext) {
                // Always remove both checks: isArray, and unsafe supported
                return null;
            }
        };
    }
}
