import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.3.0"
	kotlin("plugin.spring") version "2.3.0"
}

group = "no.nav"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

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

val tokenSupportVersion = "6.0.2"
val jsonassertVersion = "1.5.3"
val k9FormatVersion = "12.7.5"
val ungDeltakelseOpplyserVersjon = "2.9.2"
val springMockkVersion = "5.0.1"
val logstashLogbackEncoderVersion = "9.0"
val openhtmltopdfVersion = "1.1.4"
val handlebarsVersion = "4.5.0"
val retryVersion = "2.0.12"
val awailitilityKotlinVersion = "4.3.0"
val wiremockVersion = "3.13.2"
val orgJsonVersion = "20251224"
val springdocVersion = "3.0.1"
val pdfBoxVersion = "3.0.6"
val imageIOVersion = "3.13.0"
val fpsakTidsserieVersion = "2.7.4"
val gcpStorageVersion = "2.62.0"
val auth0Version = "4.5.0"
val tikaVersion = "3.2.3"
val aivenFakeGCSServerVersion = "0.3.0"

dependencies {
	implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
	implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
	testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")

	// Swagger (openapi 3)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

	// K9-format
	implementation("no.nav.k9:soknad:$k9FormatVersion")
	implementation("no.nav.k9:ettersendelse:$k9FormatVersion")
	implementation("no.nav.k9:oppgave-ungdomsytelse:$k9FormatVersion")

	// Ung-deltakelseopplyser kontrakt
	implementation("no.nav.ung.deltakelseopplyser:kontrakt:$ungDeltakelseOpplyserVersjon")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-restclient")
	implementation("org.springframework.retry:spring-retry:$retryVersion")
	implementation("org.springframework:spring-aspects")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-resttestclient")

	// kafka
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.apache.kafka:kafka-streams")
	testImplementation("org.springframework.kafka:spring-kafka-test")

	// PDF
	implementation("at.datenwort.openhtmltopdf:openhtmltopdf-pdfbox:$openhtmltopdfVersion") {
		// Erstattes av apache pdfbox $pdfBoxVersion
		exclude(group = "org.apache.pdfbox", module = "pdfbox")
	}
	implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion")
	implementation("at.datenwort.openhtmltopdf:openhtmltopdf-slf4j:$openhtmltopdfVersion")
	implementation("org.slf4j:jcl-over-slf4j")
	implementation("com.github.jknack:handlebars:$handlebarsVersion")
	implementation("org.apache.tika:tika-core:$tikaVersion")

	// Bilde til PDF
	implementation("org.apache.pdfbox:pdfbox-io:$pdfBoxVersion")
	implementation("com.twelvemonkeys.imageio:imageio-jpeg:$imageIOVersion")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

	// Logging
	implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")

	// Jackson 2 compatibility for Spring Boot 4 (k9-søknad uses Jackson 2)
	implementation("org.springframework.boot:spring-boot-jackson2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	testImplementation("org.skyscreamer:jsonassert:$jsonassertVersion")
	testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
	testImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")
	testImplementation("org.awaitility:awaitility-kotlin:$awailitilityKotlinVersion")

	// Google Cloud
	implementation("com.google.cloud:google-cloud-storage:$gcpStorageVersion") {
		exclude(group = "com.google.guava", module = "listenablefuture")
	}
	testImplementation("io.aiven:testcontainers-fake-gcs-server:$aivenFakeGCSServerVersion")

	// Kryptokgrafi
	implementation("com.auth0:java-jwt:$auth0Version")

	// Diverse
	implementation("org.json:json:$orgJsonVersion")
	implementation("no.nav.fpsak.tidsserie:fpsak-tidsserie:$fpsakTidsserieVersion")


	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
}

tasks {
	withType<KotlinCompile> {
		compilerOptions {
			freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            jvmTarget.set(JvmTarget.JVM_21)
		}
	}

	getByName<Jar>("jar") {
		enabled = false
	}

	withType<Test> {
		jvmArgs("-Xmx6g") // 6GB er for å unngå OutOfMemoryError
		useJUnitPlatform {
			if (project.hasProperty("groups")) {
				includeTags(project.property("groups") as String)
			}
		}
		testLogging {
			exceptionFormat = TestExceptionFormat.FULL
			showStackTraces = true
		}
	}

}
