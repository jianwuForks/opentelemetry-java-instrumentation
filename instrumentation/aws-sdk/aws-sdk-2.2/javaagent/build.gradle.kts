plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("software.amazon.awssdk")
    module.set("aws-core")
    versions.set("[2.2.0,)")
    // Used by all SDK services, the only case it isn't is an SDK extension such as a custom HTTP
    // client, which is not target of instrumentation anyways.
    extraDependency("software.amazon.awssdk:protocol-core")
    // several software.amazon.awssdk artifacts are missing for this version
    skip("2.17.200")
  }
}

dependencies {
  implementation(project(":instrumentation:aws-sdk:aws-sdk-2.2:library-autoconfigure"))

  library("software.amazon.awssdk:aws-core:2.2.0")

  testImplementation(project(":instrumentation:aws-sdk:aws-sdk-2.2:testing"))
  // Make sure these don't add HTTP headers
  testImplementation(project(":instrumentation:apache-httpclient:apache-httpclient-4.0:javaagent"))
  testImplementation(project(":instrumentation:netty:netty-4.1:javaagent"))

  latestDepTestLibrary("software.amazon.awssdk:aws-json-protocol:2.17.+")
  latestDepTestLibrary("software.amazon.awssdk:aws-core:+")
  latestDepTestLibrary("software.amazon.awssdk:dynamodb:+")
  latestDepTestLibrary("software.amazon.awssdk:ec2:+")
  latestDepTestLibrary("software.amazon.awssdk:kinesis:+")
  latestDepTestLibrary("software.amazon.awssdk:rds:+")
  latestDepTestLibrary("software.amazon.awssdk:s3:+")
  latestDepTestLibrary("software.amazon.awssdk:sqs:+")
}

tasks.withType<Test>().configureEach {
  systemProperty("testLatestDeps", findProperty("testLatestDeps") as Boolean)
  // TODO run tests both with and without experimental span attributes
  jvmArgs("-Dotel.instrumentation.aws-sdk.experimental-span-attributes=true")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
  mergeServiceFiles {
    include("software/amazon/awssdk/global/handlers/execution.interceptors")
  }
}
