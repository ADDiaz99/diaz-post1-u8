package com.example.auditoria.arquitectura;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La Dependency Rule convertida en prueba: lee los imports del código fuente
 * (Maven ejecuta las pruebas desde la carpeta del proyecto).
 */
class ReglaDeDependenciaTest {

    private static final Path RAIZ = Path.of("src/main/java/com/example/auditoria");

    @Test
    void domainSoloImportaJavaYASiMismo() {
        assertThat(importsQueNoEmpiezanCon("domain", "java.", "com.example.auditoria.domain")).isEmpty();
    }

    @Test
    void usecaseSoloImportaJavaDomainYUsecase() {
        assertThat(importsQueNoEmpiezanCon("usecase", "java.",
                "com.example.auditoria.domain", "com.example.auditoria.usecase")).isEmpty();
    }

    private List<String> importsQueNoEmpiezanCon(String paquete, String... permitidos) {
        try (Stream<Path> archivos = Files.walk(RAIZ.resolve(paquete))) {
            return archivos.filter(archivo -> archivo.toString().endsWith(".java"))
                    .flatMap(archivo -> leerImports(archivo).stream()
                            .filter(imp -> Stream.of(permitidos).noneMatch(imp::startsWith))
                            .map(imp -> archivo.getFileName() + " importa " + imp))
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static List<String> leerImports(Path archivo) {
        try {
            return Files.readAllLines(archivo).stream()
                    .map(String::trim)
                    .filter(linea -> linea.startsWith("import "))
                    .map(linea -> linea.substring("import ".length()).replace("static ", "").replace(";", "").trim())
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
