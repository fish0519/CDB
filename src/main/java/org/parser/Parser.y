%language "Java"
%file-prefix "Parser"
%define api.parser.public
%define api.parser.class {Parser}
%define api.package {org.parser}

%locations

%{
    import java.io.*;
    import org.parser.Position;
%}

%code{
    boolean interactive = true;
    public StringBuilder result = new StringBuilder();
}

%token NL
%token <Float> NUM

%type <Float> exp

%left '-' '+'
%left '*' '/'
%left NEG
%right '^'

%%

input: /* empty string */
    | input line
    ;

line: NL { }
    | exp NL {
        result.append(((MyLexer)yylexer).getOriginStr());
        result.append(" = " + $exp);
    }
    ;

exp: NUM                  { $$ = $1; }
   | exp '+' exp          { $$ = $1 + $3; }
   | exp '-' exp          { $$ = $1 - $3; }
   | exp '*' exp          { $$ = $1 * $3; }
   | exp '/' exp          { $$ = $1 / $3; }
   | '-' exp %prec NEG    { $$ = -$2; }
   | exp '^' exp    { $$ = Math.pow($1,$3); }
   | '(' exp ')'    { $$ = $2; }
   ;

%%
