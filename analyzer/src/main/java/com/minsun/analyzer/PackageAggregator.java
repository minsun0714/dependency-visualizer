package com.minsun.analyzer;

/**
 * 클래스 레벨 의존 그래프를 패키지 레벨로 집계한다.
 *
 * <p>각 클래스 FQN 을 소속 패키지로 접고, 서로 다른 패키지 사이의 간선만 남긴다.
 * 같은 패키지 내부 간선({@code a.B -> a.C})은 "패키지 간 순환"이 아니므로 버린다.
 * 이렇게 하면 클래스 레벨에서 쏟아지던 순환 중 패키지 안에서만 얽힌 것들이 걸러지고,
 * 실제 패키지 경계를 넘나드는 순환만 남는다.
 *
 * <p>결과는 다시 {@link DependencyGraph} 라서 {@link TarjanScc}·{@link MermaidRenderer}
 * 를 그대로 재사용할 수 있다.
 */
public final class PackageAggregator {

    private PackageAggregator() {
    }

    /** 클래스 그래프를 패키지 그래프로 접는다 (패키지 간 간선만 유지). */
    public static DependencyGraph aggregate(DependencyGraph classGraph) {
        DependencyGraph packageGraph = new DependencyGraph();
        for (String from : classGraph.nodes()) {
            String fromPkg = packageOf(from);
            for (String to : classGraph.successors(from)) {
                String toPkg = packageOf(to);
                if (!fromPkg.equals(toPkg)) {
                    packageGraph.addEdge(fromPkg, toPkg);
                }
            }
        }
        return packageGraph;
    }

    /** FQN 의 소속 패키지. 최상위(패키지 없음) 타입은 {@code "(default)"}. */
    static String packageOf(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot < 0 ? "(default)" : fqn.substring(0, lastDot);
    }
}
