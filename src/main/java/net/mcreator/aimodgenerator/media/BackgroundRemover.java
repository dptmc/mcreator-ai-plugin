package net.mcreator.aimodgenerator.media;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Utility class for removing backgrounds from generated textures
 * Handles automatic background removal and transparency creation
 */
public class BackgroundRemover {
    
    private static final int TRANSPARENCY_THRESHOLD = 10;
    private static final int COLOR_SIMILARITY_THRESHOLD = 30;
    
    /**
     * Removes background from an image and makes it transparent
     * @param inputPath Path to the input image
     * @param outputPath Path to save the processed image
     * @return True if successful, false otherwise
     */
    public static boolean removeBackground(String inputPath, String outputPath) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            BufferedImage processedImage = removeBackgroundFromImage(originalImage);
            
            // Save as PNG to preserve transparency
            return ImageIO.write(processedImage, "PNG", new File(outputPath));
        } catch (IOException e) {
            System.err.println("Failed to process image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Removes background from a BufferedImage
     * @param originalImage The original image
     * @return Image with transparent background
     */
    public static BufferedImage removeBackgroundFromImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Create new image with alpha channel
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Detect background color (usually corners)
        Color backgroundColor = detectBackgroundColor(originalImage);
        
        // Process each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(originalImage.getRGB(x, y));
                
                if (isBackgroundColor(pixelColor, backgroundColor)) {
                    // Make transparent
                    result.setRGB(x, y, 0x00000000);
                } else {
                    // Keep original color
                    result.setRGB(x, y, originalImage.getRGB(x, y));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Removes background using flood fill algorithm
     * @param originalImage The original image
     * @return Image with transparent background
     */
    public static BufferedImage removeBackgroundFloodFill(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Copy original image
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        
        // Get corner colors as potential background colors
        Set<Color> backgroundColors = getCornerColors(originalImage);
        
        // Apply flood fill from corners
        for (Color bgColor : backgroundColors) {
            floodFillTransparent(result, 0, 0, bgColor);
            floodFillTransparent(result, width - 1, 0, bgColor);
            floodFillTransparent(result, 0, height - 1, bgColor);
            floodFillTransparent(result, width - 1, height - 1, bgColor);
        }
        
        return result;
    }
    
    /**
     * Creates a clean Minecraft-style texture with proper transparency
     * @param inputPath Path to the input image
     * @param outputPath Path to save the processed image
     * @param targetSize Target size (16, 32, 64, etc.)
     * @return True if successful
     */
    public static boolean createMinecraftTexture(String inputPath, String outputPath, int targetSize) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            
            // Resize to target size if needed
            BufferedImage resizedImage = resizeImage(originalImage, targetSize, targetSize);
            
            // Remove background
            BufferedImage transparentImage = removeBackgroundFromImage(resizedImage);
            
            // Clean up pixels (remove anti-aliasing artifacts)
            BufferedImage cleanImage = cleanupPixelArt(transparentImage);
            
            // Save as PNG
            return ImageIO.write(cleanImage, "PNG", new File(outputPath));
        } catch (IOException e) {
            System.err.println("Failed to create Minecraft texture: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Detects the background color from corner pixels
     */
    private static Color detectBackgroundColor(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Sample corner pixels
        Color[] corners = {
            new Color(image.getRGB(0, 0)),
            new Color(image.getRGB(width - 1, 0)),
            new Color(image.getRGB(0, height - 1)),
            new Color(image.getRGB(width - 1, height - 1))
        };
        
        // Find most common corner color
        Color mostCommon = corners[0];
        int maxCount = 1;
        
        for (int i = 0; i < corners.length; i++) {
            int count = 1;
            for (int j = i + 1; j < corners.length; j++) {
                if (colorsAreSimilar(corners[i], corners[j])) {
                    count++;
                }
            }
            if (count > maxCount) {
                maxCount = count;
                mostCommon = corners[i];
            }
        }
        
        return mostCommon;
    }
    
    /**
     * Gets unique colors from image corners
     */
    private static Set<Color> getCornerColors(BufferedImage image) {
        Set<Color> colors = new HashSet<>();
        int width = image.getWidth();
        int height = image.getHeight();
        
        colors.add(new Color(image.getRGB(0, 0)));
        colors.add(new Color(image.getRGB(width - 1, 0)));
        colors.add(new Color(image.getRGB(0, height - 1)));
        colors.add(new Color(image.getRGB(width - 1, height - 1)));
        
        return colors;
    }
    
    /**
     * Flood fill algorithm to make similar colors transparent
     */
    private static void floodFillTransparent(BufferedImage image, int startX, int startY, Color targetColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (startX < 0 || startX >= width || startY < 0 || startY >= height) {
            return;
        }
        
        Color startColor = new Color(image.getRGB(startX, startY), true);
        
        if (!colorsAreSimilar(startColor, targetColor) || startColor.getAlpha() == 0) {
            return;
        }
        
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(startX, startY));
        
        while (!stack.isEmpty()) {
            Point point = stack.pop();
            int x = point.x;
            int y = point.y;
            
            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }
            
            Color currentColor = new Color(image.getRGB(x, y), true);
            
            if (!colorsAreSimilar(currentColor, targetColor) || currentColor.getAlpha() == 0) {
                continue;
            }
            
            // Make transparent
            image.setRGB(x, y, 0x00000000);
            
            // Add neighboring pixels
            stack.push(new Point(x + 1, y));
            stack.push(new Point(x - 1, y));
            stack.push(new Point(x, y + 1));
            stack.push(new Point(x, y - 1));
        }
    }
    
    /**
     * Checks if a color is similar to the background color
     */
    private static boolean isBackgroundColor(Color color, Color backgroundColor) {
        return colorsAreSimilar(color, backgroundColor);
    }
    
    /**
     * Checks if two colors are similar within threshold
     */
    private static boolean colorsAreSimilar(Color color1, Color color2) {
        int rDiff = Math.abs(color1.getRed() - color2.getRed());
        int gDiff = Math.abs(color1.getGreen() - color2.getGreen());
        int bDiff = Math.abs(color1.getBlue() - color2.getBlue());
        
        return (rDiff + gDiff + bDiff) <= COLOR_SIMILARITY_THRESHOLD;
    }
    
    /**
     * Resizes an image to specified dimensions
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Use nearest neighbor for pixel art
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * Cleans up pixel art by removing anti-aliasing artifacts
     */
    private static BufferedImage cleanupPixelArt(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage cleaned = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                
                // If pixel is mostly transparent, make it fully transparent
                if (pixelColor.getAlpha() < TRANSPARENCY_THRESHOLD) {
                    cleaned.setRGB(x, y, 0x00000000);
                } else {
                    // Keep original color but ensure full opacity
                    Color solidColor = new Color(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue(), 255);
                    cleaned.setRGB(x, y, solidColor.getRGB());
                }
            }
        }
        
        return cleaned;
    }
    
    /**
     * Removes glow effects from an image
     * @param inputPath Path to the input image
     * @param outputPath Path to save the processed image
     * @return True if successful
     */
    public static boolean removeGlowEffect(String inputPath, String outputPath) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            BufferedImage processedImage = removeGlowFromImage(originalImage);
            
            return ImageIO.write(processedImage, "PNG", new File(outputPath));
        } catch (IOException e) {
            System.err.println("Failed to remove glow effect: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Removes glow effects by detecting and removing soft edges
     */
    private static BufferedImage removeGlowFromImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(originalImage.getRGB(x, y), true);
                
                // If pixel is very bright or has low opacity (glow effect), make it transparent
                if (isGlowPixel(pixelColor)) {
                    result.setRGB(x, y, 0x00000000);
                } else {
                    result.setRGB(x, y, originalImage.getRGB(x, y));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Determines if a pixel is part of a glow effect
     */
    private static boolean isGlowPixel(Color color) {
        // Check for very bright colors or semi-transparent pixels that might be glow
        int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return (brightness > 240 && color.getAlpha() < 255) || 
               (color.getAlpha() < 100 && brightness > 200);
    }
    
    /**
     * Creates a perfect pixel art texture from any input
     * @param inputPath Path to the input image
     * @param outputPath Path to save the processed image
     * @param size Target size (16, 32, 64)
     * @param removeGlow Whether to remove glow effects
     * @return True if successful
     */
    public static boolean createPerfectPixelArt(String inputPath, String outputPath, int size, boolean removeGlow) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            
            // Step 1: Resize to target size
            BufferedImage resized = resizeImage(originalImage, size, size);
            
            // Step 2: Remove glow if requested
            if (removeGlow) {
                resized = removeGlowFromImage(resized);
            }
            
            // Step 3: Remove background
            BufferedImage transparent = removeBackgroundFromImage(resized);
            
            // Step 4: Clean up pixel art
            BufferedImage clean = cleanupPixelArt(transparent);
            
            // Step 5: Enhance pixel art appearance
            BufferedImage enhanced = enhancePixelArt(clean);
            
            return ImageIO.write(enhanced, "PNG", new File(outputPath));
        } catch (IOException e) {
            System.err.println("Failed to create perfect pixel art: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Enhances pixel art by sharpening edges and improving contrast
     */
    private static BufferedImage enhancePixelArt(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage enhanced = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                
                if (pixelColor.getAlpha() == 0) {
                    enhanced.setRGB(x, y, 0x00000000);
                } else {
                    // Enhance contrast slightly
                    int r = Math.min(255, Math.max(0, (int)(pixelColor.getRed() * 1.1)));
                    int g = Math.min(255, Math.max(0, (int)(pixelColor.getGreen() * 1.1)));
                    int b = Math.min(255, Math.max(0, (int)(pixelColor.getBlue() * 1.1)));
                    
                    Color enhancedColor = new Color(r, g, b, 255);
                    enhanced.setRGB(x, y, enhancedColor.getRGB());
                }
            }
        }
        
        return enhanced;
    }
}


    
    /**
     * Removes unwanted glow effects from textures while preserving intentional bright pixels
     * @param inputPath Path to input image
     * @param outputPath Path to save processed image
     * @return true if successful, false otherwise
     */
    public static boolean removeGlowEffects(String inputPath, String outputPath) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            BufferedImage processedImage = removeGlowFromImage(originalImage);
            
            // Save the processed image
            ImageIO.write(processedImage, "PNG", new File(outputPath));
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to remove glow effects: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Removes glow effects from a BufferedImage
     */
    private static BufferedImage removeGlowFromImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);
                Color color = new Color(rgb, true);
                
                // Check if this pixel is part of a glow effect
                if (isGlowPixel(originalImage, x, y, color)) {
                    // Remove glow by making it transparent or reducing intensity
                    if (isEdgeGlow(originalImage, x, y)) {
                        // Edge glow - make transparent
                        processedImage.setRGB(x, y, 0x00000000);
                    } else {
                        // Reduce intensity of glow pixels
                        Color reducedColor = reduceGlowIntensity(color);
                        processedImage.setRGB(x, y, reducedColor.getRGB());
                    }
                } else {
                    // Keep original pixel
                    processedImage.setRGB(x, y, rgb);
                }
            }
        }
        
        return processedImage;
    }
    
    /**
     * Determines if a pixel is part of a glow effect
     */
    private static boolean isGlowPixel(BufferedImage image, int x, int y, Color color) {
        // Check if pixel is very bright and has low saturation (typical of glow effects)
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float brightness = hsb[2];
        float saturation = hsb[1];
        
        // Glow pixels are typically very bright with low saturation
        if (brightness > 0.8f && saturation < 0.3f) {
            return true;
        }
        
        // Check if pixel is part of a gradient (common in glow effects)
        return isPartOfGradient(image, x, y, color);
    }
    
    /**
     * Checks if a pixel is part of a gradient (glow effect characteristic)
     */
    private static boolean isPartOfGradient(BufferedImage image, int x, int y, Color color) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Check surrounding pixels for gradient pattern
        int gradientCount = 0;
        int totalNeighbors = 0;
        
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx;
                int ny = y + dy;
                
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    Color neighborColor = new Color(image.getRGB(nx, ny), true);
                    
                    // Check if neighbor has similar hue but different brightness
                    float[] currentHSB = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                    float[] neighborHSB = Color.RGBtoHSB(neighborColor.getRed(), neighborColor.getGreen(), neighborColor.getBlue(), null);
                    
                    float hueDiff = Math.abs(currentHSB[0] - neighborHSB[0]);
                    float brightnessDiff = Math.abs(currentHSB[2] - neighborHSB[2]);
                    
                    if (hueDiff < 0.1f && brightnessDiff > 0.1f) {
                        gradientCount++;
                    }
                    totalNeighbors++;
                }
            }
        }
        
        // If more than half the neighbors show gradient pattern, this is likely a glow pixel
        return totalNeighbors > 0 && (float) gradientCount / totalNeighbors > 0.5f;
    }
    
    /**
     * Checks if a glow pixel is on the edge (should be removed completely)
     */
    private static boolean isEdgeGlow(BufferedImage image, int x, int y) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Check if pixel is near the edge of a solid object
        boolean hasTransparentNeighbor = false;
        boolean hasSolidNeighbor = false;
        
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx;
                int ny = y + dy;
                
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    Color neighborColor = new Color(image.getRGB(nx, ny), true);
                    
                    if (neighborColor.getAlpha() < 128) {
                        hasTransparentNeighbor = true;
                    } else {
                        hasSolidNeighbor = true;
                    }
                }
            }
        }
        
        // Edge glow pixels have both transparent and solid neighbors
        return hasTransparentNeighbor && hasSolidNeighbor;
    }
    
    /**
     * Reduces the intensity of glow pixels while preserving the base color
     */
    private static Color reduceGlowIntensity(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        
        // Reduce brightness while maintaining hue and saturation
        float newBrightness = Math.max(0.3f, hsb[2] * 0.7f);
        
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], newBrightness);
        return new Color(rgb);
    }

