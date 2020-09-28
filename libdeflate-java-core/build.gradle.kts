import java.nio.file.Paths
import java.util.Locale
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    `java-library`
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0\"")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

task("compileNatives") {
    // Note: we should prefer compilation with GCC
    val jniTempPath = Paths.get(project.rootDir.toString(), "tmp")

    fun osSetup(env: MutableMap<String, String>) {
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
                env["CFLAGS"] = "-O2 -fomit-frame-pointer -Werror -Wall -fPIC"
            }
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                // Hey, so, um, I hope you're running something NT-based, because bless your heart if you're still on 9x
                throw RuntimeException("Please install a better, more secure OS.")
            }
            else -> {
                throw RuntimeException("Your strange, weird OS is not supported. Did you know it is 2020?")
            }
        }
    }

    doLast {
        val env = hashMapOf("LIB_NAME" to "libdeflate_jni")
        env.putAll(System.getenv())
        osSetup(env)

        exec {
            executable = "make"
            args = arrayListOf("clean", "all")
            environment = env.toMap()
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

tasks.named<Test>("test") {
    dependsOn(tasks.get("compileNatives"))
    useJUnitPlatform()
}

tasks.jar {
    dependsOn(tasks.get("compileNatives"))
}