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
    private static final boolean D = true;

    private OnBLEManageFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Intent request codes
    //rework, cause we cant use the secure channel for multiple connection
    //private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    //private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    //private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    //types from BluetoothMSGService
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    //Key-Names / tmp Placeholder
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private TextView mtextHeader;

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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
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

        mtextHeader = (TextView) bleManagerView.findViewById(R.id.textHeader);

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
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new BLEDeviceAdapter(bleDevices.getConnectedDevices());
        mRecyclerView.setAdapter(mAdapter);

        return bleManagerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mMSGService == null) setupConversation(2);
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
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMSGService != null) mMSGService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }


    //public void onResume() {
    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        if (mMSGService != null) {
            if (mMSGService.getState() == BluetoothMsgService.STATE_NONE) {
                mMSGService.start();
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

        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);
        mConversationView.setAdapter(mConversationArrayAdapter);

        mOutEditText.setOnEditorActionListener(mWriteListener);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message); //TODO change for multiple Services
                }
            }
        });

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
        if(D) Log.d(TAG, "ensure discoverable");
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
        if (mMSGService.getState() != BluetoothMsgService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mMSGService.write(send);
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
        for(int i = 1; i < mMSGServices.length; i++){
            if (mMSGServices[i].getState() != BluetoothMsgService.STATE_CONNECTED) {
                Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                return;
            }

            if (message.length() > 0) {
                byte[] send = message.getBytes();
                mMSGServices[i].write(send);
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
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothMsgService.STATE_CONNECTED:
                            mtextHeader.setText(R.string.title_connected_to);
                            mtextHeader.append(mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothMsgService.STATE_CONNECTING:
                            mtextHeader.setText(R.string.title_connecting);
                            break;
                        case BluetoothMsgService.STATE_LISTEN:
                        case BluetoothMsgService.STATE_NONE:
                            mtextHeader.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.length() > 0) {
                        mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getActivity(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    //if (!msg.getData().getString(TOAST).contains("Unable to connect device")) {
                    Toast.makeText(getActivity(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    //}
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = "";
                    if(data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS) != null){
                        address = data.getExtras()
                                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    }
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mMSGService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupConversation(2);
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(),
                            R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
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
        //TODO: is this needed?
        String address = "";
        if(data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS) != null){
            address = data.getExtras()
                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        //reworked - there is no secure channel anymore
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
     * @param device a Bluetooth Low Energy Device
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
     * @param position Position of the Element in the view
     * @param list Used list of Bluetooth devices
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
