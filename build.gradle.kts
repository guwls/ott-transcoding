plugins {
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.6"
    java
}

allprojects {
    group = "com.example"
    version = "0.1.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    }
    tasks.test {
        useJUnitPlatform()
    }
}
