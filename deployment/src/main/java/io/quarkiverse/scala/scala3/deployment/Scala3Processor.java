package io.quarkiverse.scala.scala3.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.jackson.spi.ClassPathJacksonModuleBuildItem;

class Scala3Processor {

    private static final String FEATURE = "scala3";
    private static final String SCALA_JACKSON_MODULE = "com.fasterxml.jackson.module.scala.DefaultScalaModule";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    /*
     * Register the Scala Jackson module if that has been added to the classpath
     * Producing the BuildItem is entirely safe since if quarkus-jackson is not on
     * the classpath the BuildItem will just be ignored
     */
    @BuildStep
    void registerScalaJacksonModule(BuildProducer<ClassPathJacksonModuleBuildItem> classPathJacksonModules) {
        try {
            Class.forName(SCALA_JACKSON_MODULE, false, Thread.currentThread().getContextClassLoader());
            classPathJacksonModules.produce(new ClassPathJacksonModuleBuildItem(SCALA_JACKSON_MODULE));
        } catch (Exception ignored) {
        }
    }
}
