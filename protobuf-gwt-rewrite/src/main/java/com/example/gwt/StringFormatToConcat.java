package com.example.gwt;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.CoordinateBuilder;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openrewrite.Tree.randomId;

/**
 * Rewrite all calls to String.format(). Currently only supports %s, %d, etc, and rewrites to plain concatenation
 * of those values into a string, with no locale support or other formatting.
 */
public class StringFormatToConcat extends Recipe {
    private static final MethodMatcher FORMAT_MATCHER = new MethodMatcher("java.lang.String format(java.lang.String,..)");
    private static final MethodMatcher FORMAT_LOCALE_MATCHER = new MethodMatcher("java.lang.String format(java.util.Locale,java.lang.String,..)");
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
                    List<Expression> args = method.getArguments().subList(1, method.getArguments().size());
                    String formatStr = getConstantStringFromExpression(method.getArguments().get(0))
                            .orElseThrow(() -> new IllegalStateException("String.format()'s format pattern must be a literal string"));
                    return replace(formatStr, args, method.getCoordinates());

                } else if (FORMAT_LOCALE_MATCHER.matches(method)) {
                    // For the locale version, we ignore the locale and just use the format string
                    List<Expression> args = method.getArguments().subList(2, method.getArguments().size());
                    String formatStr = getConstantStringFromExpression(method.getArguments().get(1))
                            .orElseThrow(() -> new IllegalStateException("String.format()'s format pattern must be a literal string"));
                    return replace(formatStr, args, method.getCoordinates());
                }

                return super.visitMethodInvocation(method, executionContext);
            }

            private J replace(String formatStr, List<Expression> args, CoordinateBuilder.MethodInvocation coordinates) {
                formatStr = formatStr.replace("\\", "\\\\") // Escape backslashes
                        .replace("\n", "\\n")// Escape newlines
                        .replace("\r", "\\r") // Escape carriage returns
                        .replace("\"", "\\\""); // Escape double quotes
                Matcher matcher = Pattern.compile("%[a-z]").matcher(formatStr);
                StringBuilder sb = new StringBuilder("\"");
                while (matcher.find()) {
                    matcher.appendReplacement(sb, "\" + #{any()} + \"");
                }
                matcher.appendTail(sb);
                sb.append("\"");
                return JavaTemplate.builder(sb.toString()).build().apply(getCursor(), coordinates.replace(), args.toArray());
            }
        };
    }

    /**
     * Type of the expression is assumed to be a string (or we can't convert non-strings to strings freely).
     * @param expr
     * @return
     */
    private static Optional<String> getConstantStringFromExpression(Expression expr) {
        if (expr instanceof J.Literal) {
            return Optional.of((J.Literal) expr).map(J.Literal::getValue).map(Object::toString);
        } else if (expr instanceof J.Binary) {
            J.Binary binary = (J.Binary) expr;
            Optional<String> left = getConstantStringFromExpression(binary.getLeft());
            Optional<String> right = getConstantStringFromExpression(binary.getRight());
            if (left.isPresent() && right.isPresent()) {
                return Optional.of(left.get() + right.get());
            }
        }
        return Optional.empty();
    }

    private static J.Literal stringLiteral(String value) {
        return new J.Literal(randomId(), Space.EMPTY, Markers.EMPTY, value,
                value, null, JavaType.Primitive.String);
    }
}
