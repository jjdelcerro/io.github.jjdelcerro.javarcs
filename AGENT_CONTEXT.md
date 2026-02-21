
# Informe Técnico de Análisis: JavaRCS

## 1. Visión General

**JavaRCS** es una reimplementación completa y moderna del sistema de control de versiones clásico **RCS (Revision Control System)**, escrita enteramente en **Java**.

El objetivo del proyecto es proporcionar la funcionalidad de control de versiones de archivos individuales (bloqueos, historial, ramas, palabras clave) en un entorno puramente Java, eliminando la dependencia de herramientas nativas de Unix/Linux.

**Diferenciador Clave:**

La diferencia arquitectónica más significativa respecto al RCS original (GNU RCS escrito en C) radica en el formato de almacenamiento de los cambios ("deltas").
*   **RCS Original:** Utiliza scripts de edición (`ed`) para almacenar las diferencias.
*   **JavaRCS:** Utiliza el formato **Unified Diff**. Esto moderniza el motor de diferencias, haciéndolo más robusto y legible, aunque sacrifica la compatibilidad binaria estricta con los archivos `,v` generados por el comando `ci` original de GNU (aunque la estructura de metadatos y cabeceras se mantiene idéntica).

### 1.1. Contexto de Uso y Restricciones Arquitectónicas

Este sistema está diseñado para ser utilizado como **subsistema de memoria y seguridad** embebido dentro de agentes autónomos de IA (ej. `ChatAgent`). Esto impone restricciones estrictas de diseño que diferencian a JavaRCS de otras herramientas de control de versiones:

1.  **Atomicidad y Aislamiento:** El sistema debe operar sobre archivos individuales sin asumir la existencia de un repositorio global o estructura de proyecto.
2.  **Portabilidad "Pure Java":** Está prohibido el uso de dependencias nativas (JNI) o invocaciones al sistema operativo (`Runtime.exec("diff")`). Todo el cálculo de diferencias y parches debe ocurrir dentro de la JVM para garantizar la ejecución en entornos restringidos.
3.  **Determinismo:** Las operaciones deben ser predecibles y seguras, actuando como mecanismo de "deshacer/revertir" fiable para procesos automatizados.

## 2. Stack Tecnológico

El proyecto apuesta por un stack minimalista y estándar, priorizando la portabilidad y la mantenibilidad.

*   **Lenguaje:** **Java 21**. Se hace uso extensivo de características modernas como la API NIO.2 (`java.nio.file`), Streams, Lambdas y el manejo robusto de fechas con `java.util.Calendar`/`Date` (necesario para compatibilidad con el formato RCS).
*   **Gestión de Construcción:** **Apache Maven**.
*   **Librerías de Terceros (Dependencias):**

    1.  **`java-diff-utils` (v4.12):** El núcleo del motor de diferencias. Se utiliza para generar parches (algoritmo de Myers) y aplicar parches (Unified Diffs) tanto para texto como para fusiones de tres vías (merge).
    2.  **`commons-cli` (v1.6.0):** Utilizada para el parseo profesional de argumentos de línea de comandos, facilitando la implementación de flags complejos (ej. `-r1.1`, `-m"msg"`).
    
*   **Dependencias del Sistema:** Ninguna. No requiere `diff` ni `merge` nativos instalados en el sistema operativo.

## 3. Estructura de Paquetes e Interfaces

El código sigue una arquitectura limpia, modular y desacoplada, separando la interfaz de usuario de la lógica de negocio y el modelo de datos.

### 3.1. Paquete: `io.github.jjdelcerro.javarcs.main.cli`
Contiene el punto de entrada de la aplicación.

*   **`RCSCli`:** Clase principal (`main`). Su única responsabilidad es parsear los argumentos de entrada usando `commons-cli`, configurar los objetos de opciones (`Options`) y despachar la ejecución al comando correspondiente a través del `RCSManager`.

### 3.2. Paquete: `io.github.jjdelcerro.javarcs.lib` (API Pública)
Define los contratos para interactuar con la librería RCS.

*   **`RCSManager`:** Interfaz principal (Factory) para crear opciones y comandos.
*   **`RCSCommand<T>`:** Interfaz genérica que implementan todos los comandos (`execute(T options)`).
*   **`RCSLocator`:** Implementación del patrón *Service Locator* o *Singleton* para obtener la instancia del `RCSManager`.

### 3.3. Paquete: `io.github.jjdelcerro.javarcs.lib.impl` (Implementación)

*   **`RCSManagerImpl`:** Implementación concreta de la fábrica.

#### Subpaquete: `core.model` (Modelo de Dominio)
Representa la estructura en memoria de un archivo RCS (`.jv`).

*   **`RCSFile`:** Objeto raíz. Contiene cabeceras, listas de acceso, bloqueos y la lista de deltas. Mantiene flags de estado (`SYNCED`, `PARSED`).
*   **`RCSDelta`:** Representa una revisión concreta. Contiene metadatos (autor, fecha, log) y el texto del delta (o contenido completo si es binario/HEAD).
*   **`RCSRevisionNumber`:** Clase crítica y compleja. Maneja la lógica de numeración de revisiones (`1.1`, `1.1.2.1`), incrementos y comparación de ramas.
*   **Entidades auxiliares:** `RCSLockEntry`, `RCSAccessEntry`, `RCSSymbolEntry`.

#### Subpaquete: `core.commands` (Lógica de Negocio)
Implementación del patrón **Command**. Cada clase encapsula la lógica de una operación RCS:

*   `CheckinCommand` (`ci`), `CheckoutCommand` (`co`), `LogCommand` (`rlog`), `DiffCommand` (`rcsdiff`), `MergeCommand` (`rcsmerge`), `CleanCommand` (`rcsclean`), `IdentCommand` (`ident`).
*   Las clases `*OptionsImpl` son DTOs (Data Transfer Objects) que transportan la configuración desde la CLI al comando.

#### Subpaquete: `core.util` (Motor Interno)
Aquí reside la complejidad técnica del proyecto:

*   **`RCSParser`:** Parser descendente recursivo escrito a mano ("hand-written recursive descent parser"). Lee el formato RCS token por token, manejando cadenas citadas con `@`.
*   **`RCSWriter`:** Serializa el objeto `RCSFile` a disco.
*   **`RCSDeltaProcessor`:** Implementa la lógica de **Deltas Inversos (Reverse Deltas)**. Reconstruye versiones antiguas aplicando parches hacia atrás desde la `HEAD`.
*   **`DiffAlgorithm`:** Wrapper sobre `java-diff-utils` para generar Unified Diffs.
*   **`ThreeWayMergeAlgorithm`:** Implementa la lógica de fusión (diff3) para resolver conflictos entre ramas.
*   **`RCSKeywordExpander`:** Procesa la sustitución de keywords (`$Id$`, `$Date$`).

## 4. Arquitectura y Diseño

### 4.1. Patrones de Diseño Identificados

1.  **Command Pattern:** Desacopla la invocación (`RCSCli`) de la ejecución (`core.commands`). Permite añadir nuevos comandos sin modificar el núcleo.
2.  **Factory Pattern:** `RCSManager` centraliza la creación de comandos y opciones.
3.  **DTO (Data Transfer Object):** Las interfaces `*Options` y sus implementaciones aíslan la lógica de comando de las librerías de parsing de argumentos.
4.  **Reverse Delta Storage:** Adopta la filosofía de diseño clásica de RCS para optimizar el acceso a la última versión:

    *   La revisión `HEAD` (última) se guarda completa.
    *   Las revisiones anteriores se guardan como parches (deltas) necesarios para volver atrás desde la versión siguiente.
    *   *Mejora moderna:* Los parches son "Unified Diffs" estándar, no instrucciones de `ed`.

### 4.2. Flujo de Datos (Ejemplo: `ci`)

1.  **Parsing:** `RCSCli` recibe argumentos, crea `CheckinOptions`.
2.  **Carga:** `CheckinCommand` usa `RCSParser` para leer el archivo `.v` existente en un `RCSFile`.
3.  **Diff:** Compara el archivo de trabajo actual con la `HEAD` actual (reconstruida por `RCSDeltaProcessor`) usando `DiffAlgorithm`.
4.  **Actualización Modelo:**

    *   El contenido de la antigua `HEAD` se reemplaza por un delta (parche inverso) para ir de la *nueva* a la *antigua*.
    *   Se crea un nuevo `RCSDelta` con el contenido completo del archivo de trabajo.
    *   Se actualizan punteros y números de revisión (`RCSRevisionNumber`).
    
5.  **Persistencia:** `RCSWriter` escribe el `RCSFile` a un archivo temporal y realiza un **move atómico** (`Files.move` con `ATOMIC_MOVE`) para reemplazar el original, garantizando integridad transaccional.

### 4.3. Manejo de Binarios
El sistema detecta automáticamente archivos binarios (`RCSFileUtils.isBinaryFile` buscando bytes nulos).

*   Si es binario, establece el flag `expand="b"`.
*   **Comportamiento:** No calcula diferencias. Almacena snapshots completos en cada revisión para evitar corrupción de datos al intentar aplicar parches de texto sobre binarios.

## 5. Construcción y Despliegue

El archivo `pom.xml` define un ciclo de vida robusto:

1.  **Compilación:** Configurado para **Java 21**.
2.  **Empaquetado (Fat JAR):**

    *   Usa `maven-assembly-plugin` y `maven-shade-plugin`.
    *   El objetivo es generar un **Uber-JAR** (`jar-with-dependencies`) que contenga dentro las clases de `commons-cli` y `java-diff-utils`.
    *   Esto permite distribuir un único archivo `.jar` que funciona como ejecutable autocontenido (`java -jar javarcs.jar`).

## 6. Comandos RCS Soportados

El análisis del código confirma soporte para las operaciones esenciales de RCS:

1.  **`ci` (Check In):**

    *   Guarda revisiones, maneja modos interactivos para la descripción, soporta inicialización (`-i`), fechas forzadas (`-d`), autores (`-w`) y estados (`-s`).
    *   Maneja la lógica de incremento de versión (`1.1` -> `1.2`).

2.  **`co` (Check Out):**

    *   Recupera versiones específicas o la última por defecto.
    *   Soporta bloqueo (`-l`, locking) y desbloqueo (`-u`).
    *   Implementa expansión de palabras clave (Keywords: `$Id$`, `$Header$`, etc.).

3.  **`rlog` (Log):**

    *   Muestra metadatos, historial de commits, ramas y bloqueos.
    *   Soporta filtros potentes: por fecha, autor, estado o rango de revisiones.

4.  **`rcsdiff` (Diff):**

    *   Compara el archivo de trabajo contra la última revisión, o dos revisiones entre sí.
    *   Salida en formato Unified Diff estándar.

5.  **`rcsmerge` (Merge):**

    *   Realiza una fusión de 3 vías (Base, Mio, Tuyo).
    *   Detecta conflictos y escribe marcadores estándar (`<<<<<<<`, `=======`, `>>>>>>>`) en el archivo.

6.  **`rcsclean` (Clean):**

    *   Compara el archivo de trabajo con la última revisión. Si son idénticos y no está bloqueado, borra el archivo de trabajo para limpiar el directorio.

7.  **`ident`:**

    *   Escanea archivos en busca de patrones de palabras clave RCS (`$Id: ... $`) y los imprime.

## 7. Detalles Relevantes Adicionales

*   **Robustez en Parseo:** El `RCSParser` maneja correctamente el formato de strings "quoted" de RCS (usando `@` como delimitador y `@@` para escapar), lo cual es crucial para soportar descripciones y logs que contengan caracteres especiales.
*   **Gestión de Archivos Temporales:** La clase `TemporaryFileManager` registra los archivos temporales creados (para diffs y merges) y utiliza un `ShutdownHook` de la JVM para asegurar que se borren al finalizar el programa, evitando basura en el disco.
*   **Seguridad de Hilos:** Aunque RCS es fundamentalmente una herramienta de línea de comandos de un solo hilo por proceso, el uso de `Collections.synchronizedSet` en el gestor de archivos temporales denota cuidado en el diseño.
*   **Ramas (Branches):** El modelo de datos (`RCSRevisionNumber`, `RCSDelta`) tiene soporte completo para ramas (ej. `1.2.1.1`), aunque la lógica de creación de ramas en `ci` parece seguir el incremento lineal por defecto a menos que se fuerce un número.

## 8. Conclusión

**JavaRCS** es una implementación técnica de calidad. Logra un equilibrio difícil: respetar la semántica y estructura de una herramienta heredada de hace décadas (RCS) mientras moderniza su implementación interna utilizando patrones de diseño orientados a objetos y librerías modernas.

**Puntos Fuertes:**
1.  **Portabilidad Total:** Al eliminar dependencias de comandos nativos (`diff`, `ed`), funciona idénticamente en Windows, Linux y macOS solo con la JVM.
2.  **Código Limpio:** La separación `CLI -> Manager -> Command -> Model` hace que el código sea muy legible y fácil de extender.
3.  **Modernización del Diff:** El uso de Unified Diffs hace que la inspección manual de los archivos `.jv` (si fuera necesaria) sea mucho más comprensible para un humano que los scripts `ed` originales.

**Limitación (Intencional):**
La falta de compatibilidad binaria en el contenido de los deltas con el `co` de GNU RCS significa que no se pueden mezclar herramientas: no se debe usar el `co` de C para leer un archivo escrito por el `ci` de JavaRCS y viceversa, aunque los metadatos sean compatibles. Sin embargo, como solución autocontenida, es una herramienta excelente para entornos donde Git es excesivo o no está disponible.

