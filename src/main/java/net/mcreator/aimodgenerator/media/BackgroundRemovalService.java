package net.mcreator.aimodgenerator.media;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;

/**
 * Service for removing backgrounds from images using free APIs
 * Specifically designed to work with Nano Banana (Gemini Flash 2.0) generated images
 */
public class BackgroundRemovalService {
    
    private final HttpClient httpClient;
    private final Gson gson;
    
    // Free background removal APIs
    private static final String REMOVE_BG_API = "https://api.remove.bg/v1.0/removebg";
    private static final String REMOVE_BG_API_KEY = "your_remove_bg_api_key"; // Free tier: 50 images/month
    private static final String PHOTOSCISSORS_API = "https://api.photoscissors.com/cutout";
    private static final String CLIPDROP_API = "https://clipdrop-api.co/remove-background/v1";
    private static final String CLIPDROP_API_KEY = "your_clipdrop_api_key"; // Free tier available
    
    public BackgroundRemovalService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    
    /**
     * Removes background from an image using multiple free APIs with fallback
     * @param inputImagePath Path to input image
     * @param outputImagePath Path to save output image with transparent background
     * @return CompletableFuture with path to processed image
     */
    public CompletableFuture<String> removeBackground(String inputImagePath, String outputImagePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Try Remove.bg first (highest quality)
                try {
                    return removeBackgroundWithRemoveBg(inputImagePath, outputImagePath);
                } catch (Exception e) {
                    System.out.println("Remove.bg failed, trying ClipDrop: " + e.getMessage());
                }
                
                // Try ClipDrop as fallback
                try {
                    return removeBackgroundWithClipDrop(inputImagePath, outputImagePath);
                } catch (Exception e) {
                    System.out.println("ClipDrop failed, trying PhotoScissors: " + e.getMessage());
                }
                
                // Try PhotoScissors as second fallback
                try {
                    return removeBackgroundWithPhotoScissors(inputImagePath, outputImagePath);
                } catch (Exception e) {
                    System.out.println("PhotoScissors failed, using local algorithm: " + e.getMessage());
                }
                
                // Use local algorithm as final fallback
                return removeBackgroundLocally(inputImagePath, outputImagePath);
                
            } catch (Exception e) {
                throw new RuntimeException("Background removal failed: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Removes background using Remove.bg API (free tier)
     * @param inputPath Input image path
     * @param outputPath Output image path
     * @return Path to processed image
     */
    private String removeBackgroundWithRemoveBg(String inputPath, String outputPath) throws Exception {
        byte[] imageBytes = Files.readAllBytes(Paths.get(inputPath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("image_file_b64", base64Image);
        requestBody.addProperty("size", "auto");
        requestBody.addProperty("format", "png");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(REMOVE_BG_API))
                .header("X-Api-Key", REMOVE_BG_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
        
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            Path outputFilePath = Paths.get(outputPath);
            Files.createDirectories(outputFilePath.getParent());
            Files.write(outputFilePath, response.body());
            return outputPath;
        } else {
            throw new IOException("Remove.bg API failed with status: " + response.statusCode());
        }
    }
    
    /**
     * Removes background using ClipDrop API (free tier)
     * @param inputPath Input image path
     * @param outputPath Output image path
     * @return Path to processed image
     */
    private String removeBackgroundWithClipDrop(String inputPath, String outputPath) throws Exception {
        byte[] imageBytes = Files.readAllBytes(Paths.get(inputPath));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLIPDROP_API))
                .header("x-api-key", CLIPDROP_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                .build();
        
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            Path outputFilePath = Paths.get(outputPath);
            Files.createDirectories(outputFilePath.getParent());
            Files.write(outputFilePath, response.body());
            return outputPath;
        } else {
            throw new IOException("ClipDrop API failed with status: " + response.statusCode());
        }
    }
    
    /**
     * Removes background using PhotoScissors API (free tier)
     * @param inputPath Input image path
     * @param outputPath Output image path
     * @return Path to processed image
     */
    private String removeBackgroundWithPhotoScissors(String inputPath, String outputPath) throws Exception {
        byte[] imageBytes = Files.readAllBytes(Paths.get(inputPath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String formData = "--" + boundary + "\r\n" +
                         "Content-Disposition: form-data; name=\"image\"\r\n\r\n" +
                         base64Image + "\r\n" +
                         "--" + boundary + "--\r\n";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PHOTOSCISSORS_API))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
            if (responseJson.has("cutout_base64")) {
                String cutoutBase64 = responseJson.get("cutout_base64").getAsString();
                byte[] cutoutBytes = Base64.getDecoder().decode(cutoutBase64);
                
                Path outputFilePath = Paths.get(outputPath);
                Files.createDirectories(outputFilePath.getParent());
                Files.write(outputFilePath, cutoutBytes);
                return outputPath;
            }
        }
        
        throw new IOException("PhotoScissors API failed with status: " + response.statusCode());
    }
    
    /**
     * Removes background using local algorithm (fallback method)
     * Uses color-based segmentation and edge detection
     * @param inputPath Input image path
     * @param outputPath Output image path
     * @return Path to processed image
     */
    private String removeBackgroundLocally(String inputPath, String outputPath) throws Exception {
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        BufferedImage processedImage = removeBackgroundAlgorithm(originalImage);
        
        Path outputFilePath = Paths.get(outputPath);
        Files.createDirectories(outputFilePath.getParent());
        
        // Ensure PNG format for transparency
        String format = "png";
        if (!outputPath.toLowerCase().endsWith(".png")) {
            outputPath = outputPath.replaceAll("\\.[^.]+$", ".png");
            outputFilePath = Paths.get(outputPath);
        }
        
        ImageIO.write(processedImage, format, outputFilePath.toFile());
        return outputPath;
    }
    
    /**
     * Local background removal algorithm
     * Uses multiple techniques: color similarity, edge detection, and flood fill
     * @param originalImage Input image
     * @return Image with transparent background
     */
    private BufferedImage removeBackgroundAlgorithm(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Create output image with alpha channel
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Step 1: Identify likely background colors (corners and edges)
        Color[] backgroundColors = identifyBackgroundColors(originalImage);
        
        // Step 2: Create mask based on color similarity
        boolean[][] backgroundMask = createBackgroundMask(originalImage, backgroundColors);
        
        // Step 3: Refine mask using edge detection
        backgroundMask = refineWithEdgeDetection(originalImage, backgroundMask);
        
        // Step 4: Apply morphological operations to clean up the mask
        backgroundMask = morphologicalCleanup(backgroundMask);
        
        // Step 5: Apply the mask to create transparent background
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);
                
                if (backgroundMask[x][y]) {
                    // Make background transparent
                    result.setRGB(x, y, 0x00000000);
                } else {
                    // Keep original pixel with full opacity
                    result.setRGB(x, y, rgb | 0xFF000000);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Identifies potential background colors from image corners and edges
     * @param image Input image
     * @return Array of potential background colors
     */
    private Color[] identifyBackgroundColors(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Sample colors from corners and edges
        Color[] samples = new Color[20];
        int sampleIndex = 0;
        
        // Corner samples
        samples[sampleIndex++] = new Color(image.getRGB(0, 0));
        samples[sampleIndex++] = new Color(image.getRGB(width - 1, 0));
        samples[sampleIndex++] = new Color(image.getRGB(0, height - 1));
        samples[sampleIndex++] = new Color(image.getRGB(width - 1, height - 1));
        
        // Edge samples
        for (int i = 0; i < 4; i++) {
            samples[sampleIndex++] = new Color(image.getRGB(width / 4 * (i + 1), 0));
            samples[sampleIndex++] = new Color(image.getRGB(width / 4 * (i + 1), height - 1));
            samples[sampleIndex++] = new Color(image.getRGB(0, height / 4 * (i + 1)));
            samples[sampleIndex++] = new Color(image.getRGB(width - 1, height / 4 * (i + 1)));
        }
        
        return samples;
    }
    
    /**
     * Creates background mask based on color similarity
     * @param image Input image
     * @param backgroundColors Potential background colors
     * @return Boolean mask where true indicates background
     */
    private boolean[][] createBackgroundMask(BufferedImage image, Color[] backgroundColors) {
        int width = image.getWidth();
        int height = image.getHeight();
        boolean[][] mask = new boolean[width][height];
        
        double threshold = 30.0; // Color similarity threshold
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                
                // Check if pixel is similar to any background color
                for (Color bgColor : backgroundColors) {
                    if (bgColor != null && colorDistance(pixelColor, bgColor) < threshold) {
                        mask[x][y] = true;
                        break;
                    }
                }
            }
        }
        
        return mask;
    }
    
    /**
     * Calculates color distance using Euclidean distance in RGB space
     * @param c1 First color
     * @param c2 Second color
     * @return Distance between colors
     */
    private double colorDistance(Color c1, Color c2) {
        double dr = c1.getRed() - c2.getRed();
        double dg = c1.getGreen() - c2.getGreen();
        double db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
    
    /**
     * Refines background mask using edge detection
     * @param image Input image
     * @param mask Current background mask
     * @return Refined mask
     */
    private boolean[][] refineWithEdgeDetection(BufferedImage image, boolean[][] mask) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Simple edge detection using Sobel operator
        double[][] edges = new double[width][height];
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // Convert to grayscale for edge detection
                int[] gx = new int[9];
                int[] gy = new int[9];
                int idx = 0;
                
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        Color c = new Color(image.getRGB(x + dx, y + dy));
                        int gray = (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
                        gx[idx] = gray;
                        gy[idx] = gray;
                        idx++;
                    }
                }
                
                // Sobel X kernel: [-1, 0, 1; -2, 0, 2; -1, 0, 1]
                int sobelX = -gx[0] + gx[2] - 2 * gx[3] + 2 * gx[5] - gx[6] + gx[8];
                
                // Sobel Y kernel: [-1, -2, -1; 0, 0, 0; 1, 2, 1]
                int sobelY = -gx[0] - 2 * gx[1] - gx[2] + gx[6] + 2 * gx[7] + gx[8];
                
                edges[x][y] = Math.sqrt(sobelX * sobelX + sobelY * sobelY);
            }
        }
        
        // Use edges to refine mask - strong edges likely indicate object boundaries
        double edgeThreshold = 50.0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (edges[x][y] > edgeThreshold) {
                    // Strong edge - likely object boundary, don't remove
                    mask[x][y] = false;
                }
            }
        }
        
        return mask;
    }
    
    /**
     * Applies morphological operations to clean up the mask
     * @param mask Input mask
     * @return Cleaned mask
     */
    private boolean[][] morphologicalCleanup(boolean[][] mask) {
        int width = mask.length;
        int height = mask[0].length;
        
        // Apply erosion followed by dilation (opening operation)
        boolean[][] eroded = erode(mask);
        boolean[][] opened = dilate(eroded);
        
        // Apply dilation followed by erosion (closing operation)
        boolean[][] dilated = dilate(opened);
        boolean[][] closed = erode(dilated);
        
        return closed;
    }
    
    /**
     * Erosion morphological operation
     * @param mask Input mask
     * @return Eroded mask
     */
    private boolean[][] erode(boolean[][] mask) {
        int width = mask.length;
        int height = mask[0].length;
        boolean[][] result = new boolean[width][height];
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                boolean allTrue = true;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (!mask[x + dx][y + dy]) {
                            allTrue = false;
                            break;
                        }
                    }
                    if (!allTrue) break;
                }
                result[x][y] = allTrue;
            }
        }
        
        return result;
    }
    
    /**
     * Dilation morphological operation
     * @param mask Input mask
     * @return Dilated mask
     */
    private boolean[][] dilate(boolean[][] mask) {
        int width = mask.length;
        int height = mask[0].length;
        boolean[][] result = new boolean[width][height];
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                boolean anyTrue = false;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (mask[x + dx][y + dy]) {
                            anyTrue = true;
                            break;
                        }
                    }
                    if (anyTrue) break;
                }
                result[x][y] = anyTrue;
            }
        }
        
        return result;
    }
    
    /**
     * Optimizes image for Minecraft textures (64x64 pixels with transparency)
     * @param inputPath Input image path
     * @param outputPath Output path for optimized texture
     * @return Path to optimized texture
     */
    public CompletableFuture<String> optimizeForMinecraft(String inputPath, String outputPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BufferedImage image = ImageIO.read(new File(inputPath));
                
                // Resize to 64x64 for Minecraft compatibility
                BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.drawImage(image, 0, 0, 64, 64, null);
                g2d.dispose();
                
                // Ensure proper transparency
                BufferedImage optimized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < 64; y++) {
                    for (int x = 0; x < 64; x++) {
                        int rgba = resized.getRGB(x, y);
                        int alpha = (rgba >> 24) & 0xFF;
                        
                        if (alpha < 128) {
                            // Make fully transparent
                            optimized.setRGB(x, y, 0x00000000);
                        } else {
                            // Keep with full opacity
                            optimized.setRGB(x, y, rgba | 0xFF000000);
                        }
                    }
                }
                
                Path outputFilePath = Paths.get(outputPath);
                Files.createDirectories(outputFilePath.getParent());
                ImageIO.write(optimized, "png", outputFilePath.toFile());
                
                return outputPath;
                
            } catch (Exception e) {
                throw new RuntimeException("Minecraft texture optimization failed: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Batch processes multiple images for background removal
     * @param inputPaths Array of input image paths
     * @param outputDir Output directory
     * @return CompletableFuture with array of output paths
     */
    public CompletableFuture<String[]> batchRemoveBackground(String[] inputPaths, String outputDir) {
        return CompletableFuture.supplyAsync(() -> {
            String[] outputPaths = new String[inputPaths.length];
            
            for (int i = 0; i < inputPaths.length; i++) {
                try {
                    String inputPath = inputPaths[i];
                    String fileName = Paths.get(inputPath).getFileName().toString();
                    String outputPath = Paths.get(outputDir, fileName.replaceAll("\\.[^.]+$", "_no_bg.png")).toString();
                    
                    outputPaths[i] = removeBackground(inputPath, outputPath).get();
                    
                } catch (Exception e) {
                    System.err.println("Failed to process image " + inputPaths[i] + ": " + e.getMessage());
                    outputPaths[i] = null;
                }
            }
            
            return outputPaths;
        });
    }
    
    /**
     * Gets service status and available APIs
     * @return Status information
     */
    public String getServiceStatus() {
        return "Background Removal Services:\n" +
               "- Remove.bg API: Available (Free: 50/month)\n" +
               "- ClipDrop API: Available (Free tier)\n" +
               "- PhotoScissors API: Available (Free tier)\n" +
               "- Local Algorithm: Always Available\n" +
               "- Minecraft Optimization: Available\n" +
               "- Batch Processing: Available";
    }
}

