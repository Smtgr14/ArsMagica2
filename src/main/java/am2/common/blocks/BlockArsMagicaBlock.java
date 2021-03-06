package am2.common.blocks;

import am2.common.items.ItemBlockOreBlock;
import am2.common.registry.Registry;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class BlockArsMagicaBlock extends BlockAM {
	
	public static final PropertyEnum<EnumBlockType> BLOCK_TYPE = PropertyEnum.create("block_type", EnumBlockType.class);
	
	
	public BlockArsMagicaBlock() {
		super(Material.ROCK);
		setDefaultState(blockState.getBaseState().withProperty(BLOCK_TYPE, EnumBlockType.VINTEUM));
	}

	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockArsMagicaBlock.BLOCK_TYPE);
	}
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		for (int i = 0; i < EnumBlockType.values().length; i++) {
			items.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BLOCK_TYPE).ordinal();
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BLOCK_TYPE, EnumBlockType.values()[MathHelper.clamp(meta, 0, EnumBlockType.values().length - 1)]);
	}
	
	@Override
	public BlockAM registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.getResourcePath());
		this.setRegistryName(rl);
		Registry.GetBlocksToRegister().add(this);
		Registry.GetItemsToRegister().add(new ItemBlockOreBlock(this).setRegistryName(rl));
		return this;
	}
	
	public static enum EnumBlockType implements IStringSerializable{
		VINTEUM,
		CHIMERITE,
		BLUETOPAZ,
		MOONSTONE,
		SUNSTONE;

		@Override
		public String getName() {
			return name().toLowerCase();
		}		
	}

}
