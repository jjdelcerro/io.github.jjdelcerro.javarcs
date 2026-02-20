package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.commands.IdentOptions;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementa el comando `ident` de RCS.
 * Basado en `ident.c` de la implementación C.
 */
public class IdentCommand implements RCSCommand<IdentOptions> {

    private static final Logger LOGGER = Logger.getLogger(IdentCommand.class.getName());
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\$([a-zA-Z]+)(:\\s*[^$]*)?\\$"); // $Keyword$ o $Keyword: value$

    @Override
    public void execute(IdentOptions options) throws RCSException {
        List<Path> filePaths = options.getFilePaths();
        boolean foundKeywordInAnyFile = false;

        if (filePaths.isEmpty()) {
            // Leer desde stdin
            if (!options.isQuiet()) {
                System.out.println("Reading from standard input:");
            }
            foundKeywordInAnyFile = identFile(System.in, "standard input", options.isQuiet());
        } else {
            for (Path filePath : filePaths) {
                if (!options.isQuiet() && filePaths.size() > 1) {
                    System.out.println(filePath + ":");
                } else if (!options.isQuiet() && filePaths.size() == 1) {
                    System.out.println(filePath + ":");
                }

                try (InputStream is = Files.newInputStream(filePath)) {
                    if (identFile(is, filePath.toString(), options.isQuiet())) {
                        foundKeywordInAnyFile = true;
                    }
                } catch (IOException e) {
                    LOGGER.warning("No se pudo leer el archivo " + filePath + ": " + e.getMessage());
                    if (!options.isQuiet()) {
                        System.err.println("ident: " + filePath + ": No such file or directory");
                    }
                }
            }
        }

        if (!foundKeywordInAnyFile && !options.isQuiet()) {
            LOGGER.info("ident: no id keywords found.");
        }
    }

    /**
     * Busca palabras clave RCS en un InputStream y las imprime.
     *
     * @param is        El InputStream del archivo.
     * @param fileName  El nombre del archivo (para mensajes de error/info).
     * @param quiet     Si es true, suprime los mensajes de "no id keywords".
     * @return true si se encontraron palabras clave, false de lo contrario.
     * @throws RCSException Si ocurre un error de E/S.
     */
    private boolean identFile(InputStream is, String fileName, boolean quiet) throws RCSException {
        boolean foundKeywords = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = KEYWORD_PATTERN.matcher(line);
                while (matcher.find()) {
                    System.out.println("	" + matcher.group(0)); // Imprimir la coincidencia completa
                    foundKeywords = true;
                }
            }
        } catch (IOException e) {
            throw new RCSException("Error de E/S al leer " + fileName + ": " + e.getMessage(), e);
        }
        return foundKeywords;
    }
}
