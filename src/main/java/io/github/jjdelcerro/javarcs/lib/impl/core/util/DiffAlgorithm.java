package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Motor de comparación de archivos basado en el algoritmo de Myers (via java-diff-utils).
 * Genera parches en formato Unified Diff, estándar, legible y muy fiable.
 */
public class DiffAlgorithm {

    private DiffAlgorithm() {
        // Clase de utilidad
    }

    /**
     * Compara dos archivos y genera un parche en formato Unified Diff.
     * 
     * @param originalPath Ruta al archivo base.
     * @param revisedPath Ruta al archivo modificado.
     * @return Array de bytes con el Unified Diff (UTF-8).
     */
    public static byte[] generateRCSDiff(Path originalPath, Path revisedPath) {
        try {
            List<String> originalLines = Files.readAllLines(originalPath, StandardCharsets.UTF_8);
            List<String> revisedLines = Files.readAllLines(revisedPath, StandardCharsets.UTF_8);

            // 1. Calcular las diferencias (Algoritmo de Myers)
            Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);

            // 2. Generar el formato Unified Diff (con 3 líneas de contexto por defecto)
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                    originalPath.getFileName().toString(),
                    revisedPath.getFileName().toString(),
                    originalLines,
                    patch,
                    3
            );

            // 3. Convertir a String y retornar bytes
            String diffOutput = String.join("\n", unifiedDiff);
            return diffOutput.getBytes(StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RCSException("Error al leer archivos para generar diff: " + e.getMessage(), e);
        }
    }
}
