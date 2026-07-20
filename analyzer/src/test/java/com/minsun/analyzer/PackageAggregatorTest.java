package com.minsun.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PackageAggregator: 클래스 그래프 → 패키지 그래프 집계")
class PackageAggregatorTest {

    @Test
    @DisplayName("서로 다른 패키지 간 클래스 간선은 패키지 간선으로 접힌다")
    void collapsesCrossPackageEdges() {
        DependencyGraph classGraph = new DependencyGraph();
        classGraph.addEdge("com.a.Foo", "com.b.Bar");

        DependencyGraph pkg = PackageAggregator.aggregate(classGraph);

        assertEquals(List.of("com.a -> com.b"), List.copyOf(pkg.toEdgeStrings()));
    }

    @Test
    @DisplayName("같은 패키지 내부 간선은 버린다 (패키지 간 순환이 아님)")
    void dropsIntraPackageEdges() {
        DependencyGraph classGraph = new DependencyGraph();
        classGraph.addEdge("com.a.Foo", "com.a.Bar");

        DependencyGraph pkg = PackageAggregator.aggregate(classGraph);

        assertTrue(pkg.toEdgeStrings().isEmpty(), "같은 패키지 간선은 남지 않아야 한다");
    }

    @Test
    @DisplayName("여러 클래스 간선이 하나의 패키지 간선으로 합쳐진다 (중복 제거)")
    void mergesDuplicatePackageEdges() {
        DependencyGraph classGraph = new DependencyGraph();
        classGraph.addEdge("com.a.Foo", "com.b.Bar");
        classGraph.addEdge("com.a.Baz", "com.b.Qux"); // 같은 a -> b

        DependencyGraph pkg = PackageAggregator.aggregate(classGraph);

        assertEquals(List.of("com.a -> com.b"), List.copyOf(pkg.toEdgeStrings()));
    }

    @Test
    @DisplayName("클래스 레벨 순환이 같은 패키지 안이면 패키지 레벨에선 순환이 사라진다")
    void intraPackageCycleDisappearsAtPackageLevel() {
        DependencyGraph classGraph = new DependencyGraph();
        classGraph.addEdge("com.a.Foo", "com.a.Bar");
        classGraph.addEdge("com.a.Bar", "com.a.Foo"); // 클래스 레벨 순환

        DependencyGraph pkg = PackageAggregator.aggregate(classGraph);

        assertTrue(TarjanScc.cycles(pkg).isEmpty(), "패키지 내부 순환은 패키지 간 순환이 아니다");
    }

    @Test
    @DisplayName("패키지 경계를 넘나드는 순환은 패키지 레벨에서도 순환으로 남는다")
    void crossPackageCycleRemains() {
        DependencyGraph classGraph = new DependencyGraph();
        classGraph.addEdge("com.a.Foo", "com.b.Bar");
        classGraph.addEdge("com.b.Bar", "com.a.Foo");

        DependencyGraph pkg = PackageAggregator.aggregate(classGraph);

        List<List<String>> cycles = TarjanScc.cycles(pkg);
        assertEquals(1, cycles.size());
        assertEquals(List.of("com.a", "com.b"), cycles.get(0));
    }

    @Test
    @DisplayName("패키지 없는 최상위 타입은 (default) 로 묶인다")
    void topLevelTypeGoesToDefaultPackage() {
        assertEquals("(default)", PackageAggregator.packageOf("Foo"));
        assertEquals("com.a", PackageAggregator.packageOf("com.a.Foo"));
    }
}
