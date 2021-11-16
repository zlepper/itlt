package dk.zlepper.itlt.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dk.zlepper.itlt.client.helpers.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FirstLaunchScreen extends Screen {
    private final Screen previousScreen;
    private final Minecraft mcInstance = Minecraft.getInstance();
    private ScrollableTextPanel scrollableTextPanel;

    public FirstLaunchScreen(final Screen previousScreen, final Component title) {
        super(title);
        this.previousScreen = previousScreen;
    }

    // only necessary for allowing the fade-in TitleScreen effect to still work, if you don't need that then you can
    // remove this method and rely on Screen's onClose() method instead
    @Override
    public void onClose() {
        mcInstance.setScreen(this.previousScreen);
    }

    protected void init() {
        final Button doneButton = new Button(this.width / 2 - 100, this.height - 28, 200, 20, CommonComponents.GUI_DONE, onPress -> this.onClose());

        this.scrollableTextPanel = new ScrollableTextPanel(mcInstance, this.width - 40, this.height - 40 - doneButton.getHeight(), 24, 20);

        // Note: this has the Files.lines() inside the try() part as it is a try-with-resources
        try (final Stream<String> lines = Files.lines(ConfigUtils.configDir.resolve("itlt/welcome.txt"))) {
            this.scrollableTextPanel.setText(lines.collect(Collectors.toList()));
        } catch (final IOException e) {
            this.scrollableTextPanel.setText(List.of("Error: Failed to load welcome.txt, see the console log for details."));
            e.printStackTrace();
        }

        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(this.scrollableTextPanel);
    }

    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderDirtBackground(0);

        drawScaledString(poseStack, this.font, this.title, this.width / 2.0F, 8, 16777215, 1.5F);

        this.scrollableTextPanel.render(poseStack, mouseX, mouseY, partialTicks);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private static void drawScaledString(final PoseStack poseStack, final Font font, final Component string, final float x, final float y, final int color, final float scale) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0F);
        font.drawShadow(poseStack, string, ((x / scale) - font.width(string) / 2.0F), y / scale, color);
        poseStack.popPose();
    }

    public class ScrollableTextPanel extends ScrollPanel {
        private List<FormattedCharSequence> lines = Collections.emptyList();
        public int padding = 6;

        ScrollableTextPanel(final Minecraft mcInstance, final int width, final int height, final int top, final int left) {
            super(mcInstance, width, height, top, left);
        }

        public void setText(final List<String> lines) {
            this.lines = wordWrapAndFormat(lines);
        }

        @Override
        protected int getContentHeight() {
            return lines.size() * font.lineHeight;
        }

        @Override
        protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tesselator, int mouseX, int mouseY) {
            for (final FormattedCharSequence line : lines) {
                if (line != null) {
                    RenderSystem.enableBlend();
                    FirstLaunchScreen.this.font.drawShadow(poseStack, line, left + padding, relativeY, 0xFFFFFF);
                    RenderSystem.disableBlend();
                }
                relativeY += font.lineHeight;
            }
        }

        // todo: check if this is the right priority and change it if necessary so narration works
        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(final NarrationElementOutput narrationElementOutput) {}

        public List<FormattedCharSequence> wordWrapAndFormat(final List<String> lines) {
            final List<FormattedCharSequence> resized = new ArrayList<>(lines.size());
            for (String line : lines) {
                if (line == null) {
                    resized.add(null);
                    continue;
                }

                // allow blank lines to be rendered
                if (line.length() == 0) line += " ";

                // apply formatting codes where appropriate
                line = line.replaceAll("(?i)&([a-f]|[0-9]|l|m|n|o|r)", "\u00a7$1");
                line = line.replace("\\\u00a7", "&"); // allow formatting escaping with backslash (e.g. "\&a")

                // this makes links clickable, underlined and blue
                final var lineWithFormattedLinks = ForgeHooks.newChatWithLinks(line, false);

                int maxTextLength = this.width - padding * 2;
                if (maxTextLength >= 0)
                    resized.addAll(Language.getInstance().getVisualOrder(font.getSplitter().splitLines(lineWithFormattedLinks, maxTextLength, Style.EMPTY)));
            }

            // add a single line at the end of the panel for aesthetical and functional reasons (hard to click links on last line)
            resized.add(new TextComponent(" ").getVisualOrderText());

            return resized;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            final Style component = findTextLine((int) mouseX, (int) mouseY);
            if (component != null) {
                FirstLaunchScreen.this.handleComponentClicked(component);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Nullable
        private Style findTextLine(final int mouseX, final int mouseY) {
            // make sure the mouse is within the Screen's boundaries
            if (!isMouseOver(mouseX, mouseY))
                return null;

            // calculate the offset based on where the user has scrolled,
            // the position of the ScrollableTextPanel and its border width
            final double offset = (mouseY - top) + border + scrollDistance + 1;
            if (offset <= 0) return null;

            // determine the line based on the offset and the height of each line
            final int lineIdx = (int) (offset / font.lineHeight);
            if (lineIdx >= lines.size() || lineIdx < 1)
                return null;

            final FormattedCharSequence line = lines.get(lineIdx - 1);
            if (line != null)
                return font.getSplitter().componentStyleAtWidth(line, mouseX - left - border);

            return null;
        }
    }
}
