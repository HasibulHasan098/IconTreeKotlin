plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")
}

group = "com.icontree"
version = "0.2.6" // keep in sync with ../package.json

// The icon JSON files live once, in the repo-root icons/ folder. They are
// copied into the library's assets at build time so the AAR ships the full set.
val iconSourceDir = rootProject.file("icons")
val syncIconAssets by tasks.registering(Sync::class) {
    from(iconSourceDir) { include("*.json") }
    into(layout.buildDirectory.dir("generated/iconAssets/icontree"))
}

android {
    namespace = "com.icontree.animate"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    sourceSets["main"].assets.srcDir(layout.buildDirectory.dir("generated/iconAssets"))

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }

    publishing {
        singleVariant("release") { withSourcesJar() }
    }
    testOptions { unitTests.isReturnDefaultValues = true }
}

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }
    .configureEach { dependsOn(syncIconAssets) }
tasks.matching { it.name.contains("Lint", ignoreCase = true) }
    .configureEach { dependsOn(syncIconAssets) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    api("com.airbnb.android:lottie-compose:6.6.7")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.icontree"
                artifactId = "icon-tree-animate"
                version = project.version.toString()
            }
        }
    }
}
