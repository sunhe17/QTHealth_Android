package qoi.myhealth.Ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.Extension.toHexString
import qoi.myhealth.Ble.Spo2.Spo2Delegate
import qoi.myhealth.Manager.ShareDataManager

object BleManager {
    private val TAG = this::class.java.simpleName
    private val bluetoothAdapter:BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeScanner:BluetoothLeScanner? = null
    private var scanCallback:ScanCallback? = null

    private var writeChar:BluetoothGattCharacteristic? = null
    private var mGatt:BluetoothGatt? = null

    var deviceDelegate:DeviceDelegate? = null
    var bleDelegate: BleDelegate? = null

    val gattCallback : BluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            when (newState){
                BluetoothGatt.STATE_CONNECTED -> {
                    println("STATE_CONNECTED")
                    mGatt = gatt
                    gatt?.discoverServices()
                }
                BluetoothGatt.STATE_CONNECTING -> {
                    println("STATE_CONNECTING")
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    println("STATE_DISCONNECTED")
                    deviceDelegate = null
                    bleDelegate?.onBleDeviceDisConnection()
                }
                BluetoothGatt.STATE_DISCONNECTING -> {
                    println("STATE_DISCONNECTING")
                }
                else -> {
                    println(newState)
                }
            }
            println("onConnectionStateChange")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                return
            }
            for (tService in mGatt!!.services){
                println("Service : " + tService.uuid)
                for (char in tService.characteristics){
                    println("Char : ${char.uuid}")
                }
                if (tService.uuid == ServiceUUID.C18_SERVICE.uuid){
                    Log.d(TAG,"C18???????????????????????????")
                    val readChar = tService.getCharacteristic(ServiceUUID.C18_kRead.uuid)
                    mGatt!!.setCharacteristicNotification(readChar,true)
                    val rDescriptor = readChar.getDescriptor(ServiceUUID.Public_Descriptor.uuid)
                    rDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    rDescriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    mGatt!!.writeDescriptor(rDescriptor)
                    writeChar = tService.getCharacteristic(ServiceUUID.C18_kWrite1.uuid)
                    mGatt!!.setCharacteristicNotification(writeChar,true)
                }else if (tService.uuid == ServiceUUID.SPO2_SERVICE.uuid) {
                    Log.d(TAG,"SPO2???????????????????????????")
                    val readChar = tService.getCharacteristic(ServiceUUID.SPO2_kRead1.uuid)
                    mGatt!!.setCharacteristicNotification(readChar,true)
                    val rDescriptor = readChar.getDescriptor(ServiceUUID.Public_Descriptor.uuid)
                    rDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    mGatt!!.writeDescriptor(rDescriptor)
                    println()
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d(TAG,"onDescriptorWrite")

            when (descriptor?.characteristic?.uuid) {
                ServiceUUID.C18_kRead.uuid -> {
                    val wDescriptor = writeChar!!.getDescriptor(ServiceUUID.Public_Descriptor.uuid)
                    wDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    wDescriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    mGatt!!.writeDescriptor(wDescriptor)
                    return
                }

                ServiceUUID.C18_kWrite1.uuid -> {
                    ShareDataManager.saveConnectedDeviceMac(mGatt!!.device.address)
                    deviceDelegate = C18Delegate(mGatt!!)
                    bleDelegate?.onBleDeviceConnection()
                    return
                }

                ServiceUUID.SPO2_kRead1.uuid -> {
                    val pulseService = mGatt!!.getService(ServiceUUID.SPO2_PULSE.uuid)
                    val kRead2Char = pulseService.getCharacteristic(ServiceUUID.SPO2_kRead2.uuid)
                    val setNotifi = mGatt!!.setCharacteristicNotification(kRead2Char,true)
                    val kRead2Des = kRead2Char.getDescriptor(ServiceUUID.Public_Descriptor.uuid)
                    kRead2Des.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    val writeDesc = mGatt!!.writeDescriptor(kRead2Des)
                    println("setNotifi is ${setNotifi}")
                    println("writeDesc is ${writeDesc}")
                    return
                }

                ServiceUUID.SPO2_kRead2.uuid -> {
                    ShareDataManager.saveConnectedDeviceMac(mGatt!!.device.address)
                    deviceDelegate = Spo2Delegate(mGatt!!)
                    deviceDelegate!!.deviceInit(null)
                    bleDelegate?.onBleDeviceConnection()

                }

            }

        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(TAG,"onCharacteristicChanged From ${characteristic!!.uuid}")
            Log.d("???????????????????????????",characteristic!!.value.toHexString())
            println("???????????????????????????${characteristic!!.value.toHexString()}")
            deviceDelegate?.recvDataUnpacket(characteristic.value)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d(TAG,"onCharacteristicWrite")

            Log.d("???????????????????????????",characteristic!!.value.toHexString())
            if (status == BluetoothGatt.GATT_SUCCESS){
                println("????????????")
            }else{
                print("????????????")
            }
        }
    }


     fun startDeviceScaner(completion:(ScanResult) -> Unit){
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            scanCallback = object :ScanCallback(){
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    if (result != null && result.device != null){
                        completion(result)
                    }
                }
            }
        val scanFilterC18 = ScanFilter.Builder().setServiceUuid(ParcelUuid(ServiceUUID.C18_PULSE.uuid)).build()
        val scanFilterSpo2 = ScanFilter.Builder().setServiceUuid(ParcelUuid(ServiceUUID.SPO2_PULSE.uuid)).build()
        var scanFilters:MutableList<ScanFilter> = mutableListOf(scanFilterC18,scanFilterSpo2)
        val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothLeScanner?.startScan(scanFilters,scanSettings, scanCallback)
    }

     fun stopDeviceScaner(){
        if (scanCallback != null){
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    fun retrieveDevice(context: Context, var1:String){

        val device = bluetoothAdapter!!.getRemoteDevice(var1)
        device.connectGatt(context,true,gattCallback)



    }

}