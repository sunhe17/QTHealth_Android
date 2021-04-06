package qoi.myhealth

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.model.BleDevice
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.adapter.DeviceAdapter

class DeviceScanActivity : AppCompatActivity(), AdapterView.OnItemClickListener,
    BleDelegate {

    val bleManager: BleManager = BleManager
    var deviceList:MutableList<BleDevice>? = null
    var deviceAdapter:DeviceAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        bleManager.bleDelegate = this
        val deviceListView = findViewById<ListView>(R.id.devices_listView)
        deviceList = mutableListOf()
        deviceAdapter = DeviceAdapter(this, deviceList!!)
        deviceListView.adapter = deviceAdapter

        bleManager.startDeviceScaner { result ->
            println(result.device.name)
            val newScanedDevice = BleDevice(result.device,result.rssi)
            for (bleDevice in deviceList!!){
                if (newScanedDevice.device == bleDevice.device){
                    return@startDeviceScaner
                }
            }
            deviceList?.add(newScanedDevice)
            deviceAdapter?.notifyDataSetChanged()
        }

        deviceListView.setOnItemClickListener(this)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val selectedDevice:BluetoothDevice? = deviceAdapter?.getItem(p2)?.device
        if (selectedDevice != null){
            selectedDevice.connectGatt(this,true,bleManager.gattCallback)
            bleManager.stopDeviceScaner()
        }

    }

    override fun onBleDeviceConnection() {
        println("onBleDeviceConnection")
        println(ShareDataManager.getConnectionDeviceMac())
        finish()
    }

    override fun onBleDeviceDisConnection() {
    }
}