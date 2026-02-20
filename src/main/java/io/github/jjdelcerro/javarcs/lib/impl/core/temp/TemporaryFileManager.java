package io.github.jjdelcerro.javarcs.lib.impl.core.temp;

import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la creación y limpieza de archivos temporales, inspirada en `worklist.c`.
 * Asegura que los archivos temporales se eliminen al finalizar la ejecución o en caso de fallo.
 */
public class TemporaryFileManager {
    private static final Logger LOGGER = Logger.getLogger(TemporaryFileManager.class.getName());
    private static final Set<Path> tempFiles = Collections.synchronizedSet(new HashSet<>());

    /**
     * Constructor privado para prevenir instanciación.
     * Esta clase se utiliza a través de métodos estáticos.
     */
    private TemporaryFileManager() {
        // Utility class
    }

    /**
     * Crea un nuevo archivo temporal y lo registra para su posterior limpieza.
     * Adaptado de la funcionalidad de `mkstemp` y `worklist_add`.
     *
     * @param prefix Prefijo del nombre del archivo temporal (puede ser null).
     * @param suffix Sufijo del nombre del archivo temporal (puede ser null).
     * @return La ruta al archivo temporal creado.
     * @throws RCSException Si ocurre un error de E/S al crear el archivo temporal.
     */
    public static Path createTempFile(String prefix, String suffix) {
        try {
            Path tempDir = Files.createTempDirectory("rcs-tmp-");
            Path tempFile = Files.createTempFile(tempDir, prefix, suffix);
            registerTempFile(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RCSException("Error al crear archivo temporal.", e);
        }
    }
    
    /**
     * Registra una ruta de archivo para ser eliminada durante la limpieza.
     *
     * @param filePath La ruta del archivo a registrar.
     */
    public static void registerTempFile(Path filePath) {
        tempFiles.add(filePath);
        LOGGER.log(Level.FINE, "Archivo temporal registrado: {0}", filePath);
    }

    /**
     * Elimina todos los archivos temporales registrados.
     * Adaptado de `worklist_run` y `worklist_unlink`.
     */
    public static void cleanUp() {
        LOGGER.log(Level.INFO, "Iniciando limpieza de {0} archivos temporales.", tempFiles.size());
        for (Path file : tempFiles) {
            try {
                if (Files.exists(file)) {
                    Files.delete(file);
                    LOGGER.log(Level.FINE, "Archivo temporal eliminado: {0}", file);
                    // Intentar eliminar el directorio padre si es un directorio temporal creado por createTempFile
                    Path parent = file.getParent();
                    if (Files.isDirectory(parent) && parent.getFileName().toString().startsWith("rcs-tmp-")) {
                        Files.deleteIfExists(parent); // Eliminar el directorio temporal si está vacío
                        LOGGER.log(Level.FINE, "Directorio temporal padre eliminado: {0}", parent);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "No se pudo eliminar el archivo temporal: " + file, e);
            }
        }
        tempFiles.clear();
        LOGGER.log(Level.INFO, "Limpieza de archivos temporales finalizada.");
    }
    
    // Configuración de un Shutdown Hook para la limpieza automática
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(TemporaryFileManager::cleanUp));
    }
}
