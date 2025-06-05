package com.example.gwt;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.util.List;

/**
 * Takes out readResolve and friends.
 */
public class JavaSerializationRemover extends Recipe {
    private static final MethodMatcher readResolve = new MethodMatcher("* readResolve()");
    private static final MethodMatcher writeReplace = new MethodMatcher("* writeReplace()");
    private static final MethodMatcher readObject = new MethodMatcher("* readObject(java.io.ObjectInputStream)");
    private static final MethodMatcher writeObject = new MethodMatcher("* writeObject(java.io.ObjectOutputStream)");

    private static final List<MethodMatcher> serializationMethods = List.of(
            readResolve,
            writeReplace,
            readObject,
            writeObject
    );
    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
                if (serializationMethods.stream().anyMatch(m -> m.matches(method, classDecl))) {
                    // Remove the method
                    return null;
                }
                return super.visitMethodDeclaration(method, executionContext);
            }
        };
    }
}
