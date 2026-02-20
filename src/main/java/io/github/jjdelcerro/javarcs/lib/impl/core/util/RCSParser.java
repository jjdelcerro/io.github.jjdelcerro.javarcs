package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSAccessEntry;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSBranchEntry;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSDelta;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFile;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileFlag;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSLockEntry;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumber;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSSymbolEntry;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parser de alto rendimiento para archivos RCS (.v). Utiliza un enfoque de
 * flujo de bytes para garantizar la integridad de archivos binarios.
 */
public class RCSParser {

  private enum TokenType {
    KEYWORD, QUOTED, REVISION, WORD, SEMICOLON, COLON, EOF
  }

  private static class Token {

    final TokenType type;
    final String textValue;
    final byte[] byteValue;
    final int line;

    Token(TokenType t, String v, int l) {
      this.type = t;
      this.textValue = v;
      this.byteValue = null;
      this.line = l;
    }

    Token(TokenType t, byte[] b, int l) {
      this.type = t;
      this.textValue = null;
      this.byteValue = b;
      this.line = l;
    }
  }

  private PushbackInputStream is;
  private int currentLineNumber;
  private Token peekedToken;
  private RCSFile rcsFile;

  public RCSParser() {
  }

  /**
   * Parsea un archivo RCS completo.
   */
  public RCSFile parse(Path path) {
    this.rcsFile = new RCSFile(path);
    this.currentLineNumber = 1;
    this.peekedToken = null;

    try (InputStream fis = new BufferedInputStream(Files.newInputStream(path))) {
      this.is = new PushbackInputStream(fis, 2048);

      parseAdminSection();
      parseDeltasSection();
      parseDescriptionSection();
      parseDeltaTextsSection();

      rcsFile.addFlag(RCSFileFlag.PARSED);
      rcsFile.addFlag(RCSFileFlag.SYNCED);
      return rcsFile;

    } catch (IOException e) {
      throw new RCSException("Error de E/S al leer el archivo RCS: " + path, e);
    }
  }

  // --- SECCIONES DE PARSING ---
  private void parseAdminSection() throws IOException {
    expectKeyword("head");
    rcsFile.setHead(expectRevision());
    expectSeparator(';');

    if (peekKeyword("branch")) {
      consume();
      rcsFile.setBranch(expectRevision());
      expectSeparator(';');
    }

    expectKeyword("access");
    while (peekToken().type == TokenType.WORD) {
      rcsFile.addAccessEntry(new RCSAccessEntry(expectString()));
    }
    expectSeparator(';');

    expectKeyword("symbols");
    while (peekToken().type == TokenType.WORD) {
      String name = expectString();
      expectSeparator(':');
      rcsFile.addSymbolicName(new RCSSymbolEntry(name, expectRevision()));
    }
    expectSeparator(';');

    expectKeyword("locks");
    while (peekToken().type == TokenType.WORD) {
      String user = expectString();
      expectSeparator(':');
      rcsFile.addLock(new RCSLockEntry(user, expectRevision()));
    }
    if (peekKeyword("strict")) {
      consume();
      rcsFile.addFlag(RCSFileFlag.STRICT_LOCK);
    }
    expectSeparator(';');

    if (peekKeyword("comment")) {
      consume();
      rcsFile.setComment(expectQuotedString());
      expectSeparator(';');
    }

    if (peekKeyword("expand")) {
      consume();
      rcsFile.setExpandKeywords(expectQuotedString());
      expectSeparator(';');
    }
  }

  private void parseDeltasSection() throws IOException {
    while (peekToken().type == TokenType.REVISION) {
      RCSDelta delta = new RCSDelta(expectRevision());
      rcsFile.addDelta(delta);

      expectKeyword("date");
      delta.setDate(RCSTimeUtils.parseDate(expectToken(TokenType.REVISION).textValue));
      expectSeparator(';');

      expectKeyword("author");
      delta.setAuthor(expectString());
      expectSeparator(';');

      expectKeyword("state");
      delta.setState(expectString());
      expectSeparator(';');

      expectKeyword("branches");
      while (peekToken().type == TokenType.REVISION) {
        delta.addBranch(new RCSBranchEntry(expectRevision()));
      }
      expectSeparator(';');

      expectKeyword("next");
      if (peekToken().type == TokenType.REVISION) {
        delta.setNextRevision(expectRevision());
      }
      expectSeparator(';');
    }
  }

  private void parseDescriptionSection() throws IOException {
    expectKeyword("desc");
    rcsFile.setDescription(expectQuotedString());
  }

  private void parseDeltaTextsSection() throws IOException {
    while (peekToken().type != TokenType.EOF) {
      RCSRevisionNumber rev = expectRevision();
      RCSDelta delta = rcsFile.findDelta(rev);
      if (delta == null) {
        throw new RCSException("Línea " + currentLineNumber + ": Delta texto para revisión inexistente: " + rev);
      }

      expectKeyword("log");
      delta.setLogMessage(expectQuotedString());

      expectKeyword("text");
      delta.setDeltaText(expectQuotedBytes());
    }
  }

  // --- LEXER DE BYTES ---
  private Token lex() throws IOException {
    int c = skipWhitespace();
    if (c == -1) {
      return new Token(TokenType.EOF, "", currentLineNumber);
    }

    switch (c) {
      case ';':
        return new Token(TokenType.SEMICOLON, ";", currentLineNumber);
      case ':':
        return new Token(TokenType.COLON, ":", currentLineNumber);
      case '@':
        return new Token(TokenType.QUOTED, readQuotedBytes(), currentLineNumber);
      default:
        is.unread(c);
        return readWord();
    }
  }

  private Token readWord() throws IOException {
    StringBuilder sb = new StringBuilder();
    int c;
    while ((c = is.read()) != -1) {
      if (Character.isWhitespace(c) || c == ';' || c == ':' || c == '@') {
        is.unread(c);
        break;
      }
      sb.append((char) c);
    }
    String val = sb.toString();
    if (val.matches("^(\\d+\\.)*\\d+$")) {
      return new Token(TokenType.REVISION, val, currentLineNumber);
    }

    // Determinar si es Keyword o Word normal
    if (isKeyword(val)) {
      return new Token(TokenType.KEYWORD, val, currentLineNumber);
    }
    return new Token(TokenType.WORD, val, currentLineNumber);
  }

  private byte[] readQuotedBytes() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int c;
    while ((c = is.read()) != -1) {
      if (c == '@') {
        int next = is.read();
        if (next == '@') {
          baos.write('@'); // Arroba escapada
        } else {
          is.unread(next); // Fin de cadena
          break;
        }
      } else {
        if (c == '\n') {
          currentLineNumber++;
        }
        baos.write(c);
      }
    }
    return baos.toByteArray();
  }

  private int skipWhitespace() throws IOException {
    int c;
    while ((c = is.read()) != -1) {
      if (Character.isWhitespace(c)) {
        if (c == '\n') {
          currentLineNumber++;
        }
        continue;
      }
      if (c == '#') { // Comentarios de sistema
        while ((c = is.read()) != -1 && c != '\n');
        if (c == '\n') {
          currentLineNumber++;
        }
        continue;
      }
      return c;
    }
    return -1;
  }

  // --- UTILIDADES ---
  private boolean isKeyword(String s) {
    switch (s) {
      case "head":
      case "branch":
      case "access":
      case "symbols":
      case "locks":
      case "strict":
      case "comment":
      case "expand":
      case "desc":
      case "date":
      case "author":
      case "state":
      case "branches":
      case "next":
      case "log":
      case "text":
        return true;
      default:
        return false;
    }
  }

  private Token peekToken() throws IOException {
    if (peekedToken == null) {
      peekedToken = lex();
    }
    return peekedToken;
  }

  private Token nextToken() throws IOException {
    Token t = (peekedToken != null) ? peekedToken : lex();
    peekedToken = null;
    return t;
  }

  private void consume() throws IOException {
    nextToken();
  }

  private boolean peekKeyword(String k) throws IOException {
    Token t = peekToken();
    return t.type == TokenType.KEYWORD && k.equals(t.textValue);
  }

  private void expectKeyword(String k) throws IOException {
    Token t = nextToken();
    if (t.type != TokenType.KEYWORD || !k.equals(t.textValue)) {
      throw new RCSException("Línea " + t.line + ": Se esperaba palabra clave '" + k + "' pero se encontró " + t.type + " '" + t.textValue + "'");
    }
  }

  private RCSRevisionNumber expectRevision() throws IOException {
    Token t = nextToken();
    if (t.type != TokenType.REVISION) {
      throw new RCSException("Línea " + t.line + ": Se esperaba número de revisión.");
    }
    return RCSRevisionNumber.parse(t.textValue);
  }

  private String expectString() throws IOException {
    Token t = nextToken();
    if (t.type != TokenType.WORD && t.type != TokenType.KEYWORD) {
      throw new RCSException("Línea " + t.line + ": Se esperaba identificador.");
    }
    return t.textValue;
  }

  private String expectQuotedString() throws IOException {
    Token t = nextToken();
    if (t.type != TokenType.QUOTED) {
      throw new RCSException("Línea " + t.line + ": Se esperaba texto entre @.");
    }
    return new String(t.byteValue, StandardCharsets.UTF_8);
  }

  private byte[] expectQuotedBytes() throws IOException {
    Token t = nextToken();
    if (t.type != TokenType.QUOTED) {
      throw new RCSException("Línea " + t.line + ": Se esperaba bloque de datos entre @.");
    }
    return t.byteValue;
  }

  private void expectSeparator(char s) throws IOException {
    Token t = nextToken();
    if (s == ';' && t.type != TokenType.SEMICOLON) {
      throw new RCSException("Línea " + t.line + ": Se esperaba ';'");
    }
    if (s == ':' && t.type != TokenType.COLON) {
      throw new RCSException("Línea " + t.line + ": Se esperaba ':'");
    }
  }

  private Token expectToken(TokenType type) throws IOException {
    Token t = nextToken();
    if (t.type != type) {
      throw new RCSException("Línea " + t.line + ": Se esperaba token de tipo " + type);
    }
    return t;
  }
}
