package io.github.jjdelcerro.javarcs.lib;

import io.github.jjdelcerro.javarcs.lib.impl.RCSManagerImpl;

/**
 *
 * @author jjdelcerro
 */
public class RCSLocator {
  
  private static RCSManager rcsManager = null;
  
  public static RCSManager getRCSManager() {
    if( rcsManager == null ) {
      rcsManager = new RCSManagerImpl();
    }
    return rcsManager;
  }
}
