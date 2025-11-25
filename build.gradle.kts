import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
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

val tokenSupportVersion = "6.0.0"
val jsonassertVersion = "1.5.3"
val k9FormatVersion = "12.6.3"
val ungDeltakelseOpplyserVersjon = "2.5.0"
val springMockkVersion = "4.0.2"
val logstashLogbackEncoderVersion = "9.0"
val slf4jVersion = "2.0.17"
val openhtmltopdfVersion = "1.1.4"
val handlebarsVersion = "4.5.0"
val retryVersion = "2.0.12"
val awailitilityKotlinVersion = "4.3.0"
val springCloudContractVersion = "5.0.0"
val orgJsonVersion = "20250517"
val springdocVersion = "3.0.0"
val pdfBoxVersion = "3.0.6"
val imageIOVersion = "3.12.0"
val fpsakTidsserieVersion = "2.7.3"
val gcpStorageVersion = "2.60.0"
val auth0Version = "4.5.0"
val tikaVersion = "3.2.3"
val testContainersVersion = "1.21.3"
val aivenFakeGCSServerVersion = "0.2.0"

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
	implementation("at.datenwort.openhtmltopdf:openhtmltopdf-pdfbox:$openhtmltopdfVersion") {
		// Erstattes av apache pdfbox $pdfBoxVersion
		exclude(group = "org.apache.pdfbox", module = "pdfbox")
	}
	implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion")
	implementation("at.datenwort.openhtmltopdf:openhtmltopdf-slf4j:$openhtmltopdfVersion")
	implementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
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

	// Jackson
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	testImplementation("org.skyscreamer:jsonassert:$jsonassertVersion")
	testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
	testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:$springCloudContractVersion")
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


	testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
	testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
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
		useJUnitPlatform()
		testLogging {
			exceptionFormat = TestExceptionFormat.FULL
			showStackTraces = true
		}
	}
	
	withType<Wrapper> {
        gradleVersion = "8.7"
	}
}
