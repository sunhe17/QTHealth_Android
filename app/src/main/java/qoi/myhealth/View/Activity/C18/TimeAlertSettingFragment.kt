package qoi.myhealth.View.Activity.C18

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import qoi.myhealth.Adapter.TimeAlertAdapter
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime_Type
import qoi.myhealth.Ble.C18.SettingDialog
import qoi.myhealth.Ble.C18.SettingDialog.timePickerDialog
import qoi.myhealth.Controller.Util.AppStatusCheck
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.MainActivity

class TimeAlertSettingFragment: Fragment(), BleDelegate {

    private var group:ViewGroup? = null
    private val bleManager: BleManager = BleManager
    private var device: C18Delegate? = null
    private val weekNameList = arrayOf("月曜日","火曜日","水曜日","木曜日","金曜日","土曜日","日曜日")

    private var alertData: C18_AlarmTime? = null
    private var addFlg: Boolean = false
    private var editAlert: C18_AlarmTime? = null

    private var timeText: TextView? = null
    private var weekText: TextView? = null

    companion object {
        fun createInstance(data: C18_AlarmTime?) : TimeAlertSettingFragment {
            val fragmentMain =  TimeAlertSettingFragment()
            val args = Bundle()
            args.putParcelable("data",data)
            fragmentMain.arguments = args
            return fragmentMain
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        group = container
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(qoi.myhealth.R.layout.fragment_time_alert_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bleManager.bleDelegate = this
        setToolBar()
        init()
        setting()
    }

    private fun setToolBar(){
        val maActivity = activity as MainActivity?

        maActivity?.getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        maActivity?.getSupportActionBar()?.setCustomView(R.layout.my_toolbar)

        val title = activity!!.findViewById<View>(qoi.myhealth.R.id.head_title) as TextView
        title.text = "アラーム時間設定"

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.setOnClickListener{
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, TimeAlertFragment.createInstance())
                commit()
            }
        }

        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.visibility = View.VISIBLE
        saveBtn.setText("保存")

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, TimeAlertFragment.createInstance())
                commit()
            }
        }
    }

    private fun init(){
        device = bleManager.deviceDelegate as C18Delegate

        alertData  = arguments!!.getParcelable("data")
        if(alertData == null){
            addFlg = true
            // 新規追加データ
            alertData = C18_AlarmTime(C18_AlarmTime_Type.WakeUp,0.toUByte(),0.toUByte(),31.toUByte())
        }
        else{
            editAlert = C18_AlarmTime(C18_AlarmTime_Type.WakeUp,alertData!!.hour,alertData!!.minute,alertData!!.repeater)
        }

        weekText = view!!.findViewById<View>(R.id.weekLoopVal) as TextView
        weekText!!.text = AppStatusCheck.checkWeek(alertData!!.week!!)

        timeInit(alertData!!)
    }

    private fun setting(){
        val timeView = view!!.findViewById<View>(R.id.timelayout) as RelativeLayout
        timeView.setOnClickListener {
            timePickerDialog(group!!.context,"時間設定", arrayOf(alertData!!.hour.toInt(),alertData!!.minute.toInt()) ){time1,time2->
                if(!addFlg){
                    editAlert!!.hour = time1[0].toUByte()
                    editAlert!!.minute = time1[1].toUByte()
                    timeInit(editAlert!!)
                }
                else{
                    alertData!!.hour = time1[0].toUByte()
                    alertData!!.minute = time1[1].toUByte()
                    timeInit(alertData!!)
                }
            }
        }

        val weekView = view!!.findViewById<View>(R.id.weekLoop) as RelativeLayout
        weekView.setOnClickListener {
            SettingDialog.maltiradioDialog(
                group!!.context,
                "繰り返し",
                weekNameList,
                arrayOf(false, false, false, false, false, false, false),
                alertData!!.week!!
            ) { setData ->
                GlobalScope.launch(Dispatchers.Main) {
                    if(!addFlg){
                        editAlert!!.setWeekData(setData)
                    }
                    else{
                        alertData!!.setWeekData(setData)
                    }
                    weekText!!.text = AppStatusCheck.checkWeek(setData)
                }
            }
        }

        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.setOnClickListener {
            if(!addFlg){
                editTime()
            }
            else{
                addTime()
            }
        }
    }

    private fun editTime(){
        device!!.settingChangeAlarmTime(alertData!!,editAlert!!){bleResult, info ->
            GlobalScope.launch(Dispatchers.Main) {
                activity!!.supportFragmentManager.beginTransaction().apply {
                    replace(R.id.clActivityMain, TimeAlertFragment.createInstance())
                    commit()
                }
            }
        }
    }

    private fun addTime(){
        device!!.settingAddAlarmTime(alertData!!){bleResult, info ->
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, TimeAlertFragment.createInstance())
                commit()
            }
        }

    }

    private fun timeInit(timeData:C18_AlarmTime){
        val timeData: String = "" + "%02d".format(timeData.hour.toInt()) + ":" + "%02d".format(timeData.minute.toInt())
        timeText = view!!.findViewById<View>(R.id.time) as TextView
        timeText!!.text = timeData
    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }

}