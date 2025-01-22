package logica;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Impresora {

    public void imprimirEtiqueta(String zplData) {
        String filePath = System.getProperty("user.home") + "\\Desktop\\bebeBoste.zpl";
        String printerName = "ZDesigner ZD421-300dpi ZPL"; // Cambiar por el nombre exacto de tu impresora
        String rawPrintDirectory = "C:\\Users\\ASUS\\OneDrive\\Escritorio"; // Directorio que contiene RawPrint.exe

        try {
            // Crear el archivo ZPL
            File zplFile = new File(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(zplFile, StandardCharsets.UTF_8))) {
                writer.write(zplData);
            }

            System.out.println("Archivo ZPL creado exitosamente en: " + zplFile.getAbsolutePath());

            // Crear el comando para ejecutar RawPrint.exe
            String rawPrintCommand = String.format(
                    "RawPrint.exe /f \"%s\" /pr \"%s\"",
                    zplFile.getAbsolutePath(),
                    printerName
            );

            // Configurar el proceso para ejecutar el comando
            ProcessBuilder rawPrintProcessBuilder = new ProcessBuilder("cmd", "/c", rawPrintCommand);
            rawPrintProcessBuilder.directory(new File(rawPrintDirectory));

            // Redirigir salida estándar y errores para que no aparezcan ventanas visibles
            rawPrintProcessBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            rawPrintProcessBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

            // Iniciar el proceso
            Process rawPrintProcess = rawPrintProcessBuilder.start();

            // Esperar a que termine la impresión
            int printExitCode = rawPrintProcess.waitFor();

            if (printExitCode == 0) {
                System.out.println("Etiqueta enviada a la impresora exitosamente.");
            } else {
                System.err.println("Error al enviar la etiqueta a la impresora. Código de salida: " + printExitCode);
            }

        } catch (IOException e) {
            System.err.println("Error al crear el archivo ZPL o al encontrar RawPrint.exe.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("El proceso fue interrumpido.");
            e.printStackTrace();
        }
    }
}
