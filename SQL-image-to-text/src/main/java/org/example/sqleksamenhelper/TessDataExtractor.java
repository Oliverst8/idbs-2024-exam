package org.example.sqleksamenhelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class TessDataExtractor {

    /**
     * Extracts the tessdata directory from resources to a temporary directory.
     *
     * @return The path to the temporary tessdata directory.
     * @throws IOException If an I/O error occurs.
     */
    public static String extractTessData() throws IOException {
        // Create a temporary directory
        Path tempDir = Files.createTempDirectory("tessdata");
        tempDir.toFile().deleteOnExit();

        // Path within the resources
        Path resourcePath = Paths.get("tessdata");

        // List of traineddata files to extract
        // Alternatively, you can programmatically list all files in the tessdata directory
        String[] trainedDataFiles = {"eng.traineddata"}; // Add more if needed

        for (String fileName : trainedDataFiles) {
            // Construct the resource path
            String resource = "/tessdata/" + fileName;

            // Get the input stream
            try (InputStream is = TessDataExtractor.class.getResourceAsStream(resource)) {
                if (is == null) {
                    throw new IOException("Resource not found: " + resource);
                }

                // Path to the output file
                Path outputPath = tempDir.resolve(fileName);

                // Copy the file
                Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
                outputPath.toFile().deleteOnExit();
            }
        }

        return tempDir.toAbsolutePath().toString();
    }
}
