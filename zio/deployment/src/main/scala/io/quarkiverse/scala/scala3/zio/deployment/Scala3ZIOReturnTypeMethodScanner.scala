package io.quarkiverse.scala.scala3.zio.deployment

import org.jboss.jandex.AnnotationInstance
import org.jboss.jandex.ClassInfo
import org.jboss.jandex.DotName
import org.jboss.jandex.MethodInfo
import org.jboss.jandex.Type
import org.jboss.resteasy.reactive.server.core.parameters.ParameterExtractor
import org.jboss.resteasy.reactive.server.model.FixedHandlerChainCustomizer
import org.jboss.resteasy.reactive.server.model.HandlerChainCustomizer
import org.jboss.resteasy.reactive.server.processor.scanning.MethodScanner
import zio.RIO

import java.util
import java.util.List as JList
import java.util.Collections as JCollections

class Scala3ZIOReturnTypeMethodScanner extends MethodScanner {
  val ZIO: DotName = DotName.createSimple("zio.ZIO")
//  val TASK: DotName = DotName.createSimple("zio.Task")
//  val UIO: DotName = DotName.createSimple("zio.UIO")
//  val RIO: DotName = DotName.createSimple("zio.RIO")




  override def scan(method: MethodInfo,
                    actualEndpointClass: ClassInfo,
                    methodContext: util.Map[String, AnyRef]
                   ): JList[HandlerChainCustomizer] = {
    println("in Scala3ZIOReturnTypeMethodScanner scan")
    if(isMethodSignatureAsync(method)) {
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



  override def isMethodSignatureAsync(info: MethodInfo): Boolean = {
    val name = info.returnType().name()
    val isCorrect = name == ZIO
    println(s"return type is $name, isCorrect: $isCorrect")
    isCorrect
//    name match {
//      case ZIO | TASK | UIO => true
//      case _ => false
//    }
  }
}
