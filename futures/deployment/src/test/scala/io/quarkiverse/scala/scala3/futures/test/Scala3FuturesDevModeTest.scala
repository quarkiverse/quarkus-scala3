package io.quarkiverse.scala.scala3.futures.test

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

class Scala3FuturesDevModeTest {

    // Start hot reload (DevMode) test with your extension loaded
    @RegisterExtension
    val devModeTest: QuarkusDevModeTest = new QuarkusDevModeTest()
            .setArchiveProducer(() => ShrinkWrap.create(classOf[JavaArchive]))

    @Test
    def writeYourOwnDevModeTest(): Unit = {
        // Write your dev mode tests here - see the testing extension guide https://quarkus.io/guides/writing-extensions#testing-hot-reload for more information
        Assertions.assertTrue(true, "Add dev mode assertions to " + getClass().getName());
    }
}
