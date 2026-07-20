package com.minsun.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
