package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes an annotation from a given methods pattern, e.g. taking off @Override from Object.clone()
 */
public class RemoveAnnotationFromMethod extends Recipe {
    @Option(displayName = "Removed method",
            description = "The method to remove",
            example = "someMethod()")
    @NonNull
    String target;

    @Option(displayName = "Match on overrides",
            description = "When enabled, find methods that are overrides of the method pattern.",
            required = false)
    @Nullable
    Boolean matchOverrides;

    @Option(displayName = "Annotation pattern",
            description = "An annotation pattern, expressed as a method pattern.",
            example = "@java.lang.SuppressWarnings(\"deprecation\")")
    @NonNull
    String annotationPattern;

    private final MethodMatcher matcher;
    private final AnnotationMatcher annotationMatcher;

    public RemoveAnnotationFromMethod(@JsonProperty("target") String target, @JsonProperty("matchOverride") Boolean matchOverrides, @JsonProperty("annotationPattern") String annotationPattern) {
        this.target = target;
        this.matchOverrides = matchOverrides;
        this.annotationPattern = annotationPattern;

        this.matcher = new MethodMatcher(target, matchOverrides);
        this.annotationMatcher = new AnnotationMatcher(annotationPattern);
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
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);
                J.Annotation annotationRemoved = getCursor().pollMessage("annotationRemoved");

                List<J.Annotation> leadingAnnotations = method.getLeadingAnnotations();
                if (annotationRemoved != null && !leadingAnnotations.isEmpty()) {
                    if (leadingAnnotations.get(0) == annotationRemoved && leadingAnnotations.size() == 1) {
                        if (!m.getModifiers().isEmpty()) {
                            m = m.withModifiers(Space.formatFirstPrefix(m.getModifiers(), Space.firstPrefix(m.getModifiers()).withWhitespace("")));
                        } else if (m.getPadding().getTypeParameters() != null) {
                            m = m.getPadding().withTypeParameters(m.getPadding().getTypeParameters().withPrefix(m.getPadding().getTypeParameters().getPrefix().withWhitespace("")));
                        } else if (m.getReturnTypeExpression() != null) {
                            m = m.withReturnTypeExpression(m.getReturnTypeExpression().withPrefix(m.getReturnTypeExpression().getPrefix().withWhitespace("")));
                        } else {
                            m = m.withName(m.getName().withPrefix(m.getName().getPrefix().withWhitespace("")));
                        }
                    } else {
                        List<J.Annotation> newLeadingAnnotations = removeAnnotationOrEmpty(leadingAnnotations, annotationRemoved);
                        if (!newLeadingAnnotations.isEmpty()) {
                            m = m.withLeadingAnnotations(newLeadingAnnotations);
                        }
                    }
                }
                return m;
            }

            @Override
            public  J.@Nullable Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
                if (annotationMatcher.matches(annotation)) {
                    getCursor().getParentOrThrow().putMessage("annotationRemoved", annotation);
                    maybeRemoveImport(TypeUtils.asFullyQualified(annotation.getType()));
                    maybeRemoveAnnotationParameterImports(annotation);
                    //noinspection ConstantConditions
                    return null;
                }
                return super.visitAnnotation(annotation, ctx);
            }

            /**
             * If the annotation has parameters, then the imports for the parameter types may need to be removed.
             *
             * @param annotation the annotation to check
             */
            private void maybeRemoveAnnotationParameterImports(J.@NonNull Annotation annotation) {
                if (ListUtils.nullIfEmpty(annotation.getArguments()) == null) {
                    return;
                }

                List<Expression> arguments = annotation.getArguments();

                arguments.forEach(argument -> {
                    if (argument instanceof J.Assignment) {
                        J.Assignment assignment = (J.Assignment) argument;
                        Expression expression = assignment.getAssignment();
                        maybeRemoveImportFromExpression(expression);
                    } else {
                        maybeRemoveImport(TypeUtils.asFullyQualified(argument.getType()));
                    }
                });
            }

            private void maybeRemoveImportFromExpression(Expression expression) {
                if (expression instanceof J.NewArray) {
                    maybeRemoveAnnotationFromArray((J.NewArray) expression);
                } else if (expression instanceof J.FieldAccess) {
                    maybeRemoveAnnotationFromFieldAccess((J.FieldAccess) expression);
                } else if (expression instanceof J.Identifier) {
                    JavaType.Variable fieldType = ((J.Identifier) expression).getFieldType();
                    if (fieldType != null) {
                        maybeRemoveImport(TypeUtils.asFullyQualified(fieldType.getOwner()));
                    }
                } else {
                    maybeRemoveImport(TypeUtils.asFullyQualified(expression.getType()));
                }
            }

            private void maybeRemoveAnnotationFromArray(J.@NonNull NewArray newArray) {
                List<Expression> initializer = newArray.getInitializer();
                if (ListUtils.nullIfEmpty(initializer) != null) {
                    initializer.forEach(this::maybeRemoveImportFromExpression);
                }
            }

            private void maybeRemoveAnnotationFromFieldAccess(J.@NonNull FieldAccess fa) {
                JavaType.Variable fieldType = fa.getName().getFieldType();
                if (fieldType != null && fieldType.getOwner() != null) {
                    maybeRemoveImport(TypeUtils.asFullyQualified(fieldType.getOwner()));
                }
            }

            /**
             * Returns a list of leading annotations with the target removed or an empty list if no changes are necessary.
             * A prefix only needs to change if the index == 0 and the prefixes of the target annotation and next annotation are not equal.
             */
            private List<J.Annotation> removeAnnotationOrEmpty(List<J.Annotation> leadingAnnotations, J.Annotation targetAnnotation) {
                int index = leadingAnnotations.indexOf(targetAnnotation);
                List<J.Annotation> newLeadingAnnotations = new ArrayList<>();
                if (index == 0) {
                    J.Annotation nextAnnotation = leadingAnnotations.get(1);
                    if (!nextAnnotation.getPrefix().equals(targetAnnotation.getPrefix())) {
                        newLeadingAnnotations.add(nextAnnotation.withPrefix(targetAnnotation.getPrefix()));
                        for (int i = 2; i < leadingAnnotations.size(); ++i) {
                            newLeadingAnnotations.add(leadingAnnotations.get(i));
                        }
                    }
                }
                return newLeadingAnnotations;
            }

        };
    }
}
