package com.minsun.analyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EdgeExtractor: 필드 타입 기반 내부 의존 간선 추출")
class EdgeExtractorTest {

    private static final String BASE_PACKAGE = "com.minsun.sample";

    @TempDir
    Path sourceRoot;

    private TreeSet<String> extract() throws IOException {
        return new EdgeExtractor(sourceRoot, BASE_PACKAGE).extract();
    }

    /** 패키지 선언에 맞는 디렉토리에 .java 파일을 심는다 (JavaParserTypeSolver 가 경로로 타입을 해석하므로 필수). */
    private void writeClass(String pkg, String simpleName, String body) throws IOException {
        Path dir = sourceRoot.resolve(pkg.replace('.', '/'));
        Files.createDirectories(dir);
        String source = "package " + pkg + ";\n\n" + body;
        Files.writeString(dir.resolve(simpleName + ".java"), source);
    }

    @Test
    @DisplayName("필드 주입(private final XxxRepository) 을 간선으로 잡는다")
    void extractsFieldInjectionEdge() throws IOException {
        writeClass("com.minsun.sample.user", "UserRepository",
            "public class UserRepository {}");
        writeClass("com.minsun.sample.user", "UserService",
            "public class UserService {\n" +
            "    private final UserRepository userRepository = null;\n" +
            "}");

        TreeSet<String> edges = extract();

        assertTrue(edges.contains(
            "com.minsun.sample.user.UserService -> com.minsun.sample.user.UserRepository"),
            () -> "필드 주입 간선이 있어야 함. 실제: " + edges);
    }

    @Test
    @DisplayName("생성자 주입 파라미터(Lombok 미사용 표준 패턴)를 간선으로 잡는다")
    void extractsConstructorInjectionEdge() throws IOException {
        writeClass("com.minsun.sample.order", "OrderRepository",
            "public class OrderRepository {}");
        writeClass("com.minsun.sample.order", "OrderService",
            "public class OrderService {\n" +
            "    private final OrderRepository repository;\n" +
            "    public OrderService(OrderRepository repository) {\n" +
            "        this.repository = repository;\n" +
            "    }\n" +
            "}");

        TreeSet<String> edges = extract();

        assertTrue(edges.contains(
            "com.minsun.sample.order.OrderService -> com.minsun.sample.order.OrderRepository"),
            () -> "생성자 주입 간선이 있어야 함. 실제: " + edges);
    }

    @Test
    @DisplayName("상속(extends) 을 간선으로 잡는다")
    void extractsExtendsEdge() throws IOException {
        writeClass("com.minsun.sample.shared", "BaseService",
            "public class BaseService {}");
        writeClass("com.minsun.sample.user", "UserService",
            "import com.minsun.sample.shared.BaseService;\n\n" +
            "public class UserService extends BaseService {}");

        TreeSet<String> edges = extract();

        assertTrue(edges.contains(
            "com.minsun.sample.user.UserService -> com.minsun.sample.shared.BaseService"),
            () -> "상속 간선이 있어야 함. 실제: " + edges);
    }

    @Test
    @DisplayName("구현(implements) 을 간선으로 잡는다")
    void extractsImplementsEdge() throws IOException {
        writeClass("com.minsun.sample.shared", "Notifier",
            "public interface Notifier {}");
        writeClass("com.minsun.sample.user", "EmailNotifier",
            "import com.minsun.sample.shared.Notifier;\n\n" +
            "public class EmailNotifier implements Notifier {}");

        TreeSet<String> edges = extract();

        assertTrue(edges.contains(
            "com.minsun.sample.user.EmailNotifier -> com.minsun.sample.shared.Notifier"),
            () -> "구현 간선이 있어야 함. 실제: " + edges);
    }

    @Test
    @DisplayName("BASE_PACKAGE 밖의 외부 타입(java.util.List 등)은 간선에서 제외한다")
    void excludesExternalTypes() throws IOException {
        writeClass("com.minsun.sample.user", "UserService",
            "import java.util.List;\n\n" +
            "public class UserService {\n" +
            "    private final List<String> names = null;\n" +
            "    private final String label = null;\n" +
            "}");

        TreeSet<String> edges = extract();

        assertTrue(edges.isEmpty(), () -> "외부 타입만 있으면 간선이 없어야 함. 실제: " + edges);
    }

    @Test
    @DisplayName("primitive 필드는 간선에서 제외한다")
    void excludesPrimitiveFields() throws IOException {
        writeClass("com.minsun.sample.order", "Order",
            "public class Order {\n" +
            "    private int quantity;\n" +
            "    private long id;\n" +
            "    private boolean paid;\n" +
            "}");

        assertTrue(extract().isEmpty());
    }

    @Test
    @DisplayName("자기 자신을 참조하는 필드는 간선에서 제외한다")
    void excludesSelfReference() throws IOException {
        writeClass("com.minsun.sample.order", "Order",
            "public class Order {\n" +
            "    private Order next = null;\n" +
            "}");

        assertFalse(extract().stream().anyMatch(e -> e.contains("Order -> com.minsun.sample.order.Order")));
    }

    @Test
    @DisplayName("같은 타입을 여러 필드로 참조해도 간선은 하나로 중복 제거된다")
    void deduplicatesRepeatedTargets() throws IOException {
        writeClass("com.minsun.sample.product", "PriceCalculator",
            "public class PriceCalculator {}");
        writeClass("com.minsun.sample.product", "ProductService",
            "public class ProductService {\n" +
            "    private final PriceCalculator base = null;\n" +
            "    private final PriceCalculator discount = null;\n" +
            "}");

        TreeSet<String> edges = extract();

        long count = edges.stream()
            .filter(e -> e.equals(
                "com.minsun.sample.product.ProductService -> com.minsun.sample.product.PriceCalculator"))
            .count();
        assertEquals(1, count, () -> "중복 간선은 1개여야 함. 실제: " + edges);
    }

    @Test
    @DisplayName("순환 참조(A -> B, B -> A) 는 양방향 간선을 모두 잡는다")
    void extractsCyclicDependency() throws IOException {
        writeClass("com.minsun.sample.order", "OrderService",
            "public class OrderService {\n" +
            "    private final ProductService productService = null;\n" +
            "}");
        writeClass("com.minsun.sample.order", "ProductService",
            "public class ProductService {\n" +
            "    private final OrderService orderService = null;\n" +
            "}");

        TreeSet<String> edges = extract();

        assertTrue(edges.contains(
            "com.minsun.sample.order.OrderService -> com.minsun.sample.order.ProductService"), edges::toString);
        assertTrue(edges.contains(
            "com.minsun.sample.order.ProductService -> com.minsun.sample.order.OrderService"), edges::toString);
    }

    @Test
    @DisplayName("소스가 없으면 빈 결과를 반환한다")
    void returnsEmptyForNoSources() throws IOException {
        assertTrue(extract().isEmpty());
    }
}
