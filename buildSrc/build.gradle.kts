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
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.5.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.5.0")
}
