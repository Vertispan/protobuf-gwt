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
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

/**
 * Removes a specific method and overrides.
 * <p>
 * Does not remove or rewrite callsites.
 */
public class RemoveMethod extends Recipe {
    @Option(displayName = "Removed method",
            description = "The method to remove",
            example = "someMethod()")
    @NonNull
    String target;

    @Option(displayName = "Match on overrides",
            description = "When enabled, find methods that are overrides of the method pattern, defaults to false",
            required = false)
    @Nullable
    Boolean matchOverrides;

    private final MethodMatcher matcher;

    public RemoveMethod(@NonNull @JsonProperty("target") String target, @JsonProperty("matchOverrides") Boolean matchOverrides) {
        this.target = target;
        this.matchOverrides = matchOverrides;
        this.matcher = new MethodMatcher(target, matchOverrides != null ? matchOverrides : false);
    }

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Removes a method";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Removes a method and any overrides in subtypes";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveMethodVisitor();
    }

    public class RemoveMethodVisitor extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

            J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
            if (matcher.matches(m, classDecl)) {
                return null;
            }
            return m;
        }
    }
}
