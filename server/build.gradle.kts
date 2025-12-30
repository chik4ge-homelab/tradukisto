plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.17.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

group = "me.chik4ge"
version = "0.0.1-SNAPSHOT"
description = "translator web backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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

extra["springAiVersion"] = "1.1.2"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    developmentOnly("org.springframework.ai:spring-ai-spring-boot-docker-compose")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val openApiGeneratedDir = layout.buildDirectory.dir("generated")

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/../specs/openapi.yaml")
    outputDir.set(openApiGeneratedDir.map { it.asFile.absolutePath })
    apiPackage.set("me.chik4ge.tradukisto.openapi")
    skipOperationExample.set(true)
    // invokerPackage.set("me.chik4ge.tradukisto.openapi.invoker")
    modelPackage.set("me.chik4ge.tradukisto.openapi.model")
    configOptions.set(
        mapOf(
            "hideGenerationTimestamp" to "false",
            "interfaceOnly" to "true",
            "dateLibrary" to "java8",
            "useTags" to "true",
            "useSpringBoot3" to "true",
            "reactive" to "true",
            "sse" to "true",
            "useResponseEntity" to "false",
        ),
    )
}

tasks.named("openApiGenerate") {
    inputs.dir("$rootDir/../specs")
    doFirst {
        delete(openApiGeneratedDir)
    }
}

sourceSets {
    main {
        kotlin.srcDir(openApiGeneratedDir.map { it.dir("src/main/kotlin") })
        java.srcDir(openApiGeneratedDir.map { it.dir("src/main/java") })
    }
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(files("src/main/kotlin", "src/test/kotlin"))
    config.setFrom(files("$rootDir/detekt.yml"))
}

ktlint {
    version.set("1.8.0")
    android.set(false)
}

tasks.named("check") {
    dependsOn("detekt")
    dependsOn("ktlintCheck")
}
