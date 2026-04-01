plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cozyla.mlkitdemo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.cozyla.mlkitdemo"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
//    /d
    androidTestImplementation(libs.androidx.espresso.core)


    //具体版本参考官网 https://developers.google.cn/ml-kit
    // To recognize Chinese script
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
    // 图像分类
    implementation("com.google.mlkit:image-labeling:17.0.9")
    // 人脸检测
    implementation("com.google.mlkit:face-detection:16.1.7")
    // Gemini API (保留文档，使用火山引擎替代)
    //implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    // 火山引擎 - OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // 火山引擎 - Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}