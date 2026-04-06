package dev.gigaherz.enderrift.automation.browser;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.common.slots.SlotFake;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
    private static final Identifier BACKGROUND_TEXTURE = EnderRiftMod.location("textures/gui/browser.png");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    private boolean isDragging;
    private int scrollY;
    private float scrollAcc = 0;

    private EditBox searchField;
    private Button sortModeButton;

    protected AbstractBrowserScreen(T container, Inventory playerInventory, Component title)
    {
        this(container, playerInventory, title, 194, 168);
    }

    protected AbstractBrowserScreen(T container, Inventory playerInventory, Component title, int width, int height)
    {
        super(container, playerInventory, title, width, height);
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.extractBackground(graphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        //Lighting.turnBackOn();
        drawCustomSlotTexts(graphics);
        //Lighting.turnOff();

        this.extractLowPowerOverlay(graphics, mouseX, mouseY);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    private void extractLowPowerOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
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
                graphics.centeredText(font, "NO POWER", l + w / 2, t + h / 2 - font.lineHeight / 2, 0xFFFFFF);
            }
        }
    }

    protected Identifier getBackgroundTexture()
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
            public boolean mouseClicked(MouseButtonEvent event, boolean p_434606_)
            {
                if (event.x() >= (double) this.getX() && event.x() < (double) (this.getX() + this.width)
                        && event.y() >= (double) this.getY() && event.y() < (double) (this.getY() + this.height))
                {
                    if (event.button() == 1 && !Strings.isNullOrEmpty(getValue()) && getValue().length() > 0)
                    {
                        setValue("");
                        return true;
                    }
                }

                return super.mouseClicked(event, p_434606_);
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
    public boolean charTyped(CharacterEvent event)
    {
        if (searchField.isFocused() && searchField.charTyped(event))
            return true;

        return this.getFocused() != null && this.getFocused().charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (searchField.isFocused())
        {
            if (searchField.keyPressed(event))
                return true;
            var key = event.key();
            if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_TAB && key != GLFW.GLFW_KEY_ENTER)
                return false;
        }

        return super.keyPressed(event);
    }

    @Override
    public void resize(int scaledWidth, int scaledHeight)
    {
        String s = searchField.getValue();
        super.resize(scaledWidth, scaledHeight);
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getBackgroundTexture(), leftPos - 27, topPos + 8, 194, 0, 27, 28, 256, 256);

        boolean isEnabled = needsScrollBar();
        if (isEnabled)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, leftPos + 174, topPos + 18 + scrollY, 12, 15);
        else
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_DISABLED_SPRITE, leftPos + 174, topPos + 18, 12, 15);

        searchField.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private void drawCustomSlotTexts(GuiGraphicsExtractor graphics)
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

    private void drawSlotText(GuiGraphicsExtractor graphics, Slot slotIn)
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

                graphics.text(font, s, xpi, ypi, 0xffffffff, true);

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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        final int w = 12;
        final int h = 62;
        double mx = event.x() - 174 - leftPos;
        double my = event.y() - 18 - topPos;
        if (mx >= 0 && mx < w && my >= 0 && my < h)
        {
            updateScrollPos((int) my);
            isDragging = true;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
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
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy)
    {
        boolean ret = false;

        double my = event.y() - 18 - topPos;
        if (isDragging)
        {
            updateScrollPos((int) my);
            ret = true;
        }
        return super.mouseDragged(event, dx, dy) || ret;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event)
    {
        boolean ret = false;

        double my = event.y() - 18 - topPos;
        if (isDragging)
        {
            updateScrollPos((int) my);
            isDragging = false;
            ret = true;
        }

        return super.mouseReleased(event) || ret;
    }
}