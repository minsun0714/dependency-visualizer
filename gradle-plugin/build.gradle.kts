import org.gradle.plugin.compatibility.compatibility

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
}

// Plugin Portal 은 정식 릴리스만 받는다(SNAPSHOT 금지). 네임스페이스는
// GitHub 계정에 매핑되는 io.github.<user> 를 쓴다.
group = "io.github.minsun0714"
version = "0.1.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// analyzer 는 개발/테스트에선 참조하되, 배포 아티팩트에는 "의존성"이 아니라
// "번들된 클래스"로 넣는다 → 플러그인 jar 하나로 자립. (analyzer 는 별도 게시 안 함)
evaluationDependsOn(":analyzer")
val analyzerMain = project(":analyzer").extensions
    .getByType(SourceSetContainer::class.java).getByName("main").output

dependencies {
    // compileOnly: 컴파일엔 쓰지만 POM/런타임 의존으로 새어나가지 않게.
    compileOnly(project(":analyzer"))
    // javaparser 는 실제 런타임에 필요 + Maven Central 에 있어 Portal 이 프록시로 해석해준다.
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.27.1")

    testImplementation(project(":analyzer"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation(gradleTestKit())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// analyzer 클래스를 플러그인 jar 안에 포함시킨다.
tasks.jar {
    from(analyzerMain)
}

// 기능 테스트(withPluginClasspath)가 번들 대상인 analyzer 클래스를 볼 수 있도록.
tasks.pluginUnderTestMetadata {
    pluginClasspath.from(analyzerMain)
}

gradlePlugin {
    website = "https://github.com/minsun0714/dependency-visualizer"
    vcsUrl = "https://github.com/minsun0714/dependency-visualizer"
    plugins {
        create("dependencyVisualizer") {
            id = "io.github.minsun0714.dependency-visualizer"
            implementationClass = "com.minsun.plugin.DependencyVisualizerPlugin"
            displayName = "Dependency Visualizer"
            description = "Java 프로젝트의 순환 의존(circular dependency)을 검출해 " +
                "Mermaid/HTML 로 시각화하는 Gradle 플러그인"
            tags = listOf("dependency", "circular-dependency", "visualization", "mermaid")
            // 태스크가 실행 시점에 project 참조 없이 lazy property + 순수 자바만
            // 사용하므로 configuration cache 와 호환된다.
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
