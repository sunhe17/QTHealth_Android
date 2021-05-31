package qoi.myhealth.View.Activity.Setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_access_key.*
import kotlinx.android.synthetic.main.activity_access_key.nextimg
import kotlinx.android.synthetic.main.activity_myid.description
import kotlinx.android.synthetic.main.activity_myid.qrimg
import kotlinx.android.synthetic.main.my_toolbar.*
import qoi.myhealth.Ble.model.Device_ID_KEY
import qoi.myhealth.Manager.ShareDataManager
import qoi.myhealth.R


class SetAccessKeyActivity: AppCompatActivity() {

    private var res: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_key)

        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()?.setCustomView(R.layout.my_toolbar)
        toolbtn.visibility = View.GONE
        backimg.visibility = View.VISIBLE
        head_title.text = "ACCESS KEY 設定"
    }

    override fun onStart() {
        super.onStart()

        var scanData:Device_ID_KEY = ShareDataManager.getScanData()
        println("uui->"+scanData!!.uuid)

        description.text = """
                            AccessKeyとは測定した健康データを保存するために使用する識別番号です。
                            QOI会員サイト-[アカウント情報]のQRコードの読み取りまたはアクセスキーを入力することで登録できます。
                            AccessKeyを持っていない場合は「あとで設定」を押してください
                            """.trimIndent()

        qrimg.setOnClickListener{
            val intentIntegrator = IntentIntegrator(this).apply {
                setPrompt("Scan a QR code")
                captureActivity = MyCaptureActivity::class.java
            }
            intentIntegrator.initiateScan()

        }

        if(res != ""){
            accesskeyval.setText(res)
        }
        else if(scanData.key != ""){
            accesskeyval.setText(scanData.key)
        }

        backimg.setOnClickListener{
            val intent = Intent(this,SetMyIDActivity::class.java)
            startActivity(intent)
        }

        nextimg.setOnClickListener{
            if(accesskeyval.text.toString() != null){
                scanData.key = accesskeyval.text.toString()
                ShareDataManager.saveScanData(scanData)

                val intent = Intent(this,UserSettingActivity::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "ACCESS KEYを入力してください", Toast.LENGTH_LONG).show()
            }
        }

        skip.setOnClickListener{
            val intent = Intent(this,UserSettingActivity::class.java)
            startActivity(intent)
        }

    }

    // 読取後に呼ばれる
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if ((result!!.contents != null) and (result!!.contents != "")) {
            res = result!!.contents.replace("QOI://?access=","")
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            //Toast.makeText(this, "読み取りに失敗しました", Toast.LENGTH_LONG).show()
        }
    }
}