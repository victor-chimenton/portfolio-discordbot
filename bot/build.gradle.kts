plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("net.dv8tion:JDA:5.3.0") {
        exclude(module = "opus-java")
    }
}
