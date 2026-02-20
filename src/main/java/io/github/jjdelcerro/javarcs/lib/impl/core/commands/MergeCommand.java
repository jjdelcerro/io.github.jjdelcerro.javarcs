package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.commands.MergeOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumberImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.temp.TemporaryFileManager;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSDeltaProcessor;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSFileUtils;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSParser;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.ThreeWayMergeAlgorithm;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementa el comando `rcsmerge` o `merge` de RCS.
 * Basado en `rcsmerge.c` y `merge.c` de la implementación C.
 */
public class MergeCommand implements RCSCommand<MergeOptions> {

    private static final Logger LOGGER = Logger.getLogger(MergeCommand.class.getName());
    public static final int D_OVERLAPS = 1; // De diff.h, indica conflictos

    @Override
    public void execute(MergeOptions options) throws RCSException {
        Path workFilePath = options.getWorkFilePath();
        Path rcsFilePath;
        RCSFileImpl rcsFile;

        try {
            // 1. Encontrar y parsear el archivo RCS
            Optional<Path> foundRCSFile = RCSFileUtils.chooseRCSFile(workFilePath, null);
            if (foundRCSFile.isEmpty()) {
                throw new RCSException("No se encontró el archivo RCS para " + workFilePath);
            }
            rcsFilePath = foundRCSFile.get();
            RCSParser parser = new RCSParser();
            rcsFile = parser.parse(rcsFilePath);

            // 2. Determinar las revisiones
            RCSRevisionNumberImpl baseRevisionNumber = RCSRevisionNumberImpl.parse(options.getBaseRevision());
            RCSRevisionNumberImpl compareRevisionNumber;

            if (options.getCompareRevision() != null && !options.getCompareRevision().isEmpty()) {
                compareRevisionNumber = RCSRevisionNumberImpl.parse(options.getCompareRevision());
            } else {
                // Si no se especifica compareRevision, se usa la HEAD
                compareRevisionNumber = rcsFile.getHead();
                if (compareRevisionNumber == null) {
                    throw new RCSException("El archivo RCS no tiene HEAD para comparar.");
                }
            }

            // 3. Reconstruir contenidos
            byte[] commonAncestorContent = RCSDeltaProcessor.reconstructFileContent(rcsFile, baseRevisionNumber);
            byte[] modified1Content = Files.readAllBytes(workFilePath); // Archivo de trabajo es la primera modificación
            byte[] modified2Content = RCSDeltaProcessor.reconstructFileContent(rcsFile, compareRevisionNumber); // La otra revisión

            // 4. Realizar la fusión de 3 vías
            // Las etiquetas para los conflictos
            String label1 = options.getLabels().size() > 0 ? options.getLabels().get(0) : workFilePath.getFileName().toString();
            String label2 = options.getLabels().size() > 1 ? options.getLabels().get(1) : compareRevisionNumber.toString();

            ThreeWayMergeAlgorithm.MergeResult mergeResult = ThreeWayMergeAlgorithm.merge(
                commonAncestorContent, modified1Content, modified2Content, label1, label2
            );

            // 5. Escribir el resultado
            if (options.isPipeOut()) {
                System.out.write(mergeResult.getMergedContent());
                System.out.flush();
            } else {
                Files.write(workFilePath, mergeResult.getMergedContent(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            if (mergeResult.isConflictsDetected() && !options.isQuiet()) {
                System.err.println("Advertencia: Se detectaron conflictos durante la fusión.");
                // En C, rcsmerge devuelve 1 (D_OVERLAPS) si hay conflictos.
                // Podríamos devolver este valor como parte de un estado de salida.
            }

            if (!options.isQuiet()) {
                LOGGER.info("Fusión completada para: " + workFilePath);
            }

        } catch (IOException e) {
            throw new RCSException("Error de E/S durante la fusión: " + e.getMessage(), e);
        } finally {
            TemporaryFileManager.cleanUp();
        }
    }
}
