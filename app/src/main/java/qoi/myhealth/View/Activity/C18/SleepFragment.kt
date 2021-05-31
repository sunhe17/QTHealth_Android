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
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.SettingDialog
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.MainActivity

class SleepFragment: Fragment(), BleDelegate {

    private val bleManager: BleManager = BleManager
    private var device: C18Delegate? = null
    private val userSettingInfo = ShareDataManager.getUserSetInfo()
    private var group: ViewGroup? = null

    private var sleepSwitch: Switch? = null
    private var sleepStart: TextView? = null
    private var sleepEnd: TextView? = null

    companion object {
        fun createInstance() : SleepFragment  {
            val fragmentMain = SleepFragment ()
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
        return inflater.inflate(qoi.myhealth.R.layout.fragment_sleep, container, false)
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
        title.text = "お休みモード設定"

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.setOnClickListener{
            maActivity!!.setNavi(R.id.device)
        }

        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.visibility = View.VISIBLE
        saveBtn.setText("保存")

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            maActivity!!.setNavi(R.id.device)
        }
    }

    private fun init(){
        device = bleManager.deviceDelegate as C18Delegate

        // スリープツイッチ
        sleepSwitch = view!!.findViewById<View>(R.id.sleep_on) as Switch
        sleepSwitch!!.isChecked = userSettingInfo!!.sleepInfo!!.isOpen

        timeInit()
    }

    private fun setting(){
        // 時間設定
        val timeView = view!!.findViewById<View>(R.id.sleep) as LinearLayout
        timeView.setOnClickListener{
            SettingDialog.timePickerDialog(
                group!!.context,
                "時間設定",
                arrayOf(
                    userSettingInfo!!.sleepInfo!!.start_timeHour,
                    userSettingInfo!!.sleepInfo!!.start_timeMin
                ),
                arrayOf(
                    userSettingInfo!!.sleepInfo!!.end_timeHour,
                    userSettingInfo!!.sleepInfo!!.end_timeMin
                )
            ) { time1val, time2val ->
                GlobalScope.launch(Dispatchers.Main) {
                    userSettingInfo!!.sleepInfo!!.start_timeHour = time1val[0]
                    userSettingInfo!!.sleepInfo!!.start_timeMin = time1val[1]

                    userSettingInfo!!.sleepInfo!!.end_timeHour = time2val[0]
                    userSettingInfo!!.sleepInfo!!.end_timeMin = time2val[1]

                    timeInit()
                }
            }
        }

        // スイッチ
        sleepSwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            userSettingInfo!!.sleepInfo!!.isOpen = isChecked
        }

        // 保存
        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.setOnClickListener {
            device!!.settingSleep(userSettingInfo!!.sleepInfo!!){bleResult, info -> }
        }

    }

    private fun timeInit(){
        // 開始時間
        val time_start: String = "" + "%02d".format(userSettingInfo!!.sleepInfo!!.start_timeHour) + ":" + "%02d".format(userSettingInfo!!.sleepInfo!!.start_timeMin)
        sleepStart = view!!.findViewById<View>(R.id.start_time) as TextView
        sleepStart!!.text = time_start

        // 終了時間
        val time_end: String = "" + "%02d".format(userSettingInfo!!.sleepInfo!!.end_timeHour) + ":" + "%02d".format(userSettingInfo!!.sleepInfo!!.end_timeMin)
        sleepEnd = view!!.findViewById<View>(R.id.end_time) as TextView
        sleepEnd!!.text = time_end
    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }

}