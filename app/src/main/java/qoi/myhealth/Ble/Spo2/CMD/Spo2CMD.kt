package qoi.myhealth.Ble.Spo2.CMD

import qoi.myhealth.Ble.Spo2.Spo2Utils.checkSpo2Sum
import java.util.*


enum class Spo2_Read_Key(val vl: UByte){
    ReadFile(0x02u),
    ReadHeader(0x03u),
    ReadData(0x04u),
    TransferMode(0xF0u)
}

enum class ReadingState(){
    None,
    Header,
    Data
}

object Spo2CMD {

    fun setCurrDate() : ByteArray {
        var tDateBytes = ubyteArrayOf(0xF1u,0x00u)
        val calendar = Calendar.getInstance()
        var stdYear = calendar.get(Calendar.YEAR) % 2000
        tDateBytes += stdYear.toUByte()                                   //1byte
        tDateBytes += (calendar.get(Calendar.MONTH) + 1).toUByte()        //1byte
        tDateBytes += calendar.get(Calendar.DATE).toUByte()               //1byte
        tDateBytes += calendar.get(Calendar.HOUR_OF_DAY).toUByte()        //1byte
        tDateBytes += calendar.get(Calendar.MINUTE).toUByte()             //1byte
        tDateBytes += calendar.get(Calendar.SECOND).toUByte()             //1byte
        tDateBytes += calendar.get(Calendar.DAY_OF_WEEK).toUByte()        //1byte
        return tDateBytes.checkSpo2Sum().toByteArray()
    }

    fun enterDataTransFerMode() : ByteArray {
        val data = ubyteArrayOf(0xF0u,0x44u,0x55u,0x66u,0x77u)
        return  data.checkSpo2Sum().toByteArray()
    }

    fun exitDataTransferMode() : ByteArray {
        val data = ubyteArrayOf(0x09u,0x00u)
        return data.checkSpo2Sum().toByteArray()
    }

    fun readMode() : ByteArray{
        val data = ubyteArrayOf(0xF0u,0xFFu)
        return data.checkSpo2Sum().toByteArray()
    }

    fun readRTC() : ByteArray {
        val data = ubyteArrayOf(0xF1u,0xFFu)
        return data.checkSpo2Sum().toByteArray()
    }

    fun eraseAllFlashData() : ByteArray {
        val data = ubyteArrayOf(0x01u,0xFFu)
        return data.checkSpo2Sum().toByteArray()
    }

    fun readNumOfFile() : ByteArray {
        val data = ubyteArrayOf(0x02u,0xFFu)
        return data.checkSpo2Sum().toByteArray()
    }

    fun rowDataMode() : ByteArray {
        val data = ubyteArrayOf(0xF6u,0x01u)
        return data.checkSpo2Sum().toByteArray()
    }

    fun readFileHeader(index:UByte) : ByteArray {
        val data = ubyteArrayOf(0x03u,index)
        return data.checkSpo2Sum().toByteArray()
    }

    fun readFileData(startAddress:UByteArray,endAddress:UByteArray) : ByteArray {
        var data = ubyteArrayOf(0x04u)
        data += startAddress
        data += endAddress
        data += 0x14u
        return data.checkSpo2Sum().toByteArray()
    }
}