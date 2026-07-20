package com.minsun.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * 소스 루트를 훑어 내부 의존 간선(from -> to)을 뽑아낸다.
 * 필드 타입을 resolve 해서 {@code basePackage} 로 시작하는 내부 타입만 채택한다.
 *
 * <p>전역 상태({@code StaticJavaParser}) 대신 인스턴스별 {@link JavaParser} 를 써서
 * 소스 루트마다 독립적으로 분석 가능하도록 했다 (테스트 격리 목적).
 */
public class EdgeExtractor {

    private final Path sourceRoot;
    private final String basePackage;
    private final JavaParser parser;

    public EdgeExtractor(Path sourceRoot, String basePackage) {
        this.sourceRoot = sourceRoot;
        this.basePackage = basePackage;
        this.parser = createParser(sourceRoot);
    }

    /**
     * 소스 루트 전체를 분석해 "from -> to" 간선을 정렬된 집합으로 반환한다.
     * (중복 제거 + 안정적 출력 순서)
     */
    public TreeSet<String> extract() throws IOException {
        TreeSet<String> edges = new TreeSet<>();
        for (Path file : collectJavaFiles()) {
            CompilationUnit cu = parser.parse(file).getResult().orElseThrow(
                () -> new IllegalStateException("파싱 실패: " + file));
            for (ClassOrInterfaceDeclaration type : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                extractEdges(type, edges);
            }
        }
        return edges;
    }

    /**
     * 한 클래스의 의존 타입들을 resolve 해서 내부 의존 간선(from -> to)을 뽑는다.
     * 아래 경로를 모두 의존으로 본다:
     * <ul>
     *   <li>필드 주입 — {@code private final XxxService} (Lombok @RequiredArgsConstructor 포함)</li>
     *   <li>생성자 주입 — {@code XxxService(YyyRepository repo)} 파라미터 (Lombok 안 쓰는 표준 패턴)</li>
     *   <li>메서드 시그니처 — 파라미터 타입(세터 주입 포함) + 반환 타입</li>
     *   <li>상속/구현 — {@code extends}, {@code implements}</li>
     *   <li>객체 생성 — {@code new Xxx()}</li>
     * </ul>
     * 위 모든 타입의 <b>제네릭 타입 인자</b>({@code List<Order>} 의 Order 등)도 재귀적으로 본다.
     */
    void extractEdges(ClassOrInterfaceDeclaration type, TreeSet<String> edges) {
        String source = type.getFullyQualifiedName().orElse(type.getNameAsString());

        // 1. 필드 주입
        for (FieldDeclaration field : type.getFields()) {
            tryAddEdge(source, field.getElementType(), edges);
        }

        // 2. 생성자 주입 파라미터
        for (ConstructorDeclaration ctor : type.getConstructors()) {
            for (Parameter param : ctor.getParameters()) {
                tryAddEdge(source, param.getType(), edges);
            }
        }

        // 3. 메서드 시그니처 (파라미터 + 반환 타입). void/primitive 반환은 resolve 단계에서 걸러진다.
        for (MethodDeclaration method : type.getMethods()) {
            tryAddEdge(source, method.getType(), edges);
            for (Parameter param : method.getParameters()) {
                tryAddEdge(source, param.getType(), edges);
            }
        }

        // 4. 상속 / 구현
        for (ClassOrInterfaceType extended : type.getExtendedTypes()) {
            tryAddEdge(source, extended, edges);
        }
        for (ClassOrInterfaceType implemented : type.getImplementedTypes()) {
            tryAddEdge(source, implemented, edges);
        }

        // 5. 객체 생성 (new Xxx()) — 메서드 본문 등에서 직접 생성하는 의존
        for (ObjectCreationExpr creation : type.findAll(ObjectCreationExpr.class)) {
            tryAddEdge(source, creation.getType(), edges);
        }
    }

    /**
     * 타입 노드를 resolve 해서 내부 타입이면 간선으로 추가한다.
     * resolve 실패(외부 타입 등)는 조용히 건너뛴다.
     */
    private void tryAddEdge(String source, Type typeNode, TreeSet<String> edges) {
        try {
            addResolved(source, typeNode.resolve(), edges);
        } catch (RuntimeException e) {
            // 사전(TypeSolver)에 없는 타입 등 resolve 실패 → 내부 타입 아님, 무시
        }
    }

    /**
     * resolve 된 타입을 훑어 내부 참조 타입이면 간선으로 추가한다.
     * 제네릭 타입 인자({@code Map<Long, Product>} 의 Product 등)도 재귀적으로 본다.
     * 자기 자신 참조는 제외한다.
     */
    private void addResolved(String source, ResolvedType resolved, TreeSet<String> edges) {
        if (!resolved.isReferenceType()) {
            return; // primitive / void / 타입변수 등은 의존 대상이 아님
        }
        var ref = resolved.asReferenceType();

        String target = ref.getQualifiedName();
        if (target.startsWith(basePackage) && !target.equals(source)) {
            edges.add(source + " -> " + target);
        }

        // 제네릭 타입 인자 재귀 (List<Order>, Optional<User>, Map<Long, Product> ...)
        for (ResolvedType typeArg : ref.typeParametersValues()) {
            addResolved(source, typeArg, edges);
        }
    }

    /**
     * SymbolSolver 셋업: 이름 -> 실제 클래스(FQN) 해석에 참고할 "사전"들을 등록한다.
     * - ReflectionTypeSolver : JDK 표준 타입 (java.util.* 등)
     * - JavaParserTypeSolver : 분석 대상 소스 (basePackage.*)
     * 외부 라이브러리(Spring/Lombok) jar 는 순환 분석에 불필요하므로 등록하지 않는다.
     */
    private static JavaParser createParser(Path sourceRoot) {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(sourceRoot));

        ParserConfiguration config = new ParserConfiguration()
            .setSymbolResolver(new JavaSymbolSolver(typeSolver));
        return new JavaParser(config);
    }

    private List<Path> collectJavaFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths
                .filter(p -> p.toString().endsWith(".java"))
                .sorted()
                .toList();
        }
    }
}
