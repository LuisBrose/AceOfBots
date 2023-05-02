package com.lbrose.mergeImages;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * ImageMerger horizontally merges images into a single image
 */
public class ImageMerger {
    private final File[] images;

    /** Creates a new ImageMerger object
     * @param images The images to merge
     */
    public ImageMerger(File[] images) {
        this.images = images;
    }

    /** Horizontally merges the images into a single image and saves it to the outputPath
     * @param outputPath The path to save the merged image to
     * @param padding The padding between the images
     */
    public void mergeImages(String outputPath, int padding) {
        BufferedImage[] images = new BufferedImage[this.images.length];
        int maxHeight = 0;
        int combinedWidth = 0;

        for (int i = 0; i < this.images.length; i++) {
            try {
                images[i] = ImageIO.read(this.images[i]);
                maxHeight = Math.max(maxHeight, images[i].getHeight());
                combinedWidth += images[i].getWidth() + padding;
            } catch (Exception e) {
                System.out.println("Error reading image: " + this.images[i].getName());
                e.printStackTrace();
            }
        }
        combinedWidth -= padding;

        BufferedImage combinedImage = new BufferedImage(combinedWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = combinedImage.getGraphics();
        for (int i = 0, x = 0; i < images.length; i++) {
            graphics.drawImage(images[i], x, 0, null);
            x += images[i].getWidth() + padding;
        }
        graphics.dispose();

        try {
            ImageIO.write(combinedImage, "png", new File(outputPath));
        } catch (Exception e) {
            System.out.println("Error writing image: " + outputPath);
            e.printStackTrace();
        }
    }
}
