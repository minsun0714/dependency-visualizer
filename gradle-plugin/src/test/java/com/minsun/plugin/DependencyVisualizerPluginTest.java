package com.minsun.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DependencyVisualizerPlugin: apply 배선")
class DependencyVisualizerPluginTest {

    @Test
    @DisplayName("적용하면 extension 과 태스크가 등록된다")
    void registersExtensionAndTask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(DependencyVisualizerPlugin.class);

        assertNotNull(
            project.getExtensions().findByName("dependencyVisualizer"),
            "dependencyVisualizer extension 이 등록돼야 한다");

        Task task = project.getTasks().findByName("visualizeDependencies");
        assertNotNull(task, "visualizeDependencies 태스크가 등록돼야 한다");
        assertTrue(task instanceof VisualizeDependenciesTask);
    }

    @Test
    @DisplayName("java 플러그인이 있으면 sourceRoot 기본값이 main sourceSet 으로 잡힌다")
    void defaultsSourceRootToMainSourceSet() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply(DependencyVisualizerPlugin.class);

        VisualizeDependenciesTask task = (VisualizeDependenciesTask)
            project.getTasks().getByName("visualizeDependencies");

        assertTrue(
            task.getSourceRoot().get().getAsFile().getPath().replace('\\', '/').endsWith("src/main/java"),
            "sourceRoot 기본값은 src/main/java 여야 한다");
    }
}
