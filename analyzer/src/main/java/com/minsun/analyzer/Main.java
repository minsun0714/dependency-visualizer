package com.minsun.analyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    // 분석 대상 소스 루트 (일단 하드코딩 — Phase 7 플러그인화에서 파라미터화 예정)
    private static final Path SAMPLE_SRC = Path.of("sample-project", "src", "main", "java");

    // 이 prefix 로 시작하는 타입만 내부 의존으로 채택 (JDK/Spring/Lombok 등 외부 타입 제외)
    private static final String BASE_PACKAGE = "com.minsun.sample";

    public static void main(String[] args) throws IOException {
        System.out.println("Analyzing " + SAMPLE_SRC + " ...");
        System.out.println();

        DependencyGraph graph = new EdgeExtractor(SAMPLE_SRC, BASE_PACKAGE).extract();

        System.out.println("Nodes: " + graph.nodeCount() + ", Edges: " + graph.edgeCount());
        System.out.println("-".repeat(50));
        graph.toEdgeStrings().forEach(System.out::println);

        // 클래스 레벨 순환 (드릴다운 용)
        List<List<String>> classCycles = TarjanScc.cycles(graph);
        printCycles("Class-level circular dependencies", classCycles, "classes");

        // 패키지 레벨 순환 (기본 뷰) — 같은 패키지 안에서만 얽힌 순환은 걸러진다
        DependencyGraph packageGraph = PackageAggregator.aggregate(graph);
        List<List<String>> packageCycles = TarjanScc.cycles(packageGraph);
        printCycles("Package-level circular dependencies", packageCycles, "packages");

        printMermaid("Mermaid (package level)", packageGraph, packageCycles, "packages");
        printMermaid("Mermaid (class level)", graph, classCycles, "classes");
    }

    private static void printMermaid(String title, DependencyGraph graph,
                                     List<List<String>> cycles, String unit) {
        System.out.println();
        System.out.println(title + ":");
        System.out.println("-".repeat(50));
        System.out.print(MermaidRenderer.renderCycles(graph, cycles, unit));
    }

    private static void printCycles(String title, List<List<String>> cycles, String unit) {
        System.out.println();
        System.out.println(title + " (" + cycles.size() + " SCC):");
        System.out.println("-".repeat(50));
        if (cycles.isEmpty()) {
            System.out.println("(none)");
            return;
        }
        for (int i = 0; i < cycles.size(); i++) {
            List<String> scc = cycles.get(i);
            System.out.println("[" + (i + 1) + "] " + scc.size() + " " + unit + ": " + scc);
        }
    }
}
