package io.quarkiverse.scala.scala3.zio.it

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
  