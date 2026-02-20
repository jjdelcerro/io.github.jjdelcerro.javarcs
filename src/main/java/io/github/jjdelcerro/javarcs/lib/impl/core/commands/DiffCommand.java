package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.commands.DiffOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFile;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumber;
import io.github.jjdelcerro.javarcs.lib.impl.core.temp.TemporaryFileManager;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.DiffAlgorithm;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSDeltaProcessor;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSFileUtils;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSParser;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Implementa 'rcsdiff'. Compara revisiones generando una salida en formato
 * Unified Diff.
 */
public class DiffCommand implements RCSCommand<DiffOptions> {

  @Override
  public void execute(DiffOptions options) throws RCSException {
    List<Path> targets = options.getFilePaths();

    for (Path workFile : targets) {
      try {
        Optional<Path> rcsPath = RCSFileUtils.chooseRCSFile(workFile, null);
        if (rcsPath.isEmpty()) {
          continue;
        }

        RCSFile rcsFile = new RCSParser().parse(rcsPath.get());

        RCSRevisionNumber rev1Num = options.getRevision1() != null
                ? RCSRevisionNumber.parse(options.getRevision1()) : rcsFile.getHead();

        byte[] content1 = RCSDeltaProcessor.reconstructFileContent(rcsFile, rev1Num);
        byte[] content2;

        if (options.getRevision2() != null) {
          content2 = RCSDeltaProcessor.reconstructFileContent(rcsFile, RCSRevisionNumber.parse(options.getRevision2()));
        } else {
          content2 = Files.readAllBytes(workFile);
        }

        Path p1 = TemporaryFileManager.createTempFile("diff-v1-", ".tmp");
        Path p2 = TemporaryFileManager.createTempFile("diff-v2-", ".tmp");
        Files.write(p1, content1);
        Files.write(p2, content2);

        byte[] diffResult = DiffAlgorithm.generateRCSDiff(p1, p2);
        System.out.println(new String(diffResult, StandardCharsets.UTF_8));

      } catch (IOException e) {
        throw new RCSException("Error procesando diff para " + workFile);
      } finally {
        TemporaryFileManager.cleanUp();
      }
    }
  }
}
