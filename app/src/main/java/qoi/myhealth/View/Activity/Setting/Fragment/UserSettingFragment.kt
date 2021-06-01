package qoi.myhealth.View.Activity.Setting.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import qoi.myhealth.API.APIManager
import qoi.myhealth.Ble.C18.SettingDialog.InputDialog
import qoi.myhealth.Ble.C18.SettingDialog.dataPickreDialog
import qoi.myhealth.Ble.C18.SettingDialog.makeViewDialog
import qoi.myhealth.Ble.C18.SettingDialog.pickerDialog
import qoi.myhealth.Ble.model.LocalUserInfo
import qoi.myhealth.Controller.Util.Progress
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.C18.TimeAlertFragment
import qoi.myhealth.View.Activity.Setting.MainActivity
import java.time.LocalDate
import java.time.Period

class UserSettingFragment: Fragment() {

    private var appUserInfo: LocalUserInfo = ShareDataManager.getAppUserInfo()

    private var group: ViewGroup? = null

    private var nameText: TextView? = null
    private var birthText: TextView? = null
    private var genderText: TextView? = null
    private var lengthText: TextView? = null
    private var wightText: TextView? = null
    private var bloodText: TextView? = null
    private var skinImg: ImageView? = null

    private var genderType: Array<String> = arrayOf()
    private var bloodType: Array<String> = arrayOf()

    companion object {

        private var instance: UserSettingFragment = UserSettingFragment()

        // インスタンス取得
        fun getInstance() : UserSettingFragment {
            return instance
        }

        fun createInstance(edit:Boolean) : UserSettingFragment {
            val fragmentMain = UserSettingFragment()
            val args = Bundle()
            args.putBoolean("editKey",edit)
            fragmentMain.arguments = args
            return fragmentMain
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        group = container
        return inflater.inflate(qoi.myhealth.R.layout.fragment_user_setting, container, false)
    }

    override fun onStart() {
        super.onStart()
        appUserInfo = ShareDataManager.getAppUserInfo()
        init()
        setting()

        if(arguments != null){
            if(arguments!!.getBoolean("editKey")){
                edit()
            }
        }
    }

    private fun edit(){
        val maActivity = activity as MainActivity?


        maActivity?.getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        maActivity?.getSupportActionBar()?.setCustomView(R.layout.my_toolbar)

        val title = activity!!.findViewById<View>(qoi.myhealth.R.id.head_title) as TextView
        title.text = getString(R.string.mySetting_Title)

        val backBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.backimg) as ImageView
        backBtn.setOnClickListener{
            maActivity!!.setNavi(R.id.user)
        }

        // 保存ボタン
        val saveBtn = activity!!.findViewById<View>(qoi.myhealth.R.id.toolbtn) as Button
        saveBtn.visibility = View.VISIBLE
        saveBtn.setText(getString(R.string.save))
        saveBtn.setOnClickListener {
            Progress.getInstance().showDialog(group!!.context,"保存中")
            APIManager.getInstance().setUserInfo(group!!.context,appUserInfo.birth,appUserInfo.gender.toString(),appUserInfo.height.toString(),appUserInfo.wight.toString()){ it->
                if(it == APIManager.getInstance().OK){saveAppInfoDat()}
                maActivity!!.setNavi(R.id.user)
                Progress.getInstance().closeDialog()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            maActivity!!.setNavi(R.id.user)
        }
    }

    // 初期化表示
    private fun init(){
        genderType = arrayOf(
            getString(R.string.mySetting_Man),
            getString(R.string.mySetting_Woman)
        )
        bloodType= arrayOf(
            getString(R.string.mySetting_BloodStatus_Low),
            getString(R.string.mySetting_BloodStatus_Normal),
            getString(R.string.mySetting_BloosStatus_LittleHigher),
            getString(R.string.mySetting_BloodStatus_High),
            getString(R.string.mySetting_Status_VeryHigh)
        )

        // ニックネーム
        nameText = view!!.findViewById(qoi.myhealth.R.id.nameVal) as TextView
        nameText!!.text = appUserInfo.name

        // 生年月日
        birthText = view!!.findViewById(qoi.myhealth.R.id.birthVal) as TextView
        birthText!!.text = calcAge(appUserInfo.birth).toString()

        // 性別
        genderText = view!!.findViewById(qoi.myhealth.R.id.sexVal) as TextView
        genderText!!.text = genderType[(appUserInfo.gender - 1)]

        // 身長
        lengthText = view!!.findViewById(qoi.myhealth.R.id.lengthVal) as TextView
        lengthText!!.text = "" + appUserInfo.height + "cm"

        // 体重
        wightText = view!!.findViewById(qoi.myhealth.R.id.weightVal) as TextView
        wightText!!.text = "" + appUserInfo.wight + "kg"

        /// TODO 肌の色
        skinImg =  view!!.findViewById(qoi.myhealth.R.id.skinVal) as ImageView
        var skinID: Int = 0
        when(appUserInfo.skincolor){
            0 -> { skinID = R.drawable.skin_white }
            1->{ skinID = R.drawable.skin_flesh }
            2->{ skinID = R.drawable.skin_light_brown }
            3->{ skinID = R.drawable.skin_brown }
            4->{ skinID = R.drawable.skin_dark_brown }
            5->{ skinID = R.drawable.skin_brack }
            else->{ skinID = R.drawable.skin_white }
        }
        skinImg!!.setImageResource(skinID)

        // 血圧
        bloodText = view!!.findViewById(qoi.myhealth.R.id.bloodVal) as TextView
        bloodText!!.text = bloodType[appUserInfo.blood]
    }

    // 設定
    private fun setting(){
        // 名前
        val nameView: RelativeLayout = view!!.findViewById(qoi.myhealth.R.id.name) as RelativeLayout
        nameView.setOnClickListener {
            InputDialog(group!!.context,getString(R.string.mySetting_Nickname),nameText!!.text.toString()){inputname->
                nameText!!.text = inputname
                appUserInfo.name = inputname
            }
        }

        // 生年月日
        val birthView: RelativeLayout = view!!.findViewById(qoi.myhealth.R.id.birth) as RelativeLayout
        birthView.setOnClickListener {
            dataPickreDialog(group!!.context){year, month, day->
                appUserInfo.birth = "" + year +"-"+ "%02d".format(month) + "-" + "%02d".format(day)
                birthText!!.text = calcAge(appUserInfo.birth).toString()
            }
        }

        // 性別
        val genderView= view!!.findViewById(qoi.myhealth.R.id.sex) as RelativeLayout
        genderView.setOnClickListener {
            pickerDialog(group!!.context,getString(R.string.mySetting_Sex),genderType,genderType[(appUserInfo.gender - 1)]){num->
                val now: Int = (num + 1)
                appUserInfo.gender = now
                genderText!!.text = genderType[num]
            }
        }

        // 身長
        val lengthView = view!!.findViewById(qoi.myhealth.R.id.length) as RelativeLayout
        var lengthList: Array<String> = arrayOf()
        for (i in 100..200){
            lengthList += "" + i + "cm"
        }

        lengthView.setOnClickListener {
            pickerDialog(group!!.context, getString(R.string.mySetting_Height), lengthList, "" + appUserInfo.height + "cm") { num ->
                lengthText!!.text = lengthList[num]
                val data: Int = lengthList[num].replace("cm", "").toInt()
                appUserInfo.height = data
            }
        }

        // 体重
        val wightView = view!!.findViewById(qoi.myhealth.R.id.weight) as RelativeLayout
        var wightList: Array<String> = arrayOf()
        for (i in 40..120){
            wightList += "" + i + "kg"
        }
        wightView.setOnClickListener {
            pickerDialog(group!!.context, getString(R.string.mySetting_Weight), wightList, "" + appUserInfo.wight + "kg") { num ->
                wightText!!.text = wightList[num]
                val data: Int = wightList[num].replace("kg", "").toInt()
                appUserInfo.wight = data
            }
        }

        // 血圧
        val bloodView = view!!.findViewById(qoi.myhealth.R.id.blood) as RelativeLayout
        bloodView.setOnClickListener {
            pickerDialog(group!!.context, getString(R.string.mySetting_BP), bloodType, bloodType[appUserInfo.blood]) { num ->
                appUserInfo.blood = num
                bloodText!!.text = bloodType[num]
            }
        }

        // 肌の色
        val skincolView =  view!!.findViewById(qoi.myhealth.R.id.skin) as RelativeLayout
        skincolView.setOnClickListener {
            makeViewDialog(group!!.context,getString(R.string.mySetting_SkinColor),appUserInfo.skincolor){selectID,srcID->
                skinImg!!.setImageResource(srcID)
                appUserInfo.skincolor = selectID
            }
        }


    }

    // 生年月日から年齢を計算
    @SuppressLint("NewApi")
    private fun calcAge(birthday: String): Int? {
        val today = LocalDate.now()
        return Period.between(LocalDate.parse(birthday), today).getYears()
    }

    // データを保存する
    fun saveAppInfoDat(){
        ShareDataManager.saveAppUserInfo(appUserInfo)
    }
}