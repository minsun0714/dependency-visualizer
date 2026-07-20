package com.minsun.analyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 데모용 CLI 드라이버. 실제 파이프라인은 {@link CycleAnalysis} 가 담당하고,
 * 여기서는 그 결과를 콘솔에 보기 좋게 찍기만 한다.
 */
public class Main {

    // 분석 대상 소스 루트 (일단 하드코딩 — Phase 7 플러그인화에서 파라미터화 예정)
    private static final Path SAMPLE_SRC = Path.of("sample-project", "src", "main", "java");

    // 이 prefix 로 시작하는 타입만 내부 의존으로 채택 (JDK/Spring/Lombok 등 외부 타입 제외)
    private static final String BASE_PACKAGE = "com.minsun.sample";

    public static void main(String[] args) throws IOException {
        System.out.println("Analyzing " + SAMPLE_SRC + " ...");
        System.out.println();

        CycleAnalysis analysis = CycleAnalysis.run(SAMPLE_SRC, BASE_PACKAGE);

        DependencyGraph graph = analysis.classGraph();
        System.out.println("Nodes: " + graph.nodeCount() + ", Edges: " + graph.edgeCount());
        System.out.println("-".repeat(50));
        graph.toEdgeStrings().forEach(System.out::println);

        printCycles("Class-level circular dependencies", analysis.classCycles(), "classes");
        printCycles("Package-level circular dependencies", analysis.packageCycles(), "packages");

        printMermaid("Mermaid (package level)", analysis.packageMermaid());
        printMermaid("Mermaid (class level)", analysis.classMermaid());
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

    private static void printMermaid(String title, String mermaid) {
        System.out.println();
        System.out.println(title + ":");
        System.out.println("-".repeat(50));
        System.out.print(mermaid);
    }
}
