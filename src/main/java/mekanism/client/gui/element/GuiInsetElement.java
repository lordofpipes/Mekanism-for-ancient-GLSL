package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiInsetElement<DATA_SOURCE> extends GuiSideHolder {

    protected final int border;
    protected final int innerWidth;
    protected final int innerHeight;
    protected final DATA_SOURCE dataSource;
    protected final ResourceLocation overlay;

    public GuiInsetElement(ResourceLocation overlay, IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left) {
        super(gui, x, y, height, left, false);
        this.overlay = overlay;
        this.dataSource = dataSource;
        this.innerWidth = innerSize;
        this.innerHeight = innerSize;
        //TODO: decide what to do if this doesn't divide nicely
        this.border = (width - innerWidth) / 2;
        playClickSound = true;
        active = true;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= x + border && xAxis < x + width - border && yAxis >= y + border && yAxis < y + height - border;
    }

    @Override
    protected int getButtonX() {
        return x + border + (left ? 1 : -1);
    }

    @Override
    protected int getButtonY() {
        return y + border;
    }

    @Override
    protected int getButtonWidth() {
        return innerWidth;
    }

    @Override
    protected int getButtonHeight() {
        return innerHeight;
    }

    protected ResourceLocation getOverlay() {
        return overlay;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw the button background
        drawButton(matrix, mouseX, mouseY);
        drawBackgroundOverlay(matrix);
    }

    protected void drawBackgroundOverlay(@NotNull PoseStack matrix) {
        RenderSystem.setShaderTexture(0, getOverlay());
        blit(matrix, getButtonX(), getButtonY(), 0, 0, innerWidth, innerHeight, innerWidth, innerHeight);
    }
}