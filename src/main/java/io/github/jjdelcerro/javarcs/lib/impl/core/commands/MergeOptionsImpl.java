package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import io.github.jjdelcerro.javarcs.lib.commands.MergeOptions;

/**
 * Clase que encapsula las opciones para el comando `rcsmerge` o `merge`.
 * Adaptado de las opciones de `rcsmerge.c` y `merge.c`.
 */
public class MergeOptionsImpl implements MergeOptions {

  private Path workFilePath; // El archivo de trabajo donde se aplica la fusión
  private String baseRevision; // -r flag, la revisión base para la fusión
  private String compareRevision; // La revisión con la que se compara la base (opcional, si no, HEAD)
  private List<String> labels; // -L flag, etiquetas para los bloques de conflicto
  private boolean quiet; // -q flag
  private boolean pipeOut; // -p flag, escribir a stdout
  private boolean eflag; // -e flag (generar script de edición para ed)
  private boolean eflagPlusOflag; // -E flag (generar script de edición con overlaps)

  public MergeOptionsImpl(Path workFilePath, String baseRevision) {
    this.workFilePath = Objects.requireNonNull(workFilePath, "Work file path cannot be null");
    this.baseRevision = Objects.requireNonNull(baseRevision, "Base revision cannot be null");
    this.labels = Collections.emptyList();
  }

  // Getters y Setters
  @Override
  public Path getWorkFilePath() {
    return workFilePath;
  }

  @Override
  public String getBaseRevision() {
    return baseRevision;
  }

  @Override
  public String getCompareRevision() {
    return compareRevision;
  }

  @Override
  public MergeOptionsImpl setCompareRevision(String compareRevision) {
    this.compareRevision = compareRevision;
    return this;
  }

  @Override
  public List<String> getLabels() {
    return labels;
  }

  @Override
  public MergeOptionsImpl setLabels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  @Override
  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public MergeOptionsImpl setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }

  @Override
  public boolean isPipeOut() {
    return pipeOut;
  }

  @Override
  public MergeOptionsImpl setPipeOut(boolean pipeOut) {
    this.pipeOut = pipeOut;
    return this;
  }

  @Override
  public boolean isEflag() {
    return eflag;
  }

  @Override
  public MergeOptionsImpl setEflag(boolean eflag) {
    this.eflag = eflag;
    return this;
  }

  @Override
  public boolean isEflagPlusOflag() {
    return eflagPlusOflag;
  }

  @Override
  public MergeOptionsImpl setEflagPlusOflag(boolean eflagPlusOflag) {
    this.eflagPlusOflag = eflagPlusOflag;
    return this;
  }
}
