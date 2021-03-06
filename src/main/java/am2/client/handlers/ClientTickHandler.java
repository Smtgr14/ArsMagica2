package am2.client.handlers;

import am2.ArsMagica2;
import am2.api.extensions.ISpellCaster;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.spell.AbstractSpellPart;
import am2.client.gui.AMGuiHelper;
import am2.client.particles.AMLineArc;
import am2.common.LogHelper;
import am2.common.armor.ArmorHelper;
import am2.common.armor.infusions.GenericImbuement;
import am2.common.bosses.BossSpawnHelper;
import am2.common.defs.ItemDefs;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemSpellBase;
import am2.common.items.ItemSpellBook;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.packet.AMPacketIDs;
import am2.common.power.PowerNodeEntry;
import am2.common.power.PowerTypes;
import am2.common.spell.SpellCaster;
import am2.common.spell.component.Telekinesis;
import am2.common.trackers.EntityItemWatcher;
import am2.common.utils.DimensionUtilities;
import am2.common.world.MeteorSpawnHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class ClientTickHandler{
	public static HashMap<EntityLiving, EntityLivingBase> targetsToSet = new HashMap<EntityLiving, EntityLivingBase>();
	private int mouseWheelValue = 0;
	private int currentSlot = -1;
	private boolean usingItem;

	public static String worldName;

	private boolean firstTick = true;

	private ArrayList<AMLineArc> arcs = new ArrayList<AMLineArc>();
	private int arcSpawnCounter = 0;
	private final int arcSpawnFrequency = 95;

	private int powerWatchSyncTick = 0;
	private Vec3d powerWatch = Vec3d.ZERO;
	private boolean hasSynced = false;
	private PowerNodeEntry powerData = null;

	private String lastWorldName;

	private void gameTick_Start(){

		if (Minecraft.getMinecraft().isIntegratedServerRunning()){
			if (worldName == null || !worldName.equals(Minecraft.getMinecraft().getIntegratedServer().getWorldName())){
				worldName = Minecraft.getMinecraft().getIntegratedServer().getWorldName();
				firstTick = true;
			}
		}else{
			if (worldName != null && (lastWorldName == null || lastWorldName != worldName.replace(" ", "_"))){
				lastWorldName = worldName.replace(" ", "_");
				firstTick = true;
			}
		}

		if (firstTick){
			ItemDefs.crystalPhylactery.getSpawnableEntities(Minecraft.getMinecraft().world);
			firstTick = false;
		}
		ArsMagica2.proxy.itemFrameWatcher.checkWatchedFrames();
	}

	private void applyDeferredPotionEffects(){
		for (EntityLivingBase ent : ArsMagica2.proxy.getDeferredPotionEffects().keySet()){
			ArrayList<PotionEffect> potions = ArsMagica2.proxy.getDeferredPotionEffects().get(ent);
			for (PotionEffect effect : potions)
				ent.addPotionEffect(effect);
		}

		ArsMagica2.proxy.clearDeferredPotionEffects();
	}

	private void applyDeferredDimensionTransfers(){
		for (EntityLivingBase ent : ArsMagica2.proxy.getDeferredDimensionTransfers().keySet()){
			DimensionUtilities.doDimensionTransfer(ent, ArsMagica2.proxy.getDeferredDimensionTransfers().get(ent));
		}

		ArsMagica2.proxy.clearDeferredDimensionTransfers();
	}

	private void gameTick_End(){

		AMGuiHelper.instance.tick();
		EntityItemWatcher.instance.tick();
		checkMouseDWheel();

		if (Minecraft.getMinecraft().isIntegratedServerRunning()){
			MeteorSpawnHelper.instance.tick();
			applyDeferredPotionEffects();
		}

		if (!powerWatch.equals(Vec3d.ZERO)){
			if (powerWatchSyncTick++ == 0){
				AMNetHandler.INSTANCE.sendPowerRequestToServer(powerWatch);
			}
			powerWatchSyncTick %= 20;
		}
	}

	private void spawnPowerPathVisuals(){
		if (Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null &&
				(Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == ItemDefs.magitechGoggles ||
						ArmorHelper.isInfusionPreset(Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD), GenericImbuement.magitechGoggleIntegration))
				){

			if (arcSpawnCounter++ >= arcSpawnFrequency){
				arcSpawnCounter = 0;

				AMVector3 playerPos = new AMVector3(Minecraft.getMinecraft().player);

				HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> paths = ArsMagica2.proxy.getPowerPathVisuals();
				if (paths != null){
					for (PowerTypes type : paths.keySet()){

						String texture =
								type == PowerTypes.LIGHT ? "textures/blocks/oreblockbluetopaz.png" :
										type == PowerTypes.NEUTRAL ? "textures/blocks/oreblockvinteum.png" :
												type == PowerTypes.DARK ? "textures/blocks/oreblocksunstone.png" :
														"textures/blocks/oreblocksunstone.png";

						ArrayList<LinkedList<BlockPos>> pathList = paths.get(type);
						for (LinkedList<BlockPos> individualPath : pathList){
							for (int i = 0; i < individualPath.size() - 1; ++i){
								BlockPos start = individualPath.get(i + 1);
								BlockPos end = individualPath.get(i);

								if (start.distanceSq(playerPos.toBlockPos()) > 2500 || end.distanceSq(playerPos.toBlockPos()) > 2500){
									continue;
								}


								TileEntity teStart = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(start));
								TileEntity teEnd = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(end));

								if (teEnd == null || !(teEnd instanceof IPowerNode))
									break;

								double startX = start.getX() + ((teStart != null && teStart instanceof IPowerNode) ? ((IPowerNode<?>)teStart).particleOffset(0) : 0.5f);
								double startY = start.getY() + ((teStart != null && teStart instanceof IPowerNode) ? ((IPowerNode<?>)teStart).particleOffset(1) : 0.5f);
								double startZ = start.getZ() + ((teStart != null && teStart instanceof IPowerNode) ? ((IPowerNode<?>)teStart).particleOffset(2) : 0.5f);

								double endX = end.getX() + ((IPowerNode<?>)teEnd).particleOffset(0);
								double endY = end.getY() + ((IPowerNode<?>)teEnd).particleOffset(1);
								double endZ = end.getZ() + ((IPowerNode<?>)teEnd).particleOffset(2);

								AMLineArc arc = (AMLineArc)ArsMagica2.proxy.particleManager.spawn(
										Minecraft.getMinecraft().world,
										texture,
										startX,
										startY,
										startZ,
										endX,
										endY,
										endZ);

								if (arc != null){
									arcs.add(arc);
								}
							}
						}
					}
				}
			}
		}else
		{
			Iterator<AMLineArc> it = arcs.iterator();
			while (it.hasNext()){
				AMLineArc arc = it.next();
				if (arc == null || !arc.isAlive())
					it.remove();
				else
					arc.setExpired();
			}
			arcSpawnCounter = arcSpawnFrequency;
		}
	}

	private void checkMouseDWheel(){
		if (this.mouseWheelValue != 0 && this.currentSlot > -1){
			Minecraft.getMinecraft().player.inventory.currentItem = this.currentSlot;

			ItemStack stack = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);

			if (checkForTKMove(stack)){
				EntityExtension props = (EntityExtension)EntityExtension.For(Minecraft.getMinecraft().player);
				if (this.mouseWheelValue > 0 && props.getTKDistance() < 10){
					props.addToTKDistance(0.5f);
				}else if (this.mouseWheelValue < 0 && props.getTKDistance() > 0.3){
					props.addToTKDistance(-0.5f);
				}
				LogHelper.debug("TK Distance: %.2f", props.getTKDistance());
				props.syncTKDistance();
			}
				else if (stack.getItem() instanceof ItemSpellBook && Minecraft.getMinecraft().player.isSneaking()){
				ItemSpellBook isb = (ItemSpellBook)stack.getItem();
				if (this.mouseWheelValue != 0){
					byte subID = 0;
					if (this.mouseWheelValue < 0){
						isb.SetNextSlot(Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND));
						subID = ItemSpellBook.ID_NEXT_SPELL;
					}else{
						isb.SetPrevSlot(Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND));
						subID = ItemSpellBook.ID_PREV_SPELL;
					}
					//send packet to server
					AMNetHandler.INSTANCE.sendPacketToServer(
							AMPacketIDs.SPELLBOOK_CHANGE_ACTIVE_SLOT,
							new AMDataWriter()
									.add(subID)
									.add(Minecraft.getMinecraft().player.getEntityId())
									.add(Minecraft.getMinecraft().player.inventory.currentItem)
									.generate()
					);
				}
			}
			this.currentSlot = -1;
			this.mouseWheelValue = 0;
		}
	}

	private boolean checkForTKMove(ItemStack stack){
		if (stack.getItem() instanceof ItemSpellBook){
			ItemStack activeStack = ((ItemSpellBook)stack.getItem()).GetActiveItemStack(stack);
			if (activeStack != null)
				stack = activeStack;
		}
		if (stack.getItem() instanceof ItemSpellBase && stack.hasCapability(SpellCaster.INSTANCE, null) && this.usingItem){
			ISpellCaster caster = stack.getCapability(SpellCaster.INSTANCE, null);
			for (List<AbstractSpellPart> components : caster.getSpellCommon()) {
				for (AbstractSpellPart component : components) {
					if (component instanceof Telekinesis){
						return true;
					}
				}
			}
		}
		return false;
	}

	private void renderTick_Start(){
		if (!Minecraft.getMinecraft().inGameHasFocus)
			AMGuiHelper.instance.guiTick();
	}

	private void renderTick_End(){
	}

	private void localServerTick_End(){
		BossSpawnHelper.instance.tick();
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event){
		if (event.phase == TickEvent.Phase.START){
			GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
			if (guiscreen != null){
			}else{
				gameTick_Start();
			}
		}else if (event.phase == TickEvent.Phase.END){
			GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
			if (guiscreen != null){
			}else{
				gameTick_End();
			}

			if (Minecraft.getMinecraft().world != null)
				spawnPowerPathVisuals();
		}
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event){
		if (event.phase == TickEvent.Phase.START){
			renderTick_Start();
		}else if (event.phase == TickEvent.Phase.END){
			renderTick_End();
		}
	}
	
	@SubscribeEvent
	public void onBlockHighlight(DrawBlockHighlightEvent event){
		ArsMagica2.proxy.drawPowerOnBlockHighlight(event.getPlayer(), event.getTarget(), event.getPartialTicks());
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event){
//		if (Minecraft.getMinecraft().isIntegratedServerRunning()){
//			if (ArsMagica2.config.retroactiveWorldgen())
//				RetroactiveWorldgenerator.instance.continueRetrogen(event.world);
//		}
		if (event.phase == TickEvent.Phase.END){
			applyDeferredDimensionTransfers();
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event){
		if (event.phase == TickEvent.Phase.END){
			localServerTick_End();
		}
	}

	public void setDWheel(int dWheel, int slot, boolean usingItem){
		this.mouseWheelValue = dWheel;
		this.currentSlot = slot;
		this.usingItem = usingItem;
	}

	public Vec3d getTrackLocation(){
		return this.powerWatch;
	}

	public PowerNodeEntry getTrackData(){
		return this.powerData;
	}

	public void setTrackLocation(Vec3d location){
		if (location.equals(Vec3d.ZERO)){
			this.hasSynced = false;
			this.powerWatch = location;
			return;
		}
		if (!this.powerWatch.equals(location)){
			this.powerWatch = location;
			this.powerWatchSyncTick = 0;
			this.hasSynced = false;
		}
	}

	public void setTrackData(NBTTagCompound compound){
		this.powerData = new PowerNodeEntry();
		this.powerData.readFromNBT(compound);
		this.hasSynced = true;
	}

	public boolean getHasSynced(){
		return this.hasSynced;
	}

	public void addDeferredTarget(EntityLiving ent, EntityLivingBase target){
		targetsToSet.put(ent, target);
	}
}
