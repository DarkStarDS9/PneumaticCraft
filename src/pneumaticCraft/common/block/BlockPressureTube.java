package pneumaticCraft.common.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.item.ItemTubeModule;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.BBConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPressureTube extends BlockPneumaticCraftModeled{

    public AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[6];
    private final float dangerPressure, criticalPressure;
    private final int volume;

    public BlockPressureTube(Material par2Material, float dangerPressure, float criticalPressure, int volume){
        super(par2Material);

        double width = (BBConstants.PRESSURE_PIPE_MAX_POS - BBConstants.PRESSURE_PIPE_MIN_POS) / 2;
        double height = BBConstants.PRESSURE_PIPE_MIN_POS;

        boundingBoxes[0] = AxisAlignedBB.getBoundingBox(0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width);
        boundingBoxes[1] = AxisAlignedBB.getBoundingBox(0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        boundingBoxes[2] = AxisAlignedBB.getBoundingBox(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = AxisAlignedBB.getBoundingBox(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height);
        boundingBoxes[4] = AxisAlignedBB.getBoundingBox(BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        boundingBoxes[5] = AxisAlignedBB.getBoundingBox(BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);

        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.volume = volume;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return null;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata){
        return new TileEntityPressureTube(dangerPressure, criticalPressure, volume);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        if(!world.isRemote) {
            if(player.getCurrentEquippedItem() != null) {
                if(player.getCurrentEquippedItem().getItem() instanceof ItemTubeModule) {
                    TileEntityPressureTube pressureTube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
                    if(pressureTube.modules[par6] == null) {
                        TubeModule module = ModuleRegistrator.getModule(((ItemTubeModule)player.getCurrentEquippedItem().getItem()).moduleName);
                        pressureTube.setModule(module, ForgeDirection.getOrientation(par6));
                        onNeighborBlockChange(world, x, y, z, this);
                        world.notifyBlocksOfNeighborChange(x, y, z, this, ForgeDirection.getOrientation(par6).getOpposite().ordinal());
                        if(!player.capabilities.isCreativeMode) player.getCurrentEquippedItem().stackSize--;
                        world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Block.soundTypeGlass.getStepResourcePath(), Block.soundTypeGlass.getVolume() * 5.0F, Block.soundTypeGlass.getPitch() * .9F);
                        return true;
                    }
                } else if(player.getCurrentEquippedItem().getItem() == Itemss.advancedPCB) {
                    TubeModule module = BlockPressureTube.getLookedModule(world, x, y, z, player);
                    if(module != null && !module.isUpgraded() && module.canUpgrade()) {
                        if(!world.isRemote) {
                            module.upgrade();
                            if(!player.capabilities.isCreativeMode) player.getCurrentEquippedItem().stackSize--;
                        }
                        return true;
                    }
                }
            }

        }
        if(!player.isSneaking()) {
            TubeModule module = getLookedModule(world, x, y, z, player);
            if(module != null) {
                return module.onActivated(player);
            }
        }
        return false;
    }

    public static TubeModule getLookedModule(World world, int x, int y, int z, EntityPlayer player){
        MovingObjectPosition mop = PneumaticCraftUtils.getEntityLookedObject(player);
        if(mop != null && mop.hitInfo instanceof ForgeDirection && (ForgeDirection)mop.hitInfo != ForgeDirection.UNKNOWN) {
            TileEntityPressureTube tube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
            return tube.modules[((ForgeDirection)mop.hitInfo).ordinal()];
        }
        return null;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction){
        MovingObjectPosition bestMOP = null;
        AxisAlignedBB bestAABB = null;

        setBlockBounds(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
        MovingObjectPosition mop = super.collisionRayTrace(world, x, y, z, origin, direction);
        if(isCloserMOP(origin, bestMOP, mop)) {
            bestMOP = mop;
            bestAABB = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        TileEntityPressureTube tube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
        for(int i = 0; i < 6; i++) {
            if(tube.sidesConnected[i]) {
                setBlockBounds(boundingBoxes[i]);
                mop = super.collisionRayTrace(world, x, y, z, origin, direction);
                if(isCloserMOP(origin, bestMOP, mop)) {
                    bestMOP = mop;
                    bestAABB = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }

        if(bestMOP != null) bestMOP.hitInfo = ForgeDirection.UNKNOWN;//unknown indicates we hit the tube.

        TubeModule[] modules = ((TileEntityPressureTube)world.getTileEntity(x, y, z)).modules;
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if(modules[dir.ordinal()] != null) {
                setBlockBounds(modules[dir.ordinal()].boundingBoxes[dir.ordinal()]);
                mop = super.collisionRayTrace(world, x, y, z, origin, direction);
                if(isCloserMOP(origin, bestMOP, mop)) {
                    mop.hitInfo = dir;
                    bestMOP = mop;
                    bestAABB = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
        if(bestAABB != null) setBlockBounds(bestAABB);
        return bestMOP;
    }

    private boolean isCloserMOP(Vec3 origin, MovingObjectPosition originalMOP, MovingObjectPosition newMOP){
        if(newMOP == null) return false;
        if(originalMOP == null) return true;
        return PneumaticCraftUtils.distBetween(origin, newMOP.hitVec) < PneumaticCraftUtils.distBetween(origin, originalMOP.hitVec);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z){
        if(target.hitInfo == ForgeDirection.UNKNOWN) {
            return super.getPickBlock(target, world, x, y, z);
        } else {
            TileEntityPressureTube tube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
            return new ItemStack(ModuleRegistrator.getModuleItem(tube.modules[((ForgeDirection)target.hitInfo).ordinal()].getType()));
        }
    }

    private void setBlockBounds(AxisAlignedBB aabb){
        this.setBlockBounds((float)aabb.minX, (float)aabb.minY, (float)aabb.minZ, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z){
        MovingObjectPosition mop = PneumaticCraftUtils.getEntityLookedObject(player);
        if(mop != null && mop.hitInfo instanceof ForgeDirection) {
            if(mop.hitInfo != ForgeDirection.UNKNOWN) {
                if(!world.isRemote) {
                    TileEntityPressureTube tube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
                    if(!player.capabilities.isCreativeMode) {
                        List<ItemStack> drops = tube.modules[((ForgeDirection)mop.hitInfo).ordinal()].getDrops();
                        for(ItemStack drop : drops) {
                            EntityItem entity = new EntityItem(world, x, y, z);
                            entity.setEntityItemStack(drop);
                            world.spawnEntityInWorld(entity);
                        }
                    }
                    tube.setModule(null, (ForgeDirection)mop.hitInfo);
                    onNeighborBlockChange(world, x, y, z, this);
                    world.notifyBlocksOfNeighborChange(x, y, z, this, ((ForgeDirection)mop.hitInfo).getOpposite().ordinal());
                }
                return false;
            }
        }
        if(!world.isRemote && !player.capabilities.isCreativeMode) {
            for(TubeModule module : ((TileEntityPressureTube)world.getTileEntity(x, y, z)).modules) {
                if(module != null) {
                    List<ItemStack> drops = module.getDrops();
                    for(ItemStack drop : drops) {
                        EntityItem entity = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5);
                        entity.setEntityItemStack(drop);
                        world.spawnEntityInWorld(entity);
                    }
                }
            }
        }
        return super.removedByPlayer(world, player, x, y, z);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBounds(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
        super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, par7Entity);

        TileEntity te = world.getTileEntity(x, y, z);
        TileEntityPressureTube tePt = (TileEntityPressureTube)te;

        for(int i = 0; i < 6; i++) {
            if(tePt.sidesConnected[i]) {
                setBlockBounds(boundingBoxes[i]);
                super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, par7Entity);
            } else if(tePt.modules[i] != null) {
                setBlockBounds(tePt.modules[i].boundingBoxes[i]);
                super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, par7Entity);
            }
        }
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z){
        super.onBlockAdded(world, x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube)te;
            tePt.updateConnections(world, x, y, z);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random){
        TileEntity te = par1World.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube)te;
            int l = 0;
            for(TubeModule module : tePt.modules)
                if(module != null) l = Math.max(l, module.getRedstoneLevel());
            if(l > 0) {
                // for(int i = 0; i < 4; i++){
                double d0 = par2 + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                double d1 = par3 + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                double d2 = par4 + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                float f = l / 15.0F;
                float f1 = f * 0.6F + 0.4F;

                if(l == 0) {
                    f1 = 0.0F;
                }

                float f2 = f * f * 0.7F - 0.5F;
                float f3 = f * f * 0.6F - 0.7F;
                if(f2 < 0.0F) {
                    f2 = 0.0F;
                }

                if(f3 < 0.0F) {
                    f3 = 0.0F;
                }
                // PacketDispatcher.sendPacketToAllPlayers(PacketHandlerPneumaticCraft.spawnParticle("reddust",
                // d0, d1, d2, (double)f1, (double)f2, (double)f3));
                par1World.spawnParticle("reddust", d0, d1, d2, f1, f2, f3);
                // }
            }
        }

    }

    /**
     * Returns true if the block is emitting direct/strong redstone power on the
     * specified side. Args: World, X, Y, Z, side. Note that the side is
     * reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    @Override
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
        return 0;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int side){

        TileEntity te = par1IBlockAccess.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube)te;
            int redstoneLevel = 0;
            for(int i = 0; i < 6; i++) {
                if(tePt.modules[i] != null) {
                    if((side ^ 1) == i || i != side && tePt.modules[i].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                        redstoneLevel = Math.max(redstoneLevel, tePt.modules[i].getRedstoneLevel());
                    }
                }
            }
            return redstoneLevel;
        }
        return 0;
    }

}
