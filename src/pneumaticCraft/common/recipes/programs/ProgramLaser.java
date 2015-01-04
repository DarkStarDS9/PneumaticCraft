package pneumaticCraft.common.recipes.programs;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketPlaySound;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.common.tileentity.TileEntityAssemblyController;
import pneumaticCraft.common.tileentity.TileEntityAssemblyDrill;
import pneumaticCraft.common.tileentity.TileEntityAssemblyIOUnit;
import pneumaticCraft.common.tileentity.TileEntityAssemblyLaser;
import pneumaticCraft.common.tileentity.TileEntityAssemblyPlatform;

public class ProgramLaser extends AssemblyProgram{

    @Override
    public EnumMachine[] getRequiredMachines(){
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(World world, TileEntityAssemblyController controller, TileEntityAssemblyPlatform platform, TileEntityAssemblyIOUnit ioUnitImport, TileEntityAssemblyIOUnit ioUnitExport, TileEntityAssemblyDrill drill, TileEntityAssemblyLaser laser){
        boolean useAir = true;

        if(platform.getHeldStack() != null) {
            if(canItemBeLasered(platform.getHeldStack())) {
                laser.startLasering();
                
                if(laser.getLaserPercentDone() == 50) {
                    // plants are very delicate and have a high chance of being destroyed
                    if(platform.getHeldStack().getItem() instanceof ItemPlasticPlants && world.rand.nextInt(50) != 0) {
                        NetworkHandler.sendToAllAround(new PacketPlaySound("fire.fire", platform.xCoord + 0.5F, platform.yCoord + 0.5F, platform.zCoord + 0.5F, 0.5F, 2.0F, true), world);
                        NetworkHandler.sendToAllAround(new PacketSpawnParticle("flame", platform.xCoord + 0.5F, platform.yCoord + 0.5F, platform.zCoord + 0.5F, 0, 0, 0), world);
                        NetworkHandler.sendToAllAround(new PacketSpawnParticle("smoke", platform.xCoord + 0.5F, platform.yCoord + 0.5F, platform.zCoord + 0.5F, 0, 0.025F, 0), world);
                        platform.setHeldStack(null);
                        platform.openClaw();
                        laser.reset();
                    }                
                }
            } else if(laser.isIdle()) {
                    useAir = ioUnitExport.pickupItem(null);
            }
        } else if(!ioUnitExport.isIdle()) useAir = ioUnitExport.pickupItem(null);
        else useAir = ioUnitImport.pickupItem(getRecipeList());

        return useAir;
    }

    private boolean canItemBeLasered(ItemStack item){
        for(AssemblyRecipe recipe : getRecipeList()) {
            if(isValidInput(recipe, item)) return true;
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

    }

    @Override
    public void readFromNBT(NBTTagCompound tag){

    }

    @Override
    public List<AssemblyRecipe> getRecipeList(){
        return AssemblyRecipe.laserRecipes;
    }

}
