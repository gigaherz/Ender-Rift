package dev.gigaherz.enderrift.automation.browser;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.common.slots.SlotFake;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public abstract class AbstractBrowserScreen<T extends AbstractBrowserMenu> extends AbstractContainerScreen<T>
{
    private static final ResourceLocation BACKGROUND_TEXTURE = EnderRiftMod.location("textures/gui/browser.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    private boolean isDragging;
    private int scrollY;
    private float scrollAcc = 0;

    private EditBox searchField;
    private Button sortModeButton;

    protected AbstractBrowserScreen(T container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageWidth = 194;
        imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private static final NumberFormat longFormat = NumberFormat.getInstance(Locale.ROOT);

    static
    {
        longFormat.setGroupingUsed(true);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack pStack)
    {
        var list = super.getTooltipFromContainerItem(pStack);
        long count = pStack.getCount();
        if (this.hoveredSlot instanceof SlotFake sf)
            count = this.menu.getClient().getStackSizeForSlot(sf.getSlotIndex());
        list.add(Math.min(1, list.size()), Component.translatable("text.enderrift.browser.itemcount", longFormat.format(count)));
        return list;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);

        //Lighting.turnBackOn();
        drawCustomSlotTexts(graphics);
        //Lighting.turnOff();

        this.renderLowPowerOverlay(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderLowPowerOverlay(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (getMenu().isLowOnPower())
        {
            int l = leftPos + 7;
            int t = topPos + 17;
            int w = 162;
            int h = 54;
            graphics.fill(l, t, l + w, t + h, 0x7f000000);
            long tm = Minecraft.getInstance().level.getGameTime() % 30;
            if (tm < 15)
            {
                graphics.drawCenteredString(font, "NO POWER", l + w / 2, t + h / 2 - font.lineHeight / 2, 0xFFFFFF);
            }
        }
    }

    protected ResourceLocation getBackgroundTexture()
    {
        return BACKGROUND_TEXTURE;
    }

    @Override
    public void init()
    {
        super.init();

        addRenderableWidget(this.sortModeButton = Button.builder(Component.literal(""), (btn) -> {
            SortMode mode = getMenu().sortMode;
            switch (mode)
            {
                case ALPHABETIC:
                    mode = SortMode.STACK_SIZE;
                    break;
                case STACK_SIZE:
                    mode = SortMode.ALPHABETIC;
                    break;
            }

            changeSorting(mode);
        }).pos(leftPos - 22, topPos + 12).size(20, 20).build());

        //Keyboard.enableRepeatEvents(true);
        addRenderableWidget(this.searchField = new EditBox(this.font, leftPos + 114, topPos + 6, 71, this.font.lineHeight, Component.literal(""))
        {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
            {
                if (mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width)
                        && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height))
                {
                    if (mouseButton == 1 && !Strings.isNullOrEmpty(getValue()) && getValue().length() > 0)
                    {
                        setValue("");
                        return true;
                    }
                }

                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        });

        searchField.setMaxLength(15);
        searchField.setBordered(false);
        searchField.setVisible(true);
        searchField.setTextColor(16777215);
        searchField.setCanLoseFocus(false);
        searchField.setFocused(true);
        searchField.setValue(getMenu().filterText);
        searchField.setResponder(this::updateSearchFilter);

        changeSorting(SortMode.STACK_SIZE);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        if (searchField.isFocused() && searchField.charTyped(p_charTyped_1_, p_charTyped_2_))
            return true;

        return this.getFocused() != null && this.getFocused().charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers)
    {
        if (searchField.isFocused())
        {
            if (searchField.keyPressed(key, scanCode, modifiers))
                return true;
            if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_TAB && key != GLFW.GLFW_KEY_ENTER)
                return false;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight)
    {
        String s = searchField.getValue();
        super.resize(minecraft, scaledWidth, scaledHeight);
        searchField.setValue(s);
    }

    private void updateSearchFilter(String text)
    {
        getMenu().setFilterText(text);
    }

    private void changeSorting(SortMode mode)
    {
        switch (mode)
        {
            case ALPHABETIC:
                sortModeButton.setMessage(Component.literal("Az"));
                break;
            case STACK_SIZE:
                sortModeButton.setMessage(Component.literal("#"));
                break;
        }

        getMenu().setSortMode(mode);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int xMouse, int yMouse)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), leftPos - 27, topPos + 8, 194, 0, 27, 28, 256, 256);

        boolean isEnabled = needsScrollBar();
        if (isEnabled)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, leftPos + 174, topPos + 18 + scrollY, 12, 15);
        else
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_DISABLED_SPRITE, leftPos + 174, topPos + 18, 12, 15);

        searchField.render(graphics, xMouse, yMouse, partialTicks);
    }

    private void drawCustomSlotTexts(GuiGraphics graphics)
    {
        var poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(this.leftPos, this.topPos);
        for (int i = 0; i < AbstractBrowserMenu.SCROLL_SLOTS; ++i)
        {
            Slot slot = getMenu().slots.get(i);
            drawSlotText(graphics, slot);
        }
        poseStack.popMatrix();
    }

    private void drawSlotText(GuiGraphics graphics, Slot slotIn)
    {
        int xPosition = slotIn.x;
        int yPosition = slotIn.y;
        ItemStack stack = slotIn.getItem();

        if (stack.getCount() > 0)
        {
            long count = getMenu().getClient().getStackSizeForSlot(slotIn.index);

            if (count != 1)
            {
                String s = getSizeString(count);

                var xp = (float) (xPosition + 19 - 2 - font.width(s));
                var yp = (float) (yPosition + 6 + 3);
                var xpi = Mth.floor(xp);
                var ypi = Mth.floor(yp);
                var xpf = xp-xpi;
                var ypf = yp-ypi;

                var pose = graphics.pose();
                pose.pushMatrix();
                pose.translate(xpf,ypf);

                graphics.drawString(font, s, xpi, ypi, 0xffffffff, true);

                pose.popMatrix();
            }
        }
    }

    private static final String[] suffixes = {
            "", "k", "M", "B", "T", "Qd.", "Qt.", "S" /* A long can't reach this but just in case whatever */
    };

    private String getSizeString(long count)
    {
        String s;

        int suffix = 0;
        while (count > 1000)
        {
            count /= 1000;
            suffix++;
        }
        if (count >= 900)
        {
            suffix++;
            s = ".9" + suffixes[suffix];
        }
        else
        {
            s = count + suffixes[suffix];
        }
        return s;
    }

    private boolean needsScrollBar()
    {
        int actualSlotCount = getMenu().getActualSlotCount();

        return actualSlotCount > AbstractBrowserMenu.SCROLL_SLOTS;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double something, double wheelDelta)
    {
        if (super.mouseScrolled(mouseX, mouseY, something, wheelDelta))
            return true;

        scrollAcc += wheelDelta * 120;

        final int h = 62;
        final int bitHeight = 15;
        final int actualSlotCount = getMenu().getActualSlotCount();
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);

        if (rows > AbstractBrowserMenu.SCROLL_ROWS)
        {
            int scrollRows = rows - AbstractBrowserMenu.SCROLL_ROWS;

            int row = getMenu().scroll / 9;

            while (scrollAcc >= 120)
            {
                row -= 1;
                scrollAcc -= 120;
            }
            while (scrollAcc <= -120)
            {
                row += 1;
                scrollAcc += 120;
            }

            row = Math.max(0, Math.min(scrollRows, row));

            scrollY = row * (h - bitHeight) / scrollRows;

            getMenu().setScrollPos(row * 9);
        }
        else
        {
            scrollAcc = 0;
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        // scroll
        {
            final int w = 12;
            final int h = 62;
            double mx = mouseX - 174 - leftPos;
            double my = mouseY - 18 - topPos;
            if (mx >= 0 && mx < w && my >= 0 && my < h)
            {
                updateScrollPos((int) my);
                isDragging = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void updateScrollPos(int my)
    {
        final int h = 62;
        final int bitHeight = 15;
        final int actualSlotCount = getMenu().getActualSlotCount();
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);
        final int scrollRows = rows - AbstractBrowserMenu.SCROLL_ROWS;

        boolean isEnabled = scrollRows > 0;
        if (isEnabled)
        {
            double offset = (my - bitHeight / 2.0) * scrollRows / (h - bitHeight);
            int row = Math.round(Math.max(0, Math.min(scrollRows, (int) offset)));

            scrollY = row * (h - bitHeight) / scrollRows;

            final AbstractBrowserMenu container = getMenu();
            getMenu().setScrollPos(row * 9);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double p_mouseDragged_6_, double p_mouseDragged_8_)
    {
        boolean ret = false;

        double my = mouseY - 18 - topPos;
        if (isDragging)
        {
            updateScrollPos((int) my);
            ret = true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, p_mouseDragged_6_, p_mouseDragged_8_) || ret;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton)
    {
        boolean ret = false;

        double my = mouseY - 18 - topPos;
        if (isDragging)
        {
            updateScrollPos((int) my);
            isDragging = false;
            ret = true;
        }

        return super.mouseReleased(mouseX, mouseY, mouseButton) || ret;
    }
}