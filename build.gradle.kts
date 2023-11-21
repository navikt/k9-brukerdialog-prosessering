import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.5"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
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
			username = "k9-brukerdialog-prosessering"
			password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
		}
	}
}

val tokenSupportVersion = "3.1.9"
val jsonassertVersion = "1.5.1"
val k9FormatVersion = "9.0.5"
val springMockkVersion = "4.0.2"
val confluentVersion = "7.3.0"
val logstashLogbackEncoderVersion = "7.4"
val slf4jVersion = "2.0.9"
val jacksonVersion = "2.16.0"
val kotlinxCoroutinesVersion = "1.6.4"
val openhtmltopdfVersion = "1.0.10"
val handlebarsVersion = "4.3.1"
val retryVersion = "2.0.4"
val awailitilityKotlinVersion = "4.2.0"
val springCloudContractVersion = "4.0.4"

dependencies {
	implementation("org.yaml:snakeyaml:2.2") {
		because("https://github.com/navikt/k9-brukerdialog-prosessering/security/dependabot/4")
	}

	implementation("no.nav.security:token-validation-core:$tokenSupportVersion")
	implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
	testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")

	// K9-format
	implementation("no.nav.k9:soknad:$k9FormatVersion")
	implementation("no.nav.k9:ettersendelse:$k9FormatVersion")

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
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

	testImplementation("org.skyscreamer:jsonassert:$jsonassertVersion")
	testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
	testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:$springCloudContractVersion")
	testImplementation("org.awaitility:awaitility-kotlin:$awailitilityKotlinVersion")

}

tasks {
	withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-Xjsr305=strict")
			jvmTarget = "17"
		}
	}

	getByName<Jar>("jar") {
		enabled = false
	}

	withType<Test> {
		useJUnitPlatform()
	}
	
	withType<Wrapper> {
        gradleVersion = "8.3"
	}
}
