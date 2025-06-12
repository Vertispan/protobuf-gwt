package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class MethodThrowsException extends Recipe {
    @Option(displayName = "Removed method",
            description = "The method to remove",
            example = "someMethod()")
    @NonNull
    String target;

    @Option(displayName = "Exception template",
    description = "The exception to throw",
    example = "throw new UnsupportedOperationException(\"someMethod()\")")
    @NonNull
    String exceptionTemplate;

    private final MethodMatcher matcher;

    public MethodThrowsException(@JsonProperty("target") String target, @JsonProperty("exceptionTemplate") String exceptionTemplate) {
        this.target = target;
        if (exceptionTemplate == null) {
            this.exceptionTemplate = "throw new UnsupportedOperationException(\"" + target + "\")";
        } else {
            this.exceptionTemplate = exceptionTemplate;
        }
        this.matcher = new MethodMatcher(target, false);
    }


    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Rewrites the method to throw an exception";
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
                J.MethodDeclaration methodDeclaration = super.visitMethodDeclaration(method, executionContext);
                J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
                if (matcher.matches(method, classDecl)) {
                    final JavaTemplate replacementTemplate = JavaTemplate
                            .builder(exceptionTemplate)
                            .build();

                    return replacementTemplate.apply(getCursor(), methodDeclaration.getCoordinates().replaceBody());
                }

                return methodDeclaration;
            }
        };
    }
}
