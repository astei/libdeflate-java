plugins {
    `java-library`
}

dependencies {
    api(project(":libdeflate-java-core"))
    api("io.netty:netty-buffer:4.1.53.Final")
}