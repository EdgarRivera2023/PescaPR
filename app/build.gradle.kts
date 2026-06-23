plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.bradmir.pescapr"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bradmir.pescapr"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // --- PARSER DE PROPIEDADES NATIVO DE KOTLIN (SIN JAVA.UTIL) ---
        var geminiKey = ""
        var mapsKey = ""
        var weatherKey = ""

        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            for (linea in localPropertiesFile.readLines()) {
                val trimLinea = linea.trim()
                if (trimLinea.isNotEmpty() && !trimLinea.startsWith("#") && trimLinea.contains("=")) {
                    val partes = trimLinea.split("=", limit = 2)
                    if (partes.size == 2) {
                        val llave = partes[0].trim()
                        val valor = partes[1].trim()
                        when (llave) {
                            "GEMINI_API_KEY" -> geminiKey = valor
                            "MAPS_API_KEY" -> mapsKey = valor
                            "OPENWEATHER_API_KEY" -> weatherKey = valor
                        }
                    }
                }
            }
        }

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$weatherKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Best practice for release to shrink app size
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    // Core Android & Compose
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Firebase (BOM keeps versions perfectly in sync)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Retrofit for Weather API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Google AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
}
