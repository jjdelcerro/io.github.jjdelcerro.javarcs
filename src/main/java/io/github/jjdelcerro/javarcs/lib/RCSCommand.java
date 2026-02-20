package io.github.jjdelcerro.javarcs.lib;

import io.github.jjdelcerro.javarcs.lib.impl.exceptions.RCSException;

/**
 * Interfaz base para todos los comandos del sistema RCS.
 * Define un método `execute` que toma un objeto `options` específico del comando.
 *
 * @param <T> El tipo del objeto de opciones para este comando.
 */
public interface RCSCommand<T> {

    /**
     * Ejecuta la lógica del comando RCS.
     *
     * @param options Un objeto que contiene las opciones y argumentos específicos para este comando.
     * @throws RCSException Si ocurre un error durante la ejecución del comando.
     */
    void execute(T options) throws RCSException;
}
