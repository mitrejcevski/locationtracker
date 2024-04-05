import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.jetbrainsKotlinSerialization)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "io.github.mitrejcevski.locationtracker"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions.unitTests {
        isReturnDefaultValues = true
        all { tests ->
            tests.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.network)
    implementation(libs.play.services.location)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    testImplementation(libs.bundles.unitTest)

    testRuntimeOnly(libs.junit.jupiter.engine)
}

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true
        )
    )
    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
    signAllPublications()
    coordinates("io.github.mitrejcevski", "locationtracker", "1.0.0-SNAPSHOT")
    pom {
        name.set("Location Tracker SDK")
        description.set("This project is a helper SDK that helps tracing location.")
        inceptionYear.set("2024")
        url.set("https://github.com/mitrejcevski/locationtracker")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("jovmit")
                name.set("Jovche Mitrejchevski")
                url.set("https://github.com/mitrejcevski/")
            }
        }
        scm {
            url.set("https://github.com/mitrejcevski/locationtracker/")
            connection.set("scm:git:git://github.com/mitrejcevski/locationtracker.git")
            developerConnection.set("scm:git:ssh://git@github.com/mitrejcevski/locationtracker.git")
        }
    }
}