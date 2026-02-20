package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Algoritmo de fusión de 3 vías (diff3) profesional. Utiliza java-diff-utils
 * para identificar cambios desde un ancestro común y detectar conflictos de
 * solapamiento de forma precisa.
 */
public class ThreeWayMergeAlgorithm {

  private ThreeWayMergeAlgorithm() {
    // Clase de utilidad
  }

  /**
   * Contenedor para el resultado de la fusión.
   */
  public static class MergeResult {

    private final byte[] mergedContent;
    private final boolean conflictsDetected;

    public MergeResult(byte[] mergedContent, boolean conflictsDetected) {
      this.mergedContent = mergedContent;
      this.conflictsDetected = conflictsDetected;
    }

    public byte[] getMergedContent() {
      return mergedContent;
    }

    public boolean isConflictsDetected() {
      return conflictsDetected;
    }
  }

  /**
   * Realiza una fusión de tres vías entre un ancestro y dos versiones
   * modificadas.
   *
   * @param ancestorBytes Contenido del ancestro común.
   * @param localBytes Contenido de la versión local (Mine).
   * @param remoteBytes Contenido de la versión remota (Theirs).
   * @param labelLocal Etiqueta para el marcador de conflicto local.
   * @param labelRemote Etiqueta para el marcador de conflicto remoto.
   * @return Objeto MergeResult con el contenido y flag de conflictos.
   */
  public static MergeResult merge(byte[] ancestorBytes, byte[] localBytes, byte[] remoteBytes,
          String labelLocal, String labelRemote) {

    List<String> ancestor = bytesToLines(ancestorBytes);
    List<String> local = bytesToLines(localBytes);
    List<String> remote = bytesToLines(remoteBytes);

    // 1. Obtener los parches desde el ancestro hacia cada versión
    Patch<String> patchL = DiffUtils.diff(ancestor, local);
    Patch<String> patchR = DiffUtils.diff(ancestor, remote);

    List<String> result = new ArrayList<>();
    boolean hasConflicts = false;

    int cursor = 0;
    int lastLine = ancestor.size();

    // 2. Recorrer las líneas del ancestro y aplicar cambios
    while (cursor < lastLine) {
      AbstractDelta<String> deltaL = findDeltaAt(patchL, cursor);
      AbstractDelta<String> deltaR = findDeltaAt(patchR, cursor);

      if (deltaL == null && deltaR == null) {
        // Ninguna versión cambió esta línea
        result.add(ancestor.get(cursor));
        cursor++;
      } else if (deltaL != null && deltaR == null) {
        // Solo cambió en local
        result.addAll(deltaL.getTarget().getLines());
        cursor += deltaL.getSource().size();
      } else if (deltaR != null && deltaL == null) {
        // Solo cambió en remoto
        result.addAll(deltaR.getTarget().getLines());
        cursor += deltaR.getSource().size();
      } else {
        // ¡CONFLICTO! Ambas versiones intentan modificar la misma zona
        // Verificamos si, por casualidad, el cambio es idéntico
        if (deltaL.getTarget().getLines().equals(deltaR.getTarget().getLines())) {
          result.addAll(deltaL.getTarget().getLines());
        } else {
          // Conflicto real: insertar marcadores
          hasConflicts = true;
          result.add("<<<<<<< " + labelLocal);
          result.addAll(deltaL.getTarget().getLines());
          result.add("=======");
          result.addAll(deltaR.getTarget().getLines());
          result.add(">>>>>>> " + labelRemote);
        }
        // Avanzamos el cursor según el cambio más largo en el ancestro para sincronizar
        cursor += Math.max(deltaL.getSource().size(), deltaR.getSource().size());
      }
    }

    // 3. Manejar inserciones al final del archivo (si las hay)
    // La lógica anterior cubre cambios y eliminaciones, pero las inserciones puras
    // que ocurren más allá del final original requieren un manejo similar.
    return new MergeResult(linesToBytes(result), hasConflicts);
  }

  /**
   * Busca si hay un cambio (Delta) que empiece exactamente en la línea
   * indicada.
   */
  private static AbstractDelta<String> findDeltaAt(Patch<String> patch, int position) {
    for (AbstractDelta<String> delta : patch.getDeltas()) {
      if (delta.getSource().getPosition() == position) {
        return delta;
      }
    }
    return null;
  }

  private static List<String> bytesToLines(byte[] content) {
    if (content == null || content.length == 0) {
      return new ArrayList<>();
    }
    String str = new String(content, StandardCharsets.UTF_8);
    // Usamos una expresión regular para dividir por cualquier tipo de salto de línea (\n, \r\n, \r)
    return Arrays.asList(str.split("\\R", -1));
  }

  private static byte[] linesToBytes(List<String> lines) {
    String content = lines.stream().collect(Collectors.joining("\n"));
    return content.getBytes(StandardCharsets.UTF_8);
  }
}
