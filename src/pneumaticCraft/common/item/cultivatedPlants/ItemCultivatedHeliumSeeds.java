package pneumaticCraft.common.item.cultivatedPlants;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemPlasticPlants;

public class ItemCultivatedHeliumSeeds extends ItemCultivatedSeedBase{

    public ItemCultivatedHeliumSeeds(){
        super(ItemPlasticPlants.HELIUM_PLANT_DAMAGE);
    }

    @Override
    public Block getPlant(IBlockAccess world, int x, int y, int z){
        return Blockss.cultivatedHeliumPlant;
    }
}
