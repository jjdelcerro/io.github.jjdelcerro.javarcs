package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSKeywordExpander;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import io.github.jjdelcerro.javarcs.lib.commands.CleanOptions;

/**
 * Clase que encapsula las opciones para el comando `rcsclean`. Adaptado de las
 * opciones de `rcsclean.c`.
 */
public class CleanOptionsImpl implements CleanOptions {

  private List<Path> filePaths; // Archivos a limpiar. Si está vacío, limpiar el directorio actual.
  private int keywordExpansionMode; // -k flag
  private boolean dryRun; // -n flag (no realizar cambios, solo imprimir lo que se haría)
  private boolean quiet; // -q flag
  private String revision; // -r flag
  private boolean unlock; // -u flag
  private boolean preserveTime; // -T flag

  public CleanOptionsImpl(List<Path> filePaths) {
    this.filePaths = Objects.requireNonNull(filePaths, "File paths cannot be null");
    this.keywordExpansionMode = RCSKeywordExpander.KWEXP_DEFAULT; // Valor por defecto
  }

  // Getters y Setters
  @Override
  public List<Path> getFilePaths() {
    return filePaths;
  }

  @Override
  public int getKeywordExpansionMode() {
    return keywordExpansionMode;
  }

  @Override
  public CleanOptionsImpl setKeywordExpansionMode(int keywordExpansionMode) {
    this.keywordExpansionMode = keywordExpansionMode;
    return this;
  }

  @Override
  public boolean isDryRun() {
    return dryRun;
  }

  @Override
  public CleanOptionsImpl setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }

  @Override
  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public CleanOptionsImpl setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }

  @Override
  public String getRevision() {
    return revision;
  }

  @Override
  public CleanOptionsImpl setRevision(String revision) {
    this.revision = revision;
    return this;
  }

  @Override
  public boolean isUnlock() {
    return unlock;
  }

  @Override
  public CleanOptionsImpl setUnlock(boolean unlock) {
    this.unlock = unlock;
    return this;
  }

  @Override
  public boolean isPreserveTime() {
    return preserveTime;
  }

  @Override
  public CleanOptionsImpl setPreserveTime(boolean preserveTime) {
    this.preserveTime = preserveTime;
    return this;
  }
}
