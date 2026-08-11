plugins {
    id("java")
    id("io.freefair.lombok") version "9.5.0"
    `maven-publish`
}

group = "com.github.milomarten"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-rng-simple:1.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/milomarten/diceroll")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("diceroll") {
            from(components["java"])
        }
    }
}