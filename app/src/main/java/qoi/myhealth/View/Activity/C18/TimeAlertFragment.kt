package qoi.myhealth.View.Activity.C18

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_time_alert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import qoi.myhealth.Adapter.DeviceAdapter
import qoi.myhealth.Adapter.TimeAlertAdapter
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.Model.C18_AlarmTime
import qoi.myhealth.Ble.C18.Model.C18_DeviceSupportInfo
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.MainActivity

class TimeAlertFragment: Fragment(), BleDelegate {

    private var group:ViewGroup? = null
    private val bleManager: BleManager = BleManager
    private var device: C18Delegate? = null

    private var timeAdapter: TimeAlertAdapter? = null
    private var deviceListView: ListView? = null

    companion object {
        fun createInstance() : TimeAlertFragment  {
            val fragmentMain = TimeAlertFragment ()
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
        return inflater.inflate(qoi.myhealth.R.layout.fragment_time_alert, container, false)
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
        title.text = "アラーム"

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.setOnClickListener{
            maActivity!!.setNavi(R.id.device)
        }

        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.visibility = View.VISIBLE
        saveBtn.setText("追加")

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            maActivity!!.setNavi(R.id.device)
        }
    }

    private fun init(){
        device = bleManager.deviceDelegate as C18Delegate

        deviceListView = view!!.findViewById<View>(R.id.alert_listView) as ListView

        var alertList = arrayListOf<C18_AlarmTime>()
        // カスタムビューで存在するアラーム分追加する
        device!!.settingSelectAlarmTime(){bleResult, info ->
            GlobalScope.launch(Dispatchers.Main) {
                alertList = info!!.get(BleIdentificationKey.C18_Alarm_Setting) as ArrayList<C18_AlarmTime>
                timeAdapter = TimeAlertAdapter(group!!.context, alertList!!)
                deviceListView!!.adapter = timeAdapter
            }
        }
    }

    private fun setting(){
        // 編集
       alert_listView!!.setOnItemClickListener { parent, view, position, id ->
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, TimeAlertSettingFragment.createInstance(timeAdapter!!.getItem(position)!!))
                commit()
            }

            val alertSwitch = view!!.findViewById<View>(R.id.time_alert) as Switch
            alertSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                val data = timeAdapter!!.getItem(position)!!
                data.isOpen = isChecked
                println("switch"+position)
                //device!!.settingChangeAlarmTime(timeAdapter!!.getItem(position)!!,data){bleResult, info ->}
            }
        }

        // 新規
        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.setOnClickListener {
            activity!!.supportFragmentManager.beginTransaction().apply {
                replace(R.id.clActivityMain, TimeAlertSettingFragment.createInstance(null))
                commit()
            }
        }

    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }

}