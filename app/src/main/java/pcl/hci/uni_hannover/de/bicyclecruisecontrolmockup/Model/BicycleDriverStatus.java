package pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Model;

/**
 * Model that holds the current status of the bicycle driver.
 */
public class BicycleDriverStatus {

    /**
     * A fixed threshold for a heart rate warning
     */
    private final double criticalHeartRate = 0.00; //TODO adjust me

    /**
     * Name of the driver
     */
    private String name; //only use this if we need it

    /**
     * Secondary BLE device: Smartphone
     */
    private BLEDevice smartPhone; //only use this if we need it

    /**
     * Primary BLE device: micro controller
     */
    private BLEDevice mc;

    /**
     * A number for sequencing the group members (bicycle drivers)
     */
    private int id; //only use this if we need id

    /**
     * Current value of drivers heart rate (simulated)
     */
    private double heartRate;

    /**
     * Current value of drivers speed (simulated)
     */
    private double speed;


    public BicycleDriverStatus(BLEDevice mc, int id, double heartRate, double speed) {
        this.mc = mc;
        this.id = id;
        this.heartRate = heartRate;
        this.speed = speed;
    }

    /**
     * Returns true if the current heart rate is above the set threshold
     * @return
     */
    public boolean isHeartRateCritikal(){
        if (this.heartRate > criticalHeartRate){
            return true;
        } else {
            return false;
        }
    }

    public double getCriticalHeartRate() {
        return criticalHeartRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BLEDevice getSmartPhone() {
        return this.smartPhone;
    }

    public void setSmartPhone(BLEDevice smartPhone) {
        this.smartPhone = smartPhone;
    }

    public BLEDevice getMC() {
        return this.mc;
    }

    public void setUUIDMC(BLEDevice mc) {
        this.mc = mc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
