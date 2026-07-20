package com.minsun.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DependencyGraph: 방향 의존 그래프 모델")
class DependencyGraphTest {

    @Test
    @DisplayName("간선을 추가하면 from/to 두 노드가 모두 등록된다 (리프 노드 포함)")
    void addEdgeRegistersBothNodes() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");

        assertEquals(Set.of("A", "B"), graph.nodes());
        assertEquals(Set.of("B"), graph.successors("A"));
        assertTrue(graph.successors("B").isEmpty(), "리프 노드 B 는 나가는 간선이 없다");
    }

    @Test
    @DisplayName("중복 간선은 한 번만 저장된다")
    void deduplicatesEdges() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("A", "B");

        assertEquals(1, graph.edgeCount());
        assertEquals(Set.of("B"), graph.successors("A"));
    }

    @Test
    @DisplayName("nodeCount / edgeCount 를 센다")
    void countsNodesAndEdges() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("A", "C");

        assertEquals(3, graph.nodeCount()); // A, B, C
        assertEquals(3, graph.edgeCount()); // A->B, B->C, A->C
    }

    @Test
    @DisplayName("등록되지 않은 노드의 successors 는 빈 집합이다")
    void successorsOfUnknownNodeIsEmpty() {
        assertTrue(new DependencyGraph().successors("nope").isEmpty());
    }

    @Test
    @DisplayName("toEdgeStrings 는 정렬된 \"from -> to\" 형식을 돌려준다")
    void toEdgeStringsIsSortedAndFormatted() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("B", "C");
        graph.addEdge("A", "B");

        assertEquals(List.of("A -> B", "B -> C"), List.copyOf(graph.toEdgeStrings()));
    }
}
