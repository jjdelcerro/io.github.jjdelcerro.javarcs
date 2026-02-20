package io.github.jjdelcerro.javarcs.main.cli;

import io.github.jjdelcerro.javarcs.lib.RCSLocator;
import io.github.jjdelcerro.javarcs.lib.RCSManager;
import io.github.jjdelcerro.javarcs.lib.commands.CheckinOptions;
import io.github.jjdelcerro.javarcs.lib.commands.CheckoutOptions;
import io.github.jjdelcerro.javarcs.lib.commands.CleanOptions;
import io.github.jjdelcerro.javarcs.lib.commands.DiffOptions;
import io.github.jjdelcerro.javarcs.lib.commands.IdentOptions;
import io.github.jjdelcerro.javarcs.lib.commands.LogOptions;
import io.github.jjdelcerro.javarcs.lib.commands.MergeOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CheckinCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CheckoutCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CleanCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.DiffCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.IdentCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.IdentOptionsImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.LogCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.MergeCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSKeywordExpander;
import io.github.jjdelcerro.javarcs.lib.impl.core.util.RCSTimeUtils;
import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RCSCli {

  public static void main(String[] args) {
    if (args.length == 0) {
      printGeneralHelp();
      return;
    }

    String command = args[0].toLowerCase();
    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

    try {
      switch (command) {
        case "ci":
          executeCi(subArgs);
          break;
        case "co":
          executeCo(subArgs);
          break;
        case "rlog":
        case "log":
          executeLog(subArgs);
          break;
        case "rcsdiff":
        case "diff":
          executeDiff(subArgs);
          break;
        case "rcsmerge":
        case "merge":
          executeMerge(subArgs);
          break;
        case "rcsclean":
        case "clean":
          executeClean(subArgs);
          break;
        case "ident":
          executeIdent(subArgs);
          break;
        default:
          System.err.println("Comando desconocido: " + command);
          printGeneralHelp();
      }
    } catch (RCSException e) {
      System.err.println("Error RCS: " + e.getMessage());
      if (e.getCause() != null) {
        System.err.println("Causa: " + e.getCause().getMessage());
      }
    } catch (ParseException e) {
      System.err.println("Error en argumentos: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("Error inesperado: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // --- COMANDO CI ---
  private static void executeCi(String[] args) throws ParseException {
    Options options = new Options();
    options.addOption("m", "message", true, "Mensaje de log");
    options.addOption("r", "revision", true, "Número de revisión");
    options.addOption("t", "description", true, "Descripción del archivo");
    options.addOption("w", "author", true, "Autor de la revisión");
    options.addOption("d", "date", true, "Fecha de la revisión (YYYY.MM.DD.HH.mm.ss)");
    options.addOption("s", "state", true, "Estado de la revisión (ej. Exp, Rel)");
    options.addOption("i", "init", false, "Inicializar archivo RCS");
    options.addOption("q", "quiet", false, "Modo silencioso");
    options.addOption("h", "help", false, "Muestra esta ayuda");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("h") || cmd.getArgList().isEmpty()) {
      new HelpFormatter().printHelp("rcs ci [opciones] <archivo>", options);
      return;
    }
    RCSManager manager = RCSLocator.getRCSManager();
    CheckinOptions ciOpt = manager.createCheckinOptions(Paths.get(cmd.getArgList().get(0)));
    ciOpt.setMessage(cmd.getOptionValue("m"));
    ciOpt.setNewRevision(cmd.getOptionValue("r"));
    ciOpt.setDescription(cmd.getOptionValue("t"));
    ciOpt.setAuthor(cmd.getOptionValue("w"));
    ciOpt.setState(cmd.getOptionValue("s"));

    if (cmd.hasOption("d")) {
      ciOpt.setDate(RCSTimeUtils.parseDate(cmd.getOptionValue("d")));
    }

    ciOpt.setInit(cmd.hasOption("i"));
    ciOpt.setQuiet(cmd.hasOption("q"));
    ciOpt.setInteractive(!cmd.hasOption("q"));

    new CheckinCommand().execute(ciOpt);
  }

  // --- COMANDO CO ---
  private static void executeCo(String[] args) throws ParseException {
    Options options = new Options();
    options.addOption("r", "revision", true, "Revisión a extraer");
    options.addOption("f", "force", false, "Sobrescribir archivo de trabajo");
    options.addOption("p", "pipe", false, "Escribir a salida estándar");
    options.addOption("q", "quiet", false, "Modo silencioso");
    options.addOption("l", "lock", false, "Bloquear revisión (lock)");
    options.addOption("u", "unlock", false, "Desbloquear revisión (unlock)");
    options.addOption("k", "keywords", true, "Modo de expansión de palabras clave (ej. kv, b)");
    options.addOption("w", "author", true, "Autor (filtrado)");
    options.addOption("d", "date", true, "Fecha (filtrado)");
    options.addOption("s", "state", true, "Estado (filtrado)");
    options.addOption("h", "help", false, "Muestra esta ayuda");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("h") || cmd.getArgList().isEmpty()) {
      new HelpFormatter().printHelp("rcs co [opciones] <archivo>", options);
      return;
    }
    RCSManager manager = RCSLocator.getRCSManager();
    CheckoutOptions coOpt = manager.createCheckoutOptions(Paths.get(cmd.getArgList().get(0)));
    coOpt.setRevision(cmd.getOptionValue("r"));
    coOpt.setForce(cmd.hasOption("f"));
    coOpt.setPipeOut(cmd.hasOption("p"));
    coOpt.setQuiet(cmd.hasOption("q"));
    coOpt.setLock(cmd.hasOption("l"));
    coOpt.setUnlock(cmd.hasOption("u"));
    coOpt.setAuthor(cmd.getOptionValue("w"));
    coOpt.setDate(cmd.getOptionValue("d"));
    coOpt.setState(cmd.getOptionValue("s"));

    if (cmd.hasOption("k")) {
      coOpt.setKeywordExpansionMode(parseKeywordMode(cmd.getOptionValue("k")));
    }

    new CheckoutCommand().execute(coOpt);
  }

  // --- COMANDO RLOG ---
  private static void executeLog(String[] args) throws ParseException {
    Options options = new Options();
    options.addOption("r", "revisions", true, "Lista de revisiones separadas por coma"); // Simplificado
    options.addOption("d", "dates", true, "Fechas separadas por ;");
    options.addOption("w", "authors", true, "Autores separados por ,");
    options.addOption("s", "states", true, "Estados separados por ,");
    options.addOption("h", "header", false, "Solo imprimir cabecera");
    options.addOption("t", "text", false, "Solo imprimir descripción");
    options.addOption("q", "quiet", false, "No imprimir información administrativa");
    options.addOption("z", "zone", false, "Formato de hora ISO");
    options.addOption("help", false, "Ayuda");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("help") || cmd.getArgList().isEmpty()) {
      new HelpFormatter().printHelp("rcs rlog [opciones] <archivo>", options);
      return;
    }

    RCSManager manager = RCSLocator.getRCSManager();
    LogOptions logOpt  = manager.createLogOptions(Paths.get(cmd.getArgList().get(0)));

    if (cmd.hasOption("r")) {
      logOpt.setRevisions(Arrays.asList(cmd.getOptionValue("r").split(",")));
    }
    if (cmd.hasOption("d")) {
      logOpt.setDates(Arrays.asList(cmd.getOptionValue("d").split(";")));
    }
    if (cmd.hasOption("w")) {
      logOpt.setAuthors(Arrays.asList(cmd.getOptionValue("w").split(",")));
    }
    if (cmd.hasOption("s")) {
      logOpt.setStates(Arrays.asList(cmd.getOptionValue("s").split(",")));
    }

    logOpt.setHeaderOnly(cmd.hasOption("h"));
    logOpt.setDescriptionOnly(cmd.hasOption("t"));
    logOpt.setQuiet(cmd.hasOption("q"));
    logOpt.setIsoTimeFormat(cmd.hasOption("z"));

    new LogCommand().execute(logOpt);
  }

  // --- COMANDO RCSDIFF ---
  private static void executeDiff(String[] args) throws ParseException {
    Options options = new Options();
    // rcsdiff permite -rREV1 -rREV2. Commons CLI maneja múltiples ocurrencias si se configura
    Option revOption = Option.builder("r").hasArg().desc("Revisión(es) a comparar").build();

    options.addOption(revOption);
    options.addOption("k", "keywords", true, "Modo de expansión de palabras clave");
    options.addOption("q", "quiet", false, "Silencioso");
    options.addOption("i", "ignore-case", false, "Ignorar mayúsculas/minúsculas");
    options.addOption("w", "ignore-all-space", false, "Ignorar espacios en blanco");
    options.addOption("b", "ignore-space-change", false, "Ignorar cambios en espacios");
    options.addOption("h", "help", false, "Ayuda");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    List<Path> files = cmd.getArgList().stream().map(Paths::get).collect(Collectors.toList());

    if (cmd.hasOption("h") || files.isEmpty()) {
      new HelpFormatter().printHelp("rcs rcsdiff [opciones] <archivo>...", options);
      return;
    }

    RCSManager manager = RCSLocator.getRCSManager();
    DiffOptions diffOpt  = manager.createDiffOptions(files);

    String[] revs = cmd.getOptionValues("r");
    if (revs != null) {
      if (revs.length > 0) {
        diffOpt.setRevision1(revs[0]);
      }
      if (revs.length > 1) {
        diffOpt.setRevision2(revs[1]);
      }
    }

    if (cmd.hasOption("k")) {
      diffOpt.setKeywordExpansionMode(parseKeywordMode(cmd.getOptionValue("k")));
    }

    diffOpt.setQuiet(cmd.hasOption("q"));
    diffOpt.setIgnoreCase(cmd.hasOption("i"));
    diffOpt.setIgnoreBlanks(cmd.hasOption("w"));
    diffOpt.setFoldBlanks(cmd.hasOption("b"));

    new DiffCommand().execute(diffOpt);
  }

  // --- COMANDO RCSMERGE ---
  private static void executeMerge(String[] args) throws ParseException {
    Options options = new Options();
    // rcsmerge requiere una revisión base (-rBASE) y opcionalmente una (-rCOMPARE)
    Option revOption = Option.builder("r").hasArg().desc("Revisión base y opcionalmente revisión a comparar").build();
    Option labelOption = Option.builder("L").hasArg().desc("Etiquetas para conflictos").build();

    options.addOption(revOption);
    options.addOption(labelOption);
    options.addOption("p", "pipe", false, "Salida a stdout");
    options.addOption("q", "quiet", false, "Silencioso");
    options.addOption("h", "help", false, "Ayuda");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("h") || cmd.getArgList().isEmpty()) {
      new HelpFormatter().printHelp("rcs rcsmerge -r<rev> [opciones] <archivo>", options);
      return;
    }

    String[] revs = cmd.getOptionValues("r");
    if (revs == null || revs.length == 0) {
      System.err.println("Error: rcsmerge requiere al menos una revisión base (-r).");
      return;
    }

    Path workFile = Paths.get(cmd.getArgList().get(0));
    RCSManager manager = RCSLocator.getRCSManager();
    MergeOptions mergeOpt  = manager.createMergeOptions(workFile, revs[0]);

    if (revs.length > 1) {
      mergeOpt.setCompareRevision(revs[1]);
    }

    String[] labels = cmd.getOptionValues("L");
    if (labels != null) {
      mergeOpt.setLabels(Arrays.asList(labels));
    }

    mergeOpt.setPipeOut(cmd.hasOption("p"));
    mergeOpt.setQuiet(cmd.hasOption("q"));

    new MergeCommand().execute(mergeOpt);
  }

  // --- COMANDO RCSCLEAN ---
  private static void executeClean(String[] args) throws ParseException {
    Options options = new Options();
    options.addOption("r", "revision", true, "Comparar con esta revisión");
    options.addOption("u", "unlock", false, "Desbloquear si es posible");
    options.addOption("n", "dry-run", false, "No borrar, solo listar");
    options.addOption("q", "quiet", false, "Silencioso");
    options.addOption("k", "keywords", true, "Modo de keywords");
    options.addOption("T", "preserve-time", false, "Preservar timestamp");
    options.addOption("h", "help", false, "Ayuda");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    List<Path> files = cmd.getArgList().stream().map(Paths::get).collect(Collectors.toList());

    // rcsclean puede ejecutarse sin argumentos (limpia el dir actual)
    // pero commons-cli maneja esto bien.
    if (cmd.hasOption("h")) {
      new HelpFormatter().printHelp("rcs rcsclean [opciones] [archivo]...", options);
      return;
    }

    RCSManager manager = RCSLocator.getRCSManager();
    CleanOptions cleanOpt  = manager.createCleanOptions(files);
    
    
    cleanOpt.setRevision(cmd.getOptionValue("r"));
    cleanOpt.setUnlock(cmd.hasOption("u"));
    cleanOpt.setDryRun(cmd.hasOption("n"));
    cleanOpt.setQuiet(cmd.hasOption("q"));
    cleanOpt.setPreserveTime(cmd.hasOption("T"));
    if (cmd.hasOption("k")) {
      cleanOpt.setKeywordExpansionMode(parseKeywordMode(cmd.getOptionValue("k")));
    }

    new CleanCommand().execute(cleanOpt);
  }

  // --- COMANDO IDENT ---
  private static void executeIdent(String[] args) throws ParseException {
    Options options = new Options();
    options.addOption("q", "quiet", false, "Suprimir advertencias");
    options.addOption("h", "help", false, "Ayuda");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("h")) {
      new HelpFormatter().printHelp("rcs ident [opciones] [archivo]...", options);
      return;
    }

    List<Path> files = cmd.getArgList().stream().map(Paths::get).collect(Collectors.toList());

    RCSManager manager = RCSLocator.getRCSManager();
    IdentOptions identOpt  = manager.createIdentOptions(files);

    identOpt.setQuiet(cmd.hasOption("q"));

    new IdentCommand().execute(identOpt);
  }

  // --- AYUDA Y UTILIDADES ---
  private static void printGeneralHelp() {
    System.out.println("Uso: rcs <comando> [opciones] <archivo>");
    System.out.println("Comandos disponibles:");
    System.out.println("  ci         Check-in (guardar revisión)");
    System.out.println("  co         Check-out (extraer revisión)");
    System.out.println("  rlog       Ver historial de log");
    System.out.println("  rcsdiff    Ver diferencias entre revisiones");
    System.out.println("  rcsmerge   Fusionar ramas");
    System.out.println("  rcsclean   Limpiar archivos de trabajo sin cambios");
    System.out.println("  ident      Identificar keywords en archivos");
  }

  /**
   * Convierte el flag de string (ej. "kv", "b") a la constante entera de
   * RCSKeywordExpander.
   */
  private static int parseKeywordMode(String mode) {
    if (mode == null) {
      return RCSKeywordExpander.KWEXP_DEFAULT;
    }
    switch (mode) {
      case "kv":
        return RCSKeywordExpander.KWEXP_DEFAULT;
      case "v":
        return RCSKeywordExpander.KWEXP_VAL;
      case "k":
        return RCSKeywordExpander.KWEXP_NAME;
      case "o":
        return RCSKeywordExpander.KWEXP_OLD;
      case "b":
        return RCSKeywordExpander.KWEXP_NONE; // Tratamos 'b' como sin expansión para binarios
      // Se pueden añadir más modos (kvl, etc.) según soporte RCSKeywordExpander
      default:
        return RCSKeywordExpander.KWEXP_DEFAULT;
    }
  }
}
