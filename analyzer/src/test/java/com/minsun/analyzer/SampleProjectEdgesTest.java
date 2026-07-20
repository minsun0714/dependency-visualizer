package com.minsun.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 실제 sample-project 소스를 대상으로 한 통합 테스트.
 * sample-project 는 순환 참조 분석용으로 의도적으로 설계된 픽스처이므로,
 * 기대 간선 집합 전체를 고정해 회귀를 잡는다.
 */
@DisplayName("sample-project 간선 추출 통합 테스트")
class SampleProjectEdgesTest {

    private static final String BASE_PACKAGE = "com.minsun.sample";

    /**
     * 샘플이 만들어내야 하는 내부 의존 간선 전체.
     * 필드/생성자/세터 주입 + 상속/구현 + 메서드 시그니처(파라미터·반환) + new + 제네릭 인자까지 포함.
     */
    private static final Set<String> EXPECTED_EDGES = Set.of(
        // --- order ---
        "com.minsun.sample.order.OrderController -> com.minsun.sample.order.Order",           // 반환 타입
        "com.minsun.sample.order.OrderController -> com.minsun.sample.order.OrderService",     // 필드 주입
        "com.minsun.sample.order.OrderController -> com.minsun.sample.shared.BaseController",   // 상속
        "com.minsun.sample.order.OrderRepository -> com.minsun.sample.order.Order",            // 메서드 시그니처
        "com.minsun.sample.order.OrderService -> com.minsun.sample.order.Order",               // 반환 타입 + new Order
        "com.minsun.sample.order.OrderService -> com.minsun.sample.order.OrderRepository",     // 필드 주입
        "com.minsun.sample.order.OrderService -> com.minsun.sample.product.ProductService",    // 필드 주입 (cross-domain)
        "com.minsun.sample.order.OrderService -> com.minsun.sample.shared.PriceCalculator",    // 필드 주입
        "com.minsun.sample.order.OrderService -> com.minsun.sample.user.UserService",          // 필드 주입 (SCC A)
        // --- product ---
        "com.minsun.sample.product.InventoryService -> com.minsun.sample.product.ProductRepository",
        "com.minsun.sample.product.InventoryService -> com.minsun.sample.product.ProductService", // (SCC B)
        "com.minsun.sample.product.ProductController -> com.minsun.sample.product.Product",         // 반환 타입
        "com.minsun.sample.product.ProductController -> com.minsun.sample.product.ProductService",
        "com.minsun.sample.product.ProductController -> com.minsun.sample.shared.BaseController",   // 상속
        "com.minsun.sample.product.ProductRepository -> com.minsun.sample.product.Product",         // 메서드 시그니처
        "com.minsun.sample.product.ProductService -> com.minsun.sample.product.InventoryService",   // (SCC B)
        "com.minsun.sample.product.ProductService -> com.minsun.sample.product.Product",            // 반환 타입
        "com.minsun.sample.product.ProductService -> com.minsun.sample.product.ProductRepository",
        "com.minsun.sample.product.ProductService -> com.minsun.sample.shared.PriceCalculator",
        // --- shared ---
        "com.minsun.sample.shared.EmailSender -> com.minsun.sample.shared.Notifier",                // 구현
        // --- user ---
        "com.minsun.sample.user.NotificationService -> com.minsun.sample.shared.Notifier",          // 생성자 주입
        "com.minsun.sample.user.NotificationService -> com.minsun.sample.user.UserRepository",       // 생성자 주입
        "com.minsun.sample.user.UserController -> com.minsun.sample.shared.BaseController",          // 상속
        "com.minsun.sample.user.UserController -> com.minsun.sample.user.User",                      // 반환 타입
        "com.minsun.sample.user.UserController -> com.minsun.sample.user.UserService",
        "com.minsun.sample.user.UserRepository -> com.minsun.sample.user.User",                      // 메서드 시그니처
        "com.minsun.sample.user.UserService -> com.minsun.sample.order.OrderService",                // 필드 주입 (SCC A)
        "com.minsun.sample.user.UserService -> com.minsun.sample.shared.EmailSender",
        "com.minsun.sample.user.UserService -> com.minsun.sample.user.User",                         // 반환 타입
        "com.minsun.sample.user.UserService -> com.minsun.sample.user.UserRepository"
    );

    @Test
    @DisplayName("핵심 도메인 간선이 모두 존재한다")
    void containsCoreDomainEdges() throws IOException {
        TreeSet<String> edges = new EdgeExtractor(sampleSrc(), BASE_PACKAGE).extract().toEdgeStrings();

        // cycles.* 쇼케이스가 늘어나며 전체 간선은 커지므로 정확 일치 대신 "핵심 간선 포함"으로 검증.
        // 추출 메커니즘 자체는 EdgeExtractorTest 가 합성 케이스로 정밀 검증한다.
        assertTrue(edges.containsAll(EXPECTED_EDGES),
            () -> "빠진 핵심 간선: " + minus(EXPECTED_EDGES, edges));
    }

    private static Set<String> minus(Set<String> expected, Set<String> actual) {
        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(actual);
        return missing;
    }

    @Test
    @DisplayName("생성자 주입 / 상속 / 구현 간선이 각각 잡힌다")
    void extractsNonFieldEdges() throws IOException {
        TreeSet<String> edges = new EdgeExtractor(sampleSrc(), BASE_PACKAGE).extract().toEdgeStrings();

        // 생성자 주입 (NotificationService 는 필드 주입이 아니라 명시적 생성자로만 의존을 표현)
        assertTrue(edges.contains(
            "com.minsun.sample.user.NotificationService -> com.minsun.sample.shared.Notifier"),
            () -> "생성자 주입 간선 누락. 실제: " + edges);
        // 상속
        assertTrue(edges.contains(
            "com.minsun.sample.user.UserController -> com.minsun.sample.shared.BaseController"),
            () -> "상속 간선 누락. 실제: " + edges);
        // 구현
        assertTrue(edges.contains(
            "com.minsun.sample.shared.EmailSender -> com.minsun.sample.shared.Notifier"),
            () -> "구현 간선 누락. 실제: " + edges);
    }

    @Test
    @DisplayName("샘플의 설계된 순환 SCC 를 모두 정확히 검출한다 (end-to-end)")
    void detectsDesignedCycles() throws IOException {
        DependencyGraph graph = new EdgeExtractor(sampleSrc(), BASE_PACKAGE).extract();

        List<List<String>> cycles = TarjanScc.cycles(graph);

        // cycles.* 쇼케이스 시나리오 10개(각 독립 SCC) + 도메인 순환 2개.
        // SCC 목록은 대표(첫) 노드 FQN 기준 정렬 → cycles.* 가 order/product 보다 앞선다.
        String c = "com.minsun.sample.cycles.";
        List<List<String>> expected = List.of(
            List.of(c + "ctor.CtorA", c + "ctor.CtorB"),
            List.of(c + "figure8.F8A", c + "figure8.F8B", c + "figure8.F8Center"),
            List.of(c + "fournode.Ring4A", c + "fournode.Ring4B", c + "fournode.Ring4C", c + "fournode.Ring4D"),
            List.of(c + "generic.GenA", c + "generic.GenB"),
            List.of(c + "inheritance.InhA", c + "inheritance.InhB"),
            List.of(c + "methodparam.MpA", c + "methodparam.MpB"),
            List.of(c + "newexpr.NewA", c + "newexpr.NewB"),
            List.of(c + "tangled.TangA", c + "tangled.TangB", c + "tangled.TangC"),
            List.of(c + "threenode.RingA", c + "threenode.RingB", c + "threenode.RingC"),
            List.of(c + "twonode.DirectA", c + "twonode.DirectB"),
            List.of("com.minsun.sample.order.OrderService", "com.minsun.sample.user.UserService"),
            List.of("com.minsun.sample.product.InventoryService", "com.minsun.sample.product.ProductService")
        );
        assertEquals(expected, cycles, () -> "검출된 순환: " + cycles);
    }

    /**
     * sample-project 소스 루트를 찾는다.
     * 테스트 작업 디렉토리는 실행 위치(analyzer 모듈 or 루트)에 따라 달라질 수 있어 후보를 순회한다.
     */
    private static Path sampleSrc() {
        for (Path candidate : List.of(
                Path.of("sample-project", "src", "main", "java"),
                Path.of("..", "sample-project", "src", "main", "java"))) {
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
            "sample-project 소스를 찾지 못함 (cwd=" + Path.of("").toAbsolutePath() + ")");
    }
}
