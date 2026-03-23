package io.quarkiverse.scala.scala3.futures.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.resteasy.reactive.server.model.FixedHandlerChainCustomizer;
import org.jboss.resteasy.reactive.server.model.HandlerChainCustomizer;
import org.jboss.resteasy.reactive.server.model.HandlerChainCustomizer.Phase;
import org.jboss.resteasy.reactive.server.processor.scanning.MethodScanner;

public class Scala3FutureReturnTypeMethodScanner implements MethodScanner {
    private static final DotName FUTURE = DotName.createSimple("scala.concurrent.Future");
    private static final DotName PROMISE = DotName.createSimple("scala.concurrent.Promise");
    private static final DotName BLOCKING_ANNOTATION = DotName.createSimple("io.smallrye.common.annotation.Blocking");

    private void ensureNotBlocking(MethodInfo method) {
        if (method.annotation(BLOCKING_ANNOTATION) != null) {
            String format = String.format("Suspendable @Blocking methods are not supported yet: %s.%s",
                    method.declaringClass().name(), method.name());
            throw new IllegalStateException(format);
        }
    }

    public Scala3FutureReturnTypeMethodScanner() {
    }

    @Override
    public List<HandlerChainCustomizer> scan(MethodInfo method, ClassInfo actualEndpointClass,
            Map<String, Object> methodContext) {

        if (isMethodSignatureAsync(method)) {
            ensureNotBlocking(method);

            return Collections.singletonList(new FixedHandlerChainCustomizer(
                    new Scala3FutureResponseHandler(), Phase.AFTER_METHOD_INVOKE));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isMethodSignatureAsync(MethodInfo info) {
        DotName name = info.returnType().name();
        return name.equals(FUTURE) || name.equals(PROMISE);
    }

}
