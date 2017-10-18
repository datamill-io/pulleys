package pulleys.impl.xml.conditions

import java.util.StringTokenizer

import pulleys.impl.xml.TokenTypes

/**
  * A tokenizer for the state machine expression language. The recognized tokens
  * are 'AND', 'OR', 'NOT', 'ALL', 'SOME', 'NONE', 'ANY', '(', ')', ',' and
  * identifiers (which are any character combination that doesn't include the
  * special characters above and is not one of the listed keywords.
  *
  * @author aalbu
  */
object ExpressionTokenizer {
  def main(args: Array[String]) {
    // SOME(shipped) AND (ALL(shipped, cancelled, invoiced) OR SOME
    // (invoiced))
    val t: ExpressionTokenizer = new ExpressionTokenizer(args(0))
    var tok: ExpressionTokenizer#Token = t.nextToken
    while (tok != null) {
      {
        System.out.println(tok)
      }
      tok = t.nextToken
    }
  }
}

class ExpressionTokenizer(val input: String) extends TokenTypes {
  // use white space characters, comma and parantheses as delimiters
  // and return delimiters (because comma and parantheses are
  // significant tokens)
  val st = new StringTokenizer(input, " \r\n\t\f(),", true)
  private var tokCount: Int = 0 // number of tokens returned
  private var offset: Int = 1 // character offset
  /**
    * Consume the next token.
    *
    * @return the next token, or null if no more tokens in the input
    */
  def nextToken: Token = {
    while (st.hasMoreTokens) {
      val ret = new Token(st.nextToken)
      if (!Character.isWhitespace(ret.value.charAt(0))) {
        tokCount += 1
        return ret
      }
    }
    null
  }

  def getTokenCount: Int = {
    return tokCount
  }

  class Token (var value: String) {
    val offs = value.length
    var `type` =  if (ALL.equalsIgnoreCase(value) || SOME.equalsIgnoreCase(value) || NONE.equalsIgnoreCase(value) || ANY.equalsIgnoreCase(value)) {
      TOK_FUNC
    }
    else if (AND.equalsIgnoreCase(value)) {
      TOK_AND
    }
    else if (OR.equalsIgnoreCase(value)) {
      TOK_OR
    }
    else if (NOT.equalsIgnoreCase(value)) {
      TOK_NOT
    }
    else if ("(" == value) {
      TOK_LPAREN
    }
    else if (")" == value) {
      TOK_RPAREN
    }
    else if ("," == value) {
      TOK_COMMA
    }
    else {
      // everything else is an identifier
      TOK_ID
    }

    def getOffset: Int = {
      return offs
    }

    def getType: Int = {
      return `type`
    }

    def getValue: String = {
      return value
    }

    override def toString: String = {
      return value + " (offset = " + offs + ", type = " + `type` + ")"
    }
  }

}