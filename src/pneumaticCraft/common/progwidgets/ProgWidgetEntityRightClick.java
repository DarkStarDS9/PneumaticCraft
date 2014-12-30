package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.ai.DroneEntityBase;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class ProgWidgetEntityRightClick extends ProgWidget{

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "entityRightClick";
    }

    @Override
    public String getGuiTabText(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getGuiTabColor(){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.ACTION;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ENTITY_RIGHT_CLICK;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneEntityBase<IProgWidget, EntityLivingBase>(drone, drone.getSpeed(), widget){
            private final List<Entity> visitedEntities = new ArrayList<Entity>();

            @Override
            protected boolean isEntityValid(Entity entity){
                return entity instanceof EntityLivingBase && !visitedEntities.contains(entity);
            }

            @Override
            protected boolean doAction(){
                visitedEntities.add(targetedEntity);
                boolean activated = false;
                ItemStack stack = drone.getInventory().getStackInSlot(0);
                if(stack != null && stack.getItem().itemInteractionForEntity(stack, drone.getFakePlayer(), targetedEntity)) {
                    activated = true;
                }
                if(!activated && targetedEntity instanceof EntityAgeable && ((EntityAgeable)targetedEntity).interact(drone.getFakePlayer())) {
                    activated = true;
                }
                for(int j = 1; j < drone.getFakePlayer().inventory.mainInventory.length; j++) {
                    ItemStack excessStack = drone.getFakePlayer().inventory.mainInventory[j];
                    if(excessStack != null) {
                        ItemStack remainder = PneumaticCraftUtils.exportStackToInventory(drone.getInventory(), excessStack, ForgeDirection.UNKNOWN);
                        if(remainder != null) {
                            drone.entityDropItem(remainder, 0);
                        }
                        drone.getFakePlayer().inventory.mainInventory[j] = null;
                    }
                }
                return false;//return activated; <-- will right click as long as it's sucessfully activated.
            }

        };
    }
}
