package pulleys.impl.xml.conditions

/**
  * An abstract syntax tree node for our expression language.
  *
  * @author aalbu
  */
class ASTNode private[xml](var token: ExpressionTokenizer#Token) {
  val children = new java.util.ArrayList[ASTNode]

  def addChild(child: ASTNode) {
    children.add(child)
  }

  def getChildren: java.util.List[ASTNode] = {
    return children
  }

  def getToken: ExpressionTokenizer#Token = {
    return token
  }

  override def toString: String = {
    return toString(0).toString
  }

  def toString(indent: Int): StringBuffer = {
    val buf: StringBuffer = new StringBuffer
    var i: Int = 0
    while (i < indent) {
      {
        buf.append(' ')
      }
      {
        i += 1; i - 1
      }
    }
    buf.append(token.getValue)
    import scala.collection.JavaConversions._
    for (child <- children) {
      buf.append("\n")
      buf.append(child.toString(indent + 4))
    }
    return buf
  }
}