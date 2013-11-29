package pneumaticCraft.api.universalSensor;

import java.util.List;

import net.minecraft.world.World;

public interface IBlockAndCoordinatePollSensor{
    /**
     * See {@link ISensorSetting#getSensorPath()}
     * @return
     */
    public String getSensorPath();

    /**
     * See {@link ISensorSetting#needsTextBox()}
     * @return
     */
    public boolean needsTextBox();

    /**
     * See {@link ISensorSetting#getDescription()}
     * @return
     */
    public List<String> getDescription();

    /**
     * See {@link IPollSensorSetting#getRedstoneValue(World, int, int, int, int, String)} , but this has the GPS tracked coordinates
     * as extra parameters. This method will only be called when the coordinate is within the Universal Sensor's range.
     * @param world
     * @param x
     * @param y
     * @param z
     * @param sensorRange
     * @param textBoxText
     * @param toolX
     * @param toolY
     * @param toolZ
     * @return
     */
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText, int toolX, int toolY, int toolZ);

    /**
     * See {@link IPollSensorSetting#getPollFrequency()}
     * @return
     */
    public int getPollFrequency();

}
