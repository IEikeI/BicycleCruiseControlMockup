/**
 * Based on https://github.com/polyclef/BluetoothChatMulti/tree/master/BluetoothChatMulti and modified for its purpose.
 */

package pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.BluetoothConnection;

/**
 * Defines several constants used between {@link BluetoothMsgService} and the UI.
 * Furthermore between the app and the handheld device.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


    // Key names for data transfer and emulation
    public static final String COMMAND_PING = "#ping_from_"; //+ device name
    public static final String COMMAND_SPEED = "#speed_value_"; //+ double value
    public static final String COMMAND_HEARTRATE = "#hr_value_"; //+ double value
    public static final String COMMAND_HEARTRATE_WARNING = "#hr_warn";

}
