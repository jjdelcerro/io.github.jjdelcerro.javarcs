package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import io.github.jjdelcerro.javarcs.lib.RCSDelta;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSDeltaImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileFlag;
import io.github.jjdelcerro.javarcs.lib.impl.core.temp.TemporaryFileManager;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RCSWriter {

  private RCSWriter() {
  }

  public static void write(RCSFileImpl rcsFile, Path outputPath) {
    Path parentDir = outputPath.toAbsolutePath().getParent();
    if (parentDir == null) {
      parentDir = java.nio.file.Paths.get(".");
    }

    Path tempFile = null;
    try {
      Files.createDirectories(parentDir);
      tempFile = Files.createTempFile(parentDir, outputPath.getFileName().toString(), ".tmp");

      try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(tempFile))) {
        writeAdminSection(os, rcsFile);
        writeNewline(os);
        writeDeltasSection(os, rcsFile);
        writeNewline(os);
        writeDescriptionSection(os, rcsFile);
        writeDeltaTextsSection(os, rcsFile);
      }

      Files.move(tempFile, outputPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      rcsFile.addFlag(RCSFileFlag.SYNCED);

    } catch (IOException e) {
      throw new RCSException("Error al escribir archivo RCS: " + outputPath, e);
    } finally {
      if (tempFile != null) {
        TemporaryFileManager.registerTempFile(tempFile);
      }
    }
  }

  private static void writeAdminSection(OutputStream os, RCSFileImpl rcsFile) throws IOException {
    writeText(os, "head\t" + (rcsFile.getHead() != null ? rcsFile.getHead().toString() : "") + ";\n");
    if (rcsFile.getBranch() != null) {
      writeText(os, "branch\t" + rcsFile.getBranch().toString() + ";\n");
    }
    writeText(os, "access;"); // Simplificado
    writeNewline(os);
    writeText(os, "symbols;");
    writeNewline(os);
    writeText(os, "locks");
    if (rcsFile.hasFlag(RCSFileFlag.STRICT_LOCK)) {
      writeText(os, " strict");
    }
    writeText(os, ";\n");
    writeText(os, "comment\t" + quoteStringToString(rcsFile.getComment()) + ";\n");
    if (rcsFile.getExpandKeywords() != null) {
      writeText(os, "expand\t" + quoteStringToString(rcsFile.getExpandKeywords()) + ";\n");
    }
  }

  private static void writeDeltasSection(OutputStream os, RCSFileImpl rcsFile) throws IOException {
    List<RCSDelta> sortedDeltas = new ArrayList<>(rcsFile.getDeltas());
    sortedDeltas.sort((d1, d2) -> d2.getRevisionNumber().compareTo(d1.getRevisionNumber(), 0));
    SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    for (RCSDelta delta : sortedDeltas) {
      writeText(os, delta.getRevisionNumber().toString() + "\ndate\t" + df.format(delta.getDate()) + ";");
      writeText(os, "\tauthor " + delta.getAuthor() + ";\tstate " + delta.getState() + ";\nbranches;\nnext\t" + (delta.getNextRevision() != null ? delta.getNextRevision() : "") + ";\n\n");
    }
  }

  private static void writeDescriptionSection(OutputStream os, RCSFileImpl rcsFile) throws IOException {
    writeText(os, "desc\n");
    writeQuotedBytes(os, rcsFile.getDescription() != null ? rcsFile.getDescription().getBytes(StandardCharsets.UTF_8) : new byte[0]);
    writeNewline(os);
  }

  private static void writeDeltaTextsSection(OutputStream os, RCSFileImpl rcsFile) throws IOException {
    List<RCSDelta> sortedDeltas = new ArrayList<>(rcsFile.getDeltas());
    sortedDeltas.sort((d1, d2) -> d2.getRevisionNumber().compareTo(d1.getRevisionNumber(), 0));

    for (RCSDelta delta : sortedDeltas) {
      writeNewline(os);
      writeText(os, delta.getRevisionNumber().toString() + "\nlog\n");
      writeQuotedBytes(os, delta.getLogMessage() != null ? delta.getLogMessage().getBytes(StandardCharsets.UTF_8) : new byte[0]);
      writeText(os, "\ntext\n");
      writeQuotedBytes(os, delta.getDeltaText());
      writeNewline(os);
    }
  }

  private static void writeText(OutputStream os, String text) throws IOException {
    os.write(text.getBytes(StandardCharsets.UTF_8));
  }

  private static void writeNewline(OutputStream os) throws IOException {
    os.write('\n');
  }

  private static String quoteStringToString(String str) {
    if (str == null || str.isEmpty()) {
      return "@@";
    }
    return "@" + str.replace("@", "@@") + "@";
  }

  /**
   * ESCAPADO BINARIO: Escribe bytes crudos duplicando las '@' para el formato
   * RCS.
   */
  private static void writeQuotedBytes(OutputStream os, byte[] data) throws IOException {
    os.write('@');
    if (data != null) {
      for (byte b : data) {
        os.write(b);
        if (b == '@') {
          os.write('@');
        }
      }
    }
    os.write('@');
  }
}
