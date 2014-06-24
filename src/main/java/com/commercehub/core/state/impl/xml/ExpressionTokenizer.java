
package com.commercehub.core.state.impl.xml;

import java.util.StringTokenizer;

/**
 * A tokenizer for the state machine expression language. The recognized tokens
 * are 'AND', 'OR', 'NOT', 'ALL', 'SOME', 'NONE', 'ANY', '(', ')', ',' and
 * identifiers (which are any character combination that doesn't include the
 * special characters above and is not one of the listed keywords.
 *
 * @author aalbu
 */
public class ExpressionTokenizer implements TokenTypes {
    private StringTokenizer st;
    private int tokCount; // number of tokens returned
    private int offset; // character offset

    /**
     * Initializes a new ExpressionTokenizer object.
     *
     * @param input the string to be tokenized
     */
    public ExpressionTokenizer(String input) {
        // use white space characters, comma and parantheses as delimiters
        // and return delimiters (because comma and parantheses are
        // significant tokens)
        st = new StringTokenizer(input, " \r\n\t\f(),", true);
        offset = 1;
    }

    /**
     * Consume the next token.
     *
     * @return the next token, or null if no more tokens in the input
     */
    public Token nextToken() {
        Token ret = null;
        // get the next non-white space token
        while (st.hasMoreTokens()) {
            ret = new Token(st.nextToken());
            if (!Character.isWhitespace(ret.value.charAt(0))) {
                break;
            } else {
                // throw the token away
                ret = null;
            }
        }
        tokCount++;
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getTokenCount() {
        return tokCount;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        // SOME(shipped) AND (ALL(shipped, cancelled, invoiced) OR SOME
        // (invoiced))
        ExpressionTokenizer t = new ExpressionTokenizer(args[0]);

        for (Token tok = t.nextToken(); tok != null; tok = t.nextToken()) {
            System.out.println(tok);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author aalbu
     */
    public class Token {
        private int type;
        private int offs;
        private String value;

        /**
         * Initializes a new Token object.
         *
         * @param v DOCUMENT ME!
         */
        private Token(String v) {
            value = v;
            offs = offset;
            offset += v.length();
            if (ALL.equalsIgnoreCase(v) || SOME.equalsIgnoreCase(v)
                    || NONE.equalsIgnoreCase(v) || ANY.equalsIgnoreCase(v)) {
                type = TOK_FUNC;
            } else if (AND.equalsIgnoreCase(v)) {
                type = TOK_AND;
            } else if (OR.equalsIgnoreCase(v)) {
                type = TOK_OR;
            } else if (NOT.equalsIgnoreCase(v)) {
                type = TOK_NOT;
            } else if ("(".equals(v)) {
                type = TOK_LPAREN;
            } else if (")".equals(v)) {
                type = TOK_RPAREN;
            } else if (",".equals(v)) {
                type = TOK_COMMA;
            } else { // everything else is an identifier
                type = TOK_ID;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getOffset() {
            return offs;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getType() {
            return type;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getValue() {
            return value;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String toString() {
            return value + " (offset = " + offs + ", type = " + type + ")";
        }
    }
}
