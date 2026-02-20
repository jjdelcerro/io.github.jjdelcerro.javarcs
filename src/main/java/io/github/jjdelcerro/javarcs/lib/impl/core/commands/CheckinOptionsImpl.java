package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import io.github.jjdelcerro.javarcs.lib.commands.CheckinOptions;

public class CheckinOptionsImpl implements CheckinOptions {

  private Path workFilePath;
  private String message;
  private String newRevision;
  private String description;
  private String author;    // Para el flag -w
  private Date date;        // Para el flag -d
  private String state;     // Para el flag -s
  private boolean init;
  private boolean interactive = true;
  private boolean quiet;

  public CheckinOptionsImpl(Path workFilePath) {
    this.workFilePath = Objects.requireNonNull(workFilePath);
  }

  // Getters y Setters
  @Override
  public Path getWorkFilePath() {
    return workFilePath;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public CheckinOptionsImpl setMessage(String message) {
    this.message = message;
    return this;
  }

  @Override
  public String getNewRevision() {
    return newRevision;
  }

  @Override
  public CheckinOptionsImpl setNewRevision(String newRevision) {
    this.newRevision = newRevision;
    return this;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public CheckinOptionsImpl setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public String getAuthor() {
    return author;
  }

  @Override
  public CheckinOptionsImpl setAuthor(String author) {
    this.author = author;
    return this;
  }

  @Override
  public Date getDate() {
    return date;
  }

  @Override
  public CheckinOptionsImpl setDate(Date date) {
    this.date = date;
    return this;
  }

  @Override
  public String getState() {
    return state;
  }

  @Override
  public CheckinOptionsImpl setState(String state) {
    this.state = state;
    return this;
  }

  @Override
  public boolean isInit() {
    return init;
  }

  @Override
  public CheckinOptionsImpl setInit(boolean init) {
    this.init = init;
    return this;
  }

  @Override
  public boolean isInteractive() {
    return interactive;
  }

  @Override
  public CheckinOptionsImpl setInteractive(boolean interactive) {
    this.interactive = interactive;
    return this;
  }

  @Override
  public boolean isQuiet() {
    return quiet;
  }

  @Override
  public CheckinOptionsImpl setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }
}
