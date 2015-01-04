package pneumaticCraft.common.block.pneumaticPlants;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;

public class BlockCultivatedHeliumPlant extends BlockCultivatedPlantBase{
    IIcon[] texture;

    public BlockCultivatedHeliumPlant(){
        super(ItemPlasticPlants.HELIUM_PLANT_DAMAGE);
    }

    @Override
    public void registerBlockIcons(IIconRegister register){
        texture = new IIcon[7];

        for(int i = 0; i < 7; i++) {
            texture[i] = register.registerIcon(Textures.ICON_CULTIVATED_HELIUM_PLANT_LOCATION + i);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta){
        if(meta > -1 && meta < texture.length) {
            return texture[meta];
        } else return null;
    }

    @Override
    public Item getItem(World world, int x, int y, int z){
        return Itemss.cultivatedHeliumSeeds;
    }
}
