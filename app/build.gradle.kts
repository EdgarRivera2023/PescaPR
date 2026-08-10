plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.bradmir.pescapr"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bradmir.pescapr"
        minSdk = 24
        targetSdk = 36
        versionCode = 17
        versionName = "2.3.5"

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

        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Best practice for release to shrink app size
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Uses the standard string resource for production (Best Practice)
            manifestPlaceholders["appName"] = "@string/app_name"
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Overrides the app name for your side-by-side debug version
            manifestPlaceholders["appName"] = "Debug"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)

    // Google Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Google Play Billing
    implementation(libs.play.billing.ktx)

    // Icons
    implementation(libs.androidx.compose.icons.extended)

    // Firebase (BOM keeps versions perfectly in sync)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Coil for Image Loading
    implementation(libs.coil.compose)

    // Retrofit for Weather API
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Coroutines
    implementation(libs.kotlinx.coroutines.play.services)

    // Gemini AI SDK
    implementation(libs.generativeai)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
