# JavaRCS: Java Revision Control System

![Java Version](https://img.shields.io/badge/Java-21-blue)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

> ⚠️ **Estado del proyecto: componente experimental (Alpha)**
>
> Este repositorio nace como una pieza de infraestructura para mi investigación personal sobre arquitecturas de agentes autónomos (específicamente para el proyecto [Noema](https://github.com/jjdelcerro/io.github.jjdelcerro.noema)).
>
> **No es un producto de consumo final ni pretende ser una alternativa a herramientas modernas como Git o SVN.** Es una **Prueba de Concepto (PoC)** funcional, diseñada para proporcionar un mecanismo de control de versiones local, atómico y embebible para que los agentes de IA puedan modificar archivos de forma segura. Úsalo para estudiar la implementación, extraer patrones sobre el formato RCS o como base para tus propios experimentos.
> Ademas hay que tener en cuenta que, en esta versión, **falta por implementar el control de bloqueos**, una parte importante dentro de RCS.

**JavaRCS** es una reimplementación moderna y en Java puro del clásico sistema de control de versiones **RCS (Revision Control System)**.

A diferencia del RCS original (escrito en C y basado en comandos de edición de `ed`), esta implementación utiliza un motor moderno de diferencias (**Unified Diffs**) para gestionar el historial de cambios. Esto ofrece una arquitectura más limpia, segura y totalmente portable a cualquier sistema operativo que cuente con una JVM, eliminando la dependencia de binarios nativos.

> 💡 **Nota de compatibilidad:** Aunque JavaRCS lee y escribe archivos con una estructura compatible con el estándar RCS (los clásicos ficheros `,v` o `,jv`), el formato interno de los "deltas" (las diferencias entre versiones) utiliza *Unified Diff*. Por lo tanto, los archivos generados con esta herramienta no son binariamente compatibles con el comando `co` del GNU RCS original, aunque comparten exactamente la misma filosofía y estructura de metadatos. 

## Motivación y contexto: JavaRCS en la Arquitectura de Agentes

> *"¿Por qué alguien reescribiría RCS en Java en 2026 habiendo Git?"*

Este proyecto nació por necesidad durante el desarrollo de **[Noema](https://github.com/jjdelcerro/io.github.jjdelcerro.noema)**, mi laboratorio experimental de agentes autónomos.

Al dotar a un agente de IA de **agencia** sobre tu sistema de archivos (capacidad para crear, editar o parchear documentos y código de forma autónoma), te enfrentas a un problema grave. Un error de razonamiento o una alucinación del modelo puede destruir tu trabajo. Eso fue exactamente lo que me pasó. Cuanto le di permisos de escritura, el agente modificó algo que no debía y perdí un borrador.

Me di cuenta de que el agente necesitaba una "red de seguridad". Un sistema que forzara un commit automático antes de cada operación de escritura.

**¿Por qué no usar Git?** 

Integrar Git habría significado obligar a tener binarios externos instalados, romper la portabilidad del entorno (el *Fat JAR* de Noema) y, sobre todo, forzar el uso de un "repositorio global" para gestionar operaciones que en realidad eran modificaciones atómicas sobre archivos individuales e inconexos.

**JavaRCS** resuelve este problema actuando como una **librería de control de versiones embebida**. 

Al recuperar la filosofía clásica de RCS (donde el historial viaja asociado a cada archivo individual a través de un fichero sombra `,jv`), JavaRCS me permitió instrumentar el agente para que modifique cualquier documento con la garantía de que existe un historial recuperable. Todo ello sin dependencias del sistema operativo y sin salir de la JVM.

## ¿Qué es y para qué sirve?

JavaRCS opera bajo un paradigma fundamentalmente distinto al de las herramientas de control de versiones modernas. Es un sistema **local y centrado exclusivamente en archivos individuales**, no en repositorios o árboles de directorios.

A nivel mecánico, por cada documento que se pone bajo control (por ejemplo, `script.sh`), el sistema genera un archivo "sombra" (como `script.sh,jv`). En este archivo secundario se encapsula todo el historial de cambios mediante deltas inversos (utilizando el formato estándar *Unified Diff*), además de los metadatos de autoría, fechas y estados de bloqueo. Esto permite que el archivo de trabajo original permanezca siempre limpio y utilizable directamente por el sistema operativo.

Aunque nació como una librería embebida, su diseño arquitectónico cubre escenarios muy concretos donde los sistemas tradicionales resultan excesivos:

*   **Integración programática (Agentes de IA y automatización):** Permite dotar a sistemas autónomos de una capa de *rollback* y trazabilidad de cambios sobre el sistema de ficheros local, sin depender de la instalación de binarios externos (`git`, `diff`, `patch`) y manteniendo todo el proceso dentro de la JVM.
*   **Gestión granular de archivos aislados:** Resulta útil para mantener el historial exacto de ficheros de configuración (como `/etc/hosts` o `.conf`), scripts sueltos o borradores de texto, donde inicializar una carpeta `.git` entera añade una sobrecarga estructural innecesaria.
*   **Entornos restringidos o efímeros:** Contenedores o despliegues limitados donde instalar clientes de control de versiones pesados o configurar accesos a repositorios remotos es inviable.
*   **Arqueología de software y aprendizaje:** Al ser una implementación puramente Java y utilizar *Unified Diffs* legibles, es una herramienta excelente para inspeccionar y entender los fundamentos mecánicos clásicos del control de versiones (bloqueos explícitos, expansión de palabras clave, almacenamiento por deltas inversos).

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

## Referencia de Comandos CLI

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

## Licencia

Este proyecto está bajo la GPL 3. Consulta el archivo `LICENSE` para más detalles.

---

*Desarrollado por [Joaquin del Cerro](https://github.com/jjdelcerro) como parte de la investigación sobre arquitecturas de IA.*
