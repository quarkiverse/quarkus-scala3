# Quarkus - Scala3

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.scala/quarkus-scala3?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.scala/quarkus-scala3)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

- [Quarkus - Scala3](#quarkus---scala3)
  - [Introduction](#introduction)
  - [Installation](#installation)
    - [Gradle](#gradle)
    - [Maven](#maven)
  - [Passing Scala compiler args in Quarkus Dev Mode](#passing-scala-compiler-args-in-quarkus-dev-mode)
  - [Useful tips and tricks for building Quarkus apps with Scala, common patterns](#useful-tips-and-tricks-for-building-quarkus-apps-with-scala-common-patterns)
    - ["No tests were found"?! How can that be?](#no-tests-were-found-how-can-that-be)
    - [Scala and Jackson serialization for JSON with Scala Enum support](#scala-and-jackson-serialization-for-json-with-scala-enum-support)
    - [Scala DSL for rest-assured (similar to Kotlin DSL)](#scala-dsl-for-rest-assured-similar-to-kotlin-dsl)
    - [Functional HTTP routes (Vert.x handlers)](#functional-http-routes-vertx-handlers)
  - [Contributors ✨](#contributors-)

## Introduction

This extension provides support for Scala 3 in Quarkus.

It uses the `scala3-interfaces` library to avoid user lock-in to any specific version of the Scala 3 compiler (Dotty).
Instead, the reflection-based API is used and compilation is done by invoking the compiler version the user has installed from the runtime classpath.

For more information and background context on this, there are notes in the `Scala3CompilationProvider.java` file.

Additionally, passing compiler flags when in Dev Mode is supported through the use of an environment variable (`QUARKUS_SCALA3_COMPILER_ARGS`) which allows you to mirror your existing Maven/Gradle compilation configuration.


## Installation

### Gradle

> Note: Gradle support requires a minimum of Gradle 7.3-RC, preferably the latest version.
> For example, as of today: `distributionUrl=https://services.gradle.org/distributions/gradle-7.3-rc-3-bin.zip`
> Please use regular Gradle 7.3 or greater once they have been released.

```groovy
plugins {
    id "java"
    id "scala"
    id "io.quarkus"
}

repositories {
    mavenCentral()
    mavenLocal()
}

VERSIONS = [
    QUARKUS:        "3.10.0",
    QUARKUS_SCALA3: "1.0.0",
    SCALA3        : "3.3.3",
]

dependencies {
    implementation enforcedPlatform("io.quarkus.platform:quarkus-bom:${VERSIONS.QUARKUS}")
    implementation "io.quarkiverse.scala:quarkus-scala3:${VERSIONS.QUARKUS_SCALA3}"
    implementation "org.scala-lang:scala3-library_3:${VERSIONS.SCALA3}"
    implementation "org.scala-lang:scala3-compiler_3:${VERSIONS.SCALA3}"

    implementation "io.quarkus:quarkus-arc"
    implementation "io.quarkus:quarkus-resteasy-reactive"

    implementation "io.quarkus:quarkus-jackson"
    implementation "io.quarkus:quarkus-rest-jackson"
    implementation "com.fasterxml.jackson.module:jackson-module-scala_3"

    testImplementation "io.quarkus:quarkus-junit5"
    testImplementation "io.rest-assured:rest-assured"
}

group = "org.acme"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType(ScalaCompile) {
    scalaCompileOptions.additionalParameters = [
        "-feature",
        "-Wunused:all",
    ]
}

compileJava {
    options.encoding = "UTF-8"
    options.compilerArgs << "-parameters"
}

compileTestJava {
    options.encoding = "UTF-8"
}
```

### Maven

If you want to use this extension, you need to add the `io.quarkiverse.scala:quarkus-scala3` extension first.
In your `pom.xml` file, add:

```xml
<dependencies>
...
  <dependency>
      <groupId>io.quarkiverse.scala</groupId>
      <artifactId>quarkus-scala3</artifactId>
      <version>0.0.1<version>
  </dependency>
...
```

Then, you will need to add the Scala 3 compiler and library and the Scala Maven plugin:

```xml
<properties>
    <scala-maven-plugin.version>4.9.1</scala-maven-plugin.version>
    <scala.version>3.3.3</scala.version>
</properties>

<dependencies>
    <!-- Scala Dependencies -->
    <dependency>
      <groupId>io.quarkiverse.scala</groupId>
      <artifactId>quarkus-scala3</artifactId>
      <version>1.0.0</version>
    </dependency>
    <dependency>
      <groupId>org.scala-lang</groupId>
      <artifactId>scala3-library_3</artifactId>
      <version>${scala.version}</version>
    </dependency>
    <dependency>
      <groupId>org.scala-lang</groupId>
      <artifactId>scala3-compiler_3</artifactId>
      <version>${scala.version}</version>
    </dependency>
</dependencies>

<build>
    <sourceDirectory>src/main/scala</sourceDirectory>
    <testSourceDirectory>src/test/scala</testSourceDirectory>

    <!-- REST OF CONFIG -->

    <plugin>
        <groupId>net.alchim31.maven</groupId>
        <artifactId>scala-maven-plugin</artifactId>
        <version>${scala-maven-plugin.version}</version>
        <executions>
            <execution>
                <id>scala-compile-first</id>
                <phase>process-resources</phase>
                <goals>
                    <goal>add-source</goal>
                    <goal>compile</goal>
                </goals>
            </execution>
            <execution>
                <id>scala-test-compile</id>
                <phase>process-test-resources</phase>
                <goals>
                    <goal>add-source</goal>
                    <goal>testCompile</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <scalaVersion>${scala.version}</scalaVersion>
            <!-- Some solid defaults, change if you like -->
            <args>
                <arg>-Wunused:all</arg>
                <arg>-feature</arg>
                <arg>-deprecation</arg>
                <arg>-Ysemanticdb</arg>
            </args>
        </configuration>
    </plugin>
</build>
```

## Passing Scala compiler args in Quarkus Dev Mode

Finally, the last thing you want to do is make sure that you mirror any compiler args you have set up when you run in Dev Mode.

To do this, just run the dev command with a prefix of the environment variable set. The format is comma-delimited:

```sh
QUARKUS_SCALA3_COMPILER_ARGS="-deprecated,-explain,-feature,-Ysafe-init" mvn quarkus:dev
```

You might save this as a bash/powershell/batch script for convenience.

## Useful tips and tricks for building Quarkus apps with Scala, common patterns

### "No tests were found"?! How can that be?

JUnit requires tests to return type `void`. Scala functions which are not annotated with `: Unit` return type `Scala.Nothing`, rather than `void`.
This means that tests such as the `undiscoverable test` below will never be detected by JUnit.

See this issue for more information:
- https://github.com/junit-team/junit5/issues/2659
  
Please voice your support for a better developer experience around this behavior if it feels poor to you, by commenting on this issue:
- https://github.com/junit-team/junit5/issues/242

```scala
@QuarkusTest
class MyTest:

  @Test
  def `undiscoverable test` =
    assert(1 == 1)

  @Test
  def `discoverable test`: Unit =
    assert(2 == 2)
```

### Scala and Jackson serialization for JSON with Scala Enum support

If using Jackson for serialization, you probably want JSON support for case class and Enum. Scala Jackson module already supports Scala 3 Enums built-in.

To set this up:

- Add the following to your dependencies (in addition to existing `quarkus-jackson` and `quarkus-rest-jackson` extensions).

```xml
<!-- JSON Serialization Dependencies -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-scala_3</artifactId>
</dependency>
```

If these dependencies are added to the project, they will be automatically registered to the default `ObjectMapper` bean.

To ensure full-compatibility with native-image, it is recommended to apply the Jackson @field:JsonProperty("fieldName") annotation, and set a nullable default, as shown below.

The API is usable like this:

```scala
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.{DisplayName, Test}

import javax.inject.Inject
import scala.collection.JavaConverters.*

enum AnEnum:
  case A extends AnEnum
  case B extends AnEnum

case class Other(@JsonProperty("foo") foo: String)
case class Something(
    @JsonProperty("name") name:         String,
    @JsonProperty("someEnum") someEnum: AnEnum,
    @JsonValue other:                   Other,
  )

@QuarkusTest
class Scala3ObjectMapperCustomizerTest:

  @Inject
  var objectMapper: ObjectMapper = null

  @Test
  def `Jackson ObjectMapper can parse Scala 3 members`:
    val sampleSomethingJSON: String = """
    {
    "name": "My Something",
    "someEnum": "A",
    "other": {
        "foo": "bar"
      } 
    }
    """
    val parsed = objectMapper.readValue[Something](sampleSomethingJSON, classOf[Something])
    assertEquals(parsed.name, "My Something")
    assertEquals(parsed.someEnum, AnEnum.A)
    assertEquals(parsed.other.foo, "bar")
```

### Scala DSL for rest-assured (similar to Kotlin DSL)

If you weren't aware, Kotlin has a very nice DSL module for rest-assured that makes it far more ergonomic.
With some finagling, it's possible to replicate this (mostly) in Scala.

Here's a working outline that supports everything but `.extract()` -- a PR or issue comment adding the `.extract()` case is much-welcomed:

- Acknowledgements here should be given to Christopher Davenport from the Scala Discord for sharing the outline of how this sort of API could be written

```scala
import io.restassured.RestAssured.*
import io.restassured.internal.{ResponseSpecificationImpl, ValidatableResponseImpl}
import io.restassured.response.{ExtractableResponse, Response, ValidatableResponse}
import io.restassured.specification.{RequestSender, RequestSpecification, ResponseSpecification}

class GivenConstructor(givenBlock: RequestSpecification => RequestSpecification):
  def When(whenBlock: RequestSpecification => Response): ExpectationConstructor =
    ExpectationConstructor(givenBlock, whenBlock)

  class ExpectationConstructor(
      givenBlock: RequestSpecification => RequestSpecification,
      whenBlock: RequestSpecification => Response
  ):
    def Then(validatable: ValidatableResponse => Unit) =
      val appliedGiven: RequestSpecification = givenBlock.apply(`given`())
      val appliedWhen: Response              = whenBlock.apply(appliedGiven)
      validatable.apply(appliedWhen.`then`())

object Given:
  def apply(givenBlock: RequestSpecification => RequestSpecification): GivenConstructor = GivenConstructor(givenBlock)

def When(whenBlock: RequestSpecification => Response) =
  def blankGiven(givenBlock: RequestSpecification): RequestSpecification = `given`()
  Given(blankGiven).When(whenBlock)
```

And the way it can be used, is like this:

```scala
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.CoreMatchers.`is`
import org.acme.Given
import org.junit.jupiter.api.{DisplayName, Test}

@QuarkusTest
class GreetingResourceTest:

  @Test
  def testDSL(): Unit =
    Given {
      _.params("something", "value")
    }.When {
      _.get("/hello").prettyPeek()
    }.Then {
      _.statusCode(200)
    }
```


### Functional HTTP routes (Vert.x handlers)

While Quarkus is heavily centered around REST-easy annotations for endpoints (being Java-oriented), it also exposes the underlying Vert.x instance.

You can use this to write route handlers which are much more functional-feeling, and the API is similar to that of Express.js

- I recommend this article by Clement Escoffier which covers this far more in-depth
  - https://quarkus.io/blog/magic-control/

```scala
import io.quarkus.runtime.annotations.QuarkusMain
import io.quarkus.runtime.{Quarkus, QuarkusApplication}
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

object Application:
  def main(args: Array[String]): Unit =
    Quarkus.run(classOf[Application], args*)

@QuarkusMain
class Application extends QuarkusApplication:
  override def run(args: String*): Int =
    val vertx = CDI.current().select(classOf[Vertx]).get()
    val router = Router.router(vertx)
    mkRoutes(router)
    Quarkus.waitForExit()
    0

def mkRoutes(router: Router) =
    router.get("/hello").handler(ctx => {
        ctx.response.end("world")
    })
    router.post("/file-upload").handler(ctx => {
        ctx.fileUploads.foreach(it => {
            // Handle file
        })
        ctx.response.end("Got files")
    })
```

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/GavinRay97"><img src="https://avatars.githubusercontent.com/u/26604994?v=4?s=100" width="100px;" alt="Gavin Ray"/><br /><sub><b>Gavin Ray</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-scala3/commits?author=GavinRay97" title="Code">💻</a> <a href="#maintenance-GavinRay97" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://lesincroyableslivres.fr/"><img src="https://avatars.githubusercontent.com/u/1279749?v=4?s=100" width="100px;" alt="Guillaume Smet"/><br /><sub><b>Guillaume Smet</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-scala3/commits?author=gsmet" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/carlosedp"><img src="https://avatars.githubusercontent.com/u/20382?s=400&u=ea5348eac48fb226dc5bc4954f5408764c5914a6&v=4" width="100px;" alt="Carlos Eduardo de Paula"/><br /><sub><b>Carlos Eduardo de Paula</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-scala3/commits?author=carlosedp" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
