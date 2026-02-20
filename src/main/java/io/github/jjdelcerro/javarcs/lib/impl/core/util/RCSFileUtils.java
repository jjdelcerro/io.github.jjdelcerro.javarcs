package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class RCSFileUtils {

  public static final String RCS_DEFAULT_SUFFIX = ",jv/"; 

  public static final String RCSDIR = "RCS";
  private static final BufferedReader CONSOLE_READER = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

  private RCSFileUtils() {
  }

  /**
   * Detecta si un archivo es binario buscando bytes nulos en los primeros 1024
   * bytes.
   */
  public static boolean isBinaryFile(Path path) throws IOException {
    if (!Files.exists(path)) {
      return false;
    }
    try (InputStream is = Files.newInputStream(path)) {
      byte[] buffer = new byte[1024];
      int read = is.read(buffer);
      for (int i = 0; i < read; i++) {
        if (buffer[i] == 0) {
          return true;
        }
      }
    }
    return false;
  }

  public static Optional<Path> chooseRCSFile(Path workFilePath, String suffixes) {
    Path absoluteWorkFilePath = workFilePath.toAbsolutePath().normalize();
    Path parentDir = absoluteWorkFilePath.getParent();
    String fileName = absoluteWorkFilePath.getFileName().toString();
    String actualSuffixes = (suffixes == null || suffixes.isEmpty()) ? RCS_DEFAULT_SUFFIX : suffixes;
    String[] suffixArray = actualSuffixes.split("/");

    List<Path> potentialRCSPaths = new ArrayList<>();
    if (parentDir != null) {
      Path rcsSubDir = parentDir.resolve(RCSDIR);
      if (Files.isDirectory(rcsSubDir)) {
        potentialRCSPaths.add(rcsSubDir.resolve(fileName + ",v"));
      }
      potentialRCSPaths.add(parentDir.resolve(fileName + ",v"));
    }
    return potentialRCSPaths.stream().filter(Files::exists).findFirst();
  }

  public static String promptUser(String prompt, boolean interactive) {
    System.err.print(prompt);
    if (interactive) {
      System.err.print(">> ");
    }
    System.err.flush();
    try {
      return CONSOLE_READER.readLine();
    } catch (IOException e) {
      throw new RCSException("Error al leer la entrada del usuario.", e);
    }
  }

  public static char yesNoPrompt(char defaultChar) {
    try {
      System.err.flush();
      String input = CONSOLE_READER.readLine();
      if (input == null || input.trim().isEmpty()) {
        return Character.toLowerCase(defaultChar);
      }
      return Character.toLowerCase(input.trim().charAt(0));
    } catch (IOException e) {
      throw new RCSException("Error al leer la entrada.", e);
    }
  }

  public static void setDescription(RCSFileImpl rcsFile, String source, boolean interactive) {
    String descriptionContent;
    if (source == null) {
      descriptionContent = promptUser("Enter description, terminated with single '.' or EOF:\n", interactive);
    } else if (source.startsWith("-")) {
      descriptionContent = source.substring(1);
    } else {
      try {
        descriptionContent = Files.readString(Paths.get(source), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new RCSException("Error al leer descripción.");
      }
    }
    rcsFile.setDescription(descriptionContent);
  }
}
