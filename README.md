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
    - [Configuring Scala Jackson and the addon-on "Enum" module for JSON support](#configuring-scala-jackson-and-the-addon-on-enum-module-for-json-support)
    - [Scala DSL for rest-assured (similar to Kotlin DSL)](#scala-dsl-for-rest-assured-similar-to-kotlin-dsl)
    - [Functional HTTP routes (Vert.x handlers)](#functional-http-routes-vertx-handlers)
- [Quarkus - Scala3 - ZIO](#quarkus---scala3---zio)

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
    QUARKUS_SCALA3: "0.0.1",
    SCALA3        : "3.1.0",
    SCALA_LIBRARY : "2.13.6",
    // Scala Jackson at time of writing doesn't support Scala 3 Enum's natively. It requires another library.
    // That library doesn't support same version of Jackson that Quarkus BOM uses (2.12.5), so this is the best compromise
    // https://search.maven.org/artifact/com.github.pjfanning/jackson-module-scala3-enum_3/2.12.3/jar
    JACKSON       : "2.12.3"
]

dependencies {
    implementation "io.quarkiverse.scala:quarkus-scala3:${VERSIONS.QUARKUS_SCALA3}"
    implementation("org.scala-lang:scala3-compiler_3") {
        version {
            strictly VERSIONS.SCALA3
        }
    }
    implementation("org.scala-lang:scala3-library_3") {
        version {
            strictly VERSIONS.SCALA3
        }
    }
    implementation("org.scala-lang:scala-library") {
        version {
            strictly VERSIONS.SCALA_LIBRARY
        }
    }

    // Quarkus comes with Scala 2 distributed in it's Bill-of-Materials unfortunately
    // It's Scala 2.12.13, which is not ABI compatible -- With Scala 3, we need to exclude this entirely
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))  {
         exclude group: 'org.scala-lang', module: 'scala-library'
    }
    implementation "io.quarkus:quarkus-arc"
    implementation "io.quarkus:quarkus-resteasy-reactive"

    implementation "com.fasterxml.jackson.module:jackson-module-scala_3:${VERSIONS.JACKSON}"
    implementation "com.github.pjfanning:jackson-module-scala3-enum_3:${VERSIONS.JACKSON}"

    testImplementation "io.quarkus:quarkus-junit5"
    testImplementation "io.rest-assured:rest-assured"
}

group = "org.acme"
version = "1.0.0-SNAPSHOT"

java {
    // Set to 17 for performance reasons, feel free to change to 11 or 8
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType(ScalaCompile) {
    scalaCompileOptions.additionalParameters = [
        "-feature", // Emit warnings and locations for features that should be imported explicitly
        "-explain", // Explain (type) errors in more detail
        "-Ysafe-init", // Ensure safe initialization of objects (prevent null object init)
        "-Yrequire-targetName", // Warn if an operator is defined without a @targetName annotation
        // "-Yexplicit-nulls", // Make reference types non-nullable. Nullable types can be expressed with unions: e.g. String|Null.

        // I am unsure if this is required for proper tooling to work. Metals/IntelliJ may already cover this.
        "-Xsemanticdb", // Store information in SemanticDB
    ]
}

compileJava {
    options.encoding = 'UTF-8'
    options.compilerArgs << '-parameters'
}

compileTestJava {
    options.encoding = 'UTF-8'
}
```

### Maven

If you want to use this extension, you need to add the `io.quarkiverse.scala:quarkus-scala3` extension first.
In your `pom.xml` file, add:

```xml
<dependency>
    <groupId>io.quarkiverse.scala</groupId>
    <artifactId>quarkus-scala3</artifactId>
    <version>0.0.1<version>
</dependency>
```

Then, you will need to install the Scala 3 compiler, the Scala Maven plugin, and to fix an odd bug with the way that the Scala 3 compiler Maven dependencies are resolved.

Due to Scala 2 version in upstream `Quarkus BOM`, the wrong version of `scala-library` (a transitive dependency: `scala3-compiler_3` -> `scala3-library_3` -> `scala-library`) is resolved.

This causes binary incompatibilities -- and Scala to break. In order to fix this, you just need to manually align the version of `scala-library` to the one listed as used by the version of `scala3-library_3` that's the same as the `scala3-compiler_3` version.

So for `scala3-compiler_3` = `3.0.0`, then `scala3-library_3` = `3.0.0`, and we check the `scala-library` version it uses:
- https://mvnrepository.com/artifact/org.scala-lang/scala3-library_3/3.0.0

Here, we can see that it was compiled with `2.13.5` in it's dependencies. So that's what we set in ours:

```xml
<properties>
    <scala-maven-plugin.version>4.5.3</scala-maven-plugin.version>
    <scala.version>3.1.0</scala.version>
    <scala-library.version>2.13.6</scala-library.version>
</properties>

<dependencies>
    <!-- Scala Dependencies -->
    <dependency>
        <groupId>org.scala-lang</groupId>
        <artifactId>scala3-compiler_3</artifactId>
        <version>${scala.version}</version>
    </dependency>
    <dependency>
        <!-- Version manually aligned to scala3-library_3:3.0.0 dependency -->
        <groupId>org.scala-lang</groupId>
        <artifactId>scala-library</artifactId>
        <version>${scala-library.version}</version>
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
                <arg>-deprecated</arg>
                <arg>-explain</arg>
                <arg>-feature</arg>
                <arg>-Ysafe-init</arg>
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

### Configuring Scala Jackson and the addon-on "Enum" module for JSON support

You probably want JSON support for case class and enum serialization.
There are two things you need to enable this, as of the time of writing:

1. The standard Jackson Scala module
2. An addon module from one of the Jackson Scala maintainers for Scala 3 enums that hasn't made its way into the official module yet

To set this up:

- Add the following to your dependencies
  
```xml
<!-- JSON Serialization Dependencies -->
<dependency>
    <groupId>com.github.pjfanning</groupId>
    <artifactId>jackson-module-scala3-enum_3</artifactId>
    <version>2.12.3</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-scala_2.13</artifactId>
    <version>2.12.3</version>
</dependency>
```

- Set up something like the below in your codebase:
  
```scala
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.pjfanning.`enum`.EnumModule
import io.quarkus.jackson.ObjectMapperCustomizer

import javax.inject.Singleton

// https://quarkus.io/guides/rest-json#jackson
@Singleton
class Scala3ObjectMapperCustomizer extends ObjectMapperCustomizer:
  def customize(mapper: ObjectMapper): Unit =
    // General Scala support
    // https://github.com/FasterXML/jackson-module-scala
    mapper.registerModule(DefaultScalaModule)
    // Suport for Scala 3 Enums
    // https://github.com/pjfanning/jackson-module-scala3-enum
    mapper.registerModule(EnumModule)
```

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

case class Other(foo: String)
case class Something(name: String, someEnum: AnEnum, other: Other)

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
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

# Quarkus - Scala3 - ZIO
Add these dependencies to your `pom.xml` (respectively gradle):

```xml
    <dependency>
      <groupId>io.quarkiverse.scala</groupId>
      <artifactId>quarkus-scala3-zio</artifactId>
      <version>999-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>io.quarkiverse.scala</groupId>
      <artifactId>quarkus-scala3-zio-deployment</artifactId>
      <version>999-SNAPSHOT</version>
    </dependency>
```

Now you're able to use a `ZIO[Any, E <: Throwable, A]` in your REST-Resources, e.g.

```scala
    final case class AsyncGreetingResponse(message: String, ip: String, time: Long)

    @GET
    @Path("/greet/async")
    @Produces(Array(APPLICATION_JSON))
    def asyncGreeting(): Task[AsyncGreetingResponse] =
        val numsAmount = 10
        Log.debug(s"Generating $numsAmount numbers asynchronously...")
        val startTime = System.currentTimeMillis()
        // Get the IP address asynchronously
        val IPFuture = ZIO.fromFuture(_ => getOwnIP().map(_.body).recover:
            case e: Exception =>
                Log.error("Failed to get the IP address.")
                Left("Failed to get IP"))

        val futureSum = ZIO.foreachPar((1 to numsAmount)){i => generateNum()}.map(_.sum)
        for
            sumF <- futureSum.fork
            ipF  <- IPFuture.map(_.merge).fork
            result <- sumF.join zip ipF.join
            (sum, ip) = result
        yield
            val endTime = System.currentTimeMillis() - startTime
            Log.debug(s"My IP is: $ip")
            Log.debug(s"Generated $numsAmount numbers asynchronously in ${endTime}ms")

            AsyncGreetingResponse(
              s"The sum of the $numsAmount generated numbers is $sum. Was generated asynchronously in ${endTime}ms.\nYour IP is: $ip.",
              ip,
            endTime)

    end asyncGreeting

```

Please note that we currently don't support anything in the Environment `R`, as we don't have a way
to transfer this information from Java to Scala. Also, your error type needs to be either Nothing
or a (subtype of) Throwable.
