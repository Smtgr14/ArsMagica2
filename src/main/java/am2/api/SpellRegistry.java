package am2.api;

import am2.ArsMagica2;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.api.skill.SkillTree;
import am2.api.spell.AbstractSpellPart;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellShape;
import am2.common.LogHelper;
import am2.common.utils.NBTUtils;
import am2.common.utils.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;

/**
 * Contains all spell parts, used for both registration<BR>
 * Skill are automatically created when doing any thing
 *
 */
public class SpellRegistry {
	
	/**
	 * Register a spell component
	 * 
	 * @param id : Name of this component
	 * @param icon : Icon
	 * @param tier : Skill Point required to unlock
	 * @param part : Actual Component, use new {@link SpellComponent} ()
	 * @param tree : Skill Tree
	 * @param posX : Position in the tree
	 * @param posY : Position in the tree
	 * @param parents : Skills that need to be unlocked before this one (occulus only)
	 */
	public static void registerSpellComponent (String id, ResourceLocation icon, SkillPoint tier, SpellComponent part, SkillTree tree, int posX, int posY, int ID, String... parents) {
		id = id.toLowerCase();
		ArsMagicaAPI.getSpellRegistry ( ).register ( part.setRegistryName ( ArsMagica2.MODID , "spellcomp_" + id ) );
		ArsMagicaAPI.getSkillRegistry ( ).register ( new Skill ( icon , tier , posX , posY , tree , ID , parents ).setRegistryName ( ArsMagica2.MODID , "spellcomp_" + id ) );
	}
	
	/**
	 * Register a spell modifier
	 * 
	 * @param id : Name of this modifier
	 * @param icon : Icon
	 * @param tier : Skill Point required to unlock
	 * @param part : Actual Modifier, use new {@link SpellModifier} ()
	 * @param tree : Skill Tree
	 * @param posX : Position in the tree
	 * @param posY : Position in the tree
	 * @param parents : Skills that need to be unlocked before this one (occulus only)
	 */
	public static void registerSpellModifier (String id, ResourceLocation icon, SkillPoint tier, SpellModifier part, SkillTree tree, int posX, int posY, int ID,String... parents) {
		id = id.toLowerCase();
		ArsMagicaAPI.getSpellRegistry ( ).register ( part.setRegistryName ( ArsMagica2.MODID , "spellmodifier_" + id ) );
		ArsMagicaAPI.getSkillRegistry ( ).register ( new Skill ( icon , tier , posX , posY , tree , ID , parents ).setRegistryName ( ArsMagica2.MODID , "spellmodifier_" + id ) );
	}
	
	/**
	 * Register a spell shape
	 * 
	 * @param id : Name of this shape
	 * @param icon : Icon
	 * @param tier : Skill Point required to unlock
	 * @param part : Actual Shape, use new {@link SpellShape} ()
	 * @param tree : Skill Tree
	 * @param posX : Position in the tree
	 * @param posY : Position in the tree
	 * @param parents : Skills that need to be unlocked before this one (occulus only)
	 */
	public static void registerSpellShape (String id, ResourceLocation icon, SkillPoint tier, SpellShape part, SkillTree tree, int posX, int posY, int ID, String... parents) {
		id = id.toLowerCase();
		ArsMagicaAPI.getSpellRegistry ( ).register ( part.setRegistryName ( ArsMagica2.MODID , "spellpart_" + id ) );
		ArsMagicaAPI.getSkillRegistry ( ).register ( new Skill ( icon , tier , posX , posY , tree , ID , parents ).setRegistryName ( ArsMagica2.MODID , "spellpart_" + id ) );
	}
	
	public static Skill getSkillFromPart(AbstractSpellPart part) {
		return ArsMagicaAPI.getSkillRegistry().getValue(part.getRegistryName());
	}

	public static AbstractSpellPart getPartByRecipe(ArrayList<ItemStack> currentAddedItems) {
		for (AbstractSpellPart data : ArsMagicaAPI.getSpellRegistry().getValues()) {
			if (data != null && data.getRecipe() != null) {
				ArrayList<ItemStack> convRecipe = RecipeUtils.getConvRecipe(data);
				boolean match = currentAddedItems.size() == convRecipe.size();
				if (!match) continue;
				for (int i = 0; i < convRecipe.size(); i++) {
					match &= OreDictionary.itemMatches(convRecipe.get(i), currentAddedItems.get(i), false);
					match &= convRecipe.get ( i ).getTagCompound ( ) == null || ( currentAddedItems.get ( i ).getTagCompound ( ) != null && NBTUtils.contains ( convRecipe.get ( i ).getTagCompound ( ) , currentAddedItems.get ( i ).getTagCompound ( ) ) );
					if (!match) break;
				}
				if (!match) LogHelper.debug("Part doesn't match %s", data.getRegistryName().toString());
				if (!match) continue;
				LogHelper.debug("Part matches : %s!", data.getRegistryName().toString());
				return data;
			}
		}
		return null;
	}

	public static SpellShape getShapeFromName(String shapeName) {
		AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(shapeName));
		return part instanceof SpellShape ? (SpellShape) part : null;
	}
	
	public static SpellModifier getModifierFromName(String shapeName) {
		AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(shapeName));
		return part instanceof SpellModifier ? (SpellModifier) part : null;
	}
	
	public static SpellComponent getComponentFromName(String shapeName) {
		AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(shapeName));
		return part instanceof SpellComponent ? (SpellComponent) part : null;
	}
}
