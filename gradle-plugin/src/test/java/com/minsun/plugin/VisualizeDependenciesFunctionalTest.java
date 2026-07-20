package com.minsun.plugin;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("visualizeDependencies: 실제 빌드에서 리포트 생성")
class VisualizeDependenciesFunctionalTest {

    @Test
    @DisplayName("순환이 있는 프로젝트에 적용하면 태스크가 돌고 리포트가 생성된다")
    void generatesReportForCyclicProject(@TempDir Path projectDir) throws IOException {
        write(projectDir, "settings.gradle.kts", "rootProject.name = \"fixture\"\n");
        write(projectDir, "build.gradle.kts", """
            plugins {
                java
                id("io.github.minsun0714.dependency-visualizer")
            }
            """);
        // com.demo.a.A <-> com.demo.b.B (패키지 a <-> b 순환)
        write(projectDir, "src/main/java/com/demo/a/A.java", """
            package com.demo.a;
            import com.demo.b.B;
            public class A { private B b; }
            """);
        write(projectDir, "src/main/java/com/demo/b/B.java", """
            package com.demo.b;
            import com.demo.a.A;
            public class B { private A a; }
            """);

        BuildResult result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments("visualizeDependencies")
            .build();

        assertTrue(result.getOutput().contains("패키지 순환 1개"),
            "패키지 순환 1개가 로그에 찍혀야 한다:\n" + result.getOutput());
        assertTrue(Files.exists(projectDir.resolve("build/reports/depvis/cycles.html")),
            "cycles.html 리포트가 생성돼야 한다");
        assertTrue(Files.exists(projectDir.resolve("build/reports/depvis/cycles-package.mmd")),
            "cycles-package.mmd 가 생성돼야 한다");
    }

    private static void write(Path dir, String relPath, String content) throws IOException {
        Path file = dir.resolve(relPath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
