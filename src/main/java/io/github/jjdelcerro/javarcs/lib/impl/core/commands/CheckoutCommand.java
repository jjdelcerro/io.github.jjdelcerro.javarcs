package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.commands.CheckoutOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSDeltaImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumberImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSDeltaProcessor;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSFileUtils;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSKeywordExpander;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSParser;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Implementa el comando 'co'. Extrae versiones reconstruyendo el histórico
 * mediante la aplicación secuencial de Unified Diffs.
 */
public class CheckoutCommand implements RCSCommand<CheckoutOptions> {

  @Override
  public void execute(CheckoutOptions options) throws RCSException {
    Path workFilePath = options.getWorkFilePath();

    try {
      Optional<Path> rcsPathOpt = RCSFileUtils.chooseRCSFile(workFilePath, null);
      if (rcsPathOpt.isEmpty()) {
        throw new RCSException("Archivo RCS no encontrado para " + workFilePath);
      }

      RCSFileImpl rcsFile = new RCSParser().parse(rcsPathOpt.get());

      // Determinar revisión
      RCSRevisionNumberImpl revToGet = (options.getRevision() != null)
              ? RCSRevisionNumberImpl.parse(options.getRevision()) : rcsFile.getHead();

      if (revToGet == null) {
        throw new RCSException("El archivo RCS no contiene revisiones.");
      }

      // 1. Reconstruir contenido
      byte[] content = RCSDeltaProcessor.reconstructFileContent(rcsFile, revToGet);

      // 2. Expandir palabras clave (opcional según flags)
      RCSDeltaImpl delta = rcsFile.findDelta(revToGet);
      content = RCSKeywordExpander.expandKeywords(rcsFile, delta, content, options.getKeywordExpansionMode());

      // 3. Escribir resultado
      if (options.isPipeOut()) {
        System.out.write(content);
        System.out.flush();
      } else {
        if (Files.exists(workFilePath) && !options.isForce()) {
          System.err.println(workFilePath + " ya existe. ¿Sobrescribir? [y/N]");
          if (RCSFileUtils.yesNoPrompt('n') != 'y') {
            return;
          }
        }
        Files.write(workFilePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Revision " + revToGet + " extraída correctamente.");
      }

    } catch (IOException e) {
      throw new RCSException("Error en checkout: " + e.getMessage(), e);
    }
  }
}
