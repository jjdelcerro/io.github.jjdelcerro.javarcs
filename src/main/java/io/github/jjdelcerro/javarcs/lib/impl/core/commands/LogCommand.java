package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSAccessEntry;
import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.RCSDelta;
import io.github.jjdelcerro.javarcs.lib.RCSLockEntry;
import io.github.jjdelcerro.javarcs.lib.RCSSymbolEntry;
import io.github.jjdelcerro.javarcs.lib.commands.LogOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileFlag;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSFileUtils;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSParser;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSTimeUtils;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;
import java.io.PrintStream;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementa el comando 'rlog'. Muestra el historial completo de revisiones,
 * metadatos del archivo y mensajes de log.
 */
@SuppressWarnings("UseSpecificCatch")
public class LogCommand implements RCSCommand<LogOptions> {

  private static final String REV_SEP = "----------------------------";
  private static final String FILE_END = "=============================================================================";
  private LogOptions options;

  @Override
  public void execute(LogOptions options) throws RCSException {
    this.options = options;
    Path workFilePath = options.getWorkFilePath();

    try {
      // 1. Localizar y parsear el archivo RCS
      Optional<Path> rcsPathOpt = RCSFileUtils.chooseRCSFile(workFilePath, null);
      if (rcsPathOpt.isEmpty()) {
        throw new RCSException("No se encontró el archivo RCS para: " + workFilePath);
      }

      RCSFileImpl rcsFile = new RCSParser().parse(rcsPathOpt.get());

      // 2. Imprimir cabeceras administrativas (solo si no es modo quiet)
      if (!options.isQuiet()) {
        printHeader(rcsFile, workFilePath);
      }

      // 3. Obtener y filtrar deltas
      List<RCSDelta> sortedDeltas = rcsFile.getDeltas().stream()
              .sorted((d1, d2) -> d2.getRevisionNumber().compareTo(d1.getRevisionNumber(), 0))
              .filter(delta -> filterDelta(delta, options))
              .collect(Collectors.toList());

      // 4. Imprimir cada revisión
      if (!options.isHeaderOnly()) {
        for (RCSDelta delta : sortedDeltas) {
          printDelta(delta, options);
        }
      }

      if (!options.isQuiet()) {
        this.getOutput().println(FILE_END);
      }

    } catch (Exception e) {
      throw new RCSException("Error al ejecutar rlog: " + e.getMessage(), e);
    }
  }

  private PrintStream getOutput() {
    PrintStream out = this.options.getOutputStream();
    if( out == null ) {
      return System.out;
    }
    return out;
  }
  /**
   * Imprime la sección de metadatos del archivo RCS.
   */
  private void printHeader(RCSFileImpl rcsFile, Path workFilePath) {
    this.getOutput().println("RCS file: " + rcsFile.getFilePath());
    this.getOutput().println("Working file: " + workFilePath.getFileName());

    this.getOutput().print("head:");
    if (rcsFile.getHead() != null) {
      this.getOutput().print(" " + rcsFile.getHead());
    }
    this.getOutput().println();

    this.getOutput().print("branch:");
    if (rcsFile.getBranch() != null) {
      this.getOutput().print(" " + rcsFile.getBranch());
    }
    this.getOutput().println();

    this.getOutput().print("locks: ");
    if (rcsFile.hasFlag(RCSFileFlag.STRICT_LOCK)) {
      this.getOutput().print("strict");
    }
    for (RCSLockEntry lock : rcsFile.getLocks()) {
      this.getOutput().print("\n\t" + lock.getUsername() + ": " + lock.getRevisionNumber());
    }
    this.getOutput().println();

    this.getOutput().print("access list:");
    if (rcsFile.getAccessEntries().isEmpty()) {
      this.getOutput().print(" (empty)");
    } else {
      for (RCSAccessEntry access : rcsFile.getAccessEntries()) {
        this.getOutput().print("\n\t" + access.getUsername());
      }
    }
    this.getOutput().println();

    this.getOutput().println("symbolic names:");
    for (RCSSymbolEntry symbol : rcsFile.getSymbolicNames()) {
      this.getOutput().println("\t" + symbol.getName() + ": " + symbol.getRevisionNumber());
    }

    this.getOutput().println("keyword substitution: " + (rcsFile.getExpandKeywords() != null ? rcsFile.getExpandKeywords() : "kv"));
    this.getOutput().println("total revisions: " + rcsFile.getDeltas().size());

    if (rcsFile.getDescription() != null && !rcsFile.getDescription().isEmpty()) {
      this.getOutput().println("description:");
      this.getOutput().println(rcsFile.getDescription());
    }
  }

  /**
   * Imprime la información de una revisión específica.
   */
  private void printDelta(RCSDelta delta, LogOptions options) {
    this.getOutput().println(REV_SEP);
    this.getOutput().println("revision " + delta.getRevisionNumber());

    String dateStr = RCSTimeUtils.formatTime(delta.getDate(), options.isIsoTimeFormat(), null);
    this.getOutput().print("date: " + dateStr + ";  author: " + delta.getAuthor() + ";  state: " + delta.getState() + ";");

    if (delta.getCommitId() != null) {
      this.getOutput().print("  commitid: " + delta.getCommitId() + ";");
    }
    this.getOutput().println();

    if (delta.getLogMessage() != null) {
      this.getOutput().println(delta.getLogMessage());
    }
  }

  /**
   * Aplica los filtros de usuario (por autor, estado, etc.)
   */
  private boolean filterDelta(RCSDelta delta, LogOptions options) {
    // Filtrar por autor (-w)
    if (options.getAuthors() != null && !options.getAuthors().isEmpty()) {
      if (!options.getAuthors().contains(delta.getAuthor())) {
        return false;
      }
    }

    // Filtrar por estado (-s)
    if (options.getStates() != null && !options.getStates().isEmpty()) {
      if (!options.getStates().contains(delta.getState())) {
        return false;
      }
    }

    // Filtrar por lista de revisiones (-r)
    if (options.getRevisions() != null && !options.getRevisions().isEmpty()) {
      boolean match = false;
      for (String revStr : options.getRevisions()) {
        if (delta.getRevisionNumber().toString().startsWith(revStr)) {
          match = true;
          break;
        }
      }
      if (!match) {
        return false;
      }
    }

    return true;
  }
}
