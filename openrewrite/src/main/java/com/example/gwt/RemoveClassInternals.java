package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

/**
 * Rewrites a class to do nothing - all public methods and constructors throw, all private members are removed.
 * Useful for when a class is used in many other APIs, but will either never actually be used, or null can
 * be passed instead.
 *
 * Similar to calling MethodThrowsException with {@code fully.qualified.ClassName *(..)}, but will also specifically
 * remove private methods and fields.
 */
public class RemoveClassInternals extends Recipe {
    @Option(displayName = "Class to remove internals from",
            description = "Fully qualified class name to remove internals from, e.g. com.example.MyClass",
            example = "com.example.MyClass")
    @NonNull
    private final String fullyQualifiedClassName;

    public RemoveClassInternals(@NonNull @JsonProperty("fullyQualifiedClassName") String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Removes class internals";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Leaves the non-private members of a type but replaces all method bodies with throwing an exception";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                if (classDecl.getType() != null && classDecl.getType().getFullyQualifiedName().equals(fullyQualifiedClassName)) {
                    // Remove all private fields and methods before visiting
                    classDecl = classDecl.withBody(classDecl.getBody().withStatements(ListUtils.map(classDecl.getBody().getStatements(), stmt -> {
                        if (stmt instanceof J.MethodDeclaration && ((J.MethodDeclaration) stmt).hasModifier(J.Modifier.Type.Private)) {
                            return null; // Remove private methods
                        } else if (stmt instanceof J.VariableDeclarations && ((J.VariableDeclarations) stmt).hasModifier(J.Modifier.Type.Private)) {
                            return null; // Remove private fields
                        }
                        return stmt;
                    })));
                    // visit what's left
                    return super.visitClassDeclaration(classDecl, ctx);
                }

                return super.visitClassDeclaration(classDecl, ctx);
            }

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
                if (classDecl.getType() != null && classDecl.getType().getFullyQualifiedName().equals(fullyQualifiedClassName)) {
                    final JavaTemplate replacementTemplate = JavaTemplate
                            .builder("throw new UnsupportedOperationException(\"" + method.getSimpleName() + "\")")
                            .build();

                    return replacementTemplate.apply(getCursor(), method.getCoordinates().replaceBody());
                }
                return super.visitMethodDeclaration(method, executionContext);
            }
        };
    }
}
