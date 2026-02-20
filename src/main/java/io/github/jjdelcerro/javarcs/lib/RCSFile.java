package io.github.jjdelcerro.javarcs.lib;

import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public interface RCSFile {

  // --- Gestión de Listas de Metadatos ---
  List<RCSAccessEntry> getAccessEntries();

  RCSRevisionNumber getBranch();

  String getComment();

  List<RCSDelta> getDeltas();

  String getDescription();

  String getExpandKeywords();

  Path getFilePath();

  RCSRevisionNumber getHead();

  List<RCSLockEntry> getLocks();

  List<RCSSymbolEntry> getSymbolicNames();

  boolean isBinary();
  
}
