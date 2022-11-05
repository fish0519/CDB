/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.parser;

%%

%public
%class MyLexer
%implements Parser.Lexer

%byaccj

%{
    private Object yylval;

    int preLN = 0;

    String originStr = "";

    public String getOriginStr()
    {
        return originStr;
    }

    @Override
    public Position getStartPos()
    {
        return new Position(yyline, yycolumn, yychar);
    }

    @Override
    public Position getEndPos()
    {
        int yylength = yylength();
        return new Position(yyline, yycolumn + yylength, yychar + yylength);
    }

    @Override
    public Object getLVal()
    {
        return yylval;
    }

    @Override
    public void yyerror(Parser.Location loc, String msg)
    {
        Position pos = (loc != null ? loc.begin : null);
        if(pos != null)
        {
            msg = "error(line:" + pos.line + ",column:" + pos.column + ",char" + pos.chars + "); " + msg;
        }else{
            msg = "error:" + msg;
        }
        yyerror(msg);
    }

    private void yyerror(String msg)
    {
        throw new RuntimeException(msg);
    }
%}

NUM = [0-9]+ ("." [0-9]+)?
NL  = \n | \r | \r\n

%%

/* operators */
"+" |
"-" |
"*" |
"/" |
"^" |
"(" |
")"    { return yycharat(0); }

{NL} {
        if(0 == zzStartRead)
        {
            originStr = new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
        }else{
            originStr = new String(zzBuffer, preLN, zzStartRead-preLN);
        }
        preLN = zzStartRead + 1;
        return MyLexer.NL;
}

{NUM} {
        yylval = Float.parseFloat(yytext());
        return MyLexer.NUM;
}

/* whitespace */
[ \t]+ { }

\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }