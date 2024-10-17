package io.quarkiverse.scala.scala3.futures.test

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

class Scala3FuturesTest {

    // Start unit test with your extension loaded
    @RegisterExtension
    def unitTest: QuarkusUnitTest = new QuarkusUnitTest()
            .setArchiveProducer(() => ShrinkWrap.create(classOf[JavaArchive]))

    @Test
    def writeYourOwnUnitTest(): Unit = {
        // Write your unit tests here - see the testing extension guide https://quarkus.io/guides/writing-extensions#testing-extensions for more information
        Assertions.assertTrue(true, "Add some assertions to " + getClass().getName());
    }
}
