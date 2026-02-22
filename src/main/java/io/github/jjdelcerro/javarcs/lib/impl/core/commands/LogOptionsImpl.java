package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import io.github.jjdelcerro.javarcs.lib.commands.LogOptions;
import java.io.PrintStream;

/**
 * Clase que encapsula las opciones para el comando `rlog`. Adaptado de las
 * opciones de `rlog.c`.
 */
public class LogOptionsImpl implements LogOptions {

  private final Path workFilePath;
  private List<String> revisions; // -r flag
  private List<String> dates; // -d flag
  private List<String> authors; // -w flag
  private List<String> states; // -s flag
  private boolean headerOnly; // -h flag
  private boolean descriptionOnly; // -t flag
  private boolean quiet; // -q flag
  private boolean isoTimeFormat; // -z flag (implica formato ISO en rlog)
  private PrintStream out;

  public LogOptionsImpl(Path workFilePath) {
    this.workFilePath = Objects.requireNonNull(workFilePath, "Work file path cannot be null");
    this.revisions = Collections.emptyList();
    this.dates = Collections.emptyList();
    this.authors = Collections.emptyList();
    this.states = Collections.emptyList();
  }

  // Getters y Setters
  @Override
  public Path getWorkFilePath() {
    return workFilePath;
  }

  @Override
  public List<String> getRevisions() {
    return revisions;
  }

  @Override
  public LogOptionsImpl setRevisions(List<String> revisions) {
    this.revisions = revisions;
    return this;
  }

  @Override
  public List<String> getDates() {
    return dates;
  }

  @Override
  public LogOptionsImpl setDates(List<String> dates) {
    this.dates = dates;
    return this;
  }

  @Override
  public List<String> getAuthors() {
    return authors;
  }

  @Override
  public LogOptionsImpl setAuthors(List<String> authors) {
    this.authors = authors;
    return this;
  }

  @Override
  public List<String> getStates() {
    return states;
  }

  @Override
  public LogOptionsImpl setStates(List<String> states) {
    this.states = states;
    return this;
  }

  @Override
  public boolean isHeaderOnly() {
    return headerOnly;
  }

  @Override
  public LogOptionsImpl setHeaderOnly(boolean headerOnly) {
    this.headerOnly = headerOnly;
    return this;
  }

  @Override
  public boolean isDescriptionOnly() {
    return descriptionOnly;
  }

  @Override
  public LogOptionsImpl setDescriptionOnly(boolean descriptionOnly) {
    this.descriptionOnly = descriptionOnly;
    return this;
  }

  @Override
  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public LogOptionsImpl setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }

  @Override
  public boolean isIsoTimeFormat() {
    return isoTimeFormat;
  }

  @Override
  public LogOptionsImpl setIsoTimeFormat(boolean isoTimeFormat) {
    this.isoTimeFormat = isoTimeFormat;
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
