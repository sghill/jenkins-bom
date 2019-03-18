plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.20") {
            because("Ensure same version as used by gradle")
        }
    }
    implementation("com.squareup.moshi:moshi-kotlin:1.5.0")
    implementation("com.google.guava:guava:25.0-jre")

    testImplementation(platform("org.junit:junit-bom:5.4.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core:3.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()
}
