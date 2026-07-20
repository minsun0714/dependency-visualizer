package com.minsun.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TarjanScc: 강결합요소(SCC) 기반 순환 검출")
class TarjanSccTest {

    @Test
    @DisplayName("양방향(A<->B)은 하나의 SCC(순환)로 묶인다")
    void twoNodeCycle() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        assertEquals(List.of(List.of("A", "B")), TarjanScc.cycles(graph));
    }

    @Test
    @DisplayName("비순환 경로(A->B->C)는 순환이 없다")
    void acyclicHasNoCycles() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        assertTrue(TarjanScc.cycles(graph).isEmpty());
        // SCC 자체는 노드마다 하나씩 (싱글턴 3개)
        assertEquals(3, TarjanScc.stronglyConnectedComponents(graph).size());
    }

    @Test
    @DisplayName("3노드 순환(A->B->C->A)은 하나의 SCC로 묶인다")
    void threeNodeCycle() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        assertEquals(List.of(List.of("A", "B", "C")), TarjanScc.cycles(graph));
    }

    @Test
    @DisplayName("독립된 두 순환은 각각의 SCC로 잡힌다")
    void twoSeparateCycles() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");
        graph.addEdge("X", "Y");
        graph.addEdge("Y", "X");
        // 두 덩어리를 잇는 단방향 간선(순환 아님)
        graph.addEdge("B", "X");

        assertEquals(List.of(List.of("A", "B"), List.of("X", "Y")), TarjanScc.cycles(graph));
    }

    @Test
    @DisplayName("self-loop(A->A)은 크기 1이어도 순환으로 잡는다")
    void selfLoopIsCycle() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "A");

        assertEquals(List.of(List.of("A")), TarjanScc.cycles(graph));
    }

    @Test
    @DisplayName("큰 SCC 안에 여러 순환이 얽혀도 하나의 덩어리로 묶는다 (조합 폭발 회피)")
    void tangledClusterIsSingleScc() {
        DependencyGraph graph = new DependencyGraph();
        // A,B,C,D 가 서로 얽힘
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");
        graph.addEdge("C", "D");
        graph.addEdge("D", "B");

        List<List<String>> cycles = TarjanScc.cycles(graph);
        assertEquals(List.of(List.of("A", "B", "C", "D")), cycles);
    }
}
