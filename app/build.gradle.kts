plugins { id("com.android.application") }
android {
    namespace = "com.codewithmohamed.quranwordbyword"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.codewithmohamed.quranwordbyword"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = true }
}
dependencies { testImplementation("junit:junit:4.13.2") }
