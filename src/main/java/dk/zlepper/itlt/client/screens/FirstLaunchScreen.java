package dk.zlepper.itlt.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dk.zlepper.itlt.client.ClientConfig;
import static dk.zlepper.itlt.client.ClientModEvents.itltDir;
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
import org.apache.commons.lang3.tuple.Pair;

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

    @Override
    public void onClose() {
        ClientConfig.enableWelcomeScreen.set(false);
        mcInstance.setScreen(this.previousScreen);
    }

    protected void init() {
        final Button doneButton = new Button(this.width / 2 - 100, this.height - 28, 200, 20, CommonComponents.GUI_DONE, onPress -> this.onClose());

        this.scrollableTextPanel = new ScrollableTextPanel(mcInstance, this.width - 40, this.height - 40 - doneButton.getHeight(), 24, 20);

        // Note: this has the Files.lines() inside the try() part as it is a try-with-resources
        try (final Stream<String> lines = Files.lines(itltDir.toPath().resolve("welcome.txt"))) {
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

        drawCenteredStringWithScale(poseStack, this.font, this.title, this.width / 2.0F, 8, 16777215, 1.5F);

        this.scrollableTextPanel.render(poseStack, mouseX, mouseY, partialTicks);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private static void drawCenteredStringWithScale(final PoseStack poseStack, final Font font, final Component string, final float x, final float y, final int color, final float scale) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0F);
        font.drawShadow(poseStack, string, ((x / scale) - font.width(string) / 2.0F), y / scale, color);
        poseStack.popPose();
    }

    public class ScrollableTextPanel extends ScrollPanel {
        private List<Pair<Boolean, FormattedCharSequence>> lines = Collections.emptyList();
        public int padding = 6;

        ScrollableTextPanel(final Minecraft mcInstance, final int width, final int height, final int top, final int left) {
            super(mcInstance, width, height, top, left);
        }

        public void setText(final List<String> lines) {
            this.lines = wordWrapAndFormat(lines);
        }

        @Override
        protected int getContentHeight() {
            return (lines.size() * font.lineHeight) + font.lineHeight;
        }

        @Override
        protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tesselator, int mouseX, int mouseY) {
            for (final Pair<Boolean, FormattedCharSequence> line : lines) {
                if (line != null) {
                    RenderSystem.enableBlend();
                    if (line.getLeft()) {
                        poseStack.pushPose();
                        poseStack.scale(1.5F, 1.5F, 1.0F);
                        poseStack.translate(0.0F, 1.5F, 0.0F);
                        FirstLaunchScreen.this.font.drawShadow(poseStack, line.getRight(), (left + padding) / 1.5F, relativeY / 1.5F, 0xFFFFFF);
                        poseStack.popPose();
                    } else {
                        FirstLaunchScreen.this.font.drawShadow(poseStack, line.getRight(), left + padding, relativeY, 0xFFFFFF);
                    }
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

        // todo: change line size to 0.5F, have every paragraph (normal text) print itself plus one blank line, change
        // h1 from 2.0F to 1.5F and print itself plus 2 blank lines. This'll fix the inconsistent gap issue of headings
        // compared to paragraphs without making the headings too big
        public List<Pair<Boolean,FormattedCharSequence>> wordWrapAndFormat(final List<String> lines) {
            final List<Pair<Boolean,FormattedCharSequence>> resized = new ArrayList<>(lines.size());
            int lineCounter = 0;
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

                final boolean isHeading;
                if (line.startsWith("&h")) {
                    isHeading = true;
                    line = line.substring(2);
                } else {
                    isHeading = false;
                }

                // this makes links clickable, underlined and blue
                final var lineWithFormattedLinks = ForgeHooks.newChatWithLinks(line, false);

                final int maxTextLength = this.width - padding * 2;
                if (maxTextLength >= 0) {
                    Language.getInstance().getVisualOrder(font.getSplitter().splitLines(lineWithFormattedLinks, maxTextLength, Style.EMPTY)).forEach(formattedCharSequence -> {
                        resized.add(Pair.of(isHeading, formattedCharSequence));
                    });
                }

                lineCounter += resized.size() - lineCounter;

                // add a blank line after headings to avoid overlapping with any text that may be directly below it
                if (isHeading) {
                    //resized.add(lineCounter - 1, Pair.of(false, new TextComponent(" ").getVisualOrderText()));
                    resized.add(Pair.of(false, new TextComponent(" ").getVisualOrderText()));
                }
                lineCounter++;
            }

            // if the last line isn't a heading, add a single line at the end of the panel for
            // aesthetical (looks nicer) and functional reasons (hard to click links on last line otherwise)
            if (!resized.get(resized.size() - 1).getLeft())
                resized.add(Pair.of(false, new TextComponent(" ").getVisualOrderText()));

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

            final FormattedCharSequence line = lines.get(lineIdx - 1).getRight();
            if (line != null)
                return font.getSplitter().componentStyleAtWidth(line, mouseX - left - border);

            return null;
        }
    }
}
