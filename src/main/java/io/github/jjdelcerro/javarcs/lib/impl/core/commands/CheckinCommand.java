package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.commands.CheckinOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSDeltaImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumberImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.temp.TemporaryFileManager;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.DiffAlgorithm;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSDeltaProcessor;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSFileUtils;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSParser;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSWriter;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

public class CheckinCommand implements RCSCommand<CheckinOptions> {

  @Override
  public void execute(CheckinOptions options) throws RCSException {
    Path workFilePath = options.getWorkFilePath();
    try {
      boolean isBinary = RCSFileUtils.isBinaryFile(workFilePath);
      Optional<Path> rcsPathOpt = RCSFileUtils.chooseRCSFile(workFilePath, null);
      RCSFileImpl rcsFile = rcsPathOpt.isPresent() ? new RCSParser().parse(rcsPathOpt.get()) : new RCSFileImpl(workFilePath.resolveSibling(workFilePath.getFileName() + ",jv"));

      if (isBinary) {
        rcsFile.setExpandKeywords("b");
      }

      RCSRevisionNumberImpl currentHead = rcsFile.getHead();
      RCSRevisionNumberImpl newRev = (currentHead == null) ? RCSRevisionNumberImpl.parse("1.1") : currentHead.increment();

      byte[] workContent = Files.readAllBytes(workFilePath);
      RCSDeltaImpl newDelta = new RCSDeltaImpl(newRev);
      newDelta.setDeltaText(workContent); // La nueva HEAD siempre tiene el contenido completo

      if (currentHead != null) {
        RCSDeltaImpl oldDelta = rcsFile.findDelta(currentHead);
        if (isBinary) {
          // Binario: No hay diff, la versión anterior se queda como snapshot completo (Reverse Delta simplificado)
          // Nota: En un sistema real, aquí gestionarías el ahorro de espacio.
        } else {
          byte[] oldContent = RCSDeltaProcessor.reconstructFileContent(rcsFile, currentHead);
          Path pNew = TemporaryFileManager.createTempFile("n", ".tmp");
          Path pOld = TemporaryFileManager.createTempFile("o", ".tmp");
          Files.write(pNew, workContent);
          Files.write(pOld, oldContent);
          oldDelta.setDeltaText(DiffAlgorithm.generateRCSDiff(pNew, pOld));
        }
      }

      newDelta.setNextRevision(currentHead);
      newDelta.setAuthor(options.getAuthor() != null ? options.getAuthor() : System.getProperty("user.name"));
      newDelta.setDate(new Date());
      rcsFile.setHead(newRev);
      rcsFile.addDelta(newDelta);

      RCSWriter.write(rcsFile, rcsFile.getFilePath());
    } catch (IOException e) {
      throw new RCSException("Error en CI.");
    }
  }
}
