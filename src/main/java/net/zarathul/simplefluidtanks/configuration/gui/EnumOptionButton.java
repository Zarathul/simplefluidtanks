package net.zarathul.simplefluidtanks.configuration.gui;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiButtonExt;

@OnlyIn(Dist.CLIENT)
public class EnumOptionButton<E extends Enum<E>> extends GuiButtonExt
{
	private Class<E> clazz;
	private int selectedIndex;
	private String[] names;
	private String[] i18nNames;

	private static final String I18N_ENUM_PREFIX = "config.enums.";

	/**
	 * The button message is automatically localized. Localization keys are of the following format:
	 * "config.enums.enum_name.enum_value" e.g. "config.enums.colors.red". Note that both enum_name
	 * and enum_value need to be lower case.
	 */
	public EnumOptionButton(Class<E> clazz, String value, int x, int y, int width, int height)
	{
		super(x, y, width, height, I18n.format(I18N_ENUM_PREFIX + clazz.getSimpleName().toLowerCase() + "." + value.toLowerCase()), (button) -> {;});

		this.clazz = clazz;
		this.selectedIndex = 0;

		int i = 0;

		E[] constants = clazz.getEnumConstants();
		this.names = new String[constants.length];
		this.i18nNames = new String[constants.length];

		for (E e : constants)
		{
			names[i] = e.name();
			i18nNames[i] = I18n.format(I18N_ENUM_PREFIX + clazz.getSimpleName().toLowerCase() + "." + e.name().toLowerCase());
			if (e.name().equals(value)) selectedIndex = i;
			i++;
		}
	}

	@Override
	public void onPress()
	{
		super.onPress();
		nextValue();
	}

	public E getValue()
	{
		return Enum.valueOf(this.clazz, names[this.selectedIndex]);
	}

	public void setValue(E value)
	{
		for (int i = 0; i < names.length; i++)
		{
			if (names[i] == value.name())
			{
				this.selectedIndex = i;
				this.setMessage(this.i18nNames[i]);
				break;
			}
		}
	}

	private void nextValue()
	{
		this.selectedIndex = (this.selectedIndex + 1) % this.names.length;
		this.setMessage(this.i18nNames[this.selectedIndex]);
	}
}
