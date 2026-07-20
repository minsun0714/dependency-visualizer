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

    /** 샘플이 만들어내야 하는 내부 의존 간선 전체 (필드 주입 + 생성자 주입 + 상속/구현). */
    private static final Set<String> EXPECTED_EDGES = Set.of(
        // --- 필드 주입 (Lombok @RequiredArgsConstructor) ---
        "com.minsun.sample.order.OrderController -> com.minsun.sample.order.OrderService",
        "com.minsun.sample.order.OrderService -> com.minsun.sample.order.OrderRepository",
        "com.minsun.sample.order.OrderService -> com.minsun.sample.product.ProductService",
        "com.minsun.sample.order.OrderService -> com.minsun.sample.shared.PriceCalculator",
        "com.minsun.sample.order.OrderService -> com.minsun.sample.user.UserService",
        "com.minsun.sample.product.InventoryService -> com.minsun.sample.product.ProductRepository",
        "com.minsun.sample.product.InventoryService -> com.minsun.sample.product.ProductService",
        "com.minsun.sample.product.ProductController -> com.minsun.sample.product.ProductService",
        "com.minsun.sample.product.ProductService -> com.minsun.sample.product.InventoryService",
        "com.minsun.sample.product.ProductService -> com.minsun.sample.product.ProductRepository",
        "com.minsun.sample.product.ProductService -> com.minsun.sample.shared.PriceCalculator",
        "com.minsun.sample.user.UserController -> com.minsun.sample.user.UserService",
        "com.minsun.sample.user.UserService -> com.minsun.sample.order.OrderService",
        "com.minsun.sample.user.UserService -> com.minsun.sample.shared.EmailSender",
        "com.minsun.sample.user.UserService -> com.minsun.sample.user.UserRepository",
        // --- 생성자 주입 (Lombok 미사용, NotificationService) ---
        "com.minsun.sample.user.NotificationService -> com.minsun.sample.shared.Notifier",
        "com.minsun.sample.user.NotificationService -> com.minsun.sample.user.UserRepository",
        // --- 상속 (extends BaseController) ---
        "com.minsun.sample.order.OrderController -> com.minsun.sample.shared.BaseController",
        "com.minsun.sample.product.ProductController -> com.minsun.sample.shared.BaseController",
        "com.minsun.sample.user.UserController -> com.minsun.sample.shared.BaseController",
        // --- 구현 (EmailSender implements Notifier) ---
        "com.minsun.sample.shared.EmailSender -> com.minsun.sample.shared.Notifier"
    );

    @Test
    @DisplayName("샘플 전체 간선이 기대 집합과 정확히 일치한다")
    void extractsExactlyExpectedEdges() throws IOException {
        TreeSet<String> edges = new EdgeExtractor(sampleSrc(), BASE_PACKAGE).extract();

        assertEquals(EXPECTED_EDGES, edges,
            "샘플 간선이 기대 집합과 달라짐 — 샘플 수정 시 EXPECTED_EDGES 도 갱신 필요");
    }

    @Test
    @DisplayName("생성자 주입 / 상속 / 구현 간선이 각각 잡힌다")
    void extractsNonFieldEdges() throws IOException {
        TreeSet<String> edges = new EdgeExtractor(sampleSrc(), BASE_PACKAGE).extract();

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
