import java.nio.file.Paths
import java.util.Locale
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    `java-library`
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

task("compileNatives") {
    // Note: we should prefer compilation with GCC
    val jniTempPath = Paths.get(project.rootDir.toString(), "tmp")

    doLast {
        val env = hashMapOf("LIB_NAME" to "libdeflate_jni")
        env.putAll(System.getenv())

        when {
            Os.isFamily(Os.FAMILY_MAC) -> {
                // lol
                throw RuntimeException("Your expensive fruit computer needs to go into the trash.")
            }
            Os.isFamily(Os.FAMILY_UNIX) -> {
                // Cover most Unices. It's 2020, so hopefully you're compiling on a modern open-source BSD or Linux distribution...
                if (System.getenv("CC") == null) {
                    env["CC"] = "gcc"
                }
                val osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
                env["DYLIB_SUFFIX"] = "so"
                env["JNI_PLATFORM"] = osName
                env["LIB_DIR"] = Paths.get(jniTempPath.toString(), "compiled", osName, System.getProperty("os.arch")).toString()
                env["OBJ_DIR"] = Paths.get(jniTempPath.toString(), "objects", osName, System.getProperty("os.arch")).toString()
                env["CFLAGS"] = "-O2 -fomit-frame-pointer -Werror -Wall -fPIC -flto"

                exec {
                    executable = "make"
                    args = arrayListOf("clean", "all")
                    environment = env.toMap()
                }
            }
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                // Windows is a very, very special case... we compile with Microsoft Visual Studio, which is a mess.
                // There is the `vswhere` utility, which brings a tiny bit of sanity, but alas... it is probably better
                // to invoke the build from a batch script.
                //
                // We'll need `vswhere` (you can install it via Chocolatey).
                exec {
                    executable = "cmd"
                    args = arrayListOf("/C", "windows_build.bat")
                }
            }
            else -> {
                throw RuntimeException("Your strange, weird OS is not supported. Did you know it is 2020?")
            }
        }
    }
}

sourceSets {
    main {
        resources.srcDir(Paths.get(project.rootDir.toString(), "tmp", "compiled"))
    }
}

tasks.jar {
    val osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
    val osArch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH)
    archiveClassifier.set("${osName}-${osArch}")
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(tasks.get("compileNatives"))
}

tasks.named<Test>("test") {
    dependsOn(tasks.get("compileNatives"))
    useJUnitPlatform()
}

tasks.jar {
    dependsOn(tasks.get("compileNatives"))
}