package io.github.jjdelcerro.javarcs.lib.impl.core.model;

/**
 * Enumeración de los flags utilizados en un archivo RCS.
 * Corresponde a las macros de flags en `rcs.h` como RCS_READ, RCS_WRITE, etc.
 */
public enum RCSFileFlag {
    // Permisos y modos de apertura
    READ,       // RCS_READ
    WRITE,      // RCS_WRITE
    CREATE,     // RCS_CREATE (indica que el archivo RCS debe crearse si no existe)
    RDWR,       // RCS_RDWR (READ | WRITE)

    // Flags internos de estado del archivo
    PARSED,     // RCS_PARSED (el archivo ha sido completamente parseado)
    SYNCED,     // RCS_SYNCED (la copia en memoria está sincronizada con la copia en disco)
    STRICT_LOCK,// RCS_SLOCK (bloqueo estricto)
    
    // Flags de estado del parsing
    PARSED_DELTAS,      // PARSED_DELTAS (todos los deltas han sido parseados)
    PARSED_DESCRIPTION, // PARSED_DESC (la descripción ha sido parseada)
    PARSED_DELTATEXTS   // PARSED_DELTATEXTS (todos los textos de los deltas han sido parseados)
}
