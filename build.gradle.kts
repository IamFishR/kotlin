plugins {
    id("com.android.application") version "8.11.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}