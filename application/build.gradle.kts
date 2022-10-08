import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	id("com.github.davidmc24.gradle.plugin.avro") version "1.0.0"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "digital.porsche.croatia"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	maven (
		url = "https://packages.confluent.io/maven/"
	)
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.flywaydb:flyway-core")
	implementation("org.apache.kafka:kafka-clients")
	implementation("org.apache.kafka:kafka-streams")
	implementation("io.confluent:kafka-avro-serializer:7.2.1")
	implementation("io.confluent:kafka-streams-avro-serde:7.2.1")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.generateAvroJava {
	source = fileTree("src/main/resources/avro")
	setCreateSetters("false")
}
