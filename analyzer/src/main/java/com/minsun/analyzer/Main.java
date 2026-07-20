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

        List<List<String>> cycles = TarjanScc.cycles(graph);
        System.out.println();
        System.out.println("Circular dependencies (" + cycles.size() + " SCC):");
        System.out.println("-".repeat(50));
        if (cycles.isEmpty()) {
            System.out.println("(none)");
        } else {
            for (int i = 0; i < cycles.size(); i++) {
                List<String> scc = cycles.get(i);
                System.out.println("[" + (i + 1) + "] " + scc.size() + " classes: " + scc);
            }
        }
    }
}
