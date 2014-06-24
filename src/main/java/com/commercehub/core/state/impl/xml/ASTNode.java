
package com.commercehub.core.state.impl.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract syntax tree node for our expression language.
 *
 * @author aalbu
 */
public class ASTNode {
    private ExpressionTokenizer.Token token;
    private List children;

    /**
     * Initializes a new ASTNode object.
     *
     * @param t the token that represents it in the input stream
     */
    ASTNode(ExpressionTokenizer.Token t) {
        token = t;
        children = new ArrayList();
    }

    /**
     * DOCUMENT ME!
     *
     * @param child DOCUMENT ME!
     */
    public void addChild(ASTNode child) {
        children.add(child);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getChildren() {
        return children;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ExpressionTokenizer.Token getToken() {
        return token;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return toString(0).toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param indent DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public StringBuffer toString(int indent) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            buf.append(' ');
        }
        buf.append(token.getValue());
        for (Iterator c = children.iterator(); c.hasNext();) {
            ASTNode child = (ASTNode) c.next();
            buf.append("\n");
            buf.append(child.toString(indent + 4));
        }
        return buf;
    }
    /*
     * public StringBuffer toString(int indent) { StringBuffer buf = new
     * StringBuffer(); for (int i = 0; i < indent; i++) {     buf.append(' '); }
     * buf.append(token.getValue()); buf.append(toStringChildren(indent + 4));
     * return buf; }
     *
     * StringBuffer toStringChildren(int indent) { StringBuffer buf = new
     * StringBuffer(); for (Iterator c = children.iterator(); c.hasNext(); ) {
     * ASTNode child = (ASTNode) c.next();     buf.append("\n");
     * buf.append(child.toString(indent + 4)); } return buf; }
     */
}
