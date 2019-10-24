package net.zarathul.simplefluidtanks.configuration.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CheckboxButtonEx extends AbstractButton
{
	public boolean value;

	private static final ResourceLocation CHECKBOX_TEXTURE = new ResourceLocation("textures/gui/checkbox.png");

	public CheckboxButtonEx(int x, int y, int width, int height, String message, boolean initialValue)
	{
		super(x, y, width, height, message);

		this.value = initialValue;
	}

	@Override
	public void onPress()
	{
		this.value = !this.value;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bindTexture(CHECKBOX_TEXTURE);
		GlStateManager.enableDepthTest();
		FontRenderer fontrenderer = minecraft.fontRenderer;
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		blit(this.x, this.y, 0.0F, this.value ? 20.0F : 0.0F, 20, this.height, 32, 64);
		this.renderBg(minecraft, mouseX, mouseY);
		int i = 14737632;
		this.drawString(fontrenderer, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
	}
}
