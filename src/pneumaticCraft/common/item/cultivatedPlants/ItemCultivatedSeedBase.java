package pneumaticCraft.common.item.cultivatedPlants;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.pneumaticPlants.BlockCultivatedPlantBase;
import pneumaticCraft.common.item.ItemPneumatic;
import pneumaticCraft.common.item.Itemss;

public abstract class ItemCultivatedSeedBase extends ItemPneumatic implements IPlantable{

    public static final ArrayList<ItemCultivatedSeedBase> allSeeds = new ArrayList<ItemCultivatedSeedBase>();

    private int nonCultivatedSeedMeta;
    private IIcon icon;

    public ItemCultivatedSeedBase(int nonCultivatedSeedMeta){
        super();
        this.nonCultivatedSeedMeta = nonCultivatedSeedMeta;
        ItemCultivatedSeedBase.allSeeds.add(this);
    }

    public int getNonCultivatedSeedMeta(){
        return(nonCultivatedSeedMeta);
    }

    protected BlockCultivatedPlantBase getPlantBlock(){
        return((BlockCultivatedPlantBase)getPlant(null, 0, 0, 0));
    }

    @Override
    public IIcon getIconFromDamage(int meta){
        return(icon);
    }

    /**
     * We're using icons from the non-cultivated plants. This is a good time to get the icon.
     */
    @Override
    public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister par1IconRegister){
        icon = Itemss.plasticPlant.getIconFromDamage(nonCultivatedSeedMeta);
    };

    /*
     * sides:
     *
     * 0 = -y
     * 1 = +y
     * 2 = -z
     * 3= +z
     * 4= -x
     * 5= +x
     * 
     */
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
        if(side != 1) {
            return false;
        }

        if(player.canPlayerEdit(x, y, z, side, stack) && player.canPlayerEdit(x, y + 1, z, side, stack)) {
            Block soil = world.getBlock(x, y, z);

            if(soil != null && soil.canSustainPlant(world, x, y, z, ForgeDirection.UP, this) && world.isAirBlock(x, y + 1, z)) {
                world.setBlock(x, y + 1, z, getPlantBlock());
                stack.stackSize--;
                return true;
            } else {
                return false;
            }
        }
            return false;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z){
        return EnumPlantType.Crop;
    }

    @Override
    public int getPlantMetadata(IBlockAccess world, int x, int y, int z){
        return 0;
    }

}
