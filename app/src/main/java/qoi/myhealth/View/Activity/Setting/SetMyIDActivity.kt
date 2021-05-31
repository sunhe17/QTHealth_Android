package qoi.myhealth.View.Activity.Setting

import android.app.PendingIntent
import android.bluetooth.BluetoothClass
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.android.synthetic.main.activity_myid.*
import kotlinx.android.synthetic.main.my_toolbar.*
import qoi.myhealth.Ble.C18.SettingDialog.normalDialog
import qoi.myhealth.Ble.model.DeviceInfo
import qoi.myhealth.Ble.model.Device_ID_KEY
import qoi.myhealth.Controller.Util.AppStatusCheck
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R
import java.io.File

class MyCaptureActivity : CaptureActivity()

class SetMyIDActivity:AppCompatActivity() {

    private var res = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myid)

        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()?.setCustomView(R.layout.my_toolbar)
        toolbtn.visibility = View.GONE
        backimg.visibility = View.GONE
        head_title.text = "MyID 設定"

        // 位置情報権限確認
        AppStatusCheck.checkPermission(this,this)
    }

    override fun onStart() {
        super.onStart()
        var scanData:Device_ID_KEY = ShareDataManager.getScanData()

        if(scanData.uuid == "" ){
            scanData.uuid = "ANDROID"+ android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID).toUpperCase()
        }

        description.text = """
                            MyiDとはあなたの識別番号です。システムから36桁の番号が自動的に配布されます。
                            新規の場合、そのままで、登録ボタンを押下してください。
                            以前使ったデバイスから引き続きで利用する場合、該当MyIDを入力した後、登録ボタンを押下してください。 
                            """.trimIndent()

        qrimg.setOnClickListener{
            if(uuidval.text.toString() != ""){
                scanData.uuid = uuidval.text.toString()
                ShareDataManager.saveScanData(scanData)
            }
            val intentIntegrator = IntentIntegrator(this).apply {
                setPrompt("Scan a QR code")
                captureActivity = MyCaptureActivity::class.java
            }
            intentIntegrator.initiateScan()

        }

        if(res != ""){
            uuidval.setText(res)
        }
        else if(scanData.uuid != ""){
            uuidval.setText(scanData!!.uuid)
        }

        nextimg.setOnClickListener{
            if(uuidval.text.toString() != ""){

                /*val edit : SharedPreferences.Editor = this.getSharedPreferences("QOI_Health", Context.MODE_PRIVATE).edit()
                var scanDataJson = Device_ID_KEY(uuidval.text.toString(),"")
                edit.putString("SaveUUIDKey",Gson().toJson(scanDataJson)).commit()*/

                scanData.uuid = uuidval.text.toString()
                ShareDataManager.saveScanData(scanData)
                val intent = Intent(this,SetAccessKeyActivity::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "IDを入力してください", Toast.LENGTH_LONG).show()
            }
        }

    }

    // 読取後に呼ばれる
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if ((result!!.contents != null) and (result!!.contents != "")) {
            res = result!!.contents.replace("QOI://?access=","")
        } else {
            super.onActivityResult(requestCode, resultCode, data)
          //  Toast.makeText(this, "読み取りに失敗しました", Toast.LENGTH_LONG).show()
        }
    }

}