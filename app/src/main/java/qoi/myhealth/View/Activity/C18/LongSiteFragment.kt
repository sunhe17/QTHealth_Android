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
import qoi.myhealth.Ble.C18.Model.C18_LongSite
import qoi.myhealth.Ble.C18.Model.C18_UserSettingInfo
import qoi.myhealth.Ble.C18.SettingDialog
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.MainActivity

class LongSiteFragment: Fragment(), BleDelegate {

    companion object {
        fun createInstance() : LongSiteFragment {
            val fragmentMain = LongSiteFragment()
            return fragmentMain
        }
    }

    private var group:ViewGroup? = null

    private val bleManager: BleManager = BleManager

    private var userSetInfo: C18_UserSettingInfo? = null
    private var longsiteinfo: C18_LongSite? = null
    private var data: Boolean = false

    private var checkIntervalView: TextView? = null
    private var notificationSwitch: Switch? = null
    private var weekLoopView: TextView? = null
    private var toolBtn: Button? = null

    // 繰り返し
    private var weekList: Array<Boolean> = arrayOf()
    private var dayTypeList:Array<String> = arrayOf()

    // Fragmentで表示するViewを作成するメソッド
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // 先ほどのレイアウトをここでViewとして作成します
        group = container
        return inflater.inflate(qoi.myhealth.R.layout.fragment_long_site, container, false)
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
        title.text = "座り過ぎ通知"

        toolBtn = activity!!.findViewById<View>(R.id.toolbtn) as Button
        toolBtn!!.setText("保存")

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.setOnClickListener{
            maActivity!!.setNavi(R.id.device)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            maActivity!!.setNavi(R.id.device)
        }
    }

    private fun init(){
        // 座り過ぎデータ取得
        val settingInfo: C18Delegate = bleManager.deviceDelegate as C18Delegate

        checkIntervalView = view!!.findViewById<View>(R.id.checkIntervalContents) as TextView
        notificationSwitch = view!!.findViewById<View>(R.id.notificationVal) as Switch

        settingInfo.getUserSetInfo { bleResult, info ->
            GlobalScope.launch(Dispatchers.Main) {
                userSetInfo = info!!.get(BleIdentificationKey.C18_UserSetInfo) as C18_UserSettingInfo
                longsiteinfo = userSetInfo?.longSiteInfo

                // 通知スイッチ
                notificationSwitch!!.isChecked =longsiteinfo!!.getIsOpen()
                data = longsiteinfo!!.getIsOpen()

                // チェック間隔
                checkIntervalView!!.text = "" + longsiteinfo!!.interval + "Min"

                // 繰り返し
                checkWeek()

                // 時間確認
                checkTime()
            }
        }
    }

    // 曜日確認
    fun checkWeek(){

        weekLoopView = view!!.findViewById<View>(R.id.weekLoopVal) as TextView

        // 繰り返し
        weekList = longsiteinfo!!.getWeek()
        dayTypeList = longsiteinfo!!.getWeekList()

        when {
            // 平日か確認
            (!weekList[5]) and (!weekList[6]) and ( weekList[0] or weekList[1] or weekList[2] or weekList[4] )->{
                weekLoopView!!.text = "平日"
            }
            // 休日か確認
            (weekList[5]) or (weekList[6]) and ( (!weekList[0]) and (!weekList[1]) and (!weekList[2]) and (!weekList[4]) ) ->{
                weekLoopView!!.text = "休日"
            }
            // 毎日か確認
            !weekList.toBooleanArray().contains(false)->{
                weekLoopView!!.text = "毎日"
            }
            else->{
                var data: String = ""
                for (i in 0..6){
                    if(weekList[i]){
                        data += dayTypeList[i]
                    }
                }
                weekLoopView!!.text = data
            }
        }

    }

    // 時間確認
    fun checkTime(){

        val startTime1View: TextView = view!!.findViewById<View>(R.id.start_time1) as TextView
        val endTime1View: TextView = view!!.findViewById<View>(R.id.end_time1) as TextView

        // 時間1
        val time_start_1: String = "" + "%02d".format(longsiteinfo?.start_timeHour_1?.toInt()) + ":" + "%02d".format( longsiteinfo?.start_timeMin_1?.toInt())
        val time_end_1: String    = "" + "%02d".format(longsiteinfo?.end_timeHour_1?.toInt()) + ":" + "%02d".format( longsiteinfo?.end_timeMin_1?.toInt())
        startTime1View.text = time_start_1
        endTime1View.text =  time_end_1

        val startTime2View: TextView = view!!.findViewById<View>(R.id.start_time2) as TextView
        val endTimew2View: TextView = view!!.findViewById<View>(R.id.end_time2) as TextView

        // 時間2
        val time_start_2: String = "" + "%02d".format(longsiteinfo?.start_timeHour_2?.toInt()) + ":" + "%02d".format( longsiteinfo?.start_timeMin_2?.toInt())
        val time_end_2: String    = "" + "%02d".format(longsiteinfo?.end_timeHour_2?.toInt()) + ":" + "%02d".format( longsiteinfo?.end_timeMin_2?.toInt())
        startTime2View.text = time_start_2
        endTimew2View.text =  time_end_2
    }

    fun setting(){
        // 通知スイッチ
        notificationSwitch!!.setOnClickListener {
            data = !data
            longsiteinfo!!.setIsOpen(data)
            notificationSwitch!!.isChecked = data
            longsiteinfo!!.dealWeekIsOpen()
        }

        // チェック間隔
        val IntervalView = view!!.findViewById<View>(R.id.checkInterval) as RelativeLayout
        IntervalView.setOnClickListener{
            val interval:UByte = longsiteinfo!!.interval
            val intervalList: Array<String> = longsiteinfo!!.getIntervalList()
            val intervalByteList:Array<UByte> = longsiteinfo!!.getIntervalByteList()
            SettingDialog.pickerDialog(
                group!!.context,
                "チェック間隔",
                intervalList,
                "" + interval + "Min"
            ) { setnum ->
                GlobalScope.launch(Dispatchers.Main) {
                    longsiteinfo!!.interval = intervalByteList[setnum]
                    checkIntervalView!!.text = "" + longsiteinfo!!.interval + "Min"
                }
            }
        }


        // 繰り返し曜日
        val weekView = view!!.findViewById<View>(R.id.weekLoop) as RelativeLayout
        weekView.setOnClickListener{
            SettingDialog.maltiradioDialog(
                group!!.context,
                "繰り返し",
                longsiteinfo!!.getWeekList(),
                arrayOf(false, false, false, false, false, false, false),
                longsiteinfo!!.getWeek()
            ) { setData ->
                GlobalScope.launch(Dispatchers.Main) {
                    longsiteinfo!!.setWeek(setData)
                    checkWeek()
                }
            }
        }

        // 時間1
        val time1View = view!!.findViewById<View>(R.id.time1) as RelativeLayout
        time1View.setOnClickListener{
            SettingDialog.timePickerDialog(
                group!!.context,
                "時間設定",
                arrayOf(
                    longsiteinfo!!.start_timeHour_1?.toInt(),
                    longsiteinfo!!.start_timeMin_1?.toInt()
                ),
                arrayOf(
                    longsiteinfo!!.end_timeHour_1?.toInt(),
                    longsiteinfo!!.end_timeMin_1?.toInt()
                )
            ) { time1val, time2val ->
                GlobalScope.launch(Dispatchers.Main) {
                    longsiteinfo!!.start_timeHour_1 = time1val[0].toUByte()
                    longsiteinfo!!.start_timeMin_1 = time1val[1].toUByte()

                    longsiteinfo!!.end_timeHour_1 = time2val[0].toUByte()
                    longsiteinfo!!.end_timeMin_1 = time2val[1].toUByte()

                    // 時間表示更新
                    checkTime()
                }
            }
        }


        // 時間2
        val time2View = view!!.findViewById<View>(R.id.time2) as RelativeLayout
        time2View.setOnClickListener{
            SettingDialog.timePickerDialog(
                group!!.context,
                "時間設定",
                arrayOf(
                    longsiteinfo!!.start_timeHour_2?.toInt(),
                    longsiteinfo!!.start_timeMin_2?.toInt()
                ),
                arrayOf(
                    longsiteinfo!!.end_timeHour_2?.toInt(),
                    longsiteinfo!!.end_timeMin_2?.toInt()
                )
            ) { time1val, time2val ->
                GlobalScope.launch(Dispatchers.Main) {
                    longsiteinfo!!.start_timeHour_2 = time1val[0].toUByte()
                    longsiteinfo!!.start_timeMin_2 = time1val[1].toUByte()

                    longsiteinfo!!.end_timeHour_2 = time2val[0].toUByte()
                    longsiteinfo!!.end_timeMin_2 = time2val[1].toUByte()

                    // 時間表示更新
                    checkTime()
                }
            }
        }

        // 設定ボタン
        val settingInfo: C18Delegate = bleManager.deviceDelegate as C18Delegate
        toolBtn!!.setOnClickListener{
            SettingDialog.normalDialog(group!!.context, "メッセージ", "データを保存しますか") {
                settingInfo?.settingLongSite(longsiteinfo!!) { bleResult, info ->
                    /// TODO ポップアップダイアログを表示
                }
            }
        }

    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }
}