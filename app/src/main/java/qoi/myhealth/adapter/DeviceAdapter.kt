package qoi.myhealth.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.customview.R
import qoi.myhealth.Ble.model.BleDevice

class DeviceAdapter(context: Context,var deviceList:MutableList<BleDevice>) : ArrayAdapter<BleDevice>(context,0,deviceList){

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view:View = inflater.inflate(qoi.myhealth.R.layout.layout_device,null)
        val deviceName_tv = view.findViewById<TextView>(qoi.myhealth.R.id.device_name)
        val deviceRssi_tv = view.findViewById<TextView>(qoi.myhealth.R.id.device_rssi)

        val bleDevice = deviceList[position]
        deviceName_tv.text = bleDevice.device.name
        deviceRssi_tv.text = bleDevice.device_rssi.toString()

        return view

    }

    override fun getItem(position: Int): BleDevice? {
        return super.getItem(position)
    }


}