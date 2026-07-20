plugins {
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // 분석 파이프라인(CycleAnalysis 등)을 그대로 재사용. javaparser 는 analyzer 의
    // 전이 의존으로 태스크 실행 시점 클래스패스에 따라온다.
    implementation(project(":analyzer"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation(gradleTestKit())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins {
        create("dependencyVisualizer") {
            id = "com.minsun.dependency-visualizer"
            implementationClass = "com.minsun.plugin.DependencyVisualizerPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
