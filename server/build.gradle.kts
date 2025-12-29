plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.5.9"
  id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.17.0"
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
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.ai:spring-ai-starter-model-openai")
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

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/../specs/openapi.yaml")
    outputDir.set("$buildDir/generated")
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
		)
	)
}

tasks.named("openApiGenerate") {
    inputs.dir("$rootDir/../specs")
	doFirst {
		delete(layout.buildDirectory.dir("generated"))
	}
}

sourceSets {
  main {
    kotlin.srcDir("$buildDir/generated/src/main/kotlin")
  }
}

tasks.named("compileKotlin") {
  dependsOn("openApiGenerate")
}
