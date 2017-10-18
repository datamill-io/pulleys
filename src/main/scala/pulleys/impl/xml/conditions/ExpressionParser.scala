package pulleys.impl.xml.conditions

import pulleys.Condition
import pulleys.impl.xml.{ParseException, TokenTypes}
import java.util

/**
  * A parser for our state machine expression language. It builds {@link
  * Condition}s from expressions. Here's the grammar: <code>S -> E E -> T RestE
  * RestE -> 'OR' T RestE | null T -> F RestT RestT -> 'AND' F RestT | null F ->
  * 'NOT' X | X X -> '(' E ')' | FUNC L L -> '(' id RestL RestL -> ',' id RestL |
  * ')'</code>
  *
  * <p>To use it: <code>ExpressionParser p = new ExpressionParser(); Condition
  * cond = p.parse(expression);</code>
  */
class ExpressionParser extends TokenTypes {
  private var tokenizer: ExpressionTokenizer = _
  private var crtToken: ExpressionTokenizer#Token = _

  /**
    * The only method you care about.
    *
    * @param input a string containing an expression
    * @return the condition correponding to the input expression
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  def parse(input: String): Condition = {
    tokenizer = new ExpressionTokenizer(input)
    crtToken = tokenizer.nextToken
    // S -> E
    val n: ASTNode = expression
    if (crtToken != null) {
      throw error
    }
    return buildCondition(n)
  }

  /**
    * E -> T RestE
    *
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def expression: ASTNode = {
    // E -> T RestE
    val node: ASTNode = term
    return restExpression(node)
  }

  /**
    * RestE -> 'OR' T RestE | null
    *
    * @param n DOCUMENT ME!
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def restExpression(n: ASTNode): ASTNode = {
    // RestE -> 'OR' T RestE | null
    if (crtToken != null && crtToken.getType == TOK_OR) {
      val or: ASTNode = new ASTNode(crtToken)
      or.addChild(n)
      `match`(TOK_OR)
      or.addChild(term)
      return restExpression(or)
    }
    else {
      return n
    }
  }

  /**
    * T -> F RestT
    *
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def term: ASTNode = {
    // T -> F RestT
    val node: ASTNode = factor
    return restTerm(node)
  }

  /**
    * RestT -> 'AND' F RestT | null
    *
    * @param n DOCUMENT ME!
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def restTerm(n: ASTNode): ASTNode = {
    // RestT -> 'AND' F RestT | null
    if (crtToken != null && crtToken.getType == TOK_AND) {
      val and: ASTNode = new ASTNode(crtToken)
      and.addChild(n)
      `match`(TOK_AND)
      and.addChild(factor)
      return restTerm(and)
    }
    else {
      return n
    }
  }

  /**
    * F -> 'NOT' X | X
    *
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def factor: ASTNode = {
    // F -> 'NOT' X | X
    checkCrtToken()
    var not: ASTNode = null
    if (crtToken.getType == TOK_NOT) {
      not = new ASTNode(crtToken)
      `match`(TOK_NOT)
    }
    val node: ASTNode = x
    if (not != null) {
      not.addChild(node)
      return not
    }
    else {
      return node
    }
  }

  /**
    * X -> '(' E ')' | FUNC L
    *
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def x: ASTNode = {
    // X -> '(' E ')' | FUNC L
    checkCrtToken()
    if (crtToken.getType == TOK_LPAREN) {
      `match`(TOK_LPAREN)
      val node: ASTNode = expression
      `match`(TOK_RPAREN)
      return node
    }
    else if (crtToken.getType == TOK_FUNC) {
      val node: ASTNode = new ASTNode(crtToken)
      `match`(TOK_FUNC)
      return list(node)
    }
    else {
      throw error
    }
  }

  /**
    * L -> '(' id RestL
    *
    * @param n DOCUMENT ME!
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def list(n: ASTNode): ASTNode = {
    // L -> '(' id RestL
    checkCrtToken()
    if (crtToken.getType == TOK_LPAREN) {
      `match`(TOK_LPAREN)
      val node: ASTNode = new ASTNode(crtToken)
      n.addChild(node)
      `match`(TOK_ID)
      return restList(n)
    }
    else {
      throw error
    }
  }

  /**
    * RestL -> ',' id RestL | ')'
    *
    * @param n DOCUMENT ME!
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def restList(n: ASTNode): ASTNode = {
    // RestL -> ',' id RestL | ')'
    checkCrtToken()
    if (crtToken.getType == TOK_COMMA) {
      `match`(TOK_COMMA)
      val node: ASTNode = new ASTNode(crtToken)
      n.addChild(node)
      `match`(TOK_ID)
      return restList(n)
    }
    else if (crtToken.getType == TOK_RPAREN) {
      `match`(TOK_RPAREN)
      return n
    }
    else {
      throw error
    }
  }

  /**
    * DOCUMENT ME!
    *
    * @param tok DOCUMENT ME!
    * @return DOCUMENT ME!
    * @throws ParseException DOCUMENT ME!
    */
  @throws[ParseException]
  private def `match`(tok: Int): ExpressionTokenizer#Token = {
    checkCrtToken()
    if (crtToken.getType == tok) {
      val ret: ExpressionTokenizer#Token = crtToken
      crtToken = tokenizer.nextToken
      return ret
    }
    else {
      val msg: String = "Parse error at position " + crtToken.getOffset + " (token number " + tokenizer.getTokenCount + "); expected token type " + tok + ", but found '" + crtToken.getValue + "'"
      throw new ParseException(msg)
    }
  }

  @throws[ParseException]
  private def checkCrtToken() {
    if (crtToken == null) {
      val msg: String = "Unexpected end of input!"
      throw new ParseException(msg)
    }
  }

  private def error: ParseException = {
    val msg: String = "Unexpected token '" + crtToken.getValue + "' at position " + crtToken.getOffset
    return new ParseException(msg)
  }

  @throws[ParseException]
  private def buildCondition(n: ASTNode): Condition = {
    val cond: Condition = new Condition
    updateCondition(cond, n)
    return cond
  }

  @throws[ParseException]
  private def updateCondition(cond: Condition, n: ASTNode) {
    var c: Condition = null
    n.getToken.getType match {
      case TOK_AND =>
        c = cond.addAndClause
      case TOK_OR =>
        c = cond.addOrClause
      case TOK_NOT =>
        c = cond.addNotClause
      case TOK_FUNC =>
        val states: util.Set[String] = getChildValues(n)
        val `val`: String = n.getToken.getValue
        if (ALL.equalsIgnoreCase(`val`)) {
          cond.addAllClause(states)
        }
        else if (SOME.equalsIgnoreCase(`val`)) {
          cond.addSomeClause(states)
        }
        else if (NONE.equalsIgnoreCase(`val`)) {
          cond.addNoneClause(states)
        }
        else if (ANY.equalsIgnoreCase(`val`)) {
          cond.addAnyClause(states)
        }
        return
      case _ =>
        val msg: String = "Invalid node: " + n.getToken
        throw new ParseException(msg)
    }
    import scala.collection.JavaConversions._
    for (child <- n.getChildren) {
      updateCondition(c, child)
    }
  }

  private def getChildValues(n: ASTNode): java.util.Set[String] = {
    val res: java.util.HashSet[String] = new java.util.HashSet[String]
    import scala.collection.JavaConversions._
    for (child <- n.getChildren) {
      res.add(child.getToken.getValue)
    }
    res
  }
}