package com.vertispan.recipes.protobuf.utf8;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

public class Utf8SafeProcessor extends Recipe {

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Protobuf Utf8 must not use unsafe";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Rewrites Utf8.processor to always be a safe instance";
    }

    @Override
    public boolean causesAnotherCycle() {
        return true;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                if (classDecl.getType() != null && classDecl.getType().getFullyQualifiedName().equals("com.google.protobuf.Utf8")) {
                    return super.visitClassDeclaration(classDecl, executionContext);
                }
                return classDecl;
            }

            @Override
            public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext executionContext) {
                if (multiVariable.getVariables().size() == 1 && multiVariable.getVariables().get(0).getSimpleName().equals("processor")) {
                    JavaTemplate build = JavaTemplate.builder("private static final Processor processor = new SafeProcessor();").build();
                    return build.apply(getCursor(), multiVariable.getCoordinates().replace());
                }
                return super.visitVariableDeclarations(multiVariable, executionContext);
            }


            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                // Don't descend into methods
                return method;
            }
        };
    }

}
