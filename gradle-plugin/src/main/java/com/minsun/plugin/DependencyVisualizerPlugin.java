package com.minsun.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.Set;

/**
 * dependency-visualizer Gradle 플러그인.
 *
 * <p>적용하면 {@code visualizeDependencies} 태스크와 {@code dependencyVisualizer}
 * 설정 블록이 생긴다. 표준 Java 프로젝트라면 설정 없이 적용만으로 동작한다:
 * 소스 루트는 main sourceSet 에서 자동으로 잡고, basePackage 는 소스에서 추론한다.
 */
public class DependencyVisualizerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        DependencyVisualizerExtension ext = project.getExtensions()
            .create("dependencyVisualizer", DependencyVisualizerExtension.class);

        // 기본 출력 경로: build/reports/depvis
        ext.getOutputDir().convention(project.getLayout().getBuildDirectory().dir("reports/depvis"));

        // java 플러그인이 있으면 main sourceSet 의 첫 소스 디렉터리를 sourceRoot 기본값으로.
        project.getPlugins().withType(JavaPlugin.class, java -> {
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            Set<File> srcDirs = sourceSets.getByName("main").getJava().getSrcDirs();
            srcDirs.stream().findFirst().ifPresent(dir ->
                ext.getSourceRoot().convention(project.getLayout().dir(project.provider(() -> dir))));
        });

        project.getTasks().register("visualizeDependencies", VisualizeDependenciesTask.class, task -> {
            task.setGroup("verification");
            task.setDescription("순환 의존을 검출해 Mermaid/HTML 리포트를 생성한다");
            task.getSourceRoot().convention(ext.getSourceRoot());
            task.getBasePackage().convention(ext.getBasePackage());
            task.getOutputDir().convention(ext.getOutputDir());
        });
    }
}
