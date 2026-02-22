package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSKeywordExpander;

import java.nio.file.Path;
import java.util.Objects;
import io.github.jjdelcerro.javarcs.lib.commands.CheckoutOptions;
import java.io.PrintStream;

/**
 * Clase que encapsula las opciones para el comando `co` (checkout). Adaptado de
 * las opciones de `co.c`.
 */
public class CheckoutOptionsImpl implements CheckoutOptions {

  private Path workFilePath; // Archivo de trabajo donde se escribirá la revisión
  private String revision; // revision_str en C, la revisión a extraer (ej. "1.1")
  private int keywordExpansionMode; // kflag en C (KWEXP_NONE, KWEXP_NAME, etc.)
  private boolean lock; // -l flag
  private boolean unlock; // -u flag
  private boolean quiet; // -q flag
  private String author; // -w flag
  private String state; // -s flag
  private String date; // -d flag
  private boolean force; // -f flag (sobrescribir si existe)
  private boolean pipeOut; // -p flag (escribir a stdout)
  private boolean preserveTime; // -T flag (preservar tiempo de modificación)
  private PrintStream out;

  public CheckoutOptionsImpl(Path workFilePath) {
    this.workFilePath = Objects.requireNonNull(workFilePath, "Work file path cannot be null");
    this.keywordExpansionMode = RCSKeywordExpander.KWEXP_DEFAULT; // Valor por defecto
  }

  // Getters y Setters
  @Override
  public Path getWorkFilePath() {
    return workFilePath;
  }

  @Override
  public String getRevision() {
    return revision;
  }

  @Override
  public CheckoutOptionsImpl setRevision(String revision) {
    this.revision = revision;
    return this;
  }

  @Override
  public int getKeywordExpansionMode() {
    return keywordExpansionMode;
  }

  @Override
  public CheckoutOptionsImpl setKeywordExpansionMode(int keywordExpansionMode) {
    this.keywordExpansionMode = keywordExpansionMode;
    return this;
  }

  @Override
  public boolean isLock() {
    return lock;
  }

  @Override
  public CheckoutOptionsImpl setLock(boolean lock) {
    this.lock = lock;
    return this;
  }

  @Override
  public boolean isUnlock() {
    return unlock;
  }

  @Override
  public CheckoutOptionsImpl setUnlock(boolean unlock) {
    this.unlock = unlock;
    return this;
  }

  @Override
  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public CheckoutOptionsImpl setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }

  @Override
  public String getAuthor() {
    return author;
  }

  @Override
  public CheckoutOptionsImpl setAuthor(String author) {
    this.author = author;
    return this;
  }

  @Override
  public String getState() {
    return state;
  }

  @Override
  public CheckoutOptionsImpl setState(String state) {
    this.state = state;
    return this;
  }

  @Override
  public String getDate() {
    return date;
  }

  @Override
  public CheckoutOptionsImpl setDate(String date) {
    this.date = date;
    return this;
  }

  @Override
  public boolean isForce() {
    return force;
  }

  @Override
  public CheckoutOptionsImpl setForce(boolean force) {
    this.force = force;
    return this;
  }

  @Override
  public boolean isPipeOut() {
    return pipeOut;
  }

  @Override
  public CheckoutOptionsImpl setPipeOut(boolean pipeOut) {
    this.pipeOut = pipeOut;
    return this;
  }

  @Override
  public boolean isPreserveTime() {
    return preserveTime;
  }

  @Override
  public CheckoutOptionsImpl setPreserveTime(boolean preserveTime) {
    this.preserveTime = preserveTime;
    return this;
  }

  @Override
  public void setOutputStream(PrintStream out) {
    this.out = out;
  }

  @Override
  public PrintStream getOutputStream() {
    return this.out;
  }
}
