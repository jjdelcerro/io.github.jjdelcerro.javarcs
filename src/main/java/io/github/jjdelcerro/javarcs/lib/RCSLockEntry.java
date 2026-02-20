package io.github.jjdelcerro.javarcs.lib;

import io.github.jjdelcerro.javarcs.lib.RCSRevisionNumber;

/**
 *
 * @author jjdelcerro
 */
public interface RCSLockEntry {

  RCSRevisionNumber getRevisionNumber();

  String getUsername();
  
}
