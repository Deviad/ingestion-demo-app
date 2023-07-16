    import org.gradle.api.tasks.testing.logging.TestExceptionFormat

    // ANSI color codes
    val ANSI_RESET = "\u001B[0m"
    val ANSI_GREEN = "\u001B[32m"
    val ANSI_RED = "\u001B[31m"
    val ANSI_BLUE = "\u001B[34m"
    val ANSI_YELLOW = "\u001B[33m"


    plugins {
        java
        id("org.springframework.boot") version "3.1.0"
        id("io.spring.dependency-management") version "1.1.0"
        id("org.javamodularity.moduleplugin") version "1.8.12"

    }

    group = "com.foobar"
    version = "0.0.1-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_20
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://repo.spring.io/snapshot")
        }
        maven {
            url = uri("https://repo.spring.io/release")
        }
    }

    //dependencyManagement {
    //    imports {
    //        mavenBom("org.springframework.boot:spring-boot-dependencies:3.1.0")
    //    }
    //}

    val luceneVersion = "9.6.0"
    val jacksonDataFormatVersion = "2.15.2"
    val woodstoxCore = "6.5.1"
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.apache.lucene:lucene-core:${luceneVersion}")
        implementation("org.apache.lucene:lucene-queryparser:${luceneVersion}")
        compileOnly("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        annotationProcessor("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        implementation("com.fasterxml.woodstox:woodstox-core")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("io.micrometer:micrometer-core:latest.integration")
        implementation("org.springframework.boot:spring-boot-starter-aop")
        implementation("io.vavr:vavr:0.10.4")
        implementation("io.vavr:vavr-jackson:0.10.3")
        implementation("org.springframework.retry:spring-retry:2.0.2")
        testImplementation("org.awaitility:awaitility:4.2.0")


    //    implementation("org.aspectj:aspectjweaver")

    }


    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("--enable-preview", "--add-modules", "jdk.incubator.concurrent"))
    }

    tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
        jvmArgs(listOf("-Xmx1024m", "--enable-preview", "--add-modules", "jdk.incubator.concurrent"))
    }


    tasks.withType<JavaExec> {
        doFirst {
            jvmArgs(listOf("-XX:+UnlockExperimentalVMOptions", "-XX:+EnableJVMCI", "-Djvmci.Compiler=graal", "--add-modules", "jdk.incubator.concurrent"))

        }
    }

    tasks.withType<Test> {
        doFirst {
            jvmArgs("-XX:+UnlockExperimentalVMOptions", "-XX:+EnableJVMCI", "--enable-preview")
        }
        var successCount = 0
        var failureCount = 0
        var skippedCount = 0

        useJUnitPlatform()
        testLogging {
    //        events("passed", "skipped", "failed", "standardOut", "standardError")
            events("standardError")
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }

        val listener = object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}
            override fun beforeTest(testDescriptor: TestDescriptor) {}

            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                when (result.resultType) {
                    TestResult.ResultType.SUCCESS -> successCount++
                    TestResult.ResultType.FAILURE -> failureCount++
                    TestResult.ResultType.SKIPPED -> skippedCount++
                }

                val color = getColors(result)

                println("$color Test ${testDescriptor.displayName} completed with result: ${result.resultType} $ANSI_RESET")
            }


            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                if (suite.parent == null) {
                    val color = getColors(result)
                    println("Execution completed for test suite '${suite.name}' with result: $color${result.resultType}$ANSI_RESET")
                    println("Total tests run: ${result.testCount}")
                    val output = "$ANSI_GREEN Successful: $successCount $ANSI_RESET, $ANSI_RED Failed: $failureCount $ANSI_RESET, $ANSI_YELLOW Skipped: $skippedCount $ANSI_RESET"
                    println(output)
                }
            }

            private fun getColors(result: TestResult): String {
                val color = when (result.resultType) {
                    TestResult.ResultType.SUCCESS -> ANSI_GREEN
                    TestResult.ResultType.FAILURE -> ANSI_RED
                    TestResult.ResultType.SKIPPED -> ANSI_YELLOW
                    else -> ANSI_BLUE
                }
                return color
            }
        }

        addTestListener(listener)
    }
