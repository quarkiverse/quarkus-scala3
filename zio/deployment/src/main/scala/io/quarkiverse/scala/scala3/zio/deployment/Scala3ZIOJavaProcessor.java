package io.quarkiverse.scala.scala3.zio.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.server.spi.MethodScannerBuildItem;

public class Scala3ZIOJavaProcessor {

    @BuildStep
    public FeatureBuildItem feature() {
        return new FeatureBuildItem("scala3-zio");
    }

    @BuildStep
    public MethodScannerBuildItem registerZIORestReturnTypes() {
        return new MethodScannerBuildItem(new Scala3ZIOReturnTypeMethodScanner());
    }
}
