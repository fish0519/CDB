package org.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

public class CalcLexer implements Calc.Lexer {

    StreamTokenizer st;
    PositionReader reader;

    public CalcLexer(InputStream is) {
        reader = new PositionReader(new InputStreamReader(is));
        st = new StreamTokenizer(reader);
        st.resetSyntax();
        st.eolIsSignificant(true);
        st.wordChars('0', '9');
    }

    Position start = new Position(1, 0);
    Position end = new Position(1, 0);

    /**
     * The location of the last token read.
     * Implemented with getStartPos and getEndPos in pull parsers.
     */
    public Calc.Location getLocation() {
        return new Calc.Location(new Position(start), new Position(end));
    }

    /**
     * Build and emit a syntax error message.
     */
    public void reportSyntaxError(Calc.Context ctx) {
        System.err.print(ctx.getLocation() + ": syntax error");
        {
            final int TOKENMAX = 10;
            Calc.SymbolKind[] arg = new Calc.SymbolKind[TOKENMAX];
            int n = ctx.getExpectedTokens(arg, TOKENMAX);
            for (int i = 0; i < n; ++i)
                System.err.print((i == 0 ? ": expected " : " or ")
                        + arg[i].getName());
        }
        {
            Calc.SymbolKind lookahead = ctx.getToken();
            if (lookahead != null)
                System.err.print(" before " + lookahead.getName());
        }
        System.err.println("");
    }

    /**
     * Emit an error referring to the given location in a user-defined way.
     *
     * @@param loc The location of the element to which the
     *                error message is related.
     * @@param msg The string for the error message.
     */
    public void yyerror(Calc.Location loc, String msg) {
        if (loc == null)
            System.err.println(msg);
        else
            System.err.println(loc + ": " + msg);
    }

    Integer yylval;

    /**
     * The value of the last token read.  Called getLVal in pull parsers.
     */
    public Object getValue() {
        return yylval;
    }

    /**
     * Fetch the next token.  Called yylex in pull parsers.
     */
    public int getToken() throws IOException {
        start.set(reader.getPosition());
        int ttype = st.nextToken();
        end.set(reader.getPosition());
        switch (ttype) {
            case StreamTokenizer.TT_EOF:
                return YYEOF;
            case StreamTokenizer.TT_EOL:
                end.line += 1;
                end.column = 0;
                return EOL;
            case StreamTokenizer.TT_WORD:
                yylval = Integer.parseInt(st.sval);
                end.set(reader.getPreviousPosition());
                return NUM;
            case ' ': case '\t':
                return getToken();
            case '!':
                return BANG;
            case '+':
                return PLUS;
            case '-':
                return MINUS;
            case '*':
                return STAR;
            case '/':
                return SLASH;
            case '^':
                return CARET;
            case '(':
                return LPAREN;
            case ')':
                return RPAREN;
            case '=':
                return EQUAL;
            default:
                throw new AssertionError("invalid character: " + ttype);
        }
    }
}
