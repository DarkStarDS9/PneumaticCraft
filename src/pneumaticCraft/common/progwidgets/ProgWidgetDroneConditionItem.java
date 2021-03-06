package pneumaticCraft.common.progwidgets;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetDroneConditionItem extends ProgWidgetDroneEvaluation{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "droneConditionItem";
    }

    @Override
    protected int getCount(EntityDrone drone, IProgWidget widget){
        int count = 0;
        for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
            ItemStack droneStack = drone.getInventory().getStackInSlot(i);

            if(droneStack != null && ((ProgWidgetAreaItemBase)widget).isItemValidForFilters(droneStack)) {
                count += droneStack.stackSize;
            }
        }
        return count;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_DRONE_ITEM_INVENTORY;
    }

}
