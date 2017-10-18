package pulleys.annotations

import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import pulleys.StateAction
import pulleys.Trigger
import org.reflections.Reflections
import pulleys.ClientImplProvider
import java.lang.annotation.Annotation
import java.util
import java.util.concurrent.Executors

class AnnotationBasedClientImplProvider() extends ClientImplProvider {
  val ref: Reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath).setExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)))
  val types: util.Set[Class[_]] = ref.getTypesAnnotatedWith(classOf[RefName].asInstanceOf[Annotation])

  val triggers = new util.HashMap[String, Class[_]]
  val actions = new util.HashMap[String, Class[_]]

  import scala.collection.JavaConversions._
  for (c <- types) {
    for (a <- c.getAnnotations) {
      if (a.isInstanceOf[RefName]) {
        val r: RefName = a.asInstanceOf[RefName]
        if (classOf[StateAction].isAssignableFrom(c)) {
          actions.put(r.value, c)
        }
        if (classOf[Trigger].isAssignableFrom(c)) {
          triggers.put(r.value, c)
        }
      }
    }
  }

  def getTriggerClass(name: String): Class[_] = triggers.get(name)

  def getActionClass(name: String): Class[_] =  actions.get(name)
}