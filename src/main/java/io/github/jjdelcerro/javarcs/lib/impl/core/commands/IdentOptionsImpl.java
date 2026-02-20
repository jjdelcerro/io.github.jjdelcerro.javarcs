package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import io.github.jjdelcerro.javarcs.lib.commands.IdentOptions;

/**
 * Clase que encapsula las opciones para el comando `ident`. Adaptado de las
 * opciones de `ident.c`.
 */
public class IdentOptionsImpl implements IdentOptions {

  private List<Path> filePaths; // Archivos a inspeccionar, si está vacío, usar stdin
  private boolean quiet; // -q flag

  public IdentOptionsImpl(List<Path> filePaths) {
    this.filePaths = Objects.requireNonNull(filePaths, "File paths cannot be null");
  }

  // Getters y Setters
  @Override
  public List<Path> getFilePaths() {
    return filePaths;
  }

  @Override
  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public IdentOptionsImpl setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }
}
