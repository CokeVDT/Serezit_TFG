plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.tfg"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tfg"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding= true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.google.firebase:firebase-firestore:24.10.1' // o la última versión")
    implementation ("com.github.bumptech.glide:glide:4.11.0") // para cargar imagen
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.cloudinary:cloudinary-android:2.3.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")


    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
    implementation ("com.google.firebase:firebase-storage:20.0.0")
    implementation (platform("com.google.firebase:firebase-bom:32.8.1"))

    // Dependencias de Firebase sin versión (usarán la del BoM)
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-storage")

    // Si usas AppCheck (opcional pero recomendado para seguridad)
    implementation ("com.google.firebase:firebase-appcheck")
    implementation ("com.google.firebase:firebase-appcheck-debug")
    implementation ("com.google.firebase:firebase-appcheck:17.1.2")

    // Para el modo debug (opcional, solo en entornos de desarrollo)
    debugImplementation ("com.google.firebase:firebase-appcheck-debug:17.1.2")
}