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
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavadocVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Javadoc;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Similar to {@link org.openrewrite.java.ChangeMethodTargetToStatic}, but adds the instance as the first
 * parameter to the newly targeted static method.
 */
public class RetargetInstanceMethodToStatic extends Recipe {
    @Option(displayName = "Method pattern",
            description = "The method call to rewrite",
            example = "someMethod()")
    @NonNull
    String methodPattern;

    @Option(displayName = "Fully-qualified target type name",
            description = "A fully-qualified class name of the type upon which the static method is defined.",
            example = "java.util.Set")
    @NonNull
    String fullyQualifiedTargetTypeName;

    @Option(displayName = "Method to call on the target class",
            description = "The name of the static method to call on the target class.",
            required = false)
    @Nullable
    String targetMethodName;

    private final MethodMatcher matcher;

    public RetargetInstanceMethodToStatic(@JsonProperty("methodPattern") @NonNull String methodPattern, @JsonProperty("fullyQualifiedTargetTypeName") @NonNull String fullyQualifiedTargetTypeName, @JsonProperty("targetMethodName") @Nullable String targetMethodName) {
        this.methodPattern = methodPattern;
        this.fullyQualifiedTargetTypeName = fullyQualifiedTargetTypeName;
        this.matcher = new MethodMatcher(methodPattern, false);
        this.targetMethodName = targetMethodName;
    }

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
            protected JavadocVisitor<ExecutionContext> getJavadocVisitor() {
                return new JavadocVisitor<ExecutionContext>(this) {
                    public Javadoc visitReference(Javadoc.Reference reference, ExecutionContext ctx) {
                        // Don't rewrite javadoc references
                        return reference;
                    }
                };
            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, executionContext);
                if (!matcher.matches(methodInvocation)) {
                    return methodInvocation;
                }

                String methodName = targetMethodName != null ? targetMethodName : methodInvocation.getSimpleName();
                List<Expression> argList = new ArrayList<>(methodInvocation.getArguments());
                if (argList.size() == 1 && argList.get(0) instanceof J.Empty) {
                    // If the method has no arguments, we can just remove the empty argument
                    argList.remove(0);
                }
                argList.add(0, methodInvocation.getSelect()); // Move the instance to the first argument
                StringJoiner template = new StringJoiner(", ", fullyQualifiedTargetTypeName + "." + methodName + "(", ")");
                for (Expression ignored : argList) {
                    template.add("#{any()}");
                }
                return JavaTemplate.builder(template.toString())
                        .imports(fullyQualifiedTargetTypeName)
                        .contextSensitive()
                        .build()
                        .apply(getCursor(), methodInvocation.getCoordinates().replace(), argList.toArray());
            }
        };
    }
}
