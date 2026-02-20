package io.github.jjdelcerro.javarcs.lib.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import java.nio.file.Path;
import java.util.Date;

/**
 *
 * @author jjdelcerro
 */
public interface CheckinOptions extends RCSCommandOptions {

  String getAuthor();

  Date getDate();

  String getDescription();

  String getMessage();

  String getNewRevision();

  String getState();

  Path getWorkFilePath();

  boolean isInit();

  boolean isInteractive();

  boolean isQuiet();

  CheckinOptions setAuthor(String author);

  CheckinOptions setDate(Date date);

  CheckinOptions setDescription(String description);

  CheckinOptions setInit(boolean init);

  CheckinOptions setInteractive(boolean interactive);

  CheckinOptions setMessage(String message);

  CheckinOptions setNewRevision(String newRevision);

  CheckinOptions setQuiet(boolean quiet);

  CheckinOptions setState(String state);
  
}
