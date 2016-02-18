package th.co.krungthaiaxa.elife.api.utils;

import th.co.krungthaiaxa.elife.api.exception.ImageTooSmallException;
import th.co.krungthaiaxa.elife.api.exception.InputImageException;
import th.co.krungthaiaxa.elife.api.exception.OutputImageException;
import th.co.krungthaiaxa.elife.api.exception.UnsupportedImageException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class WatermarkUtil {
    public static byte[] addTextWatermark(InputStream watermarkImageFile, String type, InputStream sourceImageFile)
            throws InputImageException, OutputImageException, ImageTooSmallException, UnsupportedImageException {
        BufferedImage sourceImage;
        try {
            sourceImage = ImageIO.read(sourceImageFile);
        } catch (IOException e) {
            throw new InputImageException(e);
        }

        BufferedImage watermarkImage;
        try {
            watermarkImage = ImageIO.read(watermarkImageFile);
        } catch (IOException e) {
            throw new InputImageException(e);
        }

        if (sourceImage == null) {
            throw new UnsupportedImageException();
        }
        Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();

        // calculates the coordinate where the String is painted
        int topLeftX = (sourceImage.getWidth() - watermarkImage.getWidth()) / 2;
        int topLeftY = (sourceImage.getHeight() - watermarkImage.getHeight()) / 2;
        if (topLeftX < 0) {
            throw new ImageTooSmallException();
        }

        // paints the textual watermark
        g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);

        byte[] result;
        try (ByteArrayOutputStream destImageFile = new ByteArrayOutputStream()) {
            ImageIO.write(sourceImage, type.substring(type.indexOf("/") + 1).toLowerCase(), destImageFile);
            result = destImageFile.toByteArray();
        } catch (IOException e) {
            throw new OutputImageException(e);
        }
        g2d.dispose();

        return result;
    }
}
