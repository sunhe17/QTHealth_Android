package qoi.myhealth.View.Activity.Setting


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.my_toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import qoi.myhealth.Ble.BleIdentificationKey
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.C18.CMD.C18_Theme_Type
import qoi.myhealth.Ble.C18.Model.*
import qoi.myhealth.Controller.Util.AppStatusCheck
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.Fragment.AppSettingFragment
import java.util.*

class MainActivity: BaseActivity(){

    private val deviceDisconnectName:String = "デバイス未接続"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 仮で強制的に未接続にする　TODO テストのみ使用
        //ShareDataManager.saveConnectType(null)

        // 初回起動か確認
        if(AppStatusCheck.isFirstJudgment(this)){
           val intent = Intent(this,SetMyIDActivity::class.java)
           startActivity(intent)
        }
        else{
            println("not_first")
        }

        // 位置情報権限確認
        AppStatusCheck.checkPermission(this,this)

        createHome()

        // 自動接続実行
        //tryToConnectDevice()
    }

    override fun onStart() {
        super.onStart()
        selectNavi()
        head_title.text = "HOME"
        toolbtn.visibility = View.GONE
        backimg.visibility = View.GONE
    }

    override fun attachBaseContext(base: Context) {
        var locale = Locale.getDefault()
        val lang =  ShareDataManager.getScanData().language
        when(lang){
            0->locale = Locale.getDefault()
            1->locale = Locale.ENGLISH
            else->locale = Locale.getDefault()
        }
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocales(LocaleList(locale))
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onBleDeviceConnection() {
    }

    override fun onBleDeviceDisConnection() {
    }

    fun reload(){
        finish()
        overridePendingTransition(0, 0)
        startActivity(getIntent())
        overridePendingTransition(0, 0)
        Toast.makeText(this, "言語を変更しました", Toast.LENGTH_LONG).show()
    }

    fun setNavi(id: Int){
        bottom_navigation.selectedItemId = id
    }

    // 読み取り後に呼ばれるメソッド
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 結果の取得
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            when(bottom_navigation.selectedItemId){
                R.id.user->{
                    AppSettingFragment.createInstance().setKeyView(result.contents)
                }
            }

        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}