import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.elsa"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.5.9"
val junitJupiterVersion = "5.9.1"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set("io.elsa.leaderboard.Main")
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-service-proxy")
  implementation("io.vertx:vertx-codegen")
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-pg-client")
  implementation("io.vertx:vertx-hazelcast")
  implementation("io.vertx:vertx-redis-client")
  implementation("com.ongres.scram:client:2.1")
  implementation("org.apache.commons:commons-lang3:3.15.0")
  implementation("com.google.code.gson:gson:2.11.0")
  implementation("org.slf4j:slf4j-api:2.0.13")
  implementation("ch.qos.logback:logback-classic:1.5.6")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  annotationProcessor("io.vertx:vertx-codegen:4.5.9:processor")
  annotationProcessor("io.vertx:vertx-service-proxy:4.5.9")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.jar {
  manifest.attributes["Main-Class"] = "io.elsa.leaderboard.Main"
}

