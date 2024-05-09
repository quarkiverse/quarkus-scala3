package io.quarkiverse.scala.scala3.deployment;

import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler;

public class Scala3FutureResponseHandler implements ServerRestHandler {

    private void handleFuture(ResteasyReactiveRequestContext requestContext,
            scala.concurrent.Future<?> f) {

        requestContext.suspend();

        f.onComplete(tryValue -> {
            if (tryValue.isSuccess()) {
                requestContext.setResult(tryValue.get());
            } else {
                requestContext.handleException(tryValue.failed().get(), true);
            }
            requestContext.resume();
            return null;
        }, scala.concurrent.ExecutionContext.global());

    }

    @Override
    public void handle(ResteasyReactiveRequestContext requestContext) throws Exception {
        if (requestContext.getResult() instanceof scala.concurrent.Future<?> future) {
            handleFuture(requestContext, future);
        } else if (requestContext.getResult() instanceof scala.concurrent.Promise<?> promise) {
            handleFuture(requestContext, promise.future());
        }
    }

}
