package io.github.jjdelcerro.javarcs.lib.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public interface LogOptions extends RCSCommandOptions {

  List<String> getAuthors();

  List<String> getDates();

  List<String> getRevisions();

  List<String> getStates();

  Path getWorkFilePath();

  boolean isDescriptionOnly();

  boolean isHeaderOnly();

  boolean isIsoTimeFormat();

  boolean isQuiet();

  LogOptions setAuthors(List<String> authors);

  LogOptions setDates(List<String> dates);

  LogOptions setDescriptionOnly(boolean descriptionOnly);

  LogOptions setHeaderOnly(boolean headerOnly);

  LogOptions setIsoTimeFormat(boolean isoTimeFormat);

  LogOptions setQuiet(boolean quiet);

  LogOptions setRevisions(List<String> revisions);

  LogOptions setStates(List<String> states);
  
}
