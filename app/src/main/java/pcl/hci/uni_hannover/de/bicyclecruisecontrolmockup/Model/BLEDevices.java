package pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Model;

import java.util.ArrayList;

/**
 * This class holds list of connected and discovered BLE devices
 */
public class BLEDevices {

    /**
     * From the discovery service
     */
    private ArrayList<BLEDevice> discoveredDevices;

    /**
     * Added and removed in the process (connection).
     */
    private ArrayList<BLEDevice> connectedDevices;

    /**
     * Primary usage
     */
    public BLEDevices() {
        this.discoveredDevices = new ArrayList<BLEDevice>();
    }

    /**
     * Optinal usage
     * @param discoveredDevices
     */
    public BLEDevices(ArrayList<BLEDevice> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }

    /**
     * Optional usage
     * @param discoveredDevices
     * @param connectedDevices
     */
    public BLEDevices(ArrayList<BLEDevice> discoveredDevices, ArrayList<BLEDevice> connectedDevices) {
        this.discoveredDevices = discoveredDevices;
        this.connectedDevices = connectedDevices;
    }

    /**
     * Works also for initialising
     * @param device
     */
    public void addConnectedDevice(BLEDevice device){
        if(connectedDevices != null){
            connectedDevices.add(device);
        } else {
            connectedDevices = new ArrayList<BLEDevice>();
            connectedDevices.add(device);
        }
    }

    /**
     * Triggered by discover Service. No 'remove' required.
     * @param devices
     */
    private void updateDiscovered(ArrayList devices){
        discoveredDevices = devices;
    }

    /**
     * Removes a device from the list
     * @param device
     */
    private void removeConnectedDevice(BLEDevice device){
      for(int i = 0; i < connectedDevices.size(); i++){
          if(connectedDevices.equals(device)){
              connectedDevices.remove(i);
          }
      }
    }

    /**
     * Raturns the number of all connected devices on the current device
     * @return
     */
    public int getCountConnectedDevices(){
        if(connectedDevices != null){
            return connectedDevices.size();
        } else
            return 0;
    }

    /**
     * Raturns the number of all discovered devices on the current device
     * @return
     */
    public int getCountDiscoveredDevices(){
        return discoveredDevices.size();
    }

    public ArrayList<BLEDevice> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void setDiscoveredDevices(ArrayList<BLEDevice> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }

    public ArrayList<BLEDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public void setConnectedDevices(ArrayList<BLEDevice> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }
}
