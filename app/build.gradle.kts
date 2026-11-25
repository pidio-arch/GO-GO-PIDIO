plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// GitHub Actions pasa -Pvc=<número> y -Pvn=<nombre> en cada compilación para numerar las versiones.
val appVersionCode = (project.findProperty("vc") as String?)?.toIntOrNull() ?: 1
val appVersionName = (project.findProperty("vn") as String?) ?: "1.0"

android {
    namespace = "com.tareasverde.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tareasverde.app"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
    }

    // Firma fija: todas las versiones (locales y de GitHub) se firman igual, así Android deja
    // instalar una actualización encima de la anterior sin borrar tus tareas.
    signingConfigs {
        create("gogo") {
            storeFile = file("gogo.keystore")
            storePassword = "gogopidio"
            keyAlias = "gogo"
            keyPassword = "gogopidio"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("gogo")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("gogo")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
