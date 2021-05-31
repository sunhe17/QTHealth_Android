package qoi.myhealth.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime_Type
import qoi.myhealth.Ble.model.BleDevice
import qoi.myhealth.Controller.Util.AppStatusCheck
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.MainActivity

class TimeAlertAdapter(context: Context, var deviceList:MutableList<C18_AlarmTime>) : ArrayAdapter<C18_AlarmTime>(context,0,deviceList),
    BleDelegate {
    private val inflater = LayoutInflater.from(context)

    private val bleManager: BleManager = BleManager
    private var device: C18Delegate? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        bleManager.bleDelegate = this
        device = bleManager.deviceDelegate as C18Delegate


        val view: View = inflater.inflate(qoi.myhealth.R.layout.layout_time_alert,null)

        val alertAdapter = view.findViewById<LinearLayout>(qoi.myhealth.R.id.alert_adapter)
        val alertTime = view.findViewById<TextView>(qoi.myhealth.R.id.time)
        val alertWeek = view.findViewById<TextView>(qoi.myhealth.R.id.repert)
        val alertSwitch    = view.findViewById<Switch>(qoi.myhealth.R.id.time_alert)

        val alertData = deviceList[position]
        alertTime.text = "" + "%02d".format(alertData!!.hour.toInt()) + ":" + "%02d".format(alertData!!.minute.toInt())
        alertSwitch.isChecked = alertData.isOpen
        alertWeek.text = AppStatusCheck.checkWeek(alertData.week!!)

        alertSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val data = C18_AlarmTime(C18_AlarmTime_Type.WakeUp,alertData!!.hour,alertData!!.minute,alertData!!.repeater)
            data.setIsOpen(isChecked)
            device!!.settingChangeAlarmTime(alertData,data){bleResult, info ->}
        }


        return view

    }

    override fun getItem(position: Int): C18_AlarmTime? {
        return super.getItem(position)
    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }

}