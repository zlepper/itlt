package dk.zlepper.itlt.client.helpers;

import dk.zlepper.itlt.common.ChecksumType;
import io.github.rctcwyvrn.blake3.Blake3;
import io.lktk.NativeBLAKE3;
import io.lktk.NativeBLAKE3Util;
import io.seruco.encoding.base62.Base62;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ChecksumUtils {
    public static Pair<ChecksumType, String> getFileChecksum(final File file, ChecksumType checksumType)
            throws IOException, NativeBLAKE3Util.InvalidNativeOutput {
        // for systems where the NativeBLAKE3 lib has not been compiled for, fallback to the Java implementation
        if (!NativeBLAKE3.isEnabled())
            return getFileChecksumFallback(file, checksumType);

        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224; // default to BLAKE3_224

        // setup the BLAKE3 hasher
        final NativeBLAKE3 hasher = new NativeBLAKE3();
        hasher.initDefault();

        final FileInputStream fis = new FileInputStream(file); // open the file to hash
        byte[] byteArray = new byte[16384]; // create byte array to read data in chunks / as a buffer

        // a 16KB buffer is what the BLAKE3 team seems to recommend as a minimum for best performance
        // please open an issue on the itlt github with an explanation and correction if I've misunderstood this, I'm
        // not sure if internally Java works best with 8KB file reads instead or something...
        // https://github.com/BLAKE3-team/BLAKE3/blob/3a8204f5f38109aae08f4ae58b275663e1cfebab/b3sum/src/main.rs#L256

        // read file data in chunks of 16KB and send it off to the hasher
        while ((fis.read(byteArray)) != -1) hasher.update(byteArray);

        fis.close(); // we're finished reading the file, so close it

        // get the hasher's output, with the number arg being the number of output bytes, prior to hex encoding
        // our default is 28, aka BLAKE3-224. for BLAKE3-256, use 32
        final byte[] bytes;
        switch (checksumType) {
            case BLAKE3_256: {
                bytes = hasher.getOutput(32);
                break;
            }
            case BLAKE3_224:
            default: {
                bytes = hasher.getOutput(28);
                break;
            }
        }

        // the NativeBLAKE3 JNI is a C lib so we need to manually tell it to free up the memory now that
        // we're done with it
        if (hasher.isValid()) hasher.close();

        // convert to hex
        /*final StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }*/

        // convert to Base64
        //final String encodedBase64 = Base64.encodeBase64String(bytes);

        // convert to Base62
        final Base62 base62 = Base62.createInstance();
        final String encodedBase62 = new String(base62.encode(bytes));

        // return the completed hash
        return Pair.of(checksumType, encodedBase62);
    }

    public static Pair<ChecksumType, String> getFileChecksum(final File file)
            throws IOException, NativeBLAKE3Util.InvalidNativeOutput {
        return getFileChecksum(file, ChecksumType.Default);
    }

    public static Pair<ChecksumType, String> getFileChecksumFallback(final File file, ChecksumType checksumType)
            throws IOException {
        if (checksumType == ChecksumType.Default) checksumType = ChecksumType.BLAKE3_224; // default to BLAKE3_224

        final Blake3 hasher = Blake3.newInstance();

        final FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[16384];

        // read file data in chunks of 16KB and send it off to the hasher
        while ((fis.read(byteArray)) != -1) hasher.update(byteArray);

        fis.close();

        final byte[] bytes;
        switch (checksumType) {
            case BLAKE3_256: {
                bytes = hasher.digest(32);
                break;
            }
            case BLAKE3_224:
            default: {
                bytes = hasher.digest(28);
                break;
            }
        }

        // convert to Base62
        final Base62 base62 = Base62.createInstance();
        final String encodedBase62 = new String(base62.encode(bytes));

        // return the completed hash
        return Pair.of(checksumType, encodedBase62);
    }
}
