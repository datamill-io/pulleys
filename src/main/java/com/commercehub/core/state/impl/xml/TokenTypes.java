
package com.commercehub.core.state.impl.xml;
public interface TokenTypes {
    String ALL = "ALL";
    String SOME = "SOME";
    String NONE = "NONE";
    String ANY = "ANY";
    String AND = "AND";
    String OR = "OR";
    String NOT = "NOT";
    int TOK_ALL = 1;
    int TOK_SOME = 2;
    int TOK_NONE = 3;
    int TOK_ANY = 4;
    int TOK_AND = 5;
    int TOK_OR = 6;
    int TOK_NOT = 7;
    int TOK_LPAREN = 8;
    int TOK_RPAREN = 9;
    int TOK_COMMA = 10;
    int TOK_ID = 11;
    int TOK_FUNC = 12;
}
