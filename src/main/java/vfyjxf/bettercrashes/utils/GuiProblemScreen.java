/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiProblemScreen.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;
import vfyjxf.bettercrashes.BetterCrashes;
import vfyjxf.bettercrashes.BetterCrashesConfig;

@SideOnly(Side.CLIENT)
public abstract class GuiProblemScreen extends GuiScreen {

    protected final CrashReport report;
    private String hasteLink = null;
    private String modListString;
    protected static final List<String> UNSUPPORTED_MOD_IDS = Arrays.asList();
    protected List<String> detectedUnsupportedModNames;
    private static final String GTNH_ISSUE_TRACKER =
            "https://github.com/GTNewHorizons/GT-New-Horizons-Modpack/labels/Type%3A%20Crash";

    public GuiProblemScreen(CrashReport report) {
        this.report = report;
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(
                1,
                width / 2 - 50,
                height / 4 + 120 + 12,
                110,
                20,
                I18n.format("bettercrashes.gui.common.openCrashReport")));
        buttonList.add(new GuiButton(
                2,
                width / 2 - 50 + 115,
                height / 4 + 120 + 12,
                110,
                20,
                I18n.format("bettercrashes.gui.common.uploadReportAndCopyLink")));
        if (BetterCrashesConfig.isGTNH) {
            buttonList.add(new GuiButton(
                    3,
                    width / 2 - 50 - 15,
                    height / 4 + 120 + 12 + 25,
                    140,
                    20,
                    I18n.format("bettercrashes.gui.common.gtnhIssueTracker")));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            try {
                CrashUtils.openCrashReport(report);
            } catch (IOException e) {
                button.displayString = I18n.format("bettercrashes.gui.common.failed");
                button.enabled = false;
                e.printStackTrace();
            }
        }
        if (button.id == 2) {
            if (hasteLink == null) {
                try {
                    hasteLink = CrashReportUpload.uploadToUbuntuPastebin(
                            "https://paste.ubuntu.com", report.getCompleteReport());
                } catch (IOException e) {
                    button.displayString = I18n.format("bettercrashes.gui.common.failed");
                    button.enabled = false;
                    e.printStackTrace();
                }
            }
            setClipboardString(hasteLink);
        }
        if (button.id == 3) {
            if (!Desktop.isDesktopSupported()) {
                BetterCrashes.logger.error("Desktop is not supported");
                return;
            }
            try {
                Desktop.getDesktop().browse(new URI(GTNH_ISSUE_TRACKER));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}

    protected String getModListString() {
        if (modListString == null) {
            final Set<ModContainer> suspectedMods = ((IPatchedCrashReport) report).getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = I18n.format("bettercrashes.gui.common.identificationErrored");
            }
            List<String> modNames = new ArrayList<>();
            for (ModContainer mod : suspectedMods) {
                modNames.add(mod.getName());
            }
            if (modNames.isEmpty()) {
                modListString = I18n.format("bettercrashes.gui.common.unknownCause");
            } else {
                modListString = StringUtils.join(modNames, ", ");
            }
        }
        return modListString;
    }

    protected int drawLongString(FontRenderer fontRenderer, String text, int x, int y, int width, int color) {
        int yOffset = 0;
        for (Object line : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, width)) {
            drawString(fontRenderer, (String) line, x, y + yOffset, color);
            yOffset += 9;
        }
        return yOffset;
    }

    protected List<String> getUnsupportedMods() {
        if (!BetterCrashesConfig.isGTNH) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (ModContainer mod : Loader.instance().getModList()) {
            if (UNSUPPORTED_MOD_IDS.contains(mod.getModId())) {
                list.add(mod.getName());
            }
        }
        if (FMLClientHandler.instance().hasOptifine()) {
            list.add("Optifine");
        }
        return list;
    }
}
