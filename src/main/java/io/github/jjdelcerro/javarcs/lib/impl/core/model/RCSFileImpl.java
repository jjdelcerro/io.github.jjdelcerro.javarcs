package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import io.github.jjdelcerro.javarcs.lib.RCSAccessEntry;
import io.github.jjdelcerro.javarcs.lib.RCSDelta;
import io.github.jjdelcerro.javarcs.lib.RCSFile;
import io.github.jjdelcerro.javarcs.lib.RCSLockEntry;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Representa la estructura completa de un archivo RCS (.v). Incluye la sección
 * de administración, el árbol de deltas y la descripción.
 */
public class RCSFileImpl implements RCSFile {

  private final Path filePath;
  private RCSRevisionNumberImpl head;
  private RCSRevisionNumberImpl branch;
  private String comment;
  private String expandKeywords; // Importante para detectar modo binario ("b")
  private String description;

  private final List<RCSAccessEntryImpl> accessEntries;
  private final List<RCSSymbolEntryImpl> symbolicNames;
  private final List<RCSLockEntryImpl> locks;
  private final List<RCSDeltaImpl> deltas;

  private final Set<RCSFileFlag> flags;

  /**
   * Constructor para un nuevo archivo RCS.
   *
   * @param filePath Ruta al archivo físico (.v).
   */
  public RCSFileImpl(Path filePath) {
    this.filePath = Objects.requireNonNull(filePath, "La ruta del archivo no puede ser nula");
    this.accessEntries = new ArrayList<>();
    this.symbolicNames = new ArrayList<>();
    this.locks = new ArrayList<>();
    this.deltas = new ArrayList<>();
    this.flags = EnumSet.noneOf(RCSFileFlag.class);
    // Por defecto, RCS usa expansión de nombre y valor (kv)
    this.expandKeywords = "kv";
  }

  // --- Métodos de Estado y Flags ---
  @Override
  public boolean isBinary() {
    return "b".equals(expandKeywords);
  }

  public void addFlag(RCSFileFlag flag) {
    this.flags.add(flag);
  }

  public void removeFlag(RCSFileFlag flag) {
    this.flags.remove(flag);
  }

  public boolean hasFlag(RCSFileFlag flag) {
    return flags.contains(flag);
  }

  /**
   * Marca el archivo como "sucio" (necesita ser guardado en disco).
   */
  private void markAsModified() {
    this.flags.remove(RCSFileFlag.SYNCED);
  }

  // --- Getters y Setters con gestión de estado ---
  @Override
  public Path getFilePath() {
    return filePath;
  }

  @Override
  public RCSRevisionNumberImpl getHead() {
    return head;
  }

  public void setHead(RCSRevisionNumberImpl head) {
    this.head = head;
    markAsModified();
  }

  @Override
  public RCSRevisionNumberImpl getBranch() {
    return branch;
  }

  public void setBranch(RCSRevisionNumberImpl branch) {
    this.branch = branch;
    markAsModified();
  }

  @Override
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
    markAsModified();
  }

  @Override
  public String getExpandKeywords() {
    return expandKeywords;
  }

  public void setExpandKeywords(String expandKeywords) {
    this.expandKeywords = expandKeywords;
    markAsModified();
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
    markAsModified();
  }

  // --- Gestión de Listas de Metadatos ---
  @Override
  public List<RCSAccessEntry> getAccessEntries() {
    return (List)accessEntries;
  }

  public void addAccessEntry(RCSAccessEntryImpl entry) {
    this.accessEntries.add(entry);
    markAsModified();
  }

  @Override
  public List<RCSSymbolEntryImpl> getSymbolicNames() {
    return symbolicNames;
  }

  public void addSymbolicName(RCSSymbolEntryImpl entry) {
    this.symbolicNames.add(entry);
    markAsModified();
  }

  @Override
  public List<RCSLockEntry> getLocks() {
    return (List)locks;
  }

  public void addLock(RCSLockEntryImpl entry) {
    this.locks.add(entry);
    markAsModified();
  }

  @Override
  public List<RCSDelta> getDeltas() {
    return (List)deltas;
  }

  /**
   * Añade un nuevo delta al archivo. En un archivo RCS bien formado, los deltas
   * se almacenan de más reciente a más antiguo.
   */
  public void addDelta(RCSDeltaImpl delta) {
    this.deltas.add(delta);
    markAsModified();
  }

  // --- Utilidades de Búsqueda ---
  /**
   * Busca un delta específico por su número de revisión.
   *
   * @param revisionNumber Número de revisión (ej. "1.1").
   * @return El RCSDelta correspondiente o null si no existe.
   */
  public RCSDeltaImpl findDelta(RCSRevisionNumberImpl revisionNumber) {
    if (revisionNumber == null) {
      return null;
    }
    return deltas.stream()
            .filter(delta -> delta.getRevisionNumber().equals(revisionNumber))
            .findFirst()
            .orElse(null);
  }

  /**
   * Busca un número de revisión asociado a un nombre simbólico (Tag).
   *
   * @param symbolName El nombre del símbolo (ej. "RELEASE_1_0").
   * @return El número de revisión o null si el símbolo no existe.
   */
  public RCSRevisionNumberImpl findRevisionBySymbol(String symbolName) {
    return symbolicNames.stream()
            .filter(s -> s.getName().equals(symbolName))
            .map(RCSSymbolEntryImpl::getRevisionNumber)
            .findFirst()
            .orElse(null);
  }

  @Override
  public String toString() {
    return "RCSFile{"
            + "path=" + filePath.getFileName()
            + ", head=" + head
            + ", binary=" + isBinary()
            + ", deltas=" + deltas.size()
            + ", synced=" + hasFlag(RCSFileFlag.SYNCED)
            + '}';
  }
}
