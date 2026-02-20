package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representa un número de revisión de RCS (e.g., "1.1", "1.1.2.1").
 * Esta clase es inmutable.
 */
public class RCSRevisionNumber {

    // Constantes adaptadas de rcs.h y rcsnum.c
    public static final int RCSNUM_MAXLEN = 64;
    public static final short RCSNUM_MAXNUM = Short.MAX_VALUE; // Aproximación de UINT16_MAX

    private static final Pattern REVISION_PATTERN = Pattern.compile("^(\\d+\\.)*\\d+$");
    private static final String MAGIC_BRANCH = ".0.";

    private final short[] ids;
    private final int length;

    /**
     * Constructor privado para crear una instancia de RCSRevisionNumber.
     * Utilice los métodos estáticos `parse` o `from` para obtener instancias.
     *
     * @param ids Un array de shorts que representan los componentes del número de revisión.
     * @param length La longitud lógica del número de revisión (número de componentes).
     */
    private RCSRevisionNumber(short[] ids, int length) {
        this.ids = Arrays.copyOf(ids, length);
        this.length = length;
    }

    /**
     * Crea un RCSRevisionNumber a partir de una cadena.
     *
     * @param str La cadena que representa el número de revisión (e.g., "1.1", "1.1.2.1").
     * @return Una nueva instancia de RCSRevisionNumber.
     * @throws IllegalArgumentException Si la cadena no es un número de revisión válido.
     */
    public static RCSRevisionNumber parse(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("La cadena de revisión no puede ser nula o vacía.");
        }

        Matcher matcher = REVISION_PATTERN.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Formato de número de revisión inválido: " + str);
        }

        String[] parts = str.split("\\.");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Formato de número de revisión inválido: " + str);
        }
        if (parts.length > RCSNUM_MAXLEN) {
            throw new IllegalArgumentException("El número de revisión excede la longitud máxima permitida de " + RCSNUM_MAXLEN);
        }

        short[] ids = new short[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                int val = Integer.parseInt(parts[i]);
                if (val < 0 || val > RCSNUM_MAXNUM) {
                    throw new IllegalArgumentException("Componente de revisión fuera de rango (0-" + RCSNUM_MAXNUM + "): " + parts[i]);
                }
                ids[i] = (short) val;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Componente de revisión no numérico: " + parts[i]);
            }
        }

        // Manejo de "magic branch" de CVS (rcsnum.c)
        // Si tiene un .0.x al final y no se ha deshabilitado el "magic" (RCSNUM_NO_MAGIC)
        // Por ahora, no se implementa RCSNUM_NO_MAGIC, asumimos que siempre se elimina el magic branch si se encuentra.
        if (ids.length > 2 && ids[ids.length - 2] == 0) {
            // Reconstruir la cadena para verificar el patrón MAGIC_BRANCH
            // Simplificación: si el penúltimo componente es 0, lo eliminamos.
            short[] newIds = new short[ids.length - 1];
            System.arraycopy(ids, 0, newIds, 0, ids.length - 2);
            newIds[ids.length - 2] = ids[ids.length - 1]; // Mover el último componente al lugar del 0
            ids = newIds; // Usar el array más corto
        }
        
        return new RCSRevisionNumber(ids, ids.length);
    }

    /**
     * Copia un número de revisión, opcionalmente truncándolo a una profundidad específica.
     *
     * @param source El número de revisión a copiar.
     * @param depth La profundidad máxima a copiar. Si es 0, copia la longitud completa.
     * @return Una nueva instancia de RCSRevisionNumber que es una copia (parcial) del original.
     */
    public static RCSRevisionNumber copy(RCSRevisionNumber source, int depth) {
        int len = source.length;
        if (depth != 0 && len > depth) {
            len = depth;
        }
        short[] newIds = Arrays.copyOf(source.ids, len);
        return new RCSRevisionNumber(newIds, len);
    }

    /**
     * Compara este número de revisión con otro.
     *
     * @param other El otro número de revisión a comparar.
     * @param depth La profundidad hasta la cual comparar. Si es 0, compara la longitud completa.
     * @return 0 si son iguales, -1 si este número es "mayor" que el otro, 1 si este número es "menor" que el otro.
     * (Siguiendo la convención de `rcsnum_cmp` donde -1 significa mayor y 1 significa menor, lo cual es invertido respecto a `compareTo`).
     */
    public int compareTo(RCSRevisionNumber other, int depth) {
        int len1 = this.length;
        int len2 = other.length;

        int limit = Math.min(len1, len2);
        if (depth != 0 && limit > depth) {
            limit = depth;
        }

        for (int i = 0; i < limit; i++) {
            int diff = this.ids[i] - other.ids[i];
            if (diff < 0) return 1;  // this.ids[i] es menor, por lo tanto, "menor" en la convención de RCS
            if (diff > 0) return -1; // this.ids[i] es mayor, por lo tanto, "mayor" en la convención de RCS
        }

        if (depth != 0 && limit == depth) {
            return 0; // Se alcanzó la profundidad y hasta ahora son iguales
        } else if (len1 > len2) {
            return -1; // Este es más largo, por lo tanto, "mayor"
        } else if (len2 > len1) {
            return 1; // El otro es más largo, por lo tanto, "menor"
        }
        return 0; // Son iguales
    }

    /**
     * Incrementa el último componente del número de revisión.
     * Por ejemplo, "1.1" se convierte en "1.2".
     *
     * @return Una nueva instancia de RCSRevisionNumber con el último componente incrementado.
     * @throws RCSException Si el último componente ya es el valor máximo permitido.
     */
    public RCSRevisionNumber increment() {
        if (length == 0) {
            throw new RCSException("No se puede incrementar un número de revisión vacío.");
        }
        short lastId = ids[length - 1];
        if (lastId == RCSNUM_MAXNUM) {
            throw new RCSException("El último componente del número de revisión ha alcanzado su valor máximo.");
        }
        short[] newIds = Arrays.copyOf(ids, length);
        newIds[length - 1]++;
        return new RCSRevisionNumber(newIds, length);
    }

    /**
     * Devuelve la longitud del número de revisión (número de componentes).
     */
    public int getLength() {
        return length;
    }

    /**
     * Devuelve una copia de los componentes del número de revisión.
     */
    public short[] getIds() {
        return Arrays.copyOf(ids, length);
    }

    /**
     * Verifica si este número de revisión representa una rama (longitud impar de componentes).
     */
    public boolean isBranch() {
        return length % 2 != 0;
    }

    /**
     * Verifica si este número de revisión es una revisión de rama (longitud par y al menos 4 componentes).
     */
    public boolean isBranchRevision() {
        return length % 2 == 0 && length >= 4;
    }

    /**
     * Convierte una revisión a su número de rama.
     * Por ejemplo, "1.1.2.3" -> "1.1.2".
     *
     * @return Una nueva instancia de RCSRevisionNumber que representa la rama, o null si no es una revisión de rama válida.
     */
    public RCSRevisionNumber convertToBranch() {
        if (length < 2) {
            return null;
        }
        // Si ya es una rama, devolver una copia
        if (isBranch()) {
            return RCSRevisionNumber.copy(this, 0);
        }
        // Si es una revisión de una rama, truncar el último componente
        return new RCSRevisionNumber(Arrays.copyOf(ids, length - 1), length - 1);
    }

    /**
     * Convierte un número de rama a su revisión inicial (añadiendo "1" al final si es necesario).
     * Por ejemplo, "1.1.2" -> "1.1.2.1".
     *
     * @return Una nueva instancia de RCSRevisionNumber que representa la revisión inicial de la rama.
     * @throws IllegalArgumentException Si no es un número de rama válido.
     */
    public RCSRevisionNumber convertToRevision() {
        if (!isBranch()) {
            throw new IllegalArgumentException("No es un número de rama válido para convertir a revisión.");
        }
        short[] newIds = Arrays.copyOf(ids, length + 1);
        newIds[length] = 1; // La revisión inicial de una rama es .1
        return new RCSRevisionNumber(newIds, length + 1);
    }
    
    /**
     * Añade un número "mágico" a una rama, comúnmente .0.
     * Ejemplo: "1.2" -> "1.0.2"
     *
     * @return Una nueva instancia de RCSRevisionNumber con el número mágico.
     */
    public RCSRevisionNumber addMagicBranchNumber() {
        if (length == 0 || length >= RCSNUM_MAXLEN) {
            throw new RCSException("No se puede añadir número mágico a una revisión con longitud 0 o máxima.");
        }
        short[] newIds = new short[length + 1];
        System.arraycopy(ids, 0, newIds, 0, length - 1); // Copia hasta el penúltimo
        newIds[length - 1] = 0; // Inserta el 0 mágico
        newIds[length] = ids[length - 1]; // Mueve el último a la nueva posición
        return new RCSRevisionNumber(newIds, length + 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ids[i]);
            if (i < length - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCSRevisionNumber that = (RCSRevisionNumber) o;
        return length == that.length && Arrays.equals(ids, that.ids);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(length);
        result = 31 * result + Arrays.hashCode(ids);
        return result;
    }
}
