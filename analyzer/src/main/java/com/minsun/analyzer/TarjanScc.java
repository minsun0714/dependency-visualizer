package com.minsun.analyzer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tarjan 알고리즘으로 {@link DependencyGraph} 의 강결합요소(SCC)를 찾는다.
 *
 * <p>SCC 하나 = "서로 도달 가능한(=순환으로 얽힌) 노드 덩어리". 크기 ≥ 2 인 SCC 는
 * 그 안에서 반드시 순환이 존재한다. 이 방식은 개별 순환(elementary cycle)을 모두
 * 나열하지 않고 덩어리로 묶어 보여주므로 조합 폭발을 피한다.
 *
 * <p>참고: 재귀 DFS 구현이라 매우 깊은 그래프(수천 depth)에서는 스택 한계에 닿을 수
 * 있다. 실제 대형 프로젝트에서 문제가 되면 명시적 스택 기반으로 전환한다.
 */
public final class TarjanScc {

    private final DependencyGraph graph;

    private int index = 0;
    private final Map<String, Integer> indices = new HashMap<>();
    private final Map<String, Integer> lowlink = new HashMap<>();
    private final Deque<String> stack = new ArrayDeque<>();
    private final Set<String> onStack = new HashSet<>();
    private final List<List<String>> components = new ArrayList<>();

    private TarjanScc(DependencyGraph graph) {
        this.graph = graph;
    }

    /**
     * 그래프의 모든 SCC 를 반환한다. 각 SCC 는 노드 이름 정렬,
     * SCC 목록은 대표(첫) 노드 기준 정렬 → 결정적 출력.
     */
    public static List<List<String>> stronglyConnectedComponents(DependencyGraph graph) {
        TarjanScc tarjan = new TarjanScc(graph);
        for (String node : graph.nodes()) {
            if (!tarjan.indices.containsKey(node)) {
                tarjan.strongConnect(node);
            }
        }
        tarjan.components.forEach(Collections::sort);
        tarjan.components.sort(Comparator.comparing(component -> component.get(0)));
        return tarjan.components;
    }

    /**
     * 순환을 이루는 SCC 만 반환한다.
     * 크기 ≥ 2 이거나, 크기 1 이어도 자기 자신으로의 간선(self-loop)이 있으면 순환이다.
     */
    public static List<List<String>> cycles(DependencyGraph graph) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> component : stronglyConnectedComponents(graph)) {
            if (component.size() >= 2) {
                result.add(component);
            } else {
                String only = component.get(0);
                if (graph.successors(only).contains(only)) { // self-loop
                    result.add(component);
                }
            }
        }
        return result;
    }

    private void strongConnect(String v) {
        indices.put(v, index);
        lowlink.put(v, index);
        index++;
        stack.push(v);
        onStack.add(v);

        for (String w : graph.successors(v)) {
            if (!indices.containsKey(w)) {
                strongConnect(w);
                lowlink.put(v, Math.min(lowlink.get(v), lowlink.get(w)));
            } else if (onStack.contains(w)) {
                lowlink.put(v, Math.min(lowlink.get(v), indices.get(w)));
            }
        }

        // v 가 SCC 의 뿌리면, 스택에서 v 까지 팝해 한 덩어리로 묶는다.
        if (lowlink.get(v).equals(indices.get(v))) {
            List<String> component = new ArrayList<>();
            String w;
            do {
                w = stack.pop();
                onStack.remove(w);
                component.add(w);
            } while (!w.equals(v));
            components.add(component);
        }
    }
}
