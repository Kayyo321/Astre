package LexicalAnalysis;

import java.util.ArrayList;
import java.util.*;

import Astre.Astre;

import static LexicalAnalysis.TokenType.*;

public class Scanner {
    private final String code;
    private final List<Token> tokens = new ArrayList<>();
    private final Map<String, TokenType> keywords = new HashMap<>();;
    private int start = 0, current = start, line = 1;

    public Scanner(String code) {
        this.code = code;
        keywords.put("and", And);
        keywords.put("struct", Struct);
        keywords.put("else", Else);
        keywords.put("false", False);
        keywords.put("for", For);
        keywords.put("function", Function);
        keywords.put("if", If);
        keywords.put("nothing", Nothing);
        keywords.put("or", Or);
        keywords.put("print", Print);
        keywords.put("return", Return);
        keywords.put("super", Super);
        keywords.put("self", Self);
        keywords.put("true", True);
        keywords.put("let", Let);
        keywords.put("while", While);
        keywords.put("derives", Derives);
    }

    public List<Token> scan() {
        while (!atEOF()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        final char c;
        switch (c = eat()) {
            case '(': addToken(LParen); break;
            case ')': addToken(RParen); break;
            case '{': addToken(LBrace); break;
            case '}': addToken(RBrace); break;
            case ',': addToken(Comma); break;
            case '-': addToken(Minus); break;
            case '+': addToken(Plus); break;
            case ';': addToken(Semicolon); break;
            case '*': addToken(Star); break;
            case '%': addToken(Modulo); break;
            case '^': addToken(Power); break;
            case '!': addToken(match('=') ? BangEqual : Bang); break;
            case '=': addToken(match('=') ? EqualEqual : Equal); break;
            case '<': addToken(match('=') ? LessEqual : Less); break;
            case '>': addToken(match('=') ? GreaterEqual : Greater); break;
            case '.':
                if (isDigit(peekNext())) {
                    --current;
                    number();
                } else {
                    addToken(Dot);
                }
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !atEOF()) {
                        eat();
                    }
                } else {
                    addToken(Slash);
                }
                break;
            case ' ' :
            case '\r':
            case '\t':
                break;
            case '\n':
                ++line;
                break;
            case '"' :
            case '\'':
                string(c); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Astre.error(line, "Unexpected character: " + c);
                }
                break;
        }
    }

    private char peek() {
        if (atEOF()) {
            return '\0';
        }

        return code.charAt(current);
    }

    private char peekNext() {
        if (current+1 >= code.length()) {
            return '\0';
        }

        return code.charAt(current+1);
    }

    private char eat() {
        return code.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, code.substring(start, current), literal, line));
    }

    private void identifier() {
        while (isAlpha(peek()) || isDigit(peek())) {
            eat();
        }

        TokenType type = keywords.get(code.substring(start, current));
        if (type == null) {
            type = Identifier;
        }

        addToken(type);
    }

    private void string(char other) {
        while (peek() != other && !atEOF()) {
            if (eat() == '\n') {
                ++line;
            }
        }

        if (atEOF()) {
            Astre.error(line, "Unterminated string.");
            return;
        }

        eat();

        addToken(String, code.substring(start+1, current-1));
    }

    private void number() {
        while (isDigit(peek())) {
            eat();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            eat();

            do {
                eat();
            } while (isDigit(peek()));
        }

        addToken(Number, Double.parseDouble(code.substring(start, current)));
    }

    private boolean atEOF() {
        return current >= code.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean match(char expected) {
        if (peek() != expected) {
            return false;
        }

        ++current;
        return true;
    }
}
