import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.1.20"
	kotlin("plugin.spring") version "2.1.20"
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

val tokenSupportVersion = "5.0.24"
val jsonassertVersion = "1.5.3"
val k9FormatVersion = "12.0.0"
val ungDeltakelseOpplyserVersjon = "1.5.0"
val springMockkVersion = "4.0.2"
val logstashLogbackEncoderVersion = "8.0"
val slf4jVersion = "2.0.17"
val jacksonVersion = "2.18.3"
val openhtmltopdfVersion = "1.1.4"
val handlebarsVersion = "4.4.0"
val retryVersion = "2.0.11"
val awailitilityKotlinVersion = "4.3.0"
val springCloudContractVersion = "4.2.1"
val orgJsonVersion = "20250107"
val springdocVersion = "2.8.6"
val pdfBoxVersion = "3.0.4"
val imageIOVersion = "3.12.0"
val fpsakTidsserieVersion = "2.7.1"

dependencies {
	implementation("org.yaml:snakeyaml:2.4") {
		because("https://github.com/navikt/k9-brukerdialog-prosessering/security/dependabot/4")
	}

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
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	testImplementation("org.skyscreamer:jsonassert:$jsonassertVersion")
	testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
	testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:$springCloudContractVersion")
	testImplementation("org.awaitility:awaitility-kotlin:$awailitilityKotlinVersion")

	// Diverse
	implementation("org.json:json:$orgJsonVersion")
	implementation("no.nav.fpsak.tidsserie:fpsak-tidsserie:$fpsakTidsserieVersion")
}

tasks {
	withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-Xjsr305=strict")
			jvmTarget = "21"
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
