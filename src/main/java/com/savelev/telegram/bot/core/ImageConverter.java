package com.savelev.telegram.bot.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Data
@NoArgsConstructor
public class ImageConverter {
    private BufferedImage currentImage;
    private String resizedSize;

    public ImageConverter(File file) throws IOException {
        this.currentImage = ImageIO.read(file);
    }

    public File saveResized(String path) throws IOException {
        BufferedImage image = calculateSize(currentImage);
        resizedSize = String.format("%dx%d", image.getWidth(), image.getHeight());
        File result = new File(path);
        ImageIO.write(image, "webp", result);
        return result;
    }

    public File save(String path) throws IOException {
        BufferedImage image = Scalr.resize(currentImage, Scalr.Mode.FIT_EXACT, 512, 512);
        File result = new File(path);
        ImageIO.write(image, "webp", result);
        return result;
    }

    private BufferedImage calculateSize(BufferedImage bufferedImage) {
        float width = bufferedImage.getWidth();
        float height = bufferedImage.getHeight();
        if (height > width) {
            width = 512 * width / height;
            return Scalr.resize(currentImage, Scalr.Mode.FIT_EXACT, (int) width, 512);
        }
        height = 512 * height / width;
        return Scalr.resize(currentImage, Scalr.Mode.FIT_EXACT, 512, (int) height);
    }


}
