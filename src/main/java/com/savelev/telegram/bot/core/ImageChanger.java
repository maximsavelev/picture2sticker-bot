package com.savelev.telegram.bot.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

@Data
@NoArgsConstructor
public class ImageChanger {
    private BufferedImage currentImage;

    public ImageChanger(File file) throws IOException {
        this.currentImage =   ImageIO.read(file);
    }


    public static BufferedImage convert8(BufferedImage src) {
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_USHORT_555_RGB);
        ColorConvertOp cco = new ColorConvertOp(src.getColorModel()
                .getColorSpace(), dest.getColorModel().getColorSpace(), null);
        cco.filter(src, dest);
        return dest;
    }


    public File saveImage(Scalr.Mode mode) throws IOException {
        currentImage = Scalr.resize(currentImage, Scalr.Mode.FIT_EXACT, 512, 512);
        File result = new File( "result_" +mode+".png");
        ImageIO.write(currentImage, "png", result);
        return result;
    }


}
