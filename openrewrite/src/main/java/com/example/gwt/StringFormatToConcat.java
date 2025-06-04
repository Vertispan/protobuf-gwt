package com.example.gwt;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

import java.util.Optional;

import static org.openrewrite.Tree.randomId;

/**
 * Rewrite calls to String.format(). Currently only keeps the main body and loses the other strings
 */
public class StringFormatToConcat extends Recipe {
    private static final MethodMatcher FORMAT_MATCHER = new MethodMatcher("java.lang.String format(java.lang.String,..)");
//    private static final MethodMatcher FORMATTED_MATCHER = new MethodMatcher("java.lang.String formatted(..)");

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "String.format() to string concatenation";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Rewrites away String.format() calls, as not compatible with GWT";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<>() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                if (FORMAT_MATCHER.matches(method)) {
                    Optional<J.Literal> literal = getLiteralFromExpression(method.getArguments().get(0));
                    if (literal.isPresent()) {
                        return literal.get();
                    }
                    throw new IllegalStateException("String.format() first argument must be a literal string");
                }

                return super.visitMethodInvocation(method, executionContext);
            }
        };
    }

    private static Optional<J.Literal> getLiteralFromExpression(Expression expr) {
        if (expr instanceof J.Literal) {
            return Optional.of((J.Literal) expr);
        } else if (expr instanceof J.Binary) {
            J.Binary binary = (J.Binary) expr;
            Optional<J.Literal> left = getLiteralFromExpression(binary.getLeft());
            Optional<J.Literal> right = getLiteralFromExpression(binary.getRight());
            if (left.isPresent() && right.isPresent()) {
                return Optional.of(stringLiteral(left.get().getValueSource() + " + " + right.get().getValueSource()));
            }
        }
        return Optional.empty();
    }

    private static J.Literal stringLiteral(String value) {
        return new J.Literal(randomId(), Space.EMPTY, Markers.EMPTY, value,
                value, null, JavaType.Primitive.String);
    }
}
