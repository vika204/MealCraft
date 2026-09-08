val springBootVersion = "3.5.5"
val jjwtVersion = "0.13.0"
val springdocVersion = "2.8.13"
val thymeleafExtrasVersion = "3.1.1.RELEASE"
val junitVersion = "1.10.2"

plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}

group = "org.L5-G7"
version = "0.0.1-SNAPSHOT"
description = "MealCraft"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:1.4.6")
    }
}

dependencies {
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
}

dependencies {

    // перша вимога: підключаємо залежності для spring modulith
    implementation("org.springframework.modulith:spring-modulith-starter-core:1.4.6")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test:1.4.6")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:$thymeleafExtrasVersion")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-web") // Logback is included automatically here, we don't need any async logs, because app is small
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.h2database:h2")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.apache.httpcomponents.core5:httpcore5")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("io.jsonwebtoken:jjwt:$jjwtVersion")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(project(":mealcraft-starter-external-recipes"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.platform:junit-platform-suite:$junitVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("$projectDir/openapi"))
    outputFileName.set("mealcraft-openapi.json")
}

tasks.named("build") {
    dependsOn("generateOpenApiDocs")
}

