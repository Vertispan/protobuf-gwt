package com.example.gwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Records all dependencies from any type to another, then traverses the graph starting from
 * the entrypoint types to find all reachable types. All other types are removed.
 */
public class EliminateUnreachableTypes extends ScanningRecipe<Map<String, EliminateUnreachableTypes.TypeModel>> {
    private final Set<String> entrypointTypes;

    public EliminateUnreachableTypes(@JsonProperty("entrypointTypes") List<String> entrypointTypes) {
        this.entrypointTypes = Set.copyOf(entrypointTypes);
    }

    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Eliminate unreachable types";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Given a set of entrypoint types, eliminate all types that are not reachable from those entrypoints.";
    }

    @Override
    public Map<String, TypeModel> getInitialValue(ExecutionContext ctx) {
        return new LinkedHashMap<>();
    }


    @Override
    public boolean causesAnotherCycle() {
        return true;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Map<String, TypeModel> acc) {
        return new ScanAllDependencies(acc);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Map<String, TypeModel> acc) {
        // Given the discovered map and the provided set of entrypoints, first work out the
        // reachable types, then visit to keep those types.
        Set<String> keep = new LinkedHashSet<>();
        for (String entrypointType : entrypointTypes) {
            TypeModel typeModel = acc.get(entrypointType);
            if (typeModel == null) {
                throw new IllegalStateException("Didn't find type " + entrypointType + " in the sources");
            }
            recordDependencies(acc, keep, Set.of(typeModel.type));
            if (!keep.contains(typeModel.type.getFullyQualifiedName())) {
                throw new IllegalStateException("Didn't actually keep " + entrypointType);
            }
        }
        return new EliminateUnreachableTypesVisitor(keep);
    }

    private void recordDependencies(Map<String, TypeModel> acc, Set<String> keep, Set<JavaType.Class> types) {
        recordDependencies(acc, keep, types, 0);
    }
    private void recordDependencies(Map<String, TypeModel> acc, Set<String> keep, Set<JavaType.Class> types, int depth) {
        for (JavaType.Class type : types) {
            TypeModel typeModel = acc.get(type.getFullyQualifiedName());
            if (typeModel != null && keep.add(type.getFullyQualifiedName())) {
//                System.out.println("  ".repeat(depth) + type);
                // If we have sources for this type and haven't already added it, add it and all its dependencies
                recordDependencies(acc, keep, typeModel.getDependencies(), depth + 1);
            }
        }
    }

    public static class TypeModel {
        // The compilation unit that the type appears in - if null, it is from a dependency, and we can't prune it.
        private J.CompilationUnit compilationUnit;
        // The type itself that this represents
        private final JavaType.Class type;
        // Types that this type depends on
        private final Set<JavaType.Class> dependencies = new HashSet<>();

        public TypeModel(JavaType.Class type) {
            this.type = type;
        }

        public void addDependency(JavaType.Class dependency) {
            dependencies.add(dependency);
        }
        public Set<JavaType.Class> getDependencies() {
            return dependencies;
        }
    }

    public class ScanAllDependencies extends JavaIsoVisitor<ExecutionContext> {
        private final Map<String, TypeModel> typeModels;
        private TypeModel currentTypeModel;
        private final Set<TypeModel> inCompilationUnit = new HashSet<>();

        public ScanAllDependencies(Map<String, TypeModel> typeModels) {
            this.typeModels = typeModels;
        }

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {
            assert inCompilationUnit.isEmpty();
            J.CompilationUnit compilationUnit = super.visitCompilationUnit(cu, executionContext);
            for (TypeModel typeModel : inCompilationUnit) {
                typeModel.compilationUnit = compilationUnit;
            }
            inCompilationUnit.clear();
            return compilationUnit;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
            TypeModel prev = currentTypeModel;
            currentTypeModel = new TypeModel(raw(classDecl.getType()).get());
            inCompilationUnit.add(currentTypeModel);
            typeModels.put(classDecl.getType().getFullyQualifiedName(), currentTypeModel);
            J.ClassDeclaration classDeclaration = super.visitClassDeclaration(classDecl, executionContext);
            currentTypeModel = prev;
            return classDeclaration;
        }
        @Override
        public J.Import visitImport(J.Import _import, ExecutionContext p) {
            // Don't descend into the tree and mark this type
            return _import;
        }

        @Override
        public J.Package visitPackage(J.Package pkg, ExecutionContext executionContext) {
            // Don't visit the package decl
            return pkg;
        }

        @Override
        public @Nullable JavaType visitType(@Nullable JavaType javaType, ExecutionContext p) {
            raw(javaType).ifPresent(currentTypeModel::addDependency);

            return super.visitType(javaType, p);
        }
    }

    public class EliminateUnreachableTypesVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final Set<String> keep;

        public EliminateUnreachableTypesVisitor(Set<String> keep) {
            this.keep = keep;
        }

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {
            J.CompilationUnit compilationUnit = super.visitCompilationUnit(cu, executionContext);
            if (compilationUnit.getClasses().isEmpty()) {
                // No types in this file, remove it.
                // Despite the warning about returning null, this seems to work?
                return null;
            }
            return compilationUnit;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
            Optional<JavaType.Class> raw = raw(classDecl.getType());
            if (raw.isEmpty() || !keep.contains(raw.get().getFullyQualifiedName())) {
                // If the type is not in the keep set, remove it.
                // Despite the warning about returning null, this seems to work?
                return null;
            }
            return super.visitClassDeclaration(classDecl, executionContext);
        }
    }

    private static Optional<JavaType.Class> raw(JavaType type) {
        if (type instanceof JavaType.Class) {
            return Optional.of((JavaType.Class) type);
        } else if (type instanceof JavaType.Parameterized) {
            return raw(((JavaType.Parameterized) type).getType());
        } else if (type instanceof JavaType.GenericTypeVariable) {
            List<JavaType> bounds = ((JavaType.GenericTypeVariable) type).getBounds();
            return bounds.isEmpty() ? Optional.empty() : raw(bounds.get(0));
        }
        return Optional.empty();
    }
}
