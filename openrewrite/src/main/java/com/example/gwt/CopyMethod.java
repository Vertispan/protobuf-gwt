package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copies some method to another location, and changes all specified usages to the new location.
 *
 * This is currently only intended for static methods, so that a given class can have its own copy of the
 * method and the other class can be safely removed.
 */
public class CopyMethod extends ScanningRecipe<Set<J.MethodDeclaration>> {

    @Option(displayName = "Source method",
            description = "The method to copy",
            example = "someMethod()")
    @NonNull
    String source;

    @Option(displayName = "The target class to copy the method into",
            description = "The class to mark as GWT incompatible",
            example = "com.example.MyClass")
    @NonNull
    private final String targetClass;

    private final MethodMatcher matcher;

    public CopyMethod(@JsonProperty("source") @NonNull String source, @JsonProperty("targetClass") @NonNull String targetClass) {
        this.source = source;
        this.targetClass = targetClass;
        matcher = new MethodMatcher(source, false);
    }



    @Override
    public Set<J.MethodDeclaration> getInitialValue(ExecutionContext ctx) {
        return new HashSet<>();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Set<J.MethodDeclaration> acc) {
        return new JavaIsoVisitor<>() {
            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.MethodDeclaration methodDeclaration = super.visitMethodDeclaration(method, executionContext);

                J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
                if (matcher.matches(methodDeclaration, classDecl)) {
                    acc.add(methodDeclaration);
                }
                return methodDeclaration;
            }
        };
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Set<J.MethodDeclaration> acc) {
        if (acc.size() != 1) {
            throw new IllegalStateException("Failed to find a single method matching: " + matcher);
        }
        J.MethodDeclaration matched = acc.iterator().next();
        MethodMatcher targetMatcher = new MethodMatcher(matched);
        return new JavaIsoVisitor<>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                if (classDecl.getType() == null || !classDecl.getType().getFullyQualifiedName().equals(targetClass)) {
                    return classDecl;
                }

                J.ClassDeclaration classDeclaration = super.visitClassDeclaration(classDecl, executionContext);

                if (classDeclaration.getBody().getStatements().stream()
                        .filter(statement -> statement instanceof J.MethodDeclaration)
                        .map(J.MethodDeclaration.class::cast)
                        .anyMatch(methodDeclaration -> targetMatcher.matches(methodDeclaration, classDeclaration))) {
                    return classDeclaration;
                }

                List<Statement> statements = new ArrayList<>(classDeclaration.getBody().getStatements());
                statements.add(matched);
                return classDeclaration.withBody(classDeclaration.getBody().withStatements(statements));
            }
        };
    }

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Copies a method to another class";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Makes a copy of the given method and places it in the target class, but does not explicitly change any callsites to match.";
    }
}
