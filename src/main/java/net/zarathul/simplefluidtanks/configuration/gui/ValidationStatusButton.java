package net.zarathul.simplefluidtanks.configuration.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
class ValidationStatusButton extends Button
{
	private boolean valid;

	public ValidationStatusButton(int x, int y, IPressable clickHandler)
	{
		super(x, y, 15, 15, "", clickHandler);

		this.valid = true;
	}

	public void setValid(boolean isValid)
	{
		this.valid = isValid;
	}

	public void setValid()
	{
		this.valid = true;
	}

	public void setInvalid()
	{
		this.valid = false;
	}

	public boolean isValid()
	{
		return this.valid;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft.getInstance().getTextureManager().bindTexture(Button.WIDGETS_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Icon icon = (this.valid) ? Icon.VALID : Icon.INVALID;

		this.blit(this.x, this.y, icon.getX(), icon.getY(), this.width, this.height);
	}

	@Override
	public boolean changeFocus(boolean forward)
	{
		return false;
	}

	enum Icon
	{
		VALID(208, 0),
		INVALID(192, 0);

		private final int x;
		private final int y;

		Icon(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public int getX()
		{
			return this.x;
		}

		public int getY()
		{
			return this.y;
		}
	}
}
