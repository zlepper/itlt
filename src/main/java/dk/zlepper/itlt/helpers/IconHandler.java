package dk.zlepper.itlt.helpers;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;

import dk.zlepper.itlt.Itlt;
import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.icns.IcnsImageParser;
import org.lwjgl.opengl.Display;

public class IconHandler
{
    //Backport some of 2.X ico and icns support
    public static void setWindowIcon(final File inputIconFile) throws IOException {
        final ByteBuffer[] buffer = new ByteBuffer[1];
        List<BufferedImage> bufferedImageList;

        final String inputIconFilenameAndExt = inputIconFile.getName().toLowerCase();
        if (inputIconFilenameAndExt.endsWith(".ico")) {
            // load all of the images inside the `.ico` file as a list of `BufferedImage`s
            bufferedImageList = ICODecoder.read(inputIconFile);
            buffer[0] = IconLoader.convertToByteBuffer(bufferedImageList.get(0));
            Display.setIcon(buffer);
            Itlt.logger.info("ICO file found, using that");
            return;
        } else if (inputIconFilenameAndExt.endsWith(".icns")) {
            // try to load all of the images inside the `.icns` file as a list of `BufferedImage`s
            try {
                bufferedImageList = new IcnsImageParser().getAllBufferedImages(inputIconFile);
                buffer[0] = IconLoader.convertToByteBuffer(bufferedImageList.get(0));
                Display.setIcon(buffer);
                Itlt.logger.info("ICNS file found, using that");
                return;
            } catch (final ImageReadException e) {
                e.printStackTrace();
                return;
            }
        } else if (inputIconFilenameAndExt.endsWith(".png")) {
            // load the `.png` file directly as an `InputStream`
            Display.setIcon(IconLoader.load(inputIconFile));
            Itlt.logger.info("PNG file found, using that");
            return;
        }
    }
}
