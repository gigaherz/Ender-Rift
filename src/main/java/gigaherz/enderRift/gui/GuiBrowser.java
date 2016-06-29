package gigaherz.enderRift.gui;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.aggregation.TileBrowser;
import gigaherz.enderRift.misc.SortMode;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiBrowser extends GuiContainer
{
    private static final ResourceLocation backgroundTexture = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/browser.png");
    private static final ResourceLocation tabsTexture = new ResourceLocation("minecraft:textures/gui/container/creative_inventory/tabs.png");
    private static final String textBrowser = "container." + EnderRiftMod.MODID + ".browser";

    protected InventoryPlayer player;

    private boolean isDragging;
    private int scrollY;
    private float scrollAcc = 0;

    private GuiTextField searchField;

    protected GuiBrowser(Container container)
    {
        super(container);
    }

    public GuiBrowser(EntityPlayer player, TileBrowser tileEntity)
    {
        super(new ContainerBrowser(tileEntity, player, true));
        this.player = player.inventory;
        xSize = 194;
        ySize = 168;
    }

    protected ResourceLocation getBackgroundTexture()
    {
        return backgroundTexture;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        GuiButton btn = new GuiButton(1, guiLeft - 22, guiTop + 12, 20, 20, "");
        buttonList.add(btn);

        Keyboard.enableRepeatEvents(true);
        this.searchField = new GuiTextField(2, this.fontRendererObj, guiLeft + 114, guiTop + 6, 71, this.fontRendererObj.FONT_HEIGHT)
        {
            @Override
            public void mouseClicked(int mouseX, int mouseY, int mouseButton)
            {
                if (mouseButton == 1 && getText() != null && getText().length() > 0)
                {
                    setText("");
                    updateSearchFilter();
                }
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        };

        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setVisible(true);
        searchField.setTextColor(16777215);
        searchField.setCanLoseFocus(false);
        searchField.setFocused(true);
        searchField.setText("");

        changeSorting(btn, SortMode.StackSize);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (!checkHotbarKeys(keyCode))
        {
            if (searchField.textboxKeyTyped(typedChar, keyCode))
            {
                updateSearchFilter();
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    private void updateSearchFilter()
    {
        ((ContainerBrowser) inventorySlots).setFilterText(searchField.getText());
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 1)
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
        mc.renderEngine.bindTexture(getBackgroundTexture());

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        drawTexturedModalRect(guiLeft - 27, guiTop + 8, 194, 0, 27, 28);

        mc.renderEngine.bindTexture(tabsTexture);

        boolean isEnabled = needsScrollBar();
        if (isEnabled)
            drawTexturedModalRect(guiLeft + 174, guiTop + 18 + scrollY, 232, 0, 12, 15);
        else
            drawTexturedModalRect(guiLeft + 174, guiTop + 18, 244, 0, 12, 15);

        searchField.drawTextBox();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int xMouse, int yMouse)
    {
        RenderHelper.enableGUIStandardItemLighting();
        drawCustomSlotTexts();
        RenderHelper.disableStandardItemLighting();

        String name = I18n.format(textBrowser);
        mc.fontRendererObj.drawString(name, 8, 6, 0x404040);
        mc.fontRendererObj.drawString(I18n.format(player.getName()), 8, ySize - 96 + 2, 0x404040);
    }

    private void drawCustomSlotTexts()
    {
        for (int i = 0; i < ContainerBrowser.FakeSlots; ++i)
        {
            Slot slot = inventorySlots.inventorySlots.get(i);
            drawSlotText(slot);
        }
    }

    private void drawSlotText(Slot slotIn)
    {
        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

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

        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;
    }

    private boolean needsScrollBar()
    {
        int actualSlotCount = ((ContainerBrowser) inventorySlots).getActualSlotCount();

        return actualSlotCount > ContainerBrowser.FakeSlots;
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        scrollAcc += Mouse.getEventDWheel();

        final ContainerBrowser container = ((ContainerBrowser) inventorySlots);
        final int h = 62;
        final int bitHeight = 15;
        final int actualSlotCount = container.getActualSlotCount();
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);

        if (rows > ContainerBrowser.FakeRows)
        {
            int scrollRows = rows - ContainerBrowser.FakeRows;

            int row = container.scroll / 9;

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

            container.setScrollPos(row * 9);
        }
        else
        {
            scrollAcc = 0;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        {
            final int w = 12;
            final int h = 62;
            int mx = mouseX - 174 - guiLeft;
            int my = mouseY - 18 - guiTop;
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
        final int actualSlotCount = ((ContainerBrowser) inventorySlots).getActualSlotCount();
        final int rows = (int) Math.ceil(actualSlotCount / 9.0);
        final int scrollRows = rows - ContainerBrowser.FakeRows;

        boolean isEnabled = scrollRows > 0;
        if (isEnabled)
        {
            double offset = (my - bitHeight / 2.0) * scrollRows / (h - bitHeight);
            int row = Math.round(Math.max(0, Math.min(scrollRows, (int) offset)));

            scrollY = row * (h - bitHeight) / scrollRows;

            final ContainerBrowser container = ((ContainerBrowser) inventorySlots);
            container.setScrollPos(row * 9);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        int my = mouseY - 18 - guiTop;
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
        int my = mouseY - 18 - guiTop;
        if (isDragging)
        {
            updateScrollPos(my);
            isDragging = false;
            return;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }
}