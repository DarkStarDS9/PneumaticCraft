package pneumaticCraft.common.nei;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.block.pneumaticPlants.BlockCultivatedPlantBase;
import pneumaticCraft.common.block.pneumaticPlants.BlockPneumaticPlantBase;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.Versions;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.guihook.GuiContainerManager;

public class NEIPluginInitConfig implements IConfigureNEI{

    @Override
    public void loadConfig(){

        Log.info("Initializing " + getName() + "...");

        //Pressure Chamber handler
        API.registerUsageHandler(new NEIPressureChamberRecipeManager());
        API.registerRecipeHandler(new NEIPressureChamberRecipeManager());

        //Assembly Controller Handler
        API.registerUsageHandler(new NEIAssemblyControllerRecipeManager());
        API.registerRecipeHandler(new NEIAssemblyControllerRecipeManager());

        GuiContainerManager.addDrawHandler(new ItemDrawHandler());

        //handle drop down windows
        /* MultiItemRange tubes = new MultiItemRange();
         tubes.add(Blockss.pressureTube, 0, BlockPressureTube.PRESSURE_TUBES_AMOUNT);
         tubes.add(Blockss.advancedPressureTube, 0, BlockPressureTube.PRESSURE_TUBES_AMOUNT);
         API.addSetRange("Mod.PneumaticCraft.Pressure Tubes", tubes);

         MultiItemRange upgrades = new MultiItemRange();
         upgrades.add(Itemss.machineUpgrade, 0, ItemMachineUpgrade.UPGRADES_AMOUNT);
         API.addSetRange("Mod.PneumaticCraft.Machine Upgrades", upgrades);*/

        for(Block b : BlockPneumaticPlantBase.allPlants) {
            API.hideItem(new ItemStack(b));
        }
        for(Block b : BlockCultivatedPlantBase.allPlants) {
            API.hideItem(new ItemStack(b));
        }
    }

    @Override
    public String getName(){
        return "PneumaticCraft built-in NEI plugin";
    }

    @Override
    public String getVersion(){
        return Versions.fullVersionString();
    }
}
