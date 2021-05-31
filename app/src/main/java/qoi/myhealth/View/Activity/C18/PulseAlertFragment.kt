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
import qoi.myhealth.Ble.C18.SettingDialog.pickerDialog
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.MainActivity

class PulseAlertFragment : Fragment(), BleDelegate {

    private var group: ViewGroup? = null

    private val bleManager: BleManager = BleManager
    private var device: C18Delegate? = null

    private var pluseSwitch: Switch? = null
    private var pulseRate: TextView? = null

    companion object {
        fun createInstance() : PulseAlertFragment  {
            val fragmentMain = PulseAlertFragment ()
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
        return inflater.inflate(qoi.myhealth.R.layout.fragment_pulse_alert, container, false)
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
        title.text = "脈拍アラート設定"

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.setOnClickListener{
            maActivity!!.setNavi(R.id.device)
        }

        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.visibility = View.GONE

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            maActivity!!.setNavi(R.id.device)
        }
    }

    private fun init(){
        device = bleManager.deviceDelegate as C18Delegate

        val userSettingInfo = ShareDataManager.getUserSetInfo()

        // アラートスイッチ
        pluseSwitch = view!!.findViewById<View>(R.id.pulse_switch) as Switch
        pluseSwitch!!.isChecked = userSettingInfo!!.heartAlert_isOpen

        // アラート数値
        pulseRate = view!!.findViewById<View>(R.id.pulse_rate) as TextView
        pulseRate!!.text = userSettingInfo!!.heartAlert_value.toString()
    }

    private fun setting(){
        val userSettingInfo = ShareDataManager.getUserSetInfo()

        // アラートスイッチ
        pluseSwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            pluseSwitch!!.isChecked = isChecked
            device!!.settingHeartAlarm(isChecked,userSettingInfo!!.heartAlert_value, 0x30) { bleResult, info ->
            }
        }

        // アラート数値
        var pulseList: Array<String> = arrayOf()
        for(i in 10..24){
            val data = i * 10
            pulseList += data.toString()
        }
        val pulseRateView: LinearLayout = view!!.findViewById<View>(R.id.pulse_guideline) as LinearLayout
        pulseRateView.setOnClickListener {
            pickerDialog(group!!.context,"アラート脈拍通知目安",pulseList,userSettingInfo!!.heartAlert_value.toString()){setData->
                pulseRate!!.text = pulseList[setData]
                device!!.settingHeartAlarm( pluseSwitch!!.isChecked,pulseList[setData].toInt(), 0x30) { bleResult, info ->}
            }
        }
    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }

}