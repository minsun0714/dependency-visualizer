package com.minsun.plugin;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

/**
 * 플러그인 설정 블록. build.gradle 에서 아래처럼 조정한다:
 *
 * <pre>{@code
 * dependencyVisualizer {
 *     basePackage = "com.acme"          // 생략 시 소스에서 자동 추론
 *     sourceRoot = file("src/main/java") // 생략 시 main sourceSet 자동 사용
 *     outputDir = layout.buildDirectory.dir("reports/depvis")
 * }
 * }</pre>
 *
 * 세 값 모두 생략 가능 — 표준 프로젝트면 플러그인 적용만으로 동작한다.
 */
public interface DependencyVisualizerExtension {

    /** 내부 타입 필터 접두어. 미설정 시 소스의 공통 최상위 패키지를 추론해 쓴다. */
    Property<String> getBasePackage();

    /** 분석 대상 소스 루트. 미설정 시 main sourceSet 의 첫 소스 디렉터리. */
    DirectoryProperty getSourceRoot();

    /** 리포트 출력 디렉터리. 기본 {@code build/reports/depvis}. */
    DirectoryProperty getOutputDir();
}
