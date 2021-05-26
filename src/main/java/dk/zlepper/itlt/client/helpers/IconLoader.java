package dk.zlepper.itlt.client.helpers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/*****************************************************************************
 * A convenience class for loading icons from images.
 *
 * Icons loaded from this class are formatted to fit within the required
 * dimension (16x16, 32x32, or 128x128). If the source image is larger than the
 * target dimension, it is shrunk down to the minimum size that will fit. If it
 * is smaller, then it is only scaled up if the new scale can be a per-pixel
 * linear scale (i.e., x2, x3, x4, etc). In both cases, the image's width/height
 * ratio is kept the same as the source image.
 *
 * @author Chris Molini
 * @author Paint_Ninja
 *****************************************************************************/
public class IconLoader {

    /*************************************************************************
     * Loads an icon in ByteBuffer form.
     *
     * @param filepath
     *            The location of the Image to use as an icon.
     *
     * @return An array of ByteBuffers containing the pixel data for the icon in
     *         varying sizes.
     *************************************************************************/
    public static ByteBuffer[] load(final String filepath) {
        return load(new File(filepath));
    }

    /*************************************************************************
     * Loads an icon in ByteBuffer form.
     *
     * @param file
     *            A File pointing to the image.
     *
     * @return An array of ByteBuffers containing the pixel data for the icon in
     *         various sizes (as recommended by the OS).
     *************************************************************************/
    public static ByteBuffer[] load(final File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final ByteBuffer[] buffers;
        if (Platform.isWindows()) {
            buffers = new ByteBuffer[2];
            buffers[0] = loadInstance(image, 16);
            buffers[1] = loadInstance(image, 32);
        } else if (Platform.isMac()) {
            buffers = new ByteBuffer[1];
            buffers[0] = loadInstance(image, 128);
        } else {
            buffers = new ByteBuffer[1];
            buffers[0] = loadInstance(image, 32);
        }
        return buffers;
    }

    /*************************************************************************
     * Copies the supplied image into a square icon at the indicated size.
     *
     * @param image
     *            The image to place onto the icon.
     * @param dimension
     *            The desired size of the icon.
     *
     * @return A ByteBuffer of pixel data at the indicated size.
     *************************************************************************/
    private static ByteBuffer loadInstance(final BufferedImage image, final int dimension) {
        final BufferedImage scaledIcon = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB_PRE);
        final Graphics2D g = scaledIcon.createGraphics();
        final double ratio = getIconRatio(image, scaledIcon);
        final double width = image.getWidth() * ratio;
        final double height = image.getHeight() * ratio;
        g.drawImage(image, (int) ((scaledIcon.getWidth() - width) / 2),
                (int) ((scaledIcon.getHeight() - height) / 2), (int) (width),
                (int) (height), null);
        g.dispose();

        return convertToByteBuffer(scaledIcon);
    }

    /*************************************************************************
     * Gets the width/height ratio of the icon. This is meant to simplify
     * scaling the icon to a new dimension.
     *
     * @param src
     *            The base image that will be placed onto the icon.
     * @param icon
     *            The icon that will have the image placed on it.
     *
     * @return The amount to scale the source image to fit it onto the icon
     *         appropriately.
     *************************************************************************/
    private static double getIconRatio(final BufferedImage src, final BufferedImage icon) {
        double ratio;
        if (src.getWidth() > icon.getWidth()) ratio = (double) (icon.getWidth()) / src.getWidth();
        else ratio = (int) (icon.getWidth() / src.getWidth());

        if (src.getHeight() > icon.getHeight()) {
            final double r2 = (double) (icon.getHeight()) / src.getHeight();
            if (r2 < ratio) ratio = r2;
        } else {
            final double r2 = (int) (icon.getHeight() / src.getHeight());
            if (r2 < ratio) ratio = r2;
        }
        return ratio;
    }

    /*************************************************************************
     * Converts a BufferedImage into a ByteBuffer of pixel data.
     *
     * @param image
     *            The image to convert.
     *
     * @return A ByteBuffer that contains the pixel data of the supplied image.
     *************************************************************************/
    public static ByteBuffer convertToByteBuffer(final BufferedImage image) {
        final byte[] buffer = new byte[image.getWidth() * image.getHeight() * 4];
        int counter = 0;
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int colorSpace = image.getRGB(j, i);
                buffer[counter    ] = (byte) ((colorSpace << 8) >> 24);
                buffer[counter + 1] = (byte) ((colorSpace << 16) >> 24);
                buffer[counter + 2] = (byte) ((colorSpace << 24) >> 24);
                buffer[counter + 3] = (byte) (colorSpace >> 24);
                counter += 4;
            }
        }
        return ByteBuffer.wrap(buffer);
    }
}