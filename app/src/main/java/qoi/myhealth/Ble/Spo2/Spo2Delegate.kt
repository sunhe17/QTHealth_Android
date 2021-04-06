package qoi.myhealth.Ble.Spo2

import android.bluetooth.BluetoothGatt
import qoi.myhealth.Ble.DeviceDelegate
import qoi.myhealth.Ble.Extension.get3Byte
import qoi.myhealth.Ble.SendCallBack
import qoi.myhealth.Ble.ServiceUUID
import qoi.myhealth.Ble.Spo2.CMD.ReadingState
import qoi.myhealth.Ble.Spo2.CMD.Spo2CMD
import qoi.myhealth.Ble.Spo2.CMD.Spo2_Read_Key
import qoi.myhealth.Ble.Spo2.Spo2Utils.records
import qoi.myhealth.Ble.Spo2.Spo2Utils.removedDuplicate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.concurrent.timerTask

class Spo2Delegate(mGatt: BluetoothGatt) :
    DeviceDelegate(mGatt) {

    private var transferCallback:((Boolean) -> Unit)? = null
    private var receivedTransferCallback:((Boolean,Array<UByteArray>) -> Unit)? = null
    private var state: ReadingState = ReadingState.None
    private var address:Array<UByteArray?>? = null
    private var read_header_count = 1
    private var transfered:MutableList<Array<UByteArray>>? = null
    private var tempData:MutableList<UByteArray> = mutableListOf<UByteArray>()

    override fun recvDataUnpacket(recvData: ByteArray) {
        val buffData = ByteBuffer.wrap(recvData)
        val tReadType = buffData.get(0).toUByte()

        when (tReadType) {
            Spo2_Read_Key.ReadFile.vl -> {
                if (state != ReadingState.None) {return}
                val num = buffData.get(1).toInt()
                var address_count = 0
                if (num == 255) {
                    address_count = 0
                }else {
                    address_count = num
                }
                address = Array<UByteArray?>(address_count + 1){ null }
                address!![address_count] = recvData.toUByteArray()
                transfered = MutableList(0){ Array(0){ ubyteArrayOf()} }

                if (num <= 0 || num >= 255) {
                    if (receivedTransferCallback != null) {
                        completeReadTransferData()
                    }
                    return
                }

                state = ReadingState.Header
                readFileHeader(read_header_count++)
            }

            Spo2_Read_Key.ReadHeader.vl -> {
                if (state != ReadingState.Header) {return}
                address?.set(buffData.get(1).toInt() - 1,recvData.toUByteArray())
                if (address == null || address!!.contains(null)){
                    readFileHeader(read_header_count++)
                    return
                }
                state = ReadingState.Data
                readTransferData(address!!)
            }

            Spo2_Read_Key.ReadData.vl -> {
                if (state != ReadingState.Data && transfered == null){return}
                if (buffData.get(1).toInt() == -1 && buffData.get(2).toInt() == 0x55){
                    val expected = expectedRecords(address!!)
                    if (tempData.size <= expected) {
                        transfered!!.add(if (tempData.size > 0) tempData.toTypedArray().records() else arrayOf())
                    }else {
                        transfered!!.add(tempData.toTypedArray().removedDuplicate(tempData.size - expected).records())
                    }
                    tempData.clear()
                    readTransferData(address!!)
                }else {
                    tempData.add(recvData.toUByteArray())
                }
            }

            Spo2_Read_Key.TransferMode.vl -> {
                val result = buffData.get(1).toInt() == 0x0B
                transferCallback?.invoke(result)
                transferCallback = null
            }
        }
    }

    override fun deviceInit(sendCallback: SendCallBack?) {
        this.synchronizeTimeToPeripheral()
    }

    override fun syncHealthData(sendCallback: SendCallBack?) {
        enterTransferModeThenReadData { b, arrayOfUByteArrays ->  }
    }

    private fun sendData(data:ByteArray){
            var wChar = mGatt!!.getService(ServiceUUID.SPO2_SERVICE.uuid).getCharacteristic(
                ServiceUUID.SPO2_kWrite2.uuid)
            val buffData = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            wChar.setValue(buffData.array())
            val result = mGatt!!.writeCharacteristic(wChar)
            println("sendDataToDev = $result")
    }

    private fun synchronizeTimeToPeripheral(){
        if (mGatt == null) {return}
        val tCurrDateBytes = Spo2CMD.setCurrDate()
        this.sendData(tCurrDateBytes)
    }

    private fun enterTransferModeThenReadData(complete: (Boolean, Array<UByteArray>) -> Unit){
        if (mGatt == null) {return}
        this.sendData(Spo2CMD.enterDataTransFerMode())

        Timer().schedule(timerTask {
            checkTransferMode { isTransferMode ->
                if (isTransferMode) {
                    startReadTransferData(complete)
                }else {

                }
            }
        },1000)
    }

    private fun startReadTransferData(complete:(Boolean,Array<UByteArray>) -> Unit) {
        receivedTransferCallback = complete
        val cmdData = Spo2CMD.readNumOfFile()
        this.sendData(cmdData)
    }

    private fun checkTransferMode(comletion:(Boolean) -> Unit) {
        if (mGatt == null) {comletion(false);return}
        transferCallback = comletion
        val cmdData = Spo2CMD.readMode()
        this.sendData(cmdData)
    }

    private fun readFileHeader(at:Int){
        val cmdData = Spo2CMD.readFileHeader(at.toUByte())
        this.sendData(cmdData)
    }

    private fun readTransferData(data:Array<UByteArray?>) {
        val num = transfered?.size ?: return
        if (num >= address!!.size - 1){
            completeReadTransferData()
            return
        }
        var start = num
        for (i in num until data.size - 1) {
            if (data[i]!!.get(2).toInt() != -1){
                start = i
                break
            }
            transfered?.add(arrayOf())
        }
        readContinueData(data[start]!!.copyOfRange(2,5), data[start+1]!!.copyOfRange(2,5))
    }

    private fun readContinueData(startAddr:UByteArray,endAddr:UByteArray){
        val cmdData = Spo2CMD.readFileData(startAddr,endAddr)
        this.sendData(cmdData)
    }

    private fun expectedRecords(data: Array<UByteArray?>) : Int {
        val num = transfered?.size ?: return 0
        if (num >= address!!.size -1) {return 0}
        val start = ByteBuffer.wrap(data[num]!!.toByteArray()).get3Byte(2)
        val end = ByteBuffer.wrap(data[num + 1]!!.toByteArray()).get3Byte(2)
        return (end - start) / 16
    }

    private fun completeReadTransferData(){

    }
}