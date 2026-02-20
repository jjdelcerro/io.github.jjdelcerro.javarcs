package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSDeltaImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFileImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumberImpl;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Motor de reconstrucción de archivos RCS. Implementa la lógica de 'Reverse
 * Delta': 1. Los archivos binarios guardan snapshots completos en cada
 * revisión. 2. Los archivos de texto guardan la HEAD completa y deltas para
 * retroceder.
 */
public class RCSDeltaProcessor {

  private RCSDeltaProcessor() {
    // Clase de utilidad
  }

  /**
   * Reconstruye el contenido de una revisión específica partiendo de la HEAD.
   *
   * @param rcsFile El archivo RCS parseado.
   * @param targetRevision La revisión que se desea obtener (ej. "1.1").
   * @return Array de bytes con el contenido reconstruido.
   * @throws RCSException si la revisión no existe o el parche está corrupto.
   */
  public static byte[] reconstructFileContent(RCSFileImpl rcsFile, RCSRevisionNumberImpl targetRevision) {
    if (rcsFile == null || targetRevision == null) {
      throw new RCSException("Parámetros insuficientes para la reconstrucción.");
    }

    // --- CASO 1: ARCHIVOS BINARIOS ---
    // Si el archivo está marcado como binario (-kb o expand b), 
    // cada delta contiene el archivo completo. No hay que aplicar parches.
    if ("b".equals(rcsFile.getExpandKeywords())) {
      RCSDeltaImpl targetDelta = rcsFile.findDelta(targetRevision);
      if (targetDelta == null) {
        throw new RCSException("No se encontró la revisión binaria: " + targetRevision);
      }
      return targetDelta.getDeltaText();
    }

    // --- CASO 2: ARCHIVOS DE TEXTO (Reverse Delta) ---
    // 1. Empezamos siempre por la HEAD (que tiene el texto completo)
    RCSRevisionNumberImpl headRev = rcsFile.getHead();
    if (headRev == null) {
      throw new RCSException("El archivo RCS no tiene una revisión HEAD definida.");
    }

    RCSDeltaImpl headDelta = rcsFile.findDelta(headRev);
    if (headDelta == null) {
      throw new RCSException("Falta el cuerpo (delta) de la revisión HEAD " + headRev);
    }

    byte[] currentBytes = headDelta.getDeltaText();

    // Si la revisión solicitada es exactamente la HEAD, terminamos ya.
    if (headRev.equals(targetRevision)) {
      return currentBytes;
    }

    // 2. Si es una revisión antigua, transformamos los bytes en líneas
    // y empezamos a aplicar parches hacia atrás.
    List<String> currentLines = bytesToLines(currentBytes);
    RCSRevisionNumberImpl currentCursor = headRev;

    // Seguimos la cadena de punteros 'next' (que en RCS van de nuevo a viejo)
    while (currentCursor != null && !currentCursor.equals(targetRevision)) {
      RCSDeltaImpl deltaCursor = rcsFile.findDelta(currentCursor);

      // Obtenemos la siguiente revisión en la cadena hacia el pasado
      RCSRevisionNumberImpl nextRev = deltaCursor.getNextRevision();
      if (nextRev == null) {
        break; // Hemos llegado al final del historial (revisión 1.1)
      }

      // En el modelo Reverse Delta, el parche para obtener la versión X 
      // está guardado precisamente en el bloque 'text' de la versión X.
      RCSDeltaImpl nextDelta = rcsFile.findDelta(nextRev);
      if (nextDelta == null || nextDelta.getDeltaText() == null) {
        throw new RCSException("Delta corrupto o faltante en la cadena: " + nextRev);
      }

      // Aplicamos el parche unificado almacenado en la revisión antigua
      // para transformar el contenido actual en el contenido de esa revisión.
      currentLines = applyUnifiedPatch(currentLines, nextDelta.getDeltaText());

      // Movemos el cursor para seguir bajando en el historial
      currentCursor = nextRev;
    }

    // Verificamos si realmente hemos alcanzado la revisión deseada
    if (!currentCursor.equals(targetRevision)) {
      throw new RCSException("La revisión " + targetRevision + " no es alcanzable desde la HEAD " + headRev);
    }

    return linesToBytes(currentLines);
  }

  /**
   * Toma una lista de líneas y aplica un parche en formato Unified Diff.
   */
  private static List<String> applyUnifiedPatch(List<String> baseLines, byte[] patchBytes) {
    if (patchBytes == null || patchBytes.length == 0) {
      return baseLines;
    }

    try {
      // Convertimos el parche (bytes) a lista de strings
      List<String> patchLines = bytesToLines(patchBytes);

      // Parseamos el Unified Diff usando java-diff-utils
      Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(patchLines);

      // Aplicamos el parche a las líneas base
      return DiffUtils.patch(baseLines, patch);

    } catch (PatchFailedException e) {
      throw new RCSException("Fallo de integridad: El parche no coincide con el contenido del archivo. " + e.getMessage());
    } catch (Exception e) {
      throw new RCSException("Error al procesar el parche Unified Diff: " + e.getMessage());
    }
  }

  /**
   * Convierte bytes (UTF-8) a una lista de líneas preservando líneas vacías.
   */
  private static List<String> bytesToLines(byte[] data) {
    if (data == null || data.length == 0) {
      return new ArrayList<>();
    }
    String s = new String(data, StandardCharsets.UTF_8);
    // split("\\R", -1) asegura que no perdamos saltos de línea al final del archivo
    return Arrays.asList(s.split("\\R", -1));
  }

  /**
   * Une una lista de líneas en un único bloque de bytes.
   */
  private static byte[] linesToBytes(List<String> lines) {
    return String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
  }
}
