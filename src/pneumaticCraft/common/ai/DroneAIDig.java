package pneumaticCraft.common.ai;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class DroneAIDig extends DroneAIBlockInteraction{

    /**
     * 
     * @param drone
     * @param speed
     * @param widget needs to implement IBlockOrdered.
     */
    public DroneAIDig(EntityDrone drone, double speed, ProgWidgetAreaItemBase widget){
        super(drone, speed, widget);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        Block block = worldCache.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        if(!worldCache.isAirBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ) && !ignoreBlock(block)) {
            int meta = worldCache.getBlockMetadata(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            List<ItemStack> droppedStacks;
            if(block.canSilkHarvest(drone.worldObj, drone.getFakePlayer(), pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, meta)) {
                droppedStacks = Arrays.asList(new ItemStack[]{getSilkTouchBlock(block, meta)});
            } else {
                droppedStacks = block.getDrops(drone.worldObj, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, meta, 0);
            }
            for(ItemStack droppedStack : droppedStacks) {
                if(widget.isItemValidForFilters(droppedStack, meta)) {
                    for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                        if(drone.isBlockValidPathfindBlock(pos.chunkPosX + dir.offsetX, pos.chunkPosY + dir.offsetY, pos.chunkPosZ + dir.offsetZ)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        if(!((FakePlayerItemInWorldManager)drone.getFakePlayer().theItemInWorldManager).isDigging() || !((FakePlayerItemInWorldManager)drone.getFakePlayer().theItemInWorldManager).isAcknowledged()) {
            int x = pos.chunkPosX;
            int y = pos.chunkPosY;
            int z = pos.chunkPosZ;

            Block block = worldCache.getBlock(x, y, z);
            if(!ignoreBlock(block) && isBlockValidForFilter(worldCache, drone, pos, widget)) {
                if(block.getBlockHardness(drone.worldObj, x, y, z) < 0) {
                    addToBlacklist(pos);
                    drone.setDugBlock(0, 0, 0);
                    return false;
                }
                FakePlayerItemInWorldManager manager = (FakePlayerItemInWorldManager)drone.getFakePlayer().theItemInWorldManager;
                manager.onBlockClicked(x, y, z, 0);
                if(!manager.isAccepted) {
                    addToBlacklist(pos);
                    drone.setDugBlock(0, 0, 0);
                    return false;
                }
                drone.setDugBlock(x, y, z);
                return true;
            }
            drone.setDugBlock(0, 0, 0);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isBlockValidForFilter(IBlockAccess worldCache, EntityDrone drone, ChunkPosition pos, ProgWidgetAreaItemBase widget){
        int x = pos.chunkPosX;
        int y = pos.chunkPosY;
        int z = pos.chunkPosZ;
        Block block = worldCache.getBlock(x, y, z);

        if(!block.isAir(worldCache, x, y, z)) {
            int meta = worldCache.getBlockMetadata(x, y, z);
            List<ItemStack> droppedStacks;
            if(block.canSilkHarvest(drone.worldObj, drone.getFakePlayer(), x, y, z, meta)) {
                droppedStacks = Arrays.asList(new ItemStack[]{getSilkTouchBlock(block, meta)});
            } else {
                droppedStacks = block.getDrops(drone.worldObj, x, y, z, meta, 0);
            }
            for(ItemStack droppedStack : droppedStacks) {
                if(widget.isItemValidForFilters(droppedStack, meta)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final HashMap<Integer, ItemStack> silkTouchBlocks = new HashMap<Integer, ItemStack>();

    private static ItemStack getSilkTouchBlock(Block block, int meta){
        ItemStack stack = silkTouchBlocks.get(Block.getIdFromBlock(block));
        if(stack == null) {
            Method method = ReflectionHelper.findMethod(Block.class, block, new String[]{"func_149644_j", "createStackedBlock"}, int.class);
            try {
                stack = (ItemStack)method.invoke(block, meta);
            } catch(Exception e) {
                Log.error("Reflection failed when trying to get a silk touch block!");
                e.printStackTrace();
            }
            silkTouchBlocks.put(Block.getIdFromBlock(block), stack);
        }
        return stack.copy();
    }

    private static boolean ignoreBlock(Block block){
        return PneumaticCraftUtils.isBlockLiquid(block);
    }

}
