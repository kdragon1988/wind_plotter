plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.windplotter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.windplotter"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        ndk {
            // DJI SDK usually supports these architectures
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/rxjava.properties"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/org/gradle/internal/logging/slf4j/impl/StaticLoggerBinder.properties"
        }
        jniLibs {
            pickFirsts.add("lib/*/libc++_shared.so")
            pickFirsts.add("lib/*/libdji_video_decoder.so")
            pickFirsts.add("lib/*/libroadline.so")
            pickFirsts.add("lib/*/libGroudStation.so")
            pickFirsts.add("lib/*/libFRTDers.so")
            pickFirsts.add("lib/*/libDJIUpgradeCore.so")
            pickFirsts.add("lib/*/libDJIFlySafeCore.so")
            pickFirsts.add("lib/*/libdjifs_jni.so")
            pickFirsts.add("lib/*/libsfjni.so")
            pickFirsts.add("lib/*/libVisualSearch.so")
            pickFirsts.add("lib/*/libUpgradeVerify.so")
            pickFirsts.add("lib/*/libdji_dputils.so")
            pickFirsts.add("lib/*/libyuv2.so")
            pickFirsts.add("lib/*/libGNaviUtils.so")
            pickFirsts.add("lib/*/libDjigram.so")
            pickFirsts.add("lib/*/libchecked.so")
            pickFirsts.add("lib/*/libdji_measure.so")
            pickFirsts.add("lib/*/libJNI_VISION.so")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // DJI SDK Dependencies
    implementation("com.dji:dji-sdk-v5-aircraft:5.9.0")
    compileOnly("com.dji:dji-sdk-v5-aircraft-provided:5.9.0")
    
    // Room (Database)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Networking (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
