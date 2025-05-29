package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.Comparator;

public class MakeClassGwtIncompatible extends Recipe {
    @Option(displayName = "Class to mark as GWT incompatible",
            description = "The class to mark as GWT incompatible",
            example = "com.example.MyClass")
    @NonNull
    private final String fullyQualifiedClassName;

    @JsonCreator
    public MakeClassGwtIncompatible(@NonNull @JsonProperty("fullyQualifiedClassName") String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Mark Class @GwtIncompatible";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Marks a class as being incompatible in GWT and J2CL";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MakeClassGwtIncompatibleVisitor();
    }

    public class MakeClassGwtIncompatibleVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final String importComponent = "com.google.gwt.core.client.GwtIncompatible";
        private final JavaTemplate componentAnnotationTemplate =
                JavaTemplate.builder("@Component").imports(importComponent)
                        .build();

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
            // Don't make changes to classes that don't match the fully qualified name
            System.out.println(classDecl.getType().getFullyQualifiedName());
            if (classDecl.getType() == null || !classDecl.getType().getFullyQualifiedName().equals(fullyQualifiedClassName)) {
                return classDecl;
            }

            classDecl = componentAnnotationTemplate.apply(getCursor(), classDecl.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));

            return classDecl;
        }
    }
}
