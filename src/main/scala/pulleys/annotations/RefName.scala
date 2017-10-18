package pulleys.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import scala.annotation.{ClassfileAnnotation, StaticAnnotation}

/**
  * Tag a class with a shortened name for inclusion in state machine definitions.
  */
@Retention(RetentionPolicy.RUNTIME)
@Target(Array(ElementType.TYPE))
case class RefName(var value: String) extends StaticAnnotation 