package com.minsun.analyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 순환 의존 분석 파이프라인의 단일 진입점(facade).
 *
 * <p>소스 루트 하나를 받아 파싱→집계→순환검출까지 한 번에 실행하고,
 * 클래스/패키지 두 레벨의 그래프·순환·Mermaid 를 모아 들고 있는다.
 * {@link Main}(데모 CLI)과 Gradle 플러그인이 이 클래스 하나만 호출하면 되도록
 * 파이프라인 배선을 한곳에 모았다.
 *
 * <pre>{@code
 * CycleAnalysis analysis = CycleAnalysis.run(sourceRoot, "com.example");
 * String mermaid = analysis.packageMermaid();
 * }</pre>
 */
public final class CycleAnalysis {

    private final DependencyGraph classGraph;
    private final DependencyGraph packageGraph;
    private final List<List<String>> classCycles;
    private final List<List<String>> packageCycles;

    private CycleAnalysis(DependencyGraph classGraph, DependencyGraph packageGraph,
                          List<List<String>> classCycles, List<List<String>> packageCycles) {
        this.classGraph = classGraph;
        this.packageGraph = packageGraph;
        this.classCycles = classCycles;
        this.packageCycles = packageCycles;
    }

    /**
     * {@code sourceRoot} 를 분석해 파이프라인 전체를 실행한다.
     * {@code basePackage} 로 시작하는 내부 타입만 의존으로 채택한다.
     */
    public static CycleAnalysis run(Path sourceRoot, String basePackage) throws IOException {
        DependencyGraph classGraph = new EdgeExtractor(sourceRoot, basePackage).extract();
        DependencyGraph packageGraph = PackageAggregator.aggregate(classGraph);
        return new CycleAnalysis(
            classGraph,
            packageGraph,
            TarjanScc.cycles(classGraph),
            TarjanScc.cycles(packageGraph));
    }

    /** 클래스 간 의존 그래프 (원본). */
    public DependencyGraph classGraph() {
        return classGraph;
    }

    /** 패키지 레벨로 집계된 의존 그래프. */
    public DependencyGraph packageGraph() {
        return packageGraph;
    }

    /** 클래스 레벨 순환(SCC) 목록 — 드릴다운 뷰. */
    public List<List<String>> classCycles() {
        return classCycles;
    }

    /** 패키지 레벨 순환(SCC) 목록 — 기본 뷰 (같은 패키지 내부 순환은 제외됨). */
    public List<List<String>> packageCycles() {
        return packageCycles;
    }

    /** 클래스 레벨 순환의 Mermaid flowchart. */
    public String classMermaid() {
        return MermaidRenderer.renderCycles(classGraph, classCycles, "classes");
    }

    /** 패키지 레벨 순환의 Mermaid flowchart. */
    public String packageMermaid() {
        return MermaidRenderer.renderCycles(packageGraph, packageCycles, "packages");
    }
}
