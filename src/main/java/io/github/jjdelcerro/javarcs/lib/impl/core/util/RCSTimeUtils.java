package io.github.jjdelcerro.javarcs.lib.impl.core.util;

import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidades para el manejo de fechas y horas en RCS, adaptadas de `rcstime.c` y `date.y`.
 */
public class RCSTimeUtils {

    // Formato de fecha estándar de RCS: YYYY.MM.DD.HH.MM.SS
    private static final String RCS_DATE_FORMAT = "yyyy.MM.dd.HH.mm.ss";
    private static final Pattern RCS_DATE_PATTERN = Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{2}$");

    // Patrón para parsear zonas horarias: +HHMM, +HH:MM, -HHMM, -HH:MM
    private static final Pattern TIMEZONE_PATTERN = Pattern.compile("^([+-])(\\d{2})(:?(\\d{2}))?$");

    private RCSTimeUtils() {
        // Utility class
    }

    /**
     * Parsea una cadena de fecha al estilo RCS y devuelve un objeto Date.
     * Adaptado de `date_parse` en `date.y` y el uso en `rcs.c`.
     * Por ahora, solo soporta el formato estricto RCS_DATE_FORMAT.
     *
     * @param dateString La cadena de fecha a parsear (ej. "2024.01.15.10.30.00").
     * @return Un objeto Date.
     * @throws RCSException Si la cadena no está en el formato esperado.
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null; // o lanzar una excepción, dependiendo del comportamiento deseado en caso de nulo/vacío
        }

        if (!RCS_DATE_PATTERN.matcher(dateString).matches()) {
            throw new RCSException("Formato de fecha inválido. Se esperaba 'YYYY.MM.DD.HH.mm.SS': " + dateString);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(RCS_DATE_FORMAT, Locale.US);
            sdf.setLenient(false); // No permitir fechas "relajadas" (ej. 2024.01.32.00.00.00)
            return sdf.parse(dateString);
        } catch (ParseException e) {
            throw new RCSException("No se pudo parsear la fecha: " + dateString, e);
        }
    }

    /**
     * Formatea un objeto Date a una cadena, en formato RCS tradicional o ISO 8601.
     * Adaptado de `rcstime_tostr`.
     *
     * @param date      El objeto Date a formatear.
     * @param isoFormat Si es true, usa formato ISO 8601; de lo contrario, usa formato RCS tradicional.
     * @param timezone  La zona horaria a aplicar (ej. "GMT", "America/New_York", "+0100", "LT" para local).
     * @return La cadena de fecha formateada.
     */
    public static String formatTime(Date date, boolean isoFormat, String timezone) {
        if (date == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // Aplicar zona horaria si se especifica
        if (timezone != null && !timezone.trim().isEmpty()) {
            setTimezone(cal, timezone);
        }

        if (isoFormat) {
            // Ejemplo ISO 8601: "YYYY-MM-DD HH:MM:SS+HH:MM"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdf.setCalendar(cal);
            String formattedDate = sdf.format(cal.getTime());

            // Añadir offset de zona horaria manualmente si se usó una zona explícita
            if (!"LT".equalsIgnoreCase(timezone)) {
                long offsetMillis = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
                long offsetHours = offsetMillis / (1000 * 60 * 60);
                long offsetMinutes = (offsetMillis % (1000 * 60 * 60)) / (1000 * 60);
                formattedDate += String.format("%+03d:%02d", offsetHours, offsetMinutes);
            }
            return formattedDate;
        } else {
            // Formato RCS tradicional: "YYYY/MM/DD HH:MM:SS"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            sdf.setCalendar(cal);
            return sdf.format(cal.getTime());
        }
    }

    /**
     * Ajusta la zona horaria de un objeto Calendar.
     * Adaptado de `rcs_set_tz`.
     *
     * @param calendar El Calendar a ajustar.
     * @param timezone La cadena de zona horaria (ej. "GMT", "America/New_York", "+0100", "-0530", "LT").
     * @throws RCSException Si la zona horaria es inválida.
     */
    private static void setTimezone(Calendar calendar, String timezone) {
        if ("LT".equalsIgnoreCase(timezone)) {
            calendar.setTimeZone(TimeZone.getDefault()); // Zona horaria local del sistema
        } else {
            TimeZone tz;
            if (TIMEZONE_PATTERN.matcher(timezone).matches()) {
                // Es un offset de +HHMM o +HH:MM
                long offsetSeconds = parseTimezoneOffset(timezone);
                tz = TimeZone.getTimeZone("GMT" + formatOffset(offsetSeconds));
            } else {
                // Es un ID de zona horaria (ej. "GMT", "Europe/Madrid")
                tz = TimeZone.getTimeZone(timezone);
                if (!tz.getID().equals(timezone) && !TimeZone.getTimeZone("GMT" + formatOffset(tz.getRawOffset() / 1000)).getID().equals(timezone)) {
                    // Si el ID devuelto no coincide, podría ser un ID inválido y Java devuelve GMT por defecto.
                    throw new RCSException("Zona horaria inválida: " + timezone);
                }
            }
            calendar.setTimeZone(tz);
        }
    }

    /**
     * Parsea una cadena de offset de zona horaria como "+HHMM" o "-HH:MM" a segundos.
     * Adaptado de `parse_timezone`.
     *
     * @param s La cadena de offset de zona horaria.
     * @return El offset en segundos.
     * @throws RCSException Si el formato es inválido.
     */
    private static long parseTimezoneOffset(String s) {
        Matcher matcher = TIMEZONE_PATTERN.matcher(s);
        if (!matcher.matches()) {
            throw new RCSException("Formato de offset de zona horaria inválido: " + s);
        }

        char sign = matcher.group(1).charAt(0);
        int hours = Integer.parseInt(matcher.group(2));
        int minutes = (matcher.group(4) != null) ? Integer.parseInt(matcher.group(4)) : 0;

        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
            throw new RCSException("Horas o minutos de offset de zona horaria fuera de rango: " + s);
        }

        long offset = hours * 3600L + minutes * 60L;
        return (sign == '-') ? -offset : offset;
    }

    /**
     * Formatea un offset en segundos a una cadena de "GMT+HH:MM" o "GMT-HH:MM".
     */
    private static String formatOffset(long offsetSeconds) {
        char sign = '+';
        if (offsetSeconds < 0) {
            sign = '-';
            offsetSeconds = -offsetSeconds;
        }
        long hours = offsetSeconds / 3600;
        long minutes = (offsetSeconds % 3600) / 60;
        return String.format("%c%02d:%02d", sign, hours, minutes);
    }
}
