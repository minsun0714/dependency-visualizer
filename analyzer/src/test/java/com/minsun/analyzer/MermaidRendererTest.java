package com.minsun.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MermaidRenderer: 순환 SCC → Mermaid flowchart")
class MermaidRendererTest {

    @Test
    @DisplayName("2노드 순환을 subgraph 로 렌더링한다")
    void rendersTwoNodeCycle() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        String mermaid = MermaidRenderer.renderCycles(graph, TarjanScc.cycles(graph));

        assertEquals("""
            flowchart LR
              subgraph scc0 ["Cycle 1 - 2 classes"]
                n_A["A"]
                n_B["B"]
                n_A --> n_B
                n_B --> n_A
              end
            """, mermaid);
    }

    @Test
    @DisplayName("SCC 내부 간선만 그리고, SCC 밖으로 나가는 간선은 제외한다")
    void rendersOnlyIntraSccEdges() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");
        graph.addEdge("B", "C"); // SCC 밖으로 나가는 간선 → 렌더링 제외

        String mermaid = MermaidRenderer.renderCycles(graph, TarjanScc.cycles(graph));

        assertEquals("""
            flowchart LR
              subgraph scc0 ["Cycle 1 - 2 classes"]
                n_A["A"]
                n_B["B"]
                n_A --> n_B
                n_B --> n_A
              end
            """, mermaid);
    }

    @Test
    @DisplayName("FQN 은 안전한 노드 id 로, 라벨은 단순 클래스명으로 렌더링한다")
    void usesSafeIdAndSimpleLabel() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("com.foo.A", "com.foo.B");
        graph.addEdge("com.foo.B", "com.foo.A");

        String mermaid = MermaidRenderer.renderCycles(graph, TarjanScc.cycles(graph));

        assertEquals("""
            flowchart LR
              subgraph scc0 ["Cycle 1 - 2 classes"]
                n_com_foo_A["A"]
                n_com_foo_B["B"]
                n_com_foo_A --> n_com_foo_B
                n_com_foo_B --> n_com_foo_A
              end
            """, mermaid);
    }

    @Test
    @DisplayName("끝조각이 같은 서로 다른 패키지는 상대 경로로 구분해 라벨링한다")
    void disambiguatesSameLeafPackages() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("com.x.a.service", "com.x.b.service");
        graph.addEdge("com.x.b.service", "com.x.a.service");

        String mermaid = MermaidRenderer.renderCycles(graph, TarjanScc.cycles(graph), "packages");

        // 둘 다 "service" 로 뭉개지지 않고 공통 접두어(com.x)를 뗀 상대 경로로 구분된다.
        assertTrue(mermaid.contains("[\"a.service\"]"), () -> "a.service 라벨이 있어야 한다:\n" + mermaid);
        assertTrue(mermaid.contains("[\"b.service\"]"), () -> "b.service 라벨이 있어야 한다:\n" + mermaid);
    }

    @Test
    @DisplayName("동일 끝조각 3개(exception)도 각각 상대 경로로 구분된다")
    void disambiguatesThreeSameLeafPackages() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("com.m.platform.cafe24.exception", "com.m.shared.exception");
        graph.addEdge("com.m.shared.exception", "com.m.platform.cafe24.exception");

        String mermaid = MermaidRenderer.renderCycles(graph, TarjanScc.cycles(graph), "packages");

        assertTrue(mermaid.contains("[\"platform.cafe24.exception\"]"), () -> mermaid);
        assertTrue(mermaid.contains("[\"shared.exception\"]"), () -> mermaid);
    }

    @Test
    @DisplayName("단일 패키지 self-loop 는 단순명으로 폴백한다")
    void singlePackageSelfLoopFallsBackToSimpleName() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("com.foo.bar", "com.foo.bar"); // self-loop

        String mermaid = MermaidRenderer.renderCycles(graph, TarjanScc.cycles(graph), "packages");

        // 노드가 하나뿐이라 공통 접두어가 전체와 같아짐 → 단순명 "bar" 로 폴백.
        assertTrue(mermaid.contains("[\"bar\"]"), () -> mermaid);
    }

    @Test
    @DisplayName("순환이 없으면 주석만 있는 빈 flowchart 를 낸다")
    void rendersEmptyWhenNoCycles() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B"); // 비순환

        String mermaid = MermaidRenderer.renderCycles(graph, TarjanScc.cycles(graph));

        assertEquals("""
            flowchart LR
              %% 순환 없음
            """, mermaid);
    }
}
