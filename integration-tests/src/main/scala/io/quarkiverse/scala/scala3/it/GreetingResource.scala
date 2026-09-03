package io.quarkiverse.scala.scala3.it

import jakarta.ws.rs.{GET, Path, Produces}
import jakarta.ws.rs.core.MediaType

/** Exercises the extension against real Scala 3 sources: an enum, a `given`
  * instance and an extension method, none of which exist in Scala 2.
  */
enum Language:
  case Scala, Java

trait Greeter[A]:
  def greet(a: A): String

given Greeter[Language] with
  def greet(language: Language): String = s"hello from ${language.toString.toLowerCase}"

extension (language: Language) def greeting(using greeter: Greeter[Language]): String = greeter.greet(language)

@Path("/scala3/greeting")
class GreetingResource:

  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  def greeting(): String = Language.Scala.greeting
