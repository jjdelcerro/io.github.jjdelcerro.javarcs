package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSDelta;
import io.github.jjdelcerro.javarcs.lib.impl.core.model.RCSFile;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gestiona la expansión de palabras clave ($Id$, $Revision$, etc.).
 * Si el archivo es binario ("expand b"), la expansión se desactiva automáticamente.
 */
public class RCSKeywordExpander {

    // Modos de expansión (Flags de bits)
    public static final int KWEXP_NONE = 0x00;
    public static final int KWEXP_NAME = 0x01; // Incluir "Keyword: "
    public static final int KWEXP_VAL  = 0x02; // Incluir el valor
    public static final int KWEXP_LKR  = 0x04; // Incluir Locker
    public static final int KWEXP_OLD  = 0x08; // Mantener antigua (no expandir)
    
    public static final int KWEXP_DEFAULT = (KWEXP_NAME | KWEXP_VAL);

    private enum RCSKeyword {
        AUTHOR("Author"), DATE("Date"), ID("Id"), HEADER("Header"),
        LOCKER("Locker"), NAME("Name"), RCSFILE("RCSfile"),
        REVISION("Revision"), SOURCE("Source"), STATE("State"),
        MDOCDATE("Mdocdate");

        final String key;
        RCSKeyword(String k) { this.key = k; }
        
        static RCSKeyword find(String s) {
            for (RCSKeyword k : values()) {
                if (k.key.equalsIgnoreCase(s)) return k;
            }
            return null;
        }
    }

    // Patrón: $Keyword$ o $Keyword: valor $
    private static final Pattern KEY_PATTERN = Pattern.compile("\\$([a-zA-Z]+)(:\\s*[^$\\n]*)?\\$");

    private RCSKeywordExpander() {}

    /**
     * Expande palabras clave en el contenido.
     */
    public static byte[] expandKeywords(RCSFile rcsFile, RCSDelta delta, byte[] content, int mode) {
        if (content == null || mode == KWEXP_NONE || (mode & KWEXP_OLD) != 0) {
            return content;
        }

        // PROTECCIÓN BINARIA: Si es "expand b", no tocamos los bytes.
        if ("b".equals(rcsFile.getExpandKeywords())) {
            return content;
        }

        String text = new String(content, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        Matcher m = KEY_PATTERN.matcher(text);
        int last = 0;

        while (m.find()) {
            sb.append(text, last, m.start());
            String keyName = m.group(1);
            RCSKeyword kw = RCSKeyword.find(keyName);

            if (kw == null) {
                sb.append(m.group(0)); // No es una keyword RCS, dejar como está
            } else {
                sb.append(formatExpansion(kw, rcsFile, delta, mode));
            }
            last = m.end();
        }
        sb.append(text.substring(last));

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String formatExpansion(RCSKeyword kw, RCSFile rcsFile, RCSDelta delta, int mode) {
        StringBuilder val = new StringBuilder("$");
        boolean showName = (mode & KWEXP_NAME) != 0;
        boolean showVal  = (mode & KWEXP_VAL) != 0;

        if (showName) {
            val.append(kw.key);
            if (showVal) val.append(": ");
        }

        if (showVal) {
            switch (kw) {
                case AUTHOR:   val.append(delta.getAuthor()); break;
                case STATE:    val.append(delta.getState()); break;
                case REVISION: val.append(delta.getRevisionNumber()); break;
                case DATE:     val.append(RCSTimeUtils.formatTime(delta.getDate(), false, null)); break;
                case RCSFILE:  val.append(extractName(rcsFile.getFilePath().getFileName().toString())); break;
                case SOURCE:   val.append(rcsFile.getFilePath().toAbsolutePath()); break;
                case LOCKER:   val.append(delta.getLocker() != null ? delta.getLocker() : ""); break;
                case MDOCDATE: val.append(new SimpleDateFormat("MMMM dd yyyy", Locale.US).format(delta.getDate())); break;
                case ID:
                case HEADER:
                    val.append(extractName(rcsFile.getFilePath().getFileName().toString())).append(" ")
                       .append(delta.getRevisionNumber()).append(" ")
                       .append(RCSTimeUtils.formatTime(delta.getDate(), false, null)).append(" ")
                       .append(delta.getAuthor()).append(" ")
                       .append(delta.getState());
                    if ((mode & KWEXP_LKR) != 0 && delta.getLocker() != null) {
                        val.append(" ").append(delta.getLocker());
                    }
                    break;
                case NAME:
                    val.append(""); // Reservado para nombres simbólicos en el futuro
                    break;
            }
            if (showName) val.append(" ");
        }

        val.append("$");
        return val.toString();
    }

    private static String extractName(String filename) {
        return filename.endsWith(",jv") ? filename.substring(0, filename.length() - 3) : filename;
    }
}
