package io.quarkiverse.scala.scala3.futures.deployment;

import io.quarkiverse.scala.scala3.deployment.Scala3FutureReturnTypeMethodScanner;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.server.spi.MethodScannerBuildItem;

public class Scala3FuturesJavaProcessor {

    @BuildStep
    public FeatureBuildItem feature() {
        return new FeatureBuildItem("scala3-futures");
    }

    @BuildStep
    public MethodScannerBuildItem registerFuturesRestReturnTypes() {
        return new MethodScannerBuildItem(new Scala3FutureReturnTypeMethodScanner());
    }
}
