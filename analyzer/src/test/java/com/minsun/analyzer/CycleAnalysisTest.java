package com.minsun.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CycleAnalysis: 파이프라인 facade")
class CycleAnalysisTest {

    @Test
    @DisplayName("패키지 경계를 넘는 순환은 패키지 레벨 1개, 클래스 레벨 1개로 잡힌다")
    void detectsCrossPackageCycle(@org.junit.jupiter.api.io.TempDir Path root) throws IOException {
        // com.ex.a.A -> com.ex.b.B -> com.ex.a.A (패키지 a <-> b 순환)
        writeClass(root, "com/ex/a/A.java", """
            package com.ex.a;
            import com.ex.b.B;
            public class A { private B b; }
            """);
        writeClass(root, "com/ex/b/B.java", """
            package com.ex.b;
            import com.ex.a.A;
            public class B { private A a; }
            """);

        CycleAnalysis analysis = CycleAnalysis.run(root, "com.ex");

        assertEquals(1, analysis.classCycles().size(), "클래스 레벨 순환 1개");
        assertEquals(1, analysis.packageCycles().size(), "패키지 레벨 순환 1개");
        assertEquals("com.ex.a", analysis.packageCycles().get(0).get(0));
        assertTrue(analysis.packageMermaid().startsWith("flowchart LR"));
    }

    @Test
    @DisplayName("같은 패키지 안 순환은 클래스 레벨엔 잡히지만 패키지 레벨에선 사라진다")
    void intraPackageCycleOnlyAtClassLevel(@org.junit.jupiter.api.io.TempDir Path root) throws IOException {
        writeClass(root, "com/ex/a/A.java", """
            package com.ex.a;
            public class A { private B b; }
            """);
        writeClass(root, "com/ex/a/B.java", """
            package com.ex.a;
            public class B { private A a; }
            """);

        CycleAnalysis analysis = CycleAnalysis.run(root, "com.ex");

        assertEquals(1, analysis.classCycles().size(), "클래스 레벨 순환 1개");
        assertTrue(analysis.packageCycles().isEmpty(), "패키지 레벨엔 순환 없음");
    }

    private static void writeClass(Path root, String relPath, String source) throws IOException {
        Path file = root.resolve(relPath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source);
    }
}
