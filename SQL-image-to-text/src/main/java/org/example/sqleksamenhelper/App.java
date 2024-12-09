package org.example.sqleksamenhelper;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class App extends Application {

    private ImageView imageView;
    private TextArea resultArea;
    private BufferedImage bufferedImage;
    private String tessDataPath;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize tessdata path
        try {
            tessDataPath = TessDataExtractor.extractTessData();
        } catch (IOException e) {
            showAlert("Initialization Error", "Failed to load tessdata: " + e.getMessage());
            e.printStackTrace();
            return; // Exit if tessdata can't be loaded
        }

        // UI Components
        Button uploadFileButton = new Button("Upload from File");
        Button pasteButton = new Button("Paste from Clipboard");
        Button convertButton = new Button("Convert to Text");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        // Layout
        HBox buttonsBox = new HBox(10, uploadFileButton, pasteButton, convertButton);
        buttonsBox.setPadding(new Insets(10));

        VBox root = new VBox(10, buttonsBox, imageView, new Label("OCR Result:"), resultArea);
        root.setPadding(new Insets(10));

        // Event Handlers
        uploadFileButton.setOnAction(e -> uploadFromFile(primaryStage));
        pasteButton.setOnAction(e -> pasteFromClipboard());
        convertButton.setOnAction(e -> convertImageToText());

        // Scene and Stage
        Scene scene = new Scene(root, 600, 700);
        primaryStage.setTitle("Image to Text Converter");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void uploadFromFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        // Set extension filters
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg, *.jpeg)", "*.jpg", "*.jpeg");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                BufferedImage originalImage = ImageIO.read(file);
                bufferedImage = ImagePreprocessor.preprocessImage(originalImage); // Preprocess the image
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(image);
            } catch (IOException ex) {
                showAlert("Error", "Failed to load image: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void pasteFromClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasImage()) {
            Image fxImage = clipboard.getImage();
            imageView.setImage(fxImage);

            // Convert JavaFX Image to BufferedImage
            BufferedImage originalImage = SwingFXUtils.fromFXImage(fxImage, null);
            bufferedImage = ImagePreprocessor.preprocessImage(originalImage); // Preprocess the image
        } else {
            showAlert("No Image", "Clipboard does not contain an image.");
        }
    }

    private void convertImageToText() {
        if (bufferedImage == null) {
            showAlert("No Image", "Please upload or paste an image first.");
            return;
        }

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("eng");

        // Set Page Segmentation Mode (PSM)
        tesseract.setPageSegMode(6); // Assume a single uniform block of text

        // Set a whitelist of characters commonly used in code
        tesseract.setTessVariable("tessedit_char_whitelist", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_();,.<>[]{}+-*/=<>!@#$%^&|~`:\"'\\|/? ");

        try {
            String result = tesseract.doOCR(bufferedImage);
            result = postProcessOCRResult(result); // Apply post-processing
            System.out.println("OCR Result:\n" + result);
            resultArea.setText(result);
        } catch (TesseractException e) {
            showAlert("OCR Error", "Failed to perform OCR: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String postProcessOCRResult(String ocrResult) {
        // Remove doubel spaces
        //ocrResult = ocrResult.replaceAll("\\s+", " ");
        
        // Replace spaces with underscores in variable names (if applicable)
        ocrResult = ocrResult.replaceAll("([a-z]{2,}) ([a-z]{2,})", "$1_$2");
        
        // Remove spaces before or after _ (underscore)
        ocrResult = ocrResult.replaceAll("\\s*_\\s*", "_");
        
        // Remove empty lines
        ocrResult = ocrResult.replaceAll("(?m)^\\s*\\n", "");
        
        // Add more replacements as needed based on common errors
        return ocrResult;
    }
}

//public class HelloApplication extends Application {
//    @Override
//    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//        stage.setTitle("Hello!");
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch();
//    }
//}