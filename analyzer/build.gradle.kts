plugins {
    java
    application
}

application {
    mainClass.set("com.minsun.analyzer.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.27.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
}

tasks.test {
    useJUnitPlatform()
}