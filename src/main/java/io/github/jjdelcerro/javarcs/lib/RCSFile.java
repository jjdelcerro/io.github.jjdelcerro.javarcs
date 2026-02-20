package io.github.jjdelcerro.javarcs.lib;

import io.github.jjdelcerro.javarcs.lib.RCSAccessEntry;
import io.github.jjdelcerro.javarcs.lib.RCSDelta;
import io.github.jjdelcerro.javarcs.lib.RCSRevisionNumber;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSSymbolEntryImpl;
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

  List<RCSSymbolEntryImpl> getSymbolicNames();

  boolean isBinary();
  
}
