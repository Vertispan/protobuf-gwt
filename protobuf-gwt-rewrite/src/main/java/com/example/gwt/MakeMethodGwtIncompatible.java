package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.migrate.javax.AnnotateTypesVisitor;
import org.openrewrite.java.tree.J;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MakeMethodGwtIncompatible extends Recipe {
    @Option(displayName = "Incompatible method",
            description = "The method to mark as @GwtIncompatible",
            example = "someMethod()")
    @NonNull
    String incompatibleMethod;


    @Option(displayName = "Match on overrides",
            description = "When enabled, find methods that are overrides of the method pattern.",
            required = false)
    @Nullable
    Boolean matchOverrides;

    private final MethodMatcher matcher;
    private final JavaTemplate template;

    public MakeMethodGwtIncompatible(@NonNull @JsonProperty("incompatibleMethod") String incompatibleMethod, @JsonProperty("matchOverrides") Boolean matchOverrides) {
        this.incompatibleMethod = incompatibleMethod;
        this.matchOverrides = matchOverrides;
        this.matcher = new MethodMatcher(incompatibleMethod, matchOverrides);

        String interfaceAsString = "package com.google.gwt.core.client;\npublic @interface GwtIncompatible {}";
        //noinspection LanguageMismatch
        this.template = JavaTemplate.builder("@GwtIncompatible")
                .imports("com.google.gwt.core.client.GwtIncompatible")
                .javaParser(JavaParser.fromJavaVersion().dependsOn(interfaceAsString))
                .build();
    }

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Mark method as @GwtIncompatible";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Ensures that the method will not be compiled to JS";
    }

    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new Visitor();
    }


    private class Visitor extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

            J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
            if (matcher.matches(m, classDecl)) {

                J.MethodDeclaration result = template.apply(getCursor(), m.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                return result;
            }
            return m;
        }
    }
}
