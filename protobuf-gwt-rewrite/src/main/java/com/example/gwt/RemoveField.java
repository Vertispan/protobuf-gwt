package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

import java.util.List;
import java.util.stream.Collectors;

public class RemoveField extends Recipe {
    @Option(displayName = "Class to remove the field from",
            description = "",
            example = "java.util.Set")
    @NonNull
    String fullyQualifiedClassName;

    @Option(displayName = "Field name to remove",
            description = "")
    @Nullable
    String fieldName;

    public RemoveField(@JsonProperty("fullyQualifiedClassName") String fullyQualifiedClassName, @JsonProperty("fieldName") String fieldName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
        this.fieldName = fieldName;
    }

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Removed a field from a class";
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
            public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext executionContext) {
                J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
                if (classDecl.getType() == null || !classDecl.getType().getFullyQualifiedName().equals(fullyQualifiedClassName)) {
                    // Not in the target class, so just return the original
                    return multiVariable;
                }
                List<J.VariableDeclarations.NamedVariable> vars = multiVariable.getVariables().stream()
                        .filter(v -> !v.getSimpleName().equals(fieldName))
                        .collect(Collectors.toList());
                if (vars.isEmpty()) {
                    return null;
                }
                return multiVariable.withVariables(vars);
            }

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                // Don't descend into methods, even to find inner classes...
                return method;
            }
        };
    }
}
