package io.github.jjdelcerro.javarcs.lib.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import io.github.jjdelcerro.javarcs.lib.RCSManager.DiffFormat;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public interface DiffOptions extends RCSCommandOptions {

  int getContextLines();

  // Getters y Setters
  List<Path> getFilePaths();

  int getKeywordExpansionMode();

  DiffFormat getOutputFormat();

  String getRevision1();

  String getRevision2();

  boolean isExpandTabs();

  boolean isFoldBlanks();

  boolean isForceAscii();

  boolean isIgnoreBlanks();

  boolean isIgnoreCase();

  boolean isMinimalDiff();

  boolean isQuiet();

  DiffOptions setContextLines(int contextLines);

  DiffOptions setExpandTabs(boolean expandTabs);

  DiffOptions setFoldBlanks(boolean foldBlanks);

  DiffOptions setForceAscii(boolean forceAscii);

  DiffOptions setIgnoreBlanks(boolean ignoreBlanks);

  DiffOptions setIgnoreCase(boolean ignoreCase);

  DiffOptions setKeywordExpansionMode(int keywordExpansionMode);

  DiffOptions setMinimalDiff(boolean minimalDiff);

  DiffOptions setOutputFormat(DiffFormat outputFormat);

  DiffOptions setQuiet(boolean quiet);

  DiffOptions setRevision1(String revision1);

  DiffOptions setRevision2(String revision2);
  
}
