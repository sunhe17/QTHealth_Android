package qoi.myhealth.View.Activity.Setting

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_access_key.*
import kotlinx.android.synthetic.main.my_toolbar.*
import qoi.myhealth.Ble.model.BleDevice
import qoi.myhealth.Controller.Util.AppStatusCheck
import qoi.myhealth.R
import qoi.myhealth.Adapter.DeviceAdapter

class DeviceScanActivity :  AdapterView.OnItemClickListener,
    BaseActivity() {

    var deviceList:MutableList<BleDevice>? = null
    var deviceAdapter:DeviceAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        bleManager.bleDelegate = this

        toolbtn.visibility = View.VISIBLE
        backimg.visibility = View.VISIBLE
        head_title.text = "デバイス接続"
        toolbtn.text = "リフレッシュ"
        connectList()

        toolbtn.setOnClickListener {
            connectList()
        }

        skip.setOnClickListener {
            AppStatusCheck.setFirstFlg(this,false)
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun connectList(){

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

            // 接続強度順に並び替え
            deviceList!!.sortBy { it.device_rssi * -1 }
        }

        deviceListView.onItemClickListener = this
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val selectedDevice:BluetoothDevice? = deviceAdapter?.getItem(p2)?.device
        if (selectedDevice != null){
            selectedDevice.connectGatt(this,true,bleManager.gattCallback)
            bleManager.stopDeviceScaner()
        }

    }

    override fun onBleDeviceConnection() {
        //finish()
        AppStatusCheck.setFirstFlg(getApplicationContext(),false)
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBleDeviceDisConnection() {
    }

    // ページ遷移等が入った場合スキャンを中断する
    override fun onPause() {
        super.onPause()
        bleManager.stopDeviceScaner()
    }
}