package vfyjxf.bettercrashes;

import cpw.mods.fml.common.Loader;
import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class BetterCrashesConfig {

    public static Configuration config;

    public static final String GENERAL = "General";

    public static boolean isGTNH = Loader.isModLoaded("dreamcraft");
    public static int crashLogLimitClient = 30;
    public static int crashLogLimitServer = 30;

    public static void init(File file) {
        config = new Configuration(file);
        syncConfig();
    }

    public static void syncConfig() {
        config.setCategoryComment(GENERAL, "General config");

        crashLogLimitClient = config.getInt(
                "crashLogLimitClient",
                GENERAL,
                30,
                0,
                Integer.MAX_VALUE,
                "Maximum number of crash logs generated per restart for client. Suppresses too many logs generated by continuous crashes.");
        crashLogLimitServer = config.getInt(
                "crashLogLimitServer",
                GENERAL,
                30,
                0,
                Integer.MAX_VALUE,
                "Maximum number of crash logs generated per restart for server. Suppresses too many logs generated by continuous crashes.");

        if (config.hasChanged()) {
            config.save();
        }
    }
}
