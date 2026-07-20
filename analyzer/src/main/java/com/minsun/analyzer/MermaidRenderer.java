package com.minsun.analyzer;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 검출된 순환(SCC)들을 Mermaid {@code flowchart} 텍스트로 렌더링한다.
 *
 * <p>각 SCC 를 {@code subgraph} 블록으로 묶고, 그 안의 내부 의존 간선만 그려
 * "이 덩어리가 어떻게 얽혀 순환을 이루는지"를 눈으로 확인할 수 있게 한다.
 * 출력은 결정적(정렬된 노드/간선)이라 스냅샷 테스트가 가능하다.
 */
public final class MermaidRenderer {

    private MermaidRenderer() {
    }

    /**
     * 순환 SCC 목록을 Mermaid flowchart 문자열로 만든다 (노드 단위는 {@code "classes"}).
     * {@code cycles} 는 {@link TarjanScc#cycles(DependencyGraph)} 의 결과를 기대한다.
     */
    public static String renderCycles(DependencyGraph graph, List<List<String>> cycles) {
        return renderCycles(graph, cycles, "classes");
    }

    /**
     * 순환 SCC 목록을 Mermaid flowchart 문자열로 만든다.
     * {@code unit} 은 subgraph 라벨에 쓰일 노드 단위 (예: {@code "classes"}, {@code "packages"}).
     */
    public static String renderCycles(DependencyGraph graph, List<List<String>> cycles, String unit) {
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart LR\n");

        if (cycles.isEmpty()) {
            sb.append("  %% 순환 없음\n");
            return sb.toString();
        }

        // 라벨 중복 방지: 렌더 대상 모든 노드의 공통 패키지 접두어를 떼어 상대 경로를 라벨로 쓴다.
        // (예: com.acme.a.service / com.acme.b.service → "a.service" / "b.service" 로 구분)
        Set<String> allNodes = new LinkedHashSet<>();
        for (List<String> scc : cycles) {
            allNodes.addAll(scc);
        }
        String commonPrefix = BasePackageInferrer.commonPackagePrefix(allNodes);

        for (int i = 0; i < cycles.size(); i++) {
            List<String> scc = cycles.get(i);
            Set<String> members = new HashSet<>(scc);

            sb.append("  subgraph scc").append(i)
                .append(" [\"Cycle ").append(i + 1).append(" - ").append(scc.size())
                .append(" ").append(unit).append("\"]\n");

            // 노드 선언 (SCC 정렬 순서)
            for (String node : scc) {
                sb.append("    ").append(nodeId(node))
                    .append("[\"").append(relativeLabel(node, commonPrefix)).append("\"]\n");
            }
            // SCC 내부 간선만 (from 정렬 순서 → successors 정렬 순서)
            for (String from : scc) {
                for (String to : graph.successors(from)) {
                    if (members.contains(to)) {
                        sb.append("    ").append(nodeId(from))
                            .append(" --> ").append(nodeId(to)).append("\n");
                    }
                }
            }
            sb.append("  end\n");
        }
        return sb.toString();
    }

    /** FQN 을 Mermaid 노드 id 로 안전하게 변환 (영숫자 외는 '_'). */
    private static String nodeId(String fqn) {
        return "n_" + fqn.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * 공통 패키지 접두어를 뗀 상대 경로 라벨.
     * 공통 접두어가 없으면 전체 이름을, 뗄 것이 없으면(노드 == 접두어) 단순명으로 폴백한다.
     */
    private static String relativeLabel(String fqn, String commonPrefix) {
        if (!commonPrefix.isEmpty() && fqn.startsWith(commonPrefix + ".")) {
            String rel = fqn.substring(commonPrefix.length() + 1);
            if (!rel.isEmpty()) {
                return rel;
            }
        }
        return commonPrefix.isEmpty() ? fqn : simpleName(fqn);
    }

    /** FQN 에서 단순 클래스명만 추출 (라벨용 폴백). */
    private static String simpleName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot < 0 ? fqn : fqn.substring(lastDot + 1);
    }
}
