import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.3"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.10"
	kotlin("plugin.spring") version "1.8.10"
}

group = "no.nav"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/navikt/k9-format")
		credentials {
			username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
			password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
		}
	}

	maven {
		name = "confluent"
		url = uri("https://packages.confluent.io/maven/")
	}
}

val tokenSupportVersion by extra("3.0.0")
val jsonassertVersion = "1.5.1"
val k9FormatVersion = "8.0.7"
val springMockkVersion by extra("3.1.2")
val confluentVersion by extra("7.3.0")
val logstashLogbackEncoderVersion by extra("7.2")
val slf4jVersion = "2.0.6"
val kotlinxCoroutinesVersion = "1.6.4"
val openhtmltopdfVersion = "1.0.10"
val handlebarsVersion = "4.3.1"
val retryVersion by extra("2.0.0")

extra["springCloudVersion"] = "2022.0.1"

dependencies {
	implementation("no.nav.security:token-validation-core:$tokenSupportVersion")
	implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
	testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")

	implementation("no.nav.k9:soknad:$k9FormatVersion")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.retry:spring-retry:$retryVersion")
	implementation("org.springframework:spring-aspects")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// kafka
	implementation("org.springframework.kafka:spring-kafka")
	implementation("io.confluent:kafka-connect-avro-converter:$confluentVersion")
	implementation("io.confluent:kafka-avro-serializer:$confluentVersion")
	implementation("org.apache.kafka:kafka-streams")
	testImplementation("org.springframework.kafka:spring-kafka-test")

	// PDF
	implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:$openhtmltopdfVersion")
	implementation("com.openhtmltopdf:openhtmltopdf-slf4j:$openhtmltopdfVersion")
	implementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
	implementation("com.github.jknack:handlebars:$handlebarsVersion")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")


	// Logging
	implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")

	// Jackson
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	testImplementation("org.skyscreamer:jsonassert:$jsonassertVersion")
	testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
	testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Jar> {
	archiveFileName.set("app.jar")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
