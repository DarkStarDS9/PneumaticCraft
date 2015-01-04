package pneumaticCraft.common.block.pneumaticPlants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.item.ItemPlasticPlants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockCultivatedPlantBase extends Block implements IGrowable, IPlantable{
    public static List<BlockCultivatedPlantBase> allPlants = new ArrayList<BlockCultivatedPlantBase>();

    protected BlockPneumaticPlantBase nonCultivatedBlock = null;

    public BlockCultivatedPlantBase(int nonCultivatedSeedMeta){
        super(Material.plants);

        setTickRandomly(true);
        this.nonCultivatedBlock = (BlockPneumaticPlantBase)ItemPlasticPlants.getPlantBlockIDFromSeed(nonCultivatedSeedMeta);
        float offs = 0.5F;
        setBlockBounds(0.5F - offs, 0.0F, 0.5F - offs, 0.5F + offs, 0.25F, 0.5F + offs);
        setCreativeTab((CreativeTabs)null);
        setHardness(0.0F);
        setStepSound(Block.soundTypeGrass);
        disableStats();

        BlockCultivatedPlantBase.allPlants.add(this);
    }

    @Override
    public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_){
        return null;
    };

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube(){
        return false;
    }

    /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType(){
        return 1;// flower rendertype
    }

    /**
     * From the specified side and block metadata retrieves the blocks texture.
     * Args: side, metadata
     */
    @Override
    public IIcon getIcon(int side, int meta){
        return nonCultivatedBlock.getIcon(side, meta);
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_){}
    */

    /**
     * Ticks the block if it's been scheduled
     */
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand){
        super.updateTick(world, x, y, z, rand);

        if(!world.isRemote) {
            if(rand.nextInt(15) == 0) {
                executeGrowthStep(world, x, y, z, rand);
            }
        }
    }

    /**
     * grows the plant
     */
    public void executeGrowthStep(World world, int x, int y, int z, Random rand){
        int meta = world.getBlockMetadata(x, y, z);
        if(isNotMature(meta)) {
            ++meta;
            world.setBlockMetadataWithNotify(x, y, z, meta, 3);
        }
    }

    private boolean isNotMature(int meta){
        return meta < 6;
    }

    /**
     * Apply bonemeal to the crops.
     */
    /*
    public boolean fertilize(World par1World, int par2, int par3, int par4, EntityPlayer player){
        int meta = par1World.getBlockMetadata(par2, par3, par4);
        if(meta == 6 || meta == 13) {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 13, 0);
            executeFullGrownEffect(par1World, par2, par3, par4, par1World.rand);
            return true;
        }
        if(meta > 13) return false;
        int l = meta + MathHelper.getRandomIntegerInRange(par1World.rand, 2, 5);
        if(meta < 6 && l > 6) {
            l = 6;
        } else if(meta > 6 && l > 13) {
            l = 13;
        }
        par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 3);
        return true;
        // player.inventory.getCurrentItem().stackSize--;//use the item
        // updateTick(par1World, par2, par3, par4, new Random());//Immediately
        // spawn a seed.
        // Doesnt work, because it first goes through the get growth rate thing.
    }
    */

    /*
    @Override
    public boolean canBlockStay(World par1World, int par2, int par3, int par4){
        Block soil = par1World.getBlock(par2, par3 - (isPlantHanging() ? -1 : 1), par4);
        return soil != null && canPlantGrowOnThisBlock(soil, par1World, par2, par3, par4);
    }
    */

    /**
     * Get the block's damage value (for use with pick block).
     */
    /*
    @Override
    public int getDamageValue(World par1World, int par2, int par3, int par4){
        return getSeedDamage();
    }
    */

    @Override
    @SideOnly(Side.CLIENT)
    public abstract Item getItem(World world, int x, int y, int z);

    @Override
    protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack stack){
        if(!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
            if(captureDrops.get()) {
                capturedDrops.get().add(stack);
                return;
            }
            float f = 0.7F;
            double d0 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double d1 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double d2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(world, x + d0, y + d1, z + d2, stack);
            // entityitem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(entityitem);
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune){
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        ret.add(new ItemStack(getItem(world, x, y, z), 1));

        if(metadata == 6) {
            ret.get(0).stackSize += 1 + world.rand.nextInt(2) + (fortune > 0 ? world.rand.nextInt(fortune + 1) : 0);
            //            ret.add(new ItemStack(getItem(world, x, y, z), 1 + world.rand.nextInt(2) + (fortune > 0 ? world.rand.nextInt(fortune + 1) : 0)));
        }

        return ret;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        if(!canBlockStay(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z){
        return world.getBlock(x, y - 1, z).canSustainPlant(world, x, y, z, ForgeDirection.UP, this);
    }

    /**
     * can this grow when bonemealed?
     */
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isRemote){
        return isNotMature(world.getBlockMetadata(x, y, z));
    }

    /**
     * can we still grow (as opposed to: already mature)?
     */
    @Override
    public boolean func_149852_a(World world, Random random, int x, int y, int z){
        return isNotMature(world.getBlockMetadata(x, y, z));
    }

    /**
     * execute a growth step
     */
    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z){
        executeGrowthStep(world, x, y, z, rand);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z){
        return EnumPlantType.Crop;
    }

    @Override
    public Block getPlant(IBlockAccess world, int x, int y, int z){
        return this;
    }

    @Override
    public int getPlantMetadata(IBlockAccess world, int x, int y, int z){
        return world.getBlockMetadata(x, y, z);
    }
}
