plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // MinIO (S3 호환)
    implementation("io.minio:minio:8.5.9")

    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    //Kafka
    implementation("org.springframework.kafka:spring-kafka")

    //Lombok
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")



    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("org.testcontainers:mysql:1.20.1")
    testImplementation("com.redis:testcontainers-redis:2.2.4")
    testImplementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    testImplementation("org.testcontainers:kafka:1.19.7")
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")

}
