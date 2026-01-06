lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}


// Deca lexer rules.

// Séparateurs
fragment EOL : '\n' | '\r' | '\r\n';
WS : (EOL | ' ' | '\t') { skip(); };

// Mots réservés
IF : 'if';
PRINTLNX : 'printlnx';
PRINTLN : 'println';
PRINTX : 'printx';
PRINT : 'print';
WHILE : 'while';
ASM : 'asm';
CLASS : 'class';
EXTENDS : 'extends';
ELSE : 'else';
FALSE : 'false';
INSTANCEOF : 'instanceof';
NEW : 'new';
NULL : 'null';
READINT : 'readInt';
READFLOAT : 'readFloat';
PROTECTED : 'protected';
RETURN : 'return';
THIS : 'this';
TRUE : 'true';

// Identificateurs
fragment LETTER : 'a' .. 'z' | 'A' .. 'Z';
fragment DIGIT : '0' .. '9';
IDENT : (LETTER | '$' | '_') (LETTER | DIGIT | '$' | '_')*;

// Symboles spéciaux
OPARENT : '('; // exemple : numéro 23 car c'est le 23 ème déclaré
CPARENT : ')';
COMMA : ',';
OBRACE : '{';
CBRACE : '}';
SEMI : ';';
NEQ : '!=';
EQEQ : '==';
EQUALS : '=';
DOT : '.';
LEQ : '<=';
LT : '<';
GEQ : '>=';
GT : '>';
SLASH : '/';
AND : '&&';
OR : '||';
EXCLAM : '!';
TIMES : '*';
PERCENT : '%';
PLUS : '+';
MINUS : '-';

// Littéraux entiers
fragment POSITIVE_DIGIT : '1' .. '9';
INT : '0' | POSITIVE_DIGIT DIGIT*;

//Littéraux flottants
fragment NUM : DIGIT+;
fragment SIGN : '+' | '-';
fragment EXP : ('E' | 'e') SIGN? NUM;
fragment DEC : NUM '.' NUM;
fragment FLOATDEC : (DEC | DEC EXP) ('F' | 'f')?;
fragment DIGITHEX : ('0' .. '9') | ('A' .. 'F') | ('a' .. 'f');
fragment NUMHEX : DIGITHEX+;
fragment FLOATHEX : ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN NUM ('F' | 'f')?;
FLOAT : FLOATDEC | FLOATHEX;

// Chaines de caractères
fragment STRING_CAR : ~( '"' | '\\' | '\r' | '\n');
STRING : '"' (STRING_CAR | '\\"' | '\\\\')* '"';
MULTI_LINE_STRING : '"' (STRING_CAR | EOL | '\\"' | '\\\\')* '"';

// Commentaires
COMMENT : ('/*' .*? '*/' {skip();}) | ('//' ~[\r\n]* {skip();});

// Inclusions de fichiers
fragment FILENAME : (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE : '#include' (' ')* '"' FILENAME '"' {
   doInclude(FILENAME.getText());
   };
   


// les numéros des tokens sont numérotés dans l'ordre de la déclaration