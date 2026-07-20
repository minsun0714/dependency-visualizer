package com.minsun.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@link CycleAnalysis} 결과를 파일 리포트로 떨군다.
 *
 * <p>출력 디렉터리에 다음 3개를 쓴다:
 * <ul>
 *   <li>{@code cycles-package.mmd} — 패키지 레벨 Mermaid 텍스트</li>
 *   <li>{@code cycles-class.mmd} — 클래스 레벨 Mermaid 텍스트</li>
 *   <li>{@code cycles.html} — 두 다이어그램을 함께 렌더링하는 뷰어(브라우저로 열면 끝)</li>
 * </ul>
 * HTML 은 mermaid.js 를 CDN 에서 불러오므로 열람 시 인터넷이 필요하다.
 */
public final class ReportWriter {

    public static final String PACKAGE_MMD = "cycles-package.mmd";
    public static final String CLASS_MMD = "cycles-class.mmd";
    public static final String HTML = "cycles.html";

    private ReportWriter() {
    }

    /** 분석 결과를 {@code outputDir} 에 .mmd 2개 + .html 1개로 쓴다. */
    public static void write(CycleAnalysis analysis, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve(PACKAGE_MMD), analysis.packageMermaid());
        Files.writeString(outputDir.resolve(CLASS_MMD), analysis.classMermaid());
        Files.writeString(outputDir.resolve(HTML), html(analysis));
    }

    private static String html(CycleAnalysis analysis) {
        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
              <meta charset="utf-8">
              <title>순환 의존 다이어그램</title>
              <script src="https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.min.js"></script>
              <style>body{font-family:sans-serif;margin:24px}h2{margin-top:32px}</style>
            </head>
            <body>
              <h1>Circular Dependencies</h1>
              <h2>패키지 레벨 (기본 뷰 · 노이즈 필터링됨)</h2>
              <pre class="mermaid">
            %s
              </pre>
              <h2>클래스 레벨 (드릴다운)</h2>
              <pre class="mermaid">
            %s
              </pre>
              <script>mermaid.initialize({startOnLoad:true});</script>
            </body>
            </html>
            """.formatted(analysis.packageMermaid(), analysis.classMermaid());
    }
}
