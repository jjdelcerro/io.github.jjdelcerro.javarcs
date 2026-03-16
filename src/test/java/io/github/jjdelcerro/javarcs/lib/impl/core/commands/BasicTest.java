package io.github.jjdelcerro.javarcs.lib.impl.core.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.RCSFile;
import io.github.jjdelcerro.javarcs.lib.RCSLocator;
import io.github.jjdelcerro.javarcs.lib.RCSManager;
import io.github.jjdelcerro.javarcs.lib.commands.CheckinOptions;
import io.github.jjdelcerro.javarcs.lib.commands.CheckoutOptions;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Disabled;

/**
 * Pruebas de integración para el comando Checkin (ci).
 * Cada prueba se ejecuta en un directorio temporal aislado para evitar interferencias.
 */
class BasicTest {

    /**
     * JUnit 5 inyectará automáticamente un directorio temporal único para cada
     * método de prueba anotado con @Test. Este directorio y su contenido se
     * eliminarán automáticamente después de que la prueba finalice.
     */
    @TempDir
    Path tempDir;


    private void generate_text_file(String suffix) throws IOException {
        String resourcePath = "/io/github/jjdelcerro/javarcs/lib/impl/core/commands/basictest_1"+suffix+".txt";
        InputStream resourceStream = getClass().getResourceAsStream(resourcePath);
        assertThat(resourceStream)
                .withFailMessage("El fichero de recurso no se encontró: " + resourcePath)
                .isNotNull()
        ;
        Path workFile = tempDir.resolve("test.txt");
        Files.copy(resourceStream, workFile, StandardCopyOption.REPLACE_EXISTING);
    }
    
    private void do_first_revision() throws IOException {
        generate_text_file("r1");
        
        RCSManager manager = RCSLocator.getRCSManager();

        Path workFile = tempDir.resolve("test.txt");
        Files.copy(workFile, Path.of(workFile.toString()+".r1"));
        
        CheckinOptions checkinOptions = manager.createCheckinOptions(workFile)
                .setMessage("Primer check-in inicial.")
                .setAuthor("tester")
                .setInit(true) 
                .setQuiet(true)
        ;
        RCSCommand command = manager.create(checkinOptions);
        command.execute(checkinOptions);

    }
    
    private void do_second_revision() throws IOException {
        generate_text_file("r2");
        RCSManager manager = RCSLocator.getRCSManager();

        Path workFile = tempDir.resolve("test.txt");
        Files.copy(workFile, Path.of(workFile.toString()+".r2"));
        CheckinOptions checkinOptions = manager.createCheckinOptions(workFile)
                .setMessage("Segunda revision")
                .setAuthor("tester")
                .setQuiet(true)
        ;
        RCSCommand command = manager.create(checkinOptions);
        command.execute(checkinOptions);

    }
    
    /**
     * Test 1: Verifica el escenario "Happy Path" de un check-in inicial.
     * - Ejecuta el comando 'ci'.
     * - Verifica que el fichero RCS (,jv) ha sido creado correctamente.
     */
    @Test
//    @Disabled
    void checkCreateFirstRevision() throws Exception {
        RCSManager manager = RCSLocator.getRCSManager();

        do_first_revision();
        
        Path rcsFile = tempDir.resolve("test.txt,jv");
        RCSFile rcs = manager.getRCSFile(rcsFile);
        assertThat(rcs.getDeltas()).isNotEmpty().hasSize(1);

        assertThat(rcsFile).exists();
        assertThat(rcsFile).isRegularFile();

        Path workFile = tempDir.resolve("test.txt");
        assertThat(workFile).exists();
        assertThat(workFile).isRegularFile();
    }

    @Test
//    @Disabled
    void checkCreateWith2Revisions() throws Exception {
        RCSManager manager = RCSLocator.getRCSManager();

        do_first_revision();
        do_second_revision();
        
        Path rcsFile = tempDir.resolve("test.txt,jv");
        RCSFile rcs = manager.getRCSFile(rcsFile);
        assertThat(rcs.getDeltas()).isNotEmpty().hasSize(2);

        assertThat(rcsFile).exists();
        assertThat(rcsFile).isRegularFile();

        Path workFile = tempDir.resolve("test.txt");
        assertThat(workFile).exists();
        assertThat(workFile).isRegularFile();
    }

    @Test
//    @Disabled
    void checkCreateWith2RevisionsAndCheckoutR1() throws Exception {
        RCSManager manager = RCSLocator.getRCSManager();

        do_first_revision();
        do_second_revision();
        
        Path workFile = tempDir.resolve("test.txt");
        String workFileContents = FileUtils.readFileToString(workFile.toFile(), StandardCharsets.UTF_8);
        String r1FileContents = FileUtils.readFileToString(tempDir.resolve("test.txt.r1").toFile(), StandardCharsets.UTF_8);
        String r2FileContents = FileUtils.readFileToString(tempDir.resolve("test.txt.r2").toFile(), StandardCharsets.UTF_8);
        
        Path rcsFile = tempDir.resolve("test.txt,jv");        
        RCSFile rcs = manager.getRCSFile(rcsFile);
        assertThat(rcsFile).exists();
        assertThat(rcsFile).isRegularFile();
        assertThat(rcs.getDeltas()).isNotEmpty().hasSize(2);

        CheckoutOptions checkoutOptions = manager.createCheckoutOptions(tempDir.resolve("test.txt"))
                .setRevision("1.1")
                .setQuiet(true)
                .setPipeOut(false)
                .setForce(true)
        ;
        RCSCommand command = manager.create(checkoutOptions);
        command.execute(checkoutOptions);
        
        assertThat(workFile).exists();
        assertThat(workFile).isRegularFile();
        assertThat(workFileContents).isEqualTo(r2FileContents);
        
        checkoutOptions = manager.createCheckoutOptions(tempDir.resolve("test.txt"))
                .setRevision("1.2")
                .setQuiet(true)
                .setPipeOut(false)
                .setForce(true)
        ;
        command = manager.create(checkoutOptions);
        
        command.execute(checkoutOptions);
        assertThat(workFileContents).isEqualTo(r2FileContents);
    }
}
