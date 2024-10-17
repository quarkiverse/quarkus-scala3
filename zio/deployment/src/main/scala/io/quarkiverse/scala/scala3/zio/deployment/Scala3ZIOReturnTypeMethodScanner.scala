package io.quarkiverse.scala.scala3.zio.deployment

import org.jboss.jandex.ClassInfo
import org.jboss.jandex.DotName
import org.jboss.jandex.MethodInfo
import org.jboss.jandex.Type
import org.jboss.resteasy.reactive.server.model.FixedHandlerChainCustomizer
import org.jboss.resteasy.reactive.server.model.HandlerChainCustomizer
import org.jboss.resteasy.reactive.server.processor.scanning.MethodScanner

import java.util
import java.util.List as JList
import java.util.Collections as JCollections

class Scala3ZIOReturnTypeMethodScanner extends MethodScanner {
  private val ZIO = DotName.createSimple("zio.ZIO")
  private val nothing$ = DotName.createSimple("scala.Nothing$")
  private val throwable = DotName.createSimple("java.lang.Throwable")


  override def scan(method: MethodInfo,
                    actualEndpointClass: ClassInfo,
                    methodContext: util.Map[String, AnyRef]
                   ): JList[HandlerChainCustomizer] = {
    if(isMethodSignatureAsync(method)) {
      ensuringFailureTypeIsNothingOrAThrowable(method)
      JCollections.singletonList(
        new FixedHandlerChainCustomizer(
          new Scala3ZIOResponseHandler(),
          HandlerChainCustomizer.Phase.AFTER_METHOD_INVOKE
        )
      )
    } else {
      JCollections.emptyList()
    }
  }

  private def ensuringFailureTypeIsNothingOrAThrowable(info: MethodInfo): Unit = {
    import scala.jdk.CollectionConverters._
    val returnType = info.returnType()
    val typeArguments: JList[Type] = returnType.asParameterizedType().arguments()
    if (typeArguments.size() != 3) {
      throw new RuntimeException("ZIO must have three type arguments")
    }
    val errorType = typeArguments.get(1)

    if !(errorType.name() == nothing$) && !(errorType.name() == throwable) then
      val realClazz = Class.forName(errorType.name().toString(), false, Thread.currentThread().getContextClassLoader)
      if (!classOf[Throwable].isAssignableFrom(realClazz)) {
        val returnType = info.returnType().toString.replaceAll("<","[").replaceAll(">","]")
        val parameters = info.parameters().asScala.map(v => s"${v.name()}:${v.`type`().toString}").mkString(",")
        val signature = s"${info.name()}(${parameters}):${returnType}"

        throw new RuntimeException(s"The error type of def ${signature} in ${info.declaringClass()} needs to be either Nothing, a Throwable or subclass of Throwable")
      }
  }


  override def isMethodSignatureAsync(info: MethodInfo): Boolean = {
      info.returnType().name() == ZIO
  }
}
