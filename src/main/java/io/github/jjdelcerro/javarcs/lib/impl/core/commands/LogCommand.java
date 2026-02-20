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

  @Override
  public void execute(LogOptions options) throws RCSException {
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
        System.out.println(FILE_END);
      }

    } catch (Exception e) {
      throw new RCSException("Error al ejecutar rlog: " + e.getMessage(), e);
    }
  }

  /**
   * Imprime la sección de metadatos del archivo RCS.
   */
  private void printHeader(RCSFileImpl rcsFile, Path workFilePath) {
    System.out.println("RCS file: " + rcsFile.getFilePath());
    System.out.println("Working file: " + workFilePath.getFileName());

    System.out.print("head:");
    if (rcsFile.getHead() != null) {
      System.out.print(" " + rcsFile.getHead());
    }
    System.out.println();

    System.out.print("branch:");
    if (rcsFile.getBranch() != null) {
      System.out.print(" " + rcsFile.getBranch());
    }
    System.out.println();

    System.out.print("locks: ");
    if (rcsFile.hasFlag(RCSFileFlag.STRICT_LOCK)) {
      System.out.print("strict");
    }
    for (RCSLockEntry lock : rcsFile.getLocks()) {
      System.out.print("\n\t" + lock.getUsername() + ": " + lock.getRevisionNumber());
    }
    System.out.println();

    System.out.print("access list:");
    if (rcsFile.getAccessEntries().isEmpty()) {
      System.out.print(" (empty)");
    } else {
      for (RCSAccessEntry access : rcsFile.getAccessEntries()) {
        System.out.print("\n\t" + access.getUsername());
      }
    }
    System.out.println();

    System.out.println("symbolic names:");
    for (RCSSymbolEntry symbol : rcsFile.getSymbolicNames()) {
      System.out.println("\t" + symbol.getName() + ": " + symbol.getRevisionNumber());
    }

    System.out.println("keyword substitution: " + (rcsFile.getExpandKeywords() != null ? rcsFile.getExpandKeywords() : "kv"));
    System.out.println("total revisions: " + rcsFile.getDeltas().size());

    if (rcsFile.getDescription() != null && !rcsFile.getDescription().isEmpty()) {
      System.out.println("description:");
      System.out.println(rcsFile.getDescription());
    }
  }

  /**
   * Imprime la información de una revisión específica.
   */
  private void printDelta(RCSDelta delta, LogOptions options) {
    System.out.println(REV_SEP);
    System.out.println("revision " + delta.getRevisionNumber());

    String dateStr = RCSTimeUtils.formatTime(delta.getDate(), options.isIsoTimeFormat(), null);
    System.out.print("date: " + dateStr + ";  author: " + delta.getAuthor() + ";  state: " + delta.getState() + ";");

    if (delta.getCommitId() != null) {
      System.out.print("  commitid: " + delta.getCommitId() + ";");
    }
    System.out.println();

    if (delta.getLogMessage() != null) {
      System.out.println(delta.getLogMessage());
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
