
package com.commercehub.core.state.impl.xml;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.commercehub.core.state.Condition;

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
public class ExpressionParser implements TokenTypes {
    private static final int LOG_ANCHOR_0 = 0;

    private ExpressionTokenizer tokenizer;
    private ExpressionTokenizer.Token crtToken;

    /**
     * Initializes a new ExpressionParser object.
     */
    public ExpressionParser() {
    }

    /**
     * The only method you care about.
     *
     * @param input a string containing an expression
     *
     * @return the condition correponding to the input expression
     *
     * @throws ParseException DOCUMENT ME!
     */
    public Condition parse(String input) throws ParseException {
        tokenizer = new ExpressionTokenizer(input);
        crtToken = tokenizer.nextToken();

        // S -> E
        ASTNode n = expression();

        if (crtToken != null) {
            throw error();
        }
        return buildCondition(n);
    }

    /**
     * E -> T RestE
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode expression() throws ParseException {
        // E -> T RestE
        ASTNode node = term();
        return restExpression(node);
    }

    /**
     * RestE -> 'OR' T RestE | null
     *
     * @param n DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode restExpression(ASTNode n) throws ParseException {
        // RestE -> 'OR' T RestE | null
        if (crtToken != null && crtToken.getType() == TOK_OR) {
            ASTNode or = new ASTNode(crtToken);
            or.addChild(n);
            match(TOK_OR);
            or.addChild(term());
            return restExpression(or);
        } else {
            return n;
        }
    }

    /**
     * T -> F RestT
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode term() throws ParseException {
        // T -> F RestT
        ASTNode node = factor();
        return restTerm(node);
    }

    /**
     * RestT -> 'AND' F RestT | null
     *
     * @param n DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode restTerm(ASTNode n) throws ParseException {
        // RestT -> 'AND' F RestT | null
        if (crtToken != null && crtToken.getType() == TOK_AND) {
            ASTNode and = new ASTNode(crtToken);
            and.addChild(n);
            match(TOK_AND);
            and.addChild(factor());
            return restTerm(and);
        } else {
            return n;
        }
    }

    /**
     * F -> 'NOT' X | X
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode factor() throws ParseException {
        // F -> 'NOT' X | X
        checkCrtToken();
        ASTNode not = null;
        if (crtToken.getType() == TOK_NOT) {
            not = new ASTNode(crtToken);
            match(TOK_NOT);
        }
        ASTNode node = x();
        if (not != null) {
            not.addChild(node);
            return not;
        } else {
            return node;
        }
    }

    /**
     * X -> '(' E ')' | FUNC L
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode x() throws ParseException {
        // X -> '(' E ')' | FUNC L
        checkCrtToken();
        if (crtToken.getType() == TOK_LPAREN) {
            match(TOK_LPAREN);
            ASTNode node = expression();
            match(TOK_RPAREN);
            return node;
        } else if (crtToken.getType() == TOK_FUNC) {
            ASTNode node = new ASTNode(crtToken);
            match(TOK_FUNC);
            return list(node);
        } else {
            throw error();
        }
    }

    /**
     * L -> '(' id RestL
     *
     * @param n DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode list(ASTNode n) throws ParseException {
        // L -> '(' id RestL
        checkCrtToken();
        if (crtToken.getType() == TOK_LPAREN) {
            match(TOK_LPAREN);
            ASTNode node = new ASTNode(crtToken);
            n.addChild(node);
            match(TOK_ID);
            return restList(n);
        } else {
            throw error();
        }
    }

    /**
     * RestL -> ',' id RestL | ')'
     *
     * @param n DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ASTNode restList(ASTNode n) throws ParseException {
        // RestL -> ',' id RestL | ')'
        checkCrtToken();
        if (crtToken.getType() == TOK_COMMA) {
            match(TOK_COMMA);
            ASTNode node = new ASTNode(crtToken);
            n.addChild(node);
            match(TOK_ID);
            return restList(n);
        } else if (crtToken.getType() == TOK_RPAREN) {
            match(TOK_RPAREN);
            return n;
        } else {
            throw error();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tok DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private ExpressionTokenizer.Token match(int tok) throws ParseException {
        checkCrtToken();
        if (crtToken.getType() == tok) {
            ExpressionTokenizer.Token ret = crtToken;
            crtToken = tokenizer.nextToken();
            return ret;
        } else {
            String msg =
                "Parse error at position " + crtToken.getOffset()
                + " (token number " + tokenizer.getTokenCount()
                + "); expected token type " + tok + ", but found '"
                + crtToken.getValue() + "'";
            throw new ParseException(msg);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private void checkCrtToken() throws ParseException {
        if (crtToken == null) {
            String msg = "Unexpected end of input!";
            throw new ParseException(msg);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ParseException error() {
        String msg =
            "Unexpected token '" + crtToken.getValue()
            + "' at position " + crtToken.getOffset();
        return new ParseException(msg);
    }

    /**
     * DOCUMENT ME!
     *
     * @param n
     *
     * @return
     *
     * @throws ParseException DOCUMENT ME!
     */
    private Condition buildCondition(ASTNode n) throws ParseException {
        Condition cond = new Condition();
        updateCondition(cond, n);
        return cond;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cond
     * @param n DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    private void updateCondition(Condition cond, ASTNode n)
                          throws ParseException {
        Condition c = null;
        switch (n.getToken().getType()) {
            case TOK_AND:
                c = cond.addAndClause();
                break;
            case TOK_OR:
                c = cond.addOrClause();
                break;
            case TOK_NOT:
                c = cond.addNotClause();
                break;
            case TOK_FUNC:
                Set states = getList(n);
                String val = n.getToken().getValue();
                if (ALL.equalsIgnoreCase(val)) {
                    cond.addAllClause(states);
                } else if (SOME.equalsIgnoreCase(val)) {
                    cond.addSomeClause(states);
                } else if (NONE.equalsIgnoreCase(val)) {
                    cond.addNoneClause(states);
                } else if (ANY.equalsIgnoreCase(val)) {
                    cond.addAnyClause(states);
                }
                return;
            default:
                String msg = "Invalid node: " + n.getToken();
                throw new ParseException(msg);
        }
        for (Iterator children = n.getChildren().iterator();
                children.hasNext();) {
            ASTNode child = (ASTNode) children.next();
            updateCondition(c, child);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param n
     *
     * @return
     */
    private Set getList(ASTNode n) {
        HashSet res = new HashSet();
        for (Iterator children = n.getChildren().iterator();
                children.hasNext();) {
            ASTNode child = (ASTNode) children.next();
            res.add(child.getToken().getValue());
        }
        return res;
    }
}
