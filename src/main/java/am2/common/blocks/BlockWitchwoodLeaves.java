package am2.common.blocks;

import am2.ArsMagica2;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.client.particles.ParticlePendulum;
import am2.common.defs.BlockDefs;
import am2.common.defs.CreativeTabsDefs;
import am2.common.items.ItemBlockSubtypes;
import am2.common.registry.Registry;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockWitchwoodLeaves extends BlockLeaves{

	public BlockWitchwoodLeaves(){
		super();
		setHardness(0.2F);
		setLightOpacity(1);
		this.setTickRandomly(true);
		setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
		this.setDefaultState(blockState.getBaseState().withProperty(CHECK_DECAY, true).withProperty(DECAYABLE, true));
		leavesFancy = true;
	}
	
	@Override
	protected int getSaplingDropChance(IBlockState state) {
		return 150;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(BlockDefs.witchwoodSapling);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(CHECK_DECAY, (meta & 0x1) == 0x1).withProperty(DECAYABLE, (meta & 0x2) == 0x2);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = 0;
		if (state.getValue(CHECK_DECAY))
			meta |= 0x1;
		if (state.getValue(DECAYABLE))
			meta |= 0x2;
		return meta;
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items){
		items.add(new ItemStack(this));
	}
	
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (!ArsMagica2.config.witchwoodLeafPFX())
			return;
		if (rand.nextInt(300) == 0 && worldIn.isAirBlock(pos.down())){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(worldIn, "leaf", pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(), pos.getZ() + rand.nextDouble());
			if (particle != null){
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, -0.05f, 1, false));
				particle.setMaxAge(120);
				particle.setParticleScale(0.1f + rand.nextFloat() * 0.1f);
				particle.AddParticleController(new ParticlePendulum(particle, 0.2f, 0.15f + rand.nextFloat() * 0.2f, 2, false));
			}
		}
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		return Lists.newArrayList(new ItemStack(this));
	}

	@Override
	public EnumType getWoodType(int meta) {
		return EnumType.OAK;
	}
	
	public BlockWitchwoodLeaves registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		this.setRegistryName(rl);
		Registry.GetBlocksToRegister().add(this);
		Registry.GetItemsToRegister().add(new ItemBlockSubtypes(this).setRegistryName(rl));
		return this;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
	}
}
