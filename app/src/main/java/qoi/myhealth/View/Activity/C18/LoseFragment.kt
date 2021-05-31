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
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.Model.C18_DeviceSupportInfo
import qoi.myhealth.Ble.C18.Model.C18_SwitchStatusInfo
import qoi.myhealth.Ble.C18.Model.C18_UserSettingInfo
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.MainActivity

class LoseFragment: Fragment(), BleDelegate {

    private val bleManager: BleManager = BleManager
    private var loseSwitch: Switch? = null
    private var device: C18Delegate? = null

    companion object {
        fun createInstance() : LoseFragment {
            val fragmentMain = LoseFragment()
            return fragmentMain
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(qoi.myhealth.R.layout.fragmen_lose, container, false)
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
        title.text = "紛失防止設定"

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

        loseSwitch = view!!.findViewById<View>(R.id.lose_switch) as Switch
        val userSettingInfo = ShareDataManager.getUserSetInfo()
        loseSwitch!!.isChecked = userSettingInfo!!.loss_switch
    }

    private fun setting(){

        loseSwitch!!.setOnCheckedChangeListener{ buttonView, isChecked ->
            GlobalScope.launch(Dispatchers.Main) {
                println("変更"+isChecked)
                device!!.settingAntiLost(isChecked){bleResult, info ->}
            }
        }

        // 振動
        val vibBtn: Button = view!!.findViewById<View>(R.id.vibtn) as Button
        vibBtn.setOnClickListener { device!!.controlFindDev(){buttonView, isChecked -> } }

    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }
}