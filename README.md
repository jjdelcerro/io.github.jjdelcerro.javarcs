# JavaRCS: Java Revision Control System

![Java Version](https://img.shields.io/badge/Java-21-blue)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)


**JavaRCS** es una reimplementación moderna y en Java puro del clásico sistema de control de versiones **RCS (Revision Control System)**.

A diferencia del RCS original (escrito en C y basado en scripts de `ed`), **JavaRCS** utiliza un motor moderno de diferencias (**Unified Diffs**) para gestionar el historial de cambios, ofreciendo una arquitectura más limpia, segura y totalmente portable a cualquier sistema operativo con una JVM.

> ⚠️ **Nota de Compatibilidad:** Aunque JavaRCS lee y escribe archivos con estructura compatible con RCS (`,v/,jv`), el formato interno de los "deltas" (las diferencias entre versiones) utiliza *Unified Diff* en lugar de comandos *ed*. Por lo tanto, los archivos `,v/,jv` generados aquí no son binariamente compatibles con la herramienta `co` del GNU RCS original, aunque comparten la misma filosofía y estructura de metadatos.


## Descripción para el Usuario

### ¿Qué es y para qué sirve?
JavaRCS es una herramienta de control de versiones **local y basada en archivos**. A diferencia de Git o SVN, que gestionan repositorios completos, RCS gestiona versiones de **archivos individuales**.

Es ideal para:

*   **Archivos de configuración:** `/etc/hosts`, ficheros `.conf`, etc.
*   **Documentos individuales:** Scripts, notas, borradores.
*   **Entornos restringidos:** Donde no puedes instalar bases de datos o clientes pesados de git.
*   **Aprendizaje:** Para entender cómo funcionan los sistemas de control de versiones (deltas inversos, bloqueos, keywords).

### ¿Cómo funciona?
Por cada archivo que quieras controlar (ej. `script.sh`), JavaRCS crea un archivo "sombra" (ej. `script.sh,jv`) donde almacena todo el historial, autores, fechas y comentarios.

## Comandos Disponibles

El uso general es:
```bash
java -jar javarcs.jar <comando> [opciones] <archivo>
```

Puede ser necesario que indiques la ruta completa al "javarcs.jar" o que incluyas el numero de revision en el nombre del jar.

### 1. `ci` (Check In)
Guarda una nueva revisión del archivo en el sistema RCS.

*   `-i`: Inicializa un nuevo archivo RCS si no existe.
*   `-m <msg>`: Especifica el mensaje de log para la revisión.
*   `-r <rev>`: Fuerza un número de revisión específico (ej. `2.0`).
*   `-l`: Realiza el check-in pero mantiene el archivo bloqueado (check-out implícito).
*   `-u`: Desbloquea el archivo tras el check-in (comportamiento por defecto).
*   `-q`: Modo silencioso.
*   `-t <desc>`: Establece la descripción inicial del archivo.

### 2. `co` (Check Out)
Extrae una revisión específica del archivo RCS al directorio de trabajo.

*   `-l`: Bloquea la revisión (Lock) para permitir edición.
*   `-u`: Extrae sin bloquear (solo lectura).
*   `-r <rev>`: Extrae una revisión específica (si no se indica, extrae la última/HEAD).
*   `-f`: Fuerza la sobrescritura del archivo de trabajo si ya existe.
*   `-p`: Imprime el contenido a la salida estándar (pipe) en lugar de escribir en disco.
*   `-k <mode>`: Modo de expansión de palabras clave (ej. `-k b` para binarios).

### 3. `rlog` (Revision Log)
Muestra el historial de cambios, autores y fechas.

*   `-r <lista>`: Filtra por revisiones específicas.
*   `-w <users>`: Filtra por autor(es).
*   `-h`: Muestra solo la cabecera (sin los mensajes de commit).
*   `-t`: Muestra solo la descripción del archivo.

### 4. `rcsdiff`
Muestra las diferencias entre el archivo de trabajo y la última revisión guardada (o entre dos revisiones).

*   `-r <rev1> [-r <rev2>]`: Compara la revisión `rev1` contra el archivo actual, o `rev1` contra `rev2`.
*   `-q`: Modo silencioso.
*   `-i`: Ignora diferencias de mayúsculas/minúsculas.
*   `-w`: Ignora espacios en blanco.

### 5. `rcsclean`
Elimina el archivo de trabajo si no tiene cambios respecto a la última revisión guardada.

*   `-u`: Desbloquea la revisión antes de limpiar.
*   `-n`: Dry-run (solo muestra qué archivos se borrarían).

### 6. `ident`

Busca y muestra palabras clave RCS (ej. `$Id$`, `$Author$`) dentro de los archivos.

### 7. `rcsmerge`
Fusiona cambios entre dos revisiones en el archivo de trabajo (3-way merge).

*   `-r <base> [-r <compare>]`: Especifica la revisión base para la fusión. Opcionalmente una segunda revisión (si no, usa HEAD).
*   `-L <label>`: Etiquetas para los marcadores de conflicto (puedes usarlo varias veces).
*   `-p`: Envía el resultado a la salida estándar en lugar de sobrescribir el archivo.
*   `-q`: Modo silencioso.

## Motivación: JavaRCS y la IA

Este proyecto nace de una necesidad específica en el desarrollo de **[ChatAgent](https://github.com/jjdelcerro/chatagent)**, un agente autónomo experimental capaz de modificar ficheros de texto y mantener memoria a largo plazo.

Para que un Agente de IA pueda modificar ficheros de texto (documentacion o código) de forma segura, necesita un mecanismo de "seguridad" que cumpla tres requisitos:

1.  **Atomicidad por Archivo:** El agente trabaja archivo a archivo, no con repositorios enteros.
2.  **Portabilidad Total:** El agente debe poder ejecutarse en cualquier JVM sin depender de herramientas instaladas en el sistema operativo anfitrión (`git`, `diff`, `patch` nativos).
3.  **Lenguaje Común (Unified Diff):** La decisión de desviar el formato interno de almacenamiento de RCS (usando *Unified Diffs* en lugar de scripts `ed`) es intencional. Los LLMs (GPT, Claude, Llama) entienden y generan *Unified Diffs* de forma nativa y robusta, mientras que tienen dificultades con las instrucciones posicionales de `ed`.

JavaRCS actúa como el la red de seguridad para el agente. Antes de  aplicar un parche o escribir un fichero existente, el agente, de forma automatica, guarda una instantánea ligera (`ci`), permitiendo un `rollback` inmediato si la alucinación hace estragos.

## Stack Tecnológico

El proyecto está construido con tecnologías estándar y modernas:

*   **Lenguaje:** Java 21 (OpenJDK).
*   **Gestión de Construcción:** Apache Maven.
*   **Librerías Principales:**
    *   `java-diff-utils` (4.12): Para la generación de parches y algoritmos de diferencias (Myers).
    *   `commons-cli` (1.6.0): Para el parseo robusto de argumentos de línea de comandos.
*   **Sin dependencias nativas:** Funciona en Linux, Windows y macOS sin recompilar.

## Documentación y Arquitectura

La documentación técnica detallada de este proyecto reside en un archivo dentro de este mismo repositorio:

📄 **[AGENT_CONTEXT.md](./AGENT_CONTEXT.md)**

> **Nota sobre este archivo:**
>
> Este proyecto se desarrolla utilizando una metodología de colaboración con IA. El archivo `AGENT_CONTEXT.md` actúa como el **Contexto** que utiliza mi asistente para entender el proyecto. Contiene el análisis de la arquitectura, los patrones de diseño y las decisiones técnicas fundamentales.
>
> Si quieres entender cómo funciona este sistema, ese es el documento que debes leer.

📄 **[DEVELOPMENT_STATUS.md](./DEVELOPMENT_STATUS.md)**

Este proyecto evoluciona a lo largo del tiempo. Para conocer el grado de completitud de cada bloque y la deuda técnica identificada, consultalo.

> **Nota:** Este informe es generado y actualizado de vez en cuando con ayuda de mi asistente de IA tras cada hito relevante, actuando como un registro del progreso y los desafíos pendientes.


### Estructura del Código

El proyecto sigue una arquitectura modular, separando la **API pública**, la **implementación del núcleo** y la **interfaz de línea de comandos (CLI)**.

1.  **`io.github.jjdelcerro.javarcs.main.cli`**:

    *   Contiene el punto de entrada (`RCSCli`).
    *   Se encarga exclusivamente del parsing de argumentos y delegar en `RCSManager`.

2.  **`io.github.jjdelcerro.javarcs.lib` (API Pública)**:

    *   Define los contratos del sistema (`RCSManager`, `RCSCommand`).
    *   Expone las interfaces de opciones (DTOs) como `CheckinOptions` o `CheckoutOptions`, desacoplando el núcleo de la CLI.

3.  **`io.github.jjdelcerro.javarcs.lib.impl.core.model`**:

    *   Representación en memoria del archivo RCS (`RCSFile`, `RCSDelta`).
    *   Manejo de la compleja lógica de numeración de revisiones (`RCSRevisionNumber`) y árboles de deltas.

4.  **`io.github.jjdelcerro.javarcs.lib.impl.core.commands`**:

    *   Implementación del patrón **Command**. Contiene la lógica de negocio pura de cada operación (`CheckinCommand`, `MergeCommand`, etc.).

5.  **`io.github.jjdelcerro.javarcs.lib.impl.core.util`**:

    *   **`RCSParser`**: Parser descendente recursivo de alto rendimiento para leer archivos `.v` y manejar el quoting (`@`).
    *   **`DiffAlgorithm`**: Wrapper sobre `java-diff-utils` para generar Unified Diffs.
    *   **`RCSDeltaProcessor`**: Motor de reconstrucción. Aplica parches inversos (Reverse Delta) para obtener versiones antiguas.
    *   **`ThreeWayMergeAlgorithm`**: Algoritmo para la fusión de ramas y detección de conflictos.
    *   **`TemporaryFileManager`**: Gestión segura de archivos temporales con limpieza automática (shutdown hooks).
    
### Compilación e Instalación

**Prerrequisitos:**
*   JDK 21 o superior.
*   Maven 3.6+.

**Pasos:**

1.  Clonar el repositorio:
    ```bash
    git clone https://github.com/jjdelcerro/javarcs.git
    cd javarcs
    ```

2.  Compilar y empaquetar:
    ```bash
    mvn clean package
    ```

3.  El ejecutable se generará en la carpeta `target`:
    ```bash
    ls target/io.github.jjdelcerro.javarcs-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

### Alias recomendado
Para facilitar su uso, puedes crear un alias en tu `.bashrc` o `.zshrc`:

```bash
alias jrcs='java -jar /ruta/a/javarcs/target/io.github.jjdelcerro.javarcs-1.0-SNAPSHOT-jar-with-dependencies.jar'
```
Ahora puedes usarlo simplemente como: `jrcs ci archivo.txt`.

## Licencia

Este proyecto está bajo la GPL 3. Consulta el archivo `LICENSE` para más detalles.

---

*Desarrollado por [Joaquin del Cerro](https://github.com/jjdelcerro) como parte de la investigación sobre arquitecturas de IA.*
