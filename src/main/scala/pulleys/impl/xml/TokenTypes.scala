package pulleys.impl.xml

trait TokenTypes {
  val ALL: String = "ALL"
  val SOME: String = "SOME"
  val NONE: String = "NONE"
  val ANY: String = "ANY"
  val AND: String = "AND"
  val OR: String = "OR"
  val NOT: String = "NOT"
  val TOK_ALL: Int = 1
  val TOK_SOME: Int = 2
  val TOK_NONE: Int = 3
  val TOK_ANY: Int = 4
  val TOK_AND: Int = 5
  val TOK_OR: Int = 6
  val TOK_NOT: Int = 7
  val TOK_LPAREN: Int = 8
  val TOK_RPAREN: Int = 9
  val TOK_COMMA: Int = 10
  val TOK_ID: Int = 11
  val TOK_FUNC: Int = 12
}