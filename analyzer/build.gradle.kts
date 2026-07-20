plugins {
    java
    application
}

application {
    mainClass.set("com.minsun.analyzer.Main")
}

// 분석 대상(sample-project) 경로를 루트 기준 상대경로로 안정적으로 찾도록 작업 디렉토리 고정
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.27.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}