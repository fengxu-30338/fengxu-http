plugins {
    java
}
apply{
    plugin("com.github.dcendents.android-maven")
}

java{
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

group = "com.github.fengxu-http"
version = "0.2.1"

repositories {
    maven("https://jitpack.io")
    mavenLocal()
    mavenCentral()
    google()
}

dependencies {
    compile("com.alibaba", "fastjson", "1.2.75")
    compileOnly("cn.hutool:hutool-http:5.6.0")
    compileOnly("com.squareup.okhttp3:okhttp:4.9.0")

    implementation("com.squareup.okhttp3:okhttp:4.9.0")
}

buildscript {
    repositories{
        mavenLocal()
        jcenter()
        google()
    }
    dependencies{
        classpath("com.github.dcendents:android-maven-gradle-plugin:1.5")
    }
}

