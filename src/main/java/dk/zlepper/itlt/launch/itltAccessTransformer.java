package dk.zlepper.itlt.launch;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class itltAccessTransformer extends AccessTransformer {
    public itltAccessTransformer() throws IOException {
        super("itlt_at.cfg");
    }
}
