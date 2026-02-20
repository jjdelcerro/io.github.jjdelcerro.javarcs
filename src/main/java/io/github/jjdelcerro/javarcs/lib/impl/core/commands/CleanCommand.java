package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.RCSLockEntry;
import io.github.jjdelcerro.javarcs.lib.commands.CleanOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSDeltaImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSLockEntryImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumberImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.temp.TemporaryFileManager;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSDeltaProcessor;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSFileUtils;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSKeywordExpander;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSParser;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSWriter;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementa el comando `rcsclean` de RCS.
 * Basado en `rcsclean.c` de la implementación C.
 */
public class CleanCommand implements RCSCommand<CleanOptions> {

    private static final Logger LOGGER = Logger.getLogger(CleanCommand.class.getName());

    @Override
    public void execute(CleanOptions options) throws RCSException {
        List<Path> targetPaths = options.getFilePaths();

        try {
            if (targetPaths.isEmpty()) {
                // Si no se especifican archivos, limpiar el directorio actual
                try (Stream<Path> walk = Files.walk(Paths.get("."))) {
                    targetPaths = walk
                            .filter(Files::isRegularFile)
                            .collect(Collectors.toList());
                }
            }

            for (Path workFilePath : targetPaths) {
                processFile(workFilePath, options);
            }

        } catch (IOException e) {
            throw new RCSException("Error de E/S durante rcsclean: " + e.getMessage(), e);
        } finally {
            TemporaryFileManager.cleanUp();
        }
    }

    private void processFile(Path workFilePath, CleanOptions options) throws RCSException, IOException {
        Path rcsFilePath;
        RCSFileImpl rcsFile;
        
        if (!Files.exists(workFilePath) || Files.isDirectory(workFilePath)) {
            LOGGER.fine("Saltando: " + workFilePath + " no es un archivo regular.");
            return;
        }

        Optional<Path> foundRCSFile = RCSFileUtils.chooseRCSFile(workFilePath, null);
        if (foundRCSFile.isEmpty()) {
            LOGGER.fine("No se encontró el archivo RCS para " + workFilePath);
            return;
        }
        rcsFilePath = foundRCSFile.get();

        RCSParser parser = new RCSParser();
        rcsFile = parser.parse(rcsFilePath);

        RCSRevisionNumberImpl revisionToCompare;
        if (options.getRevision() != null && !options.getRevision().isEmpty()) {
            revisionToCompare = RCSRevisionNumberImpl.parse(options.getRevision());
        } else {
            revisionToCompare = rcsFile.getHead();
        }

        if (revisionToCompare == null) {
            LOGGER.warning("Archivo RCS " + rcsFilePath + " no tiene HEAD o revisión especificada.");
            return;
        }
        
        RCSDeltaImpl headDelta = rcsFile.findDelta(revisionToCompare);
        if (headDelta == null) {
            throw new RCSException("No se encontró el delta para la revisión " + revisionToCompare);
        }

        // 1. Reconstruir el contenido de la revisión HEAD
        byte[] headContent = RCSDeltaProcessor.reconstructFileContent(rcsFile, revisionToCompare);
        
        // 2. Expandir palabras clave en el contenido de la HEAD
        headContent = RCSKeywordExpander.expandKeywords(rcsFile, headDelta, headContent, options.getKeywordExpansionMode());

        // 3. Comparar con el archivo de trabajo
        byte[] workFileContent = Files.readAllBytes(workFilePath);

        boolean isIdentical = compareContents(headContent, workFileContent);

        if (isIdentical) {
            if (!options.isQuiet()) {
                System.out.println(workFilePath + " no modificado.");
            }

            // Gestionar desbloqueo (-u)
            if (options.isUnlock()) {
                String currentUsername = System.getProperty("user.name");
                Optional<RCSLockEntry> userLock = rcsFile.getLocks().stream()
                    .filter(lock -> lock.getRevisionNumber().equals(revisionToCompare) && lock.getUsername().equals(currentUsername))
                    .findFirst();
                
                if (userLock.isPresent()) {
                    if (!options.isQuiet()) {
                        System.out.println("Desbloqueando " + revisionToCompare + " en " + rcsFilePath + " para " + currentUsername);
                    }
                    rcsFile.getLocks().remove(userLock.get());
                    RCSWriter.write(rcsFile, rcsFilePath); // Actualizar archivo RCS
                } else {
                    if (!options.isQuiet()) {
                        System.out.println("No se encontró bloqueo para " + currentUsername + " en " + revisionToCompare);
                    }
                }
            }

            // Eliminar el archivo de trabajo si no hay bloqueos restantes y no es dryRun
            if (rcsFile.getLocks().isEmpty()) { // Simplificado: si no hay bloqueos en absoluto
                if (!options.isQuiet()) {
                    System.out.println("Eliminando " + workFilePath);
                }
                if (!options.isDryRun()) {
                    Files.delete(workFilePath);
                }
            } else {
                if (!options.isQuiet()) {
                    System.out.println("Archivo " + workFilePath + " no eliminado debido a bloqueos restantes.");
                }
            }

        } else {
            if (!options.isQuiet()) {
                System.out.println(workFilePath + " modificado. No se elimina.");
            }
        }
    }

    private boolean compareContents(byte[] content1, byte[] content2) {
        if (content1.length != content2.length) {
            return false;
        }
        for (int i = 0; i < content1.length; i++) {
            if (content1[i] != content2[i]) {
                return false;
            }
        }
        return true;
    }
}
