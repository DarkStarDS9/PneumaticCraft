package pneumaticCraft.common.thirdparty.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class WailaPlantHandler extends WailaCultivatedPlantHandler{

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        super.getWailaBody(itemStack, currenttip, accessor, config);
        int meta = accessor.getMetadata();
        boolean active = meta > 6;
        if(active) meta -= 7;
        currenttip.add(String.format("%s : %s", I18n.format("hud.msg.state"), I18n.format("hud.msg." + (active ? "active" : "inactive"))));
        return currenttip;
    }
}
