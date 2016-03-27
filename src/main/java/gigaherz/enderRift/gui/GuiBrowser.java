package gigaherz.enderRift.gui;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileBrowser;
import gigaherz.enderRift.misc.SortMode;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiBrowser extends GuiContainer
{
    private static final String textBrowser = "container." + EnderRiftMod.MODID + ".browser";

    protected InventoryPlayer player;
    protected TileBrowser tile;
    protected ResourceLocation backgroundTexture;
    protected ResourceLocation tabsTexture;

    boolean isDragging;
    int scrollY;

    private GuiTextField searchField;

    public GuiBrowser(InventoryPlayer playerInventory, TileBrowser tileEntity)
    {
        super(new ContainerBrowser(tileEntity, playerInventory, true));
        this.player = playerInventory;
        this.tile = tileEntity;
        this.xSize = 194;
        this.ySize = 168;
        backgroundTexture = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/browser.png");
        tabsTexture = new ResourceLocation("minecraft:textures/gui/container/creative_inventory/tabs.png");
    }

    @Override
    public void initGui()
    {
        super.initGui();

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        GuiButton btn = new GuiButton(1, x - 22, y + 12, 20, 20, "");
        buttonList.add(btn);

        Keyboard.enableRepeatEvents(true);
        this.searchField = new GuiTextField(2, this.fontRendererObj, x + 114, y + 6, 71, this.fontRendererObj.FONT_HEIGHT)
        {
            @Override
            public void mouseClicked(int mouseX, int mouseY, int mouseButton)
            {
                if(mouseButton == 1 && getText() != null && getText().length() > 0)
                {
                    setText("");
                    updateSearchFilter();
                }
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        };
        this.searchField.setMaxStringLength(15);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(true);
        this.searchField.setTextColor(16777215);
        this.searchField.setCanLoseFocus(false);
        this.searchField.setFocused(true);
        this.searchField.setText("");

        changeSorting(btn, SortMode.StackSize);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (!this.checkHotbarKeys(keyCode))
        {
            if (this.searchField.textboxKeyTyped(typedChar, keyCode))
            {
                this.updateSearchFilter();
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    private void updateSearchFilter()
    {
        ((ContainerBrowser) inventorySlots).setFilterText(this.searchField.getText());
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        SortMode mode = ((ContainerBrowser) inventorySlots).sortMode;
        switch (mode)
        {
            case Alphabetic:
                mode = SortMode.StackSize;
                break;
            case StackSize:
                mode = SortMode.Alphabetic;
                break;
        }

        changeSorting(guibutton, mode);
    }

    private void changeSorting(GuiButton guibutton, SortMode mode)
    {
        switch (mode)
        {
            case Alphabetic:
                guibutton.displayString = "Az";
                break;
            case StackSize:
                guibutton.displayString = "#";
                break;
        }

        ((ContainerBrowser) inventorySlots).setSortMode(mode);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int xMouse, int yMouse)
    {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        mc.renderEngine.bindTexture(backgroundTexture);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        this.drawTexturedModalRect(x - 27, y + 8, 194, 0, 27, 28);

        // 174, 18, 12, 62
        mc.renderEngine.bindTexture(tabsTexture);

        boolean isEnabled = needsScrollBar();
        if (isEnabled)
            this.drawTexturedModalRect(x + 174, y + 18 + scrollY, 232, 0, 12, 15);
        else
            this.drawTexturedModalRect(x + 174, y + 18, 244, 0, 12, 15);

        this.searchField.drawTextBox();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int xMouse, int yMouse)
    {
        RenderHelper.enableGUIStandardItemLighting();
        drawCustomSlotTexts();
        RenderHelper.disableStandardItemLighting();

        String name = StatCollector.translateToLocal(textBrowser);
        mc.fontRendererObj.drawString(name, 8, 6, 0x404040);
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.player.getName()), 8, ySize - 96 + 2, 0x404040);
    }

    private void drawCustomSlotTexts()
    {
        for (int i = 0; i < ContainerBrowser.FakeSlots; ++i)
        {
            Slot slot = this.inventorySlots.inventorySlots.get(i);
            drawSlotText(slot);
        }
    }

    private void drawSlotText(Slot slotIn)
    {
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;

        int xPosition = slotIn.xDisplayPosition;
        int yPosition = slotIn.yDisplayPosition;
        ItemStack stack = slotIn.getStack();

        if (stack != null)
        {
            int count = ((ContainerBrowser) inventorySlots).fakeInventoryClient.getStackSizeForSlot(slotIn.slotNumber);

            if (count != 1)
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

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fontRendererObj.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fontRendererObj.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    private boolean needsScrollBar()
    {
        int actualSlotCount = ((ContainerBrowser) inventorySlots).actualSlotCount;

        return actualSlotCount > ContainerBrowser.FakeSlots;
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        final ContainerBrowser container = ((ContainerBrowser) inventorySlots);
        final int h = 62;
        final int bitHeight = 15;
        final int actualSlotCount = container.actualSlotCount;
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);

        if (i != 0 && rows > ContainerBrowser.FakeRows)
        {
            int scrollRows = rows - ContainerBrowser.FakeRows;

            int row = container.scroll / 9;

            if (i > 0) row -= 1;
            else if (i < 0) row += 1;

            row = Math.max(0, Math.min(scrollRows, row));

            scrollY = row * (h - bitHeight) / scrollRows;

            container.setScrollPosition(row * 9);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        {
            final int w = 12;
            final int h = 62;
            int mx = mouseX - 174 - x;
            int my = mouseY - 18 - y;
            if (mx >= 0 && mx < w && my >= 0 && my < h)
            {
                updateScrollPos(my);
                isDragging = true;
                return;
            }
        }

        {
            final int w = searchField.width;
            final int h = searchField.height;
            int mx = mouseX - searchField.xPosition;
            int my = mouseY - searchField.yPosition;
            if (mx >= 0 && mx < w && my >= 0 && my < h)
            {
                searchField.mouseClicked(mx, my, mouseButton);
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void updateScrollPos(int my)
    {
        final int h = 62;
        final int bitHeight = 15;
        final int actualSlotCount = ((ContainerBrowser) inventorySlots).actualSlotCount;
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);
        final int scrollRows = rows - ContainerBrowser.FakeRows;

        boolean isEnabled = scrollRows > 0;
        if (isEnabled)
        {
            double offset = (my - bitHeight / 2.0) * scrollRows / (h - bitHeight);
            int row = Math.round(Math.max(0, Math.min(scrollRows, (int) offset)));

            scrollY = row * (h - bitHeight) / scrollRows;

            final ContainerBrowser container = ((ContainerBrowser) inventorySlots);
            container.setScrollPosition(row * 9);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        int y = (height - ySize) / 2;
        int my = mouseY - 18 - y;
        if (isDragging)
        {
            updateScrollPos(my);
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        int y = (height - ySize) / 2;
        int my = mouseY - 18 - y;
        if (isDragging)
        {
            updateScrollPos(my);
            isDragging = false;
            return;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }
}