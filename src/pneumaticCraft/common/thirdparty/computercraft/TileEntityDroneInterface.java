package pneumaticCraft.common.thirdparty.computercraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.ai.DroneAIManager.EntityAITaskEntry;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketShowArea;
import pneumaticCraft.common.network.PacketSpawnRing;
import pneumaticCraft.common.progwidgets.IBlockOrdered;
import pneumaticCraft.common.progwidgets.IBlockOrdered.EnumOrder;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetItemFilter;
import pneumaticCraft.common.progwidgets.ProgWidgetString;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.lib.Log;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileEntityDroneInterface extends TileEntity implements IPeripheral{

    private final List<IComputerAccess> attachedComputers = new ArrayList<IComputerAccess>();
    private final List<ILuaMethod> luaMethods = new ArrayList<ILuaMethod>();
    private EntityDrone drone;
    public float rotationYaw, rotationPitch = (float)Math.toRadians(-42);
    private final List<Integer> ringSendList = new ArrayList<Integer>();
    private int ringSendCooldown;
    private IProgWidget curAction;

    @Override
    public void updateEntity(){
        if(drone != null && drone.isDead) {
            setDrone(null);
        }
        if(drone != null) {
            if(worldObj.isRemote) {
                double dx = drone.posX - (xCoord + 0.5);
                double dy = drone.posY - (yCoord + 0.5);
                double dz = drone.posZ - (zCoord + 0.5);
                float f3 = MathHelper.sqrt_double(dx * dx + dz * dz);
                rotationYaw = (float)-Math.atan2(dx, dz);
                rotationPitch = (float)-Math.atan2(dy, f3);
            } else {
                if(ringSendCooldown > 0) ringSendCooldown--;
                if(ringSendList.size() > 0 && ringSendCooldown <= 0) {
                    ringSendCooldown = ringSendList.size() > 10 ? 1 : 5;
                    NetworkHandler.sendToAllAround(new PacketSpawnRing(xCoord + 0.5, yCoord + 0.8, zCoord + 0.5, drone, ringSendList.remove(0)), worldObj);
                }
            }
        }
    }

    @Override
    public Packet getDescriptionPacket(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("drone", drone != null ? drone.getEntityId() : -1);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
        Entity entity = worldObj.getEntityByID(pkt.func_148857_g().getInteger("drone"));
        drone = entity instanceof EntityDrone ? (EntityDrone)entity : null;
    }

    public TileEntityDroneInterface(){
        luaMethods.add(new LuaMethod("isConnectedToDrone"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    return new Object[]{drone != null};
                } else {
                    throw new LuaException("isConnectedToDrone doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getDronePressure"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    if(drone == null) throw new LuaException("There's no connected Drone!");
                    return new Object[]{(double)drone.getPressure(null)};
                } else {
                    throw new LuaException("getDronePressure doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("exitPiece"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    if(drone == null) throw new LuaException("There's no connected Drone!");
                    setDrone(null);//disconnect
                    return null;
                } else {
                    throw new LuaException("exitPiece doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getAllActions"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    List<String> actions = new ArrayList<String>();
                    for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
                        if(widget.getWidgetAI(new EntityDrone(worldObj), getWidget()) != null) {
                            actions.add(widget.getWidgetString());
                        }
                    }
                    return actions.toArray(new String[actions.size()]);
                } else {
                    throw new LuaException("getAllActions doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getDronePosition"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    return new Double[]{drone.posX, drone.posY, drone.posZ};
                } else {
                    throw new LuaException("getDronePosition doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setBlockOrder"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    String arg = (String)args[0];
                    for(EnumOrder order : IBlockOrdered.EnumOrder.values()) {
                        if(order.toString().equalsIgnoreCase(arg)) {
                            getWidget().setOrder(order);
                            return null;
                        }
                    }
                    throw new LuaException("No valid order. Valid arguments:  'closest', 'highToLow' or 'lowToHigh'!");
                } else {
                    throw new LuaException("setBlockOrder takes one argument, 'closest', 'highToLow' or 'lowToHigh'!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getAreaTypes"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    return getWidget().getAreaTypes();
                } else {
                    throw new LuaException("getAreaTypes doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addArea"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 3) {
                    getWidget().addArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue());
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else if(args.length == 7) {
                    getWidget().addArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue(), ((Double)args[3]).intValue(), ((Double)args[4]).intValue(), ((Double)args[5]).intValue(), (String)args[6]);
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else {
                    throw new LuaException("addArea either requires 3 arguments (x, y, z), or 7 (x1, y1, z1, x2, y2, z2, areaType)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("removeArea"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 3) {
                    getWidget().removeArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue());
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else if(args.length == 7) {
                    getWidget().removeArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue(), ((Double)args[3]).intValue(), ((Double)args[4]).intValue(), ((Double)args[5]).intValue(), (String)args[6]);
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else {
                    throw new LuaException("removeArea either requires 3 arguments (x, y, z), or 7 (x1, y1, z1, x2, y2, z2, areaType)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearArea"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getWidget().clearArea();
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else {
                    throw new LuaException("clearArea doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("showArea"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    NetworkHandler.sendToAllAround(new PacketShowArea(xCoord, yCoord, zCoord, getWidget().getArea()), worldObj);
                    return null;
                } else {
                    throw new LuaException("showArea doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("hideArea"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    NetworkHandler.sendToAllAround(new PacketShowArea(xCoord, yCoord, zCoord), worldObj);
                    return null;
                } else {
                    throw new LuaException("hideArea doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addWhitelistItemFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 6) {
                    getWidget().addWhitelistItemFilter((String)args[0], ((Double)args[1]).intValue(), (Boolean)args[2], (Boolean)args[3], (Boolean)args[4], (Boolean)args[5]);
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new LuaException("addWhitelistItemFilter takes 6 arguments (<string> item/block name, <number> item/block metadata, <bool> Use Metadata, <bool> Use NBT, <bool> Use Ore Dictionary, <bool> Use Mod Similarity)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addBlacklistItemFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 6) {
                    getWidget().addBlacklistItemFilter((String)args[0], ((Double)args[1]).intValue(), (Boolean)args[2], (Boolean)args[3], (Boolean)args[4], (Boolean)args[5]);
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new LuaException("addBlacklistItemFilter takes 6 arguments (<string> item/block name, <number> item/block metadata, <bool> Use Metadata, <bool> Use NBT, <bool> Use Ore Dictionary, <bool> Use Mod Similarity)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearWhitelistItemFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getWidget().clearItemWhitelist();
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new LuaException("clearWhitelistItemFilter doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearBlacklistItemFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getWidget().clearItemBlacklist();
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new LuaException("clearBlacklistItemFilter doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addWhitelistText"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().addWhitelistText((String)args[0]);
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new LuaException("addWhitelistText takes one argument (text)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addBlacklistText"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().addBlacklistText((String)args[0]);
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new LuaException("addBlacklistText takes one argument (text)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearWhitelistText"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getWidget().clearWhitelistText();
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new LuaException("clearWhitelistText doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearBlacklistText"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getWidget().clearBlacklistText();
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new LuaException("clearBlacklistText doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setSide"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 2) {
                    ForgeDirection dir = getDirForString((String)args[0]);
                    boolean[] sides = getWidget().getSides();
                    sides[dir.ordinal()] = (Boolean)args[1];//We don't need to set them afterwards, got a reference.
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("addSide takes two arguments (direction, <boolean> valid)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setSides"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 6) {
                    boolean[] sides = new boolean[6];
                    for(int i = 0; i < 6; i++) {
                        sides[i] = (Boolean)args[i];
                    }
                    getWidget().setSides(sides);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("setSides takes 6 arguments (6x boolean)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setEmittingRedstone"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().setEmittingRedstone(((Double)args[0]).intValue());
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("setEmittingRedstone takes 1 argument (redstone strength)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addWhitelistLiquidFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().addWhitelistLiquidFilter((String)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("addWhitelistLiquidFilter takes 1 argument (liquid name)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addBlacklistLiquidFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().addBlacklistLiquidFilter((String)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("addBlacklistLiquidFilter takes 1 argument (liquid name)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearWhitelistLiquidFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getWidget().clearLiquidWhitelist();
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("clearWhitelistLiquidFilter takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearBlacklistLiquidFilter"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getWidget().clearLiquidBlacklist();
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("clearBlacklistLiquidFilter takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setDropStraight"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().setDropStraight((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("setDropStraight takes 1 argument (boolean straight true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setUseCount"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().setUseCount((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("setUseCount takes 1 argument (boolean use count true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setCount"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().setCount(((Double)args[0]).intValue());
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("setCount takes 1 argument (count)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setIsAndFunction"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().setAndFunction((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("setIsAndFunction takes 1 argument (boolean is and function true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setOperator"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    getWidget().setOperator((String)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new LuaException("setOperator takes 1 argument (\">=\" or \"=\")!");
                }
            }
        });

        luaMethods.add(new LuaMethod("evaluateCondition"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    if(curAction instanceof ICondition) {
                        Boolean bool = ((ICondition)curAction).evaluate(drone);
                        Log.info(bool.toString());
                        return new Object[]{bool};
                    } else {
                        throw new LuaException("current action is not a condition! Action: " + (curAction != null ? curAction.getWidgetString() : "no action"));
                    }
                } else {
                    throw new LuaException("evaluateCondition takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setAction"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 1) {
                    String widgetName = (String)args[0];
                    for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
                        if(widget.getWidgetString().equalsIgnoreCase(widgetName)) {
                            EntityAIBase ai = widget.getWidgetAI(drone, getWidget());
                            if(ai == null) throw new LuaException("The parsed action is not a runnable action! Action: \"" + widget.getWidgetString() + "\".");
                            getAI().setAction(widget, ai);
                            getTargetAI().setAction(widget, widget.getWidgetTargetAI(drone, getWidget()));
                            messageToDrone(widget.getGuiTabColor());
                            curAction = widget;
                            return null;
                        }
                    }
                    throw new LuaException("No action with the name \"" + widgetName + "\"!");
                } else {
                    throw new LuaException("setAction takes one argument (action)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("abortAction"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    getAI().abortAction();
                    getTargetAI().abortAction();
                    messageToDrone(0xFFFFFFFF);
                    curAction = null;
                    return null;
                } else {
                    throw new LuaException("abortAction takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("isActionDone"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    return new Object[]{getAI().isActionDone()};
                } else {
                    throw new LuaException("isActionDone doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("forgetTarget"){
            @Override
            public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
                if(args.length == 0) {
                    if(drone == null) throw new LuaException("There's no connected Drone!");
                    drone.setAttackTarget(null);
                    return null;
                } else {
                    throw new LuaException("forgetTarget doesn't take any arguments!");
                }
            }
        });
    }

    @Override
    public String getType(){
        return "droneInterface";
    }

    @Override
    public String[] getMethodNames(){
        String[] methodNames = new String[luaMethods.size()];
        for(int i = 0; i < methodNames.length; i++) {
            methodNames[i] = luaMethods.get(i).getMethodName();
        }
        return methodNames;
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException{
        return luaMethods.get(method).call(computer, context, arguments);
    }

    @Override
    public void attach(IComputerAccess computer){
        attachedComputers.add(computer);
    }

    @Override
    public void detach(IComputerAccess computer){
        attachedComputers.remove(computer);
    }

    @Override
    public boolean equals(IPeripheral other){//TODO await documention on the method, so it can be correctly implemented.
        return equals((Object)other);
    }

    private void sendEvent(String name, Object... parms){
        for(IComputerAccess computer : attachedComputers) {
            computer.queueEvent(name, parms);
        }
    }

    public void setDrone(EntityDrone drone){
        this.drone = drone;
        sendEvent(drone != null ? "droneConnected" : "droneDisconnected");
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public EntityDrone getDrone(){
        return drone;
    }

    private ProgWidgetCC getWidget() throws LuaException{
        return getAI().getWidget();
    }

    private DroneAICC getAI() throws LuaException{
        if(drone != null) {
            for(EntityAITaskEntry task : drone.getRunningTasks()) {
                if(task.action instanceof DroneAICC) return (DroneAICC)task.action;
            }
        }
        setDrone(null);//set to null in case of the drone is connected, but for some reason isn't currently running the piece (shouldn't be possible).
        throw new LuaException("There's no connected Drone!");
    }

    private DroneAICC getTargetAI() throws LuaException{
        if(drone != null && drone.getRunningTargetAI() instanceof DroneAICC) {
            return (DroneAICC)drone.getRunningTargetAI();
        } else {
            setDrone(null);//set to null in case of the drone is connected, but for some reason isn't currently running the piece (shouldn't be possible).
            throw new LuaException("There's no connected Drone!");
        }
    }

    private void messageToDrone(Class<? extends IProgWidget> widget){
        messageToDrone(ItemDye.field_150922_c[ItemProgrammingPuzzle.getWidgetForClass(widget).getCraftingColorIndex()]);
    }

    private void messageToDrone(int color){
        ringSendList.add(color);
    }

}
