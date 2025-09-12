import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.6"
    java
    id("com.diffplug.spotless") version "6.25.0" apply false
    checkstyle
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

// ⚠️ repositories는 settings.gradle.kts에서 관리 (여기서 제거)

subprojects {
    // --- Plugins per module ---
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "checkstyle")
    apply(plugin = "com.diffplug.spotless")

    // --- Java toolchain ---
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    }

    // --- Align versions with Spring Boot BOM ---
    configure<DependencyManagementExtension> {
        imports { mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.2") }
    }

    // --- Test deps ---
    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    }

    // --- Test task defaults ---
    tasks.withType<Test> {
        useJUnitPlatform()

        System.getProperty("excludeTags")
            ?.takeIf { it.isNotBlank() }
            ?.let { useJUnitPlatform { excludeTags(it) } }

        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showStandardStreams = false
        }
        reports {
            html.required.set(true)
            junitXml.required.set(true)
        }
    }

    // --- Checkstyle (extension) ---
    checkstyle {
        toolVersion = "10.17.0"
        // 업로드한 규칙 파일 사용: config/checkstyle/checkstyle.xml
        config = resources.text.fromFile(rootProject.file("config/checkstyle/checkstyle.xml"))
        isIgnoreFailures = false
        maxWarnings = 0
    }

    // --- Checkstyle task reporting ---
    tasks.withType<Checkstyle> {
        isShowViolations = true
        reports {
            xml.required.set(false)
            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.file("reports/checkstyle/${name}.html"))
        }
    }

    // --- Spotless (no cast needed) ---
    extensions.configure<SpotlessExtension> {
        java {
            target("src/**/*.java")
            googleJavaFormat("1.17.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
            // importOrder("", "java", "javax", "org", "com") // 필요 시
        }
        format("misc") {
            target("**/*.md", "**/*.yml", "**/*.yaml", "**/.gitignore")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    // --- Wire quality gates into `check` ---
    tasks.named("check") {
        dependsOn("spotlessCheck", "checkstyleMain", "checkstyleTest")
    }
}

// Optional: root shortcut to build everything without tests
tasks.register("buildAll") {
    description = "Build all subprojects without tests"
    doLast { exec { commandLine("bash", "-lc", "./gradlew build -x test") } }
}
