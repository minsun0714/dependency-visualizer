package com.minsun.plugin;

import com.minsun.analyzer.BasePackageInferrer;
import com.minsun.analyzer.CycleAnalysis;
import com.minsun.analyzer.ReportWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 순환 의존을 검출해 Mermaid/HTML 리포트를 생성하는 태스크.
 * 파이프라인 자체는 analyzer 의 {@link CycleAnalysis} 가 담당하고,
 * 이 태스크는 설정값 해석 + 파일 출력 + 요약 로그만 맡는다.
 */
public abstract class VisualizeDependenciesTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getSourceRoot();

    @Input
    @Optional
    public abstract Property<String> getBasePackage();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void visualize() throws IOException {
        Path sourceRoot = getSourceRoot().get().getAsFile().toPath();
        Path outputDir = getOutputDir().get().getAsFile().toPath();

        String basePackage = resolveBasePackage(sourceRoot);
        getLogger().lifecycle("[depvis] 분석: {} (basePackage={})", sourceRoot, basePackage);

        CycleAnalysis analysis = CycleAnalysis.run(sourceRoot, basePackage);
        ReportWriter.write(analysis, outputDir);

        List<List<String>> pkg = analysis.packageCycles();
        List<List<String>> cls = analysis.classCycles();
        getLogger().lifecycle("[depvis] 패키지 순환 {}개, 클래스 순환 {}개", pkg.size(), cls.size());
        getLogger().lifecycle("[depvis] 리포트: {}", outputDir.resolve(ReportWriter.HTML));
    }

    /** 명시된 basePackage 를 쓰되, 없으면 소스에서 추론. 둘 다 실패하면 명확히 에러. */
    private String resolveBasePackage(Path sourceRoot) throws IOException {
        String configured = getBasePackage().getOrElse("").trim();
        if (!configured.isEmpty()) {
            return configured;
        }
        String inferred = BasePackageInferrer.infer(sourceRoot);
        if (inferred.isEmpty()) {
            throw new GradleException(
                "basePackage 를 추론할 수 없습니다(공통 최상위 패키지 없음). "
                + "dependencyVisualizer { basePackage = \"...\" } 로 명시하세요.");
        }
        return inferred;
    }
}
