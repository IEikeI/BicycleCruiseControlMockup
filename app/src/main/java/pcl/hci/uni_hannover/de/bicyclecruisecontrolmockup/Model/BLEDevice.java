package pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Model;

/**
 * This class holds information on an used BLE Device
 */
public class BLEDevice {

    /**
     * A name for the device - used for debugging. E.g. "SamGax7"
     */
    private String name;

    /**
     * BLE Address of the Device. Must be set!
     */
    private String UUID;

    /**
     * Should have the same id as the driver
     */
    private int deviceId;

    public BLEDevice(String name, String UUID, int deviceId) {
        this.name = name;
        this.UUID = UUID;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
}
