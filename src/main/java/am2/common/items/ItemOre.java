package am2.common.items;

import am2.common.defs.CreativeTabsDefs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemOre extends ItemArsMagica {
	
	public static final String[] names = {
			"vinteum",
			"purified_vinteum",
			"chimerite",
			"moonstone",
			"sunstone",
			"bluetopaz",
			"arcaneash",
			"arcane_compound",
			"animal_fat"};
	public static final int META_VINTEUM = 0;
	public static final int META_PURIFIED_VINTEUM = 1;
	public static final int META_CHIMERITE = 2;
	public static final int META_MOONSTONE = 3;
	public static final int META_SUNSTONE = 4;	
	public static final int META_BLUE_TOPAZ = 5;
	public static final int META_ARCANEASH = 6;
	public static final int META_ARCANECOMPOUND = 7;
	public static final int META_ANIMALFAT = 8;
	
	public ItemOre() {
		 setHasSubtypes(true);
		 setMaxDamage(0);
		 setCreativeTab(CreativeTabsDefs.tabAM2Items);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		for (int i = 0; i < names.length; i++) {
			items.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item.arsmagica2:ore." + names[MathHelper.clamp(stack.getItemDamage(), 0, names.length - 1)];
	}
}
