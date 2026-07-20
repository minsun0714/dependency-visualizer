package com.minsun.analyzer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 클래스(FQN) 간 방향 의존 그래프.
 * 노드 = 타입 FQN, 간선 {@code from -> to} = "from 이 to 에 의존한다".
 *
 * <p>순환 검출(Tarjan SCC)의 입력이 되는 인접 리스트 모델이다.
 * 결정적(deterministic) 순회를 위해 노드/이웃을 정렬 상태로 유지한다.
 */
public class DependencyGraph {

    // from -> {to...} 인접 맵. 정렬 순서 보장을 위해 TreeMap / TreeSet.
    private final Map<String, Set<String>> adjacency = new TreeMap<>();

    /**
     * 간선 {@code from -> to} 를 추가한다.
     * from, to 두 노드 모두 그래프에 등록된다(리프 노드도 노드로 잡히도록).
     * 중복 간선은 무시된다.
     */
    public void addEdge(String from, String to) {
        adjacency.computeIfAbsent(from, k -> new TreeSet<>()).add(to);
        adjacency.computeIfAbsent(to, k -> new TreeSet<>()); // to 도 노드로 등록
    }

    /** 모든 노드 (정렬됨, 불변 뷰). */
    public Set<String> nodes() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /** {@code node} 가 의존하는 노드들 (정렬됨, 불변 뷰). 등록 안 된 노드면 빈 집합. */
    public Set<String> successors(String node) {
        return Collections.unmodifiableSet(adjacency.getOrDefault(node, Set.of()));
    }

    public int nodeCount() {
        return adjacency.size();
    }

    public int edgeCount() {
        return adjacency.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * {@code "from -> to"} 형식의 정렬된 간선 문자열 집합.
     * 콘솔 출력 및 테스트 검증용 (리프 노드는 간선이 없으므로 나타나지 않는다).
     */
    public TreeSet<String> toEdgeStrings() {
        TreeSet<String> edges = new TreeSet<>();
        adjacency.forEach((from, tos) -> tos.forEach(to -> edges.add(from + " -> " + to)));
        return edges;
    }
}
