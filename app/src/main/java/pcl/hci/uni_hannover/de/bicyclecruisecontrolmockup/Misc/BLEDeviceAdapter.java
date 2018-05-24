package pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Misc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.Model.BLEDevice;
import pcl.hci.uni_hannover.de.bicyclecruisecontrolmockup.R;

/**
 * Costume adapter for displaying BLE devices in the recycler view
 */
public class BLEDeviceAdapter extends RecyclerView.Adapter<BLEDeviceAdapter.ViewHolder> {
    private ArrayList<BLEDevice> mConnectedDevices;

    /**
     * Provide a reference to the views for each data item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewID;
        public TextView mTextViewName;
        public TextView mTextViewUUID;
        public ViewHolder(View v) {
            super(v);
            mTextViewID = v.findViewById(R.id.textViewId);
            mTextViewName = v.findViewById(R.id.textViewName);
            mTextViewUUID = v.findViewById(R.id.textViewUUID);
        }
    }

    public BLEDeviceAdapter(ArrayList<BLEDevice> dataset) {
        mConnectedDevices = dataset;
    }

    /**
     *  Create new views (invoked by the layout manager)
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public BLEDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        //create a new view layout out of xml
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_ble_device, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    /**
     * Binds given ui-elements to the set viewHolder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mConnectedDevices.get(0) != null){
            holder.mTextViewName.setText(mConnectedDevices.get(position).getName());
            holder.mTextViewID.setText(String.format("%d", mConnectedDevices.get(position).getDeviceId()));
            holder.mTextViewUUID.setText(mConnectedDevices.get(position).getUUID());
        } else {
            holder.mTextViewName.setText(R.string.no_device);
            holder.mTextViewID.setText(R.string.no_device);
            holder.mTextViewUUID.setText(R.string.no_device);
        }
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     * @return
     */
    @Override
    public int getItemCount() {
        return mConnectedDevices.size();
    }
}
