package io.github.jjdelcerro.javarcs.lib;

import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSRevisionNumberImpl;

/**
 *
 * @author jjdelcerro
 */
public interface RCSRevisionNumber {

  /**
   * Devuelve una copia de los componentes del número de revisión.
   * @return 
   */
  short[] getIds();

  /**
   * Devuelve la longitud del número de revisión (número de componentes).
   * @return 
   */
  int getLength();

  /**
   * Verifica si este número de revisión representa una rama (longitud impar de
   * componentes).
   * @return 
   */
  boolean isBranch();

  /**
   * Verifica si este número de revisión es una revisión de rama (longitud par y
   * al menos 4 componentes).
   * @return 
   */
  boolean isBranchRevision();

  public int compareTo(RCSRevisionNumber other, int depth);

}
