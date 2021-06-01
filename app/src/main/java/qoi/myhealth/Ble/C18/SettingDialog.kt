package qoi.myhealth.Ble.C18

import android.app.DatePickerDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import qoi.myhealth.Adapter.DeviceAdapter
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.model.BleDevice
import qoi.myhealth.Controller.Util.Progress
import qoi.myhealth.R


object SettingDialog {

    // 通常ダイアログ
    fun normalDialog(context: Context, title:String, contents:String, method:()->Unit){
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(contents)
            .setPositiveButton("はい") { dialog, which ->
                method()
            }
            .setNegativeButton("いいえ",null)
            .show()
    }

    // ラジオボタン形式のダイアログ
    fun RadioDialog(context: Context, deviceData:C18Delegate,  title:String, targetList:Array<String>, nowData:String, settingType:String, method1:(String,Int)->Unit) {

        var selectItem:Int = targetList.indexOf(nowData)

        AlertDialog.Builder(context)
            .setTitle(title)
            .setSingleChoiceItems(
                targetList,targetList.indexOf(nowData),{ dialog, which ->
                    selectItem = which
                })
            .setPositiveButton("確定") { dialog, which ->
                method1(targetList[selectItem],selectItem)
            }
            .setNegativeButton("キャンセル",null)
            .show()
    }

    // マルチラジオ形式ダイアログ
    fun maltiradioDialog(context: Context, title:String, targetList:Array<String>, initData:Array<Boolean>, checkData:Array<Boolean>, method1:(Array<Boolean>)->Unit){
        var boolList: Array<Boolean> = initData
        var transCheckData:BooleanArray = checkData.toBooleanArray()
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMultiChoiceItems(targetList,transCheckData){dialog, which, isChecked ->
                checkData[which] = isChecked
            }
            .setPositiveButton("確定") { dialog, which ->
                method1(checkData)
            }
            .setNegativeButton("キャンセル",null)
            .show()
    }

    // スキンカラー設定用ダイアログ
    fun makeViewDialog(context: Context,title: String,nowData:Int,method1:(Int,Int)->Unit){

        val inflater = LayoutInflater.from(context)
        val view:View = inflater.inflate(qoi.myhealth.R.layout.dialog_skin_color,null)
        val skinRadio = view.findViewById<RadioGroup>(qoi.myhealth.R.id.radio_skin)
        var selectItem:Int = nowData

        var checkID: Int = 0
        var resrcID: Int = 0
        when(nowData){
            0 -> { checkID = R.id.radio_white }
            1->{ checkID = R.id.radio_flesh }
            2->{ checkID = R.id.radio_light_brown }
            3->{ checkID = R.id.radio_brown }
            4->{ checkID = R.id.radio_dark_brown }
            5->{ checkID = R.id.radio_brack }
            else->{ checkID = R.id.radio_white }
        }
        skinRadio.check(checkID)

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setPositiveButton("確定") { dialog, which ->
                when(skinRadio.checkedRadioButtonId){
                    R.id.radio_white->{
                        selectItem = 0
                        resrcID = R.drawable.skin_white
                    }
                    R.id.radio_flesh->{
                        selectItem = 1
                        resrcID = R.drawable.skin_flesh}
                    R.id.radio_light_brown->{
                        selectItem = 2
                        resrcID = R.drawable.skin_light_brown
                    }
                    R.id.radio_brown->{
                        selectItem = 3
                        resrcID = R.drawable.skin_brown
                    }
                    R.id.radio_dark_brown->{
                        selectItem = 4
                        resrcID = R.drawable.skin_dark_brown
                    }
                    R.id.radio_brack->{
                        selectItem = 5
                        resrcID = R.drawable.skin_brack
                    }
                    else->{
                        selectItem = 0
                        resrcID = R.drawable.skin_white
                    }
                }
                method1(selectItem,resrcID)
            }
            .setNegativeButton("キャンセル",null)
            .show()
    }

    // picker形式ダイアログ
    fun pickerDialog(context: Context,title:String, targetList:Array<String>, nowData:String, method1:(Int)->Unit){
        var numberPicker: NumberPicker = NumberPicker(context)
        numberPicker.displayedValues = targetList
        numberPicker.minValue = 0
        numberPicker.maxValue = (targetList.size - 1)
        numberPicker.value = targetList.indexOf(nowData)

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(numberPicker)
            .setPositiveButton("確定") { dialog, which ->
                method1(numberPicker.value)
            }
            .setNegativeButton("キャンセル",null)
            .show()
    }

    // timepicker形式ダイアログ
    fun timePickerDialog(context: Context,title: String, start_time1: Array<Int>, end_time1: Array<Int> = arrayOf(), method1:(Array<Int>,Array<Int>)->Unit){
        val inflater = LayoutInflater.from(context)
        val view:View = inflater.inflate(qoi.myhealth.R.layout.layout_time_picker,null)

        var time_pier1 =   view.findViewById<TimePicker>(qoi.myhealth.R.id.timePicker_start)
        time_pier1.setIs24HourView(true)
        time_pier1.hour = start_time1[0]
        time_pier1.minute = start_time1[1]

        var time_pier2 =   view.findViewById<TimePicker>(qoi.myhealth.R.id.timePicker_end)
        if(end_time1.size > 0){
            time_pier2.setIs24HourView(true)
            time_pier2.hour = end_time1[0]
            time_pier2.minute = end_time1[1]
        }
        else{
            time_pier2.visibility = View.GONE
        }


        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setPositiveButton("確定") { dialog, which ->
                if(end_time1.size > 0){
                    method1(arrayOf(time_pier1.hour, time_pier1.minute), arrayOf(time_pier2.hour, time_pier2.minute))
                }
                else{
                    method1(arrayOf(time_pier1.hour, time_pier1.minute), arrayOf())
                }
            }
            .setNegativeButton("キャンセル",null)
            .show()
    }

    // 入力形式のダイアログ
    fun InputDialog(context: Context,title: String,nowData:String?, type:String = "String",method1:(String)->Unit){
        val myedit:EditText = EditText(context)
        when(type){
            "String"->""
            "Int" -> myedit.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            "Double" -> myedit.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        myedit.setText(nowData)
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(myedit)
            .setPositiveButton("確定") { dialog, which ->
                method1(myedit.text.toString())
            }
            .setNegativeButton("キャンセル",null)
            .show()
    }

    // カレンダー形式のダイアログ
    fun dataPickreDialog(context: Context,method1:(Int,Int,Int)->Unit){
        val datePickerDialog = DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener() { view, year, month, dayOfMonth->
                println("選択した日付は「${year}/${month + 1}/${dayOfMonth}」です")
                method1(year,(month + 1),dayOfMonth)
            },
            2020,
            3,
            1)
        datePickerDialog.show()
    }

}

class DeviceConnectData constructor(_ble:BleManager,_context: Context):AdapterView.OnItemClickListener{

    var deviceAdapter:DeviceAdapter? = null
    var bleManager: BleManager? = _ble
    var context: Context = _context
    var alert: AlertDialog? = null

    // デバイスを接続一覧表示ダイアログ
    fun deviceConnectDialog(title: String){
        val inflater = LayoutInflater.from(context)
        val view:View = inflater.inflate(qoi.myhealth.R.layout.fragment_device_scan,null)

        var deviceList:MutableList<BleDevice>? = null

        deviceList = mutableListOf()
        deviceAdapter = DeviceAdapter(context, deviceList!!)
        val deviceListView = view.findViewById<ListView>(R.id.devices_listView)
        deviceAdapter = DeviceAdapter(context, deviceList!!)
        deviceListView.adapter = deviceAdapter

        bleManager!!.startDeviceScaner { result ->
            println(result.device.name)
            val newScanedDevice = BleDevice(result.device,result.rssi)
            for (bleDevice in deviceList!!){
                if (newScanedDevice.device == bleDevice.device){
                    return@startDeviceScaner
                }
            }
            deviceList?.add(newScanedDevice)
            deviceAdapter?.notifyDataSetChanged()
            deviceListView.onItemClickListener = this

            // 接続強度順に並び替え
            deviceList!!.sortBy { it.device_rssi * -1 }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setView(view)
        builder.setNegativeButton("キャンセル"){dialog, which ->
            bleManager!!.stopDeviceScaner()
        }
        builder.setOnDismissListener(){
            bleManager!!.stopDeviceScaner()
        }
        alert = builder.create()
        alert!!.show()

    }

    fun closeDialog(){
        bleManager!!.stopDeviceScaner()
        alert!!.dismiss()
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val selectedDevice:BluetoothDevice? = deviceAdapter?.getItem(p2)?.device
        if (selectedDevice != null){
            selectedDevice.connectGatt(context,true,bleManager!!.gattCallback)
            bleManager!!.stopDeviceScaner()

            Progress.getInstance().showDialog(context,"接続中")
        }
    }

}
