package gigaherz.enderRift.automation.browser;

import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderRift.EnderRiftMod;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractBrowserScreen<T extends AbstractBrowserContainer> extends ContainerScreen<T>
{
    private static final ResourceLocation backgroundTexture = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/browser.png");
    private static final ResourceLocation tabsTexture = new ResourceLocation("minecraft:textures/gui/container/creative_inventory/tabs.png");

    private boolean isDragging;
    private int scrollY;
    private float scrollAcc = 0;

    private TextFieldWidget searchField;
    private Button sortModeButton;

    protected AbstractBrowserScreen(T container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        xSize = 194;
        ySize = 168;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderLowPowerOverlay(mouseX, mouseY);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    private void renderLowPowerOverlay(int mouseX, int mouseY)
    {
        if (getContainer().isLowOnPower())
        {
            int l = guiLeft + 7;
            int t = guiTop + 17;
            int w = 162;
            int h = 54;
            GlStateManager.disableDepthTest();
            GlStateManager.color4f(1, 1, 1, 1);
            fill(l, t, l + w, t + h, 0x7f000000);
            long tm = Minecraft.getInstance().world.getGameTime() % 30;
            if (tm < 15)
            {
                drawCenteredString(font, "NO POWER", l + w / 2, t + h / 2 - font.FONT_HEIGHT / 2, 0xFFFFFF);
            }
            GlStateManager.enableDepthTest();
        }
    }

    protected ResourceLocation getBackgroundTexture()
    {
        return backgroundTexture;
    }

    @Override
    public void init()
    {
        super.init();

        addButton(this.sortModeButton = new Button(guiLeft - 22, guiTop + 12, 20, 20, "", (btn) -> {
            SortMode mode = getContainer().sortMode;
            switch (mode)
            {
                case Alphabetic:
                    mode = SortMode.StackSize;
                    break;
                case StackSize:
                    mode = SortMode.Alphabetic;
                    break;
            }

            changeSorting(mode);
        }));

        //Keyboard.enableRepeatEvents(true);
        addButton(this.searchField = new TextFieldWidget(this.font, guiLeft + 114, guiTop + 6, 71, this.font.FONT_HEIGHT, "")
        {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
            {
                if (mouseX >= (double) this.x && mouseX < (double) (this.x + this.width)
                        && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height))
                {
                    if (mouseButton == 1 && !Strings.isNullOrEmpty(getText()) && getText().length() > 0)
                    {
                        setText("");
                        return true;
                    }
                }

                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        });

        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setVisible(true);
        searchField.setTextColor(16777215);
        searchField.setCanLoseFocus(false);
        searchField.setFocused2(true);
        searchField.setText(getContainer().filterText);
        searchField.setResponder(this::updateSearchFilter);

        changeSorting(SortMode.StackSize);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers)
    {
        if (super.keyPressed(key, scanCode, modifiers))
            return true;

        return searchField.keyPressed(key, scanCode, modifiers);
    }

    private void updateSearchFilter(String text)
    {
        getContainer().setFilterText(text);
    }

    private void changeSorting(SortMode mode)
    {
        switch (mode)
        {
            case Alphabetic:
                sortModeButton.setMessage("Az");
                break;
            case StackSize:
                sortModeButton.setMessage("#");
                break;
        }

        getContainer().setSortMode(mode);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int xMouse, int yMouse)
    {
        minecraft.getTextureManager().bindTexture(getBackgroundTexture());

        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
        blit(guiLeft - 27, guiTop + 8, 194, 0, 27, 28);

        minecraft.getTextureManager().bindTexture(tabsTexture);

        boolean isEnabled = needsScrollBar();
        if (isEnabled)
            blit(guiLeft + 174, guiTop + 18 + scrollY, 232, 0, 12, 15);
        else
            blit(guiLeft + 174, guiTop + 18, 244, 0, 12, 15);

        searchField.render(xMouse, yMouse, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int xMouse, int yMouse)
    {
        RenderHelper.enableGUIStandardItemLighting();
        drawCustomSlotTexts();
        RenderHelper.disableStandardItemLighting();

        font.drawString(title.getFormattedText(), 8, 6, 0x404040);
        font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8, ySize - 96 + 2, 0x404040);
    }

    private void drawCustomSlotTexts()
    {
        for (int i = 0; i < AbstractBrowserContainer.ScrollSlots; ++i)
        {
            Slot slot = getContainer().inventorySlots.get(i);
            drawSlotText(slot);
        }
    }

    private void drawSlotText(Slot slotIn)
    {
        blitOffset = 100;

        int xPosition = slotIn.xPos;
        int yPosition = slotIn.yPos;
        ItemStack stack = slotIn.getStack();

        if (stack.getCount() > 0)
        {
            int count = getContainer().getClient().getStackSizeForSlot(slotIn.slotNumber);

            if (count != 1)
            {
                String s = getSizeString(count);

                GlStateManager.disableLighting();
                GlStateManager.disableDepthTest();
                GlStateManager.disableBlend();
                font.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - font.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepthTest();
            }
        }

        blitOffset = 0;
    }

    private String getSizeString(int count)
    {
        String s;
        if (count >= 1000000000)
            s = (count / 1000000000) + "B";
        else if (count >= 900000000)
            s = ".9B";
        else if (count >= 1000000)
            s = (count / 1000000) + "M";
        else if (count >= 900000)
            s = ".9M";
        else if (count >= 1000)
            s = (count / 1000) + "k";
        else if (count >= 900)
            s = ".9k";
        else
            s = String.valueOf(count);
        return s;
    }

    private boolean needsScrollBar()
    {
        int actualSlotCount = getContainer().getActualSlotCount();

        return actualSlotCount > AbstractBrowserContainer.ScrollSlots;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double wheelDelta)
    {
        if (super.mouseScrolled(mouseX, mouseY, wheelDelta))
            return true;

        scrollAcc += wheelDelta * 120;

        final int h = 62;
        final int bitHeight = 15;
        final int actualSlotCount = getContainer().getActualSlotCount();
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);

        if (rows > AbstractBrowserContainer.ScrollRows)
        {
            int scrollRows = rows - AbstractBrowserContainer.ScrollRows;

            int row = getContainer().scroll / 9;

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

            getContainer().setScrollPos(row * 9);
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
            double mx = mouseX - 174 - guiLeft;
            double my = mouseY - 18 - guiTop;
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
        final int actualSlotCount = getContainer().getActualSlotCount();
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);
        final int scrollRows = rows - AbstractBrowserContainer.ScrollRows;

        boolean isEnabled = scrollRows > 0;
        if (isEnabled)
        {
            double offset = (my - bitHeight / 2.0) * scrollRows / (h - bitHeight);
            int row = Math.round(Math.max(0, Math.min(scrollRows, (int) offset)));

            scrollY = row * (h - bitHeight) / scrollRows;

            final AbstractBrowserContainer container = getContainer();
            getContainer().setScrollPos(row * 9);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double p_mouseDragged_6_, double p_mouseDragged_8_)
    {
        boolean ret = false;

        double my = mouseY - 18 - guiTop;
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

        double my = mouseY - 18 - guiTop;
        if (isDragging)
        {
            updateScrollPos((int) my);
            isDragging = false;
            ret = true;
        }

        return super.mouseReleased(mouseX, mouseY, mouseButton) || ret;
    }
}