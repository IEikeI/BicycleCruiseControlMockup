/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Fragments;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.BluetoothConnection.BluetoothMsgService;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.BluetoothConnection.Constants;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.BluetoothConnection.DeviceListActivity;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Logger.Log;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Misc.BLEDeviceAdapter;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Model.BLEDevice;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Model.BLEDevices;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.R;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BLEManageFragment extends Fragment {

    private static final String TAG = "BLEManageFragment";
    private OnBLEManageFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the 'conversation' thread (two devices sending and receiving data)
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the message services (data connection)
     */
    private BluetoothMsgService mMSGService = null;

    /**
     * List of all services for each device
     */
    private BluetoothMsgService[] mMSGServices = null;

    /**
     * Name of the device the app is running on
     */
    private String deviceName;

    /**
     * All devices stored here
     */
    private BLEDevices bleDevices;

    /**
     * View for displaying all connected ble devices
     */
    private RecyclerView mRecyclerView;

    /**
     * Adapter for the recycler view content
     */
    private RecyclerView.Adapter mAdapter;

    /**
     * Manager for the recycler view appearance
     */
    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * This fragtment is the controller for the (GUI) BLE device manager.
     * From here you can connect all devices, see the available and send
     * "pings"
     */
    public BLEManageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatisticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BLEManageFragment newInstance(String param1, String param2) {
        BLEManageFragment fragment = new BLEManageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //get the device name
        deviceName = android.os.Build.MODEL;

        bleDevices = new BLEDevices(); //init of arrays
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View bleManagerView = inflater.inflate(R.layout.fragment_ble_manager, container, false);

        /*Button secureScanBtn = (Button) bleManagerView.findViewById(R.id.button_secure_scan);
        secureScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });*/

        Button insecureScanBtn = (Button) bleManagerView.findViewById(R.id.button_insecure_scan);
        insecureScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            }
        });

        /*Button discoverableBtn = (Button) bleManagerView.findViewById(R.id.button_discoverable);
        secureScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
            }
        });*/


        FloatingActionButton fab_emer = (FloatingActionButton) bleManagerView.findViewById(R.id.fab_emergency);
        fab_emer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = Constants.COMMAND_PING + deviceName;
                sendMessage(message); //TODO change for multiple services
                Snackbar.make(view, "Ping sendet from " + deviceName, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerView = (RecyclerView) bleManagerView.findViewById(R.id.recycler_view_connected);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager();
        //mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new BLEDeviceAdapter(bleDevices.getConnectedDevices());
        mRecyclerView.setAdapter(mAdapter);

        return bleManagerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupConversation() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mMSGServices == null){
            setupConversation(0);
        }

        /*else if (mMSGServices != null) {
            for(int i = 1; i < mMSGServices.length; i++) {
                if (mMSGServices[i] == null){
                    setupConversation(0, mMSGService);
                }
            }
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMSGServices != null) {
            for(int i = 1; i < mMSGServices.length; i++) {
                if (mMSGServices[i] != null) {
                    mMSGServices[i].stop();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMSGServices != null) {
            for(int i = 1; i < mMSGServices.length; i++) {
                // Performing this check in onResume() covers the case in which BT was
                // not enabled during onStart(), so we were paused to enable it...
                // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
                if (mMSGServices[i] != null) {
                    // Only if the state is STATE_NONE, do we know that we haven't started already
                    if (mMSGServices[i].getState() == BluetoothMsgService.STATE_NONE) {
                        // Start the Bluetooth chat services
                        mMSGServices[i].start();
                    }
                }
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
    }

    /**
     * Set up the UI and background operations for conversations.
     */
    private void setupConversation(int index){ //BluetoothMsgService msgService) {
        Log.d(TAG, "setupConversation()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message); //TODO change for multiple Services
                }
            }
        });

        // Initialize the BluetoothMSGService to perform bluetooth connections
        //msgService = new BluetoothMsgService(getActivity(), mHandler);
        mMSGService = new BluetoothMsgService(getActivity(), mHandler);
        mMSGServices = new BluetoothMsgService[10]; //fixed size for now
        //if(!mMSGServices[0].equals(mMSGService)){
          //  mMSGServices[0] = mMSGService; //index 0 is always the initial handshake
        //}
        //mMSGServices[index] = mMSGService;

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    public void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message){ //BluetoothMsgService msgService) {
        // Check that we're actually connected before trying anything
        if (mMSGService.getState() != BluetoothMsgService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mMSGService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * Broadcasts a message.
     *
     * @param message A string of text to send to all devices.
     */
    private void sendMessageToAll(String message) {
        //start from index 1, 0 is an placeHolder
        for(int i = 1; i < mMSGServices.length; i++){
            // Check that we're actually connected before trying anything
            if (mMSGServices[i].getState() != BluetoothMsgService.STATE_CONNECTED) {
                Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                return;
            }

            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mMSGServices[i].write(send);

                // Reset out string buffer to zero and clear the edit text field
                mOutStringBuffer.setLength(0);
                mOutEditText.setText(mOutStringBuffer);
            }
        }

    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                //sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("StringFormatInvalid")
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothMsgService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothMsgService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothMsgService.STATE_LISTEN:
                        case BluetoothMsgService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                    String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
                    String mac = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    int deviceId = 1;
                    if(bleDevices.getConnectedDevices() != null){
                        deviceId = bleDevices.getConnectedDevices().size() +1;
                    }
                    BLEDevice device = new BLEDevice(name, mac, deviceId);
                    //Snackbar.make(getView(), "Device Name: "+device.getName()+", Address:"+device.getUUID() + deviceName, Snackbar.LENGTH_LONG)
                      //      .setAction("Action", null).show();
                    addBLEDevice(device);
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.invalidate();
                    refreshRecView();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupConversation(0);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        //mMSGService.connect(device, secure);
        mMSGService.connect(device);
    }

    /**
     * Force-redraw to whole fragment
     * (In this state this is only a work-around)
     */
    private void refreshRecView(){
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }

    /**
     * Helper for notifying added BLE connections and Syncs the list-adding and recycler view
     * @param device
     */
    private void addBLEDevice(BLEDevice device){
        int insertIndex = 0;
        if(bleDevices.getConnectedDevices().size() < 1){
            bleDevices.addConnectedDevice(device);
            //removeFromRecView(insertIndex, bleDevices.getConnectedDevices());
            //Snackbar.make(getView(), "Device Name: "+device.getName()+" removed", Snackbar.LENGTH_LONG)
              //      .setAction("Action", null).show();
        } else {
            bleDevices.addConnectedDevice(device);
            insertIndex = bleDevices.getCountConnectedDevices();
            //Snackbar.make(getView(), "Device Name: "+device.getName()+" added", Snackbar.LENGTH_LONG)
              //   .setAction("Action", null).show();
        }
        mAdapter.notifyItemInserted(insertIndex);
    }

    /**
     * Helper for notifying removed BLE connections and update the view
     * @param position
     * @param list
     */
    private void removeFromRecView(int position, ArrayList<BLEDevice> list){
        if(list.get(position) != null){
            list.remove(position);
        }
        if(mRecyclerView.getChildCount() > 0){
            mRecyclerView.removeViewAt(position);
        }
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, list.size());
        mRecyclerView.invalidate();
        refreshRecView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBLEManageFragmentInteractionListener) {
            mListener = (OnBLEManageFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnBLEManageFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
