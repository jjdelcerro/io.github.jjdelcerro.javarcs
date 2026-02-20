package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSKeywordExpander;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import io.github.jjdelcerro.javarcs.lib.RCSManager.DiffFormat;
import io.github.jjdelcerro.javarcs.lib.commands.DiffOptions;

/**
 * Clase que encapsula las opciones para el comando `rcsdiff`. Adaptado de las
 * opciones de `rcsdiff.c`.
 */
public class DiffOptionsImpl implements DiffOptions {

  private List<Path> filePaths; // Archivos de trabajo para los cuales calcular el diff
  private String revision1; // -r flag, primera revisión a comparar
  private String revision2; // -r flag, segunda revisión a comparar (si se compara entre dos revisiones)
  private int keywordExpansionMode; // -k flag
  private boolean quiet; // -q flag
  private boolean forceAscii; // -a flag
  private boolean foldBlanks; // -b flag
  private boolean ignoreCase; // -i flag
  private boolean minimalDiff; // -d flag
  private boolean expandTabs; // -t flag
  private boolean ignoreBlanks; // -w flag
  // Formato de salida del diff (Normal, Contexto, Unificado, RCSDIFF)
  // Por ahora, DiffAlgorithm solo genera D_RCSDIFF.
  private DiffFormat outputFormat;
  private int contextLines; // -C o -U flag

  public DiffOptionsImpl(List<Path> filePaths) {
    this.filePaths = Objects.requireNonNull(filePaths, "File paths cannot be null");
    this.keywordExpansionMode = RCSKeywordExpander.KWEXP_DEFAULT; // Valor por defecto
    this.outputFormat = DiffFormat.RCSDIFF; // Por ahora, solo soportamos RCSDIFF
    this.contextLines = 3; // Valor por defecto para diff -C y -U
  }

  // Getters y Setters
  @Override
  public List<Path> getFilePaths() {
    return filePaths;
  }

  @Override
  public String getRevision1() {
    return revision1;
  }

  @Override
  public DiffOptionsImpl setRevision1(String revision1) {
    this.revision1 = revision1;
    return this;
  }

  @Override
  public String getRevision2() {
    return revision2;
  }

  @Override
  public DiffOptionsImpl setRevision2(String revision2) {
    this.revision2 = revision2;
    return this;
  }

  @Override
  public int getKeywordExpansionMode() {
    return keywordExpansionMode;
  }

  @Override
  public DiffOptionsImpl setKeywordExpansionMode(int keywordExpansionMode) {
    this.keywordExpansionMode = keywordExpansionMode;
    return this;
  }

  @Override
  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public DiffOptionsImpl setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }

  @Override
  public boolean isForceAscii() {
    return forceAscii;
  }

  @Override
  public DiffOptionsImpl setForceAscii(boolean forceAscii) {
    this.forceAscii = forceAscii;
    return this;
  }

  @Override
  public boolean isFoldBlanks() {
    return foldBlanks;
  }

  @Override
  public DiffOptionsImpl setFoldBlanks(boolean foldBlanks) {
    this.foldBlanks = foldBlanks;
    return this;
  }

  @Override
  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  @Override
  public DiffOptionsImpl setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
    return this;
  }

  @Override
  public boolean isMinimalDiff() {
    return minimalDiff;
  }

  @Override
  public DiffOptionsImpl setMinimalDiff(boolean minimalDiff) {
    this.minimalDiff = minimalDiff;
    return this;
  }

  @Override
  public boolean isExpandTabs() {
    return expandTabs;
  }

  @Override
  public DiffOptionsImpl setExpandTabs(boolean expandTabs) {
    this.expandTabs = expandTabs;
    return this;
  }

  @Override
  public boolean isIgnoreBlanks() {
    return ignoreBlanks;
  }

  @Override
  public DiffOptionsImpl setIgnoreBlanks(boolean ignoreBlanks) {
    this.ignoreBlanks = ignoreBlanks;
    return this;
  }

  @Override
  public DiffFormat getOutputFormat() {
    return outputFormat;
  }

  @Override
  public DiffOptionsImpl setOutputFormat(DiffFormat outputFormat) {
    this.outputFormat = outputFormat;
    return this;
  }

  @Override
  public int getContextLines() {
    return contextLines;
  }

  @Override
  public DiffOptionsImpl setContextLines(int contextLines) {
    this.contextLines = contextLines;
    return this;
  }
}
