package jp.techacademy.kenichi04.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSION_REQUEST_CODE = 10
    // 画像URIを格納するリスト
    private var uriArray = arrayListOf<Uri>()
    // 選択中のuri
    private var uriNum = 0

    private var timer: Timer? = null
    private var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android6.0 以降でアプリ内でパーミッション許可取得が必要
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状況確認
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsImageUri()
            } else {
                // 許可ダイアログ表示
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            }
        // Android5系以下は必要なし
        } else {
            getContentsImageUri()
        }

        next_button.setOnClickListener(this)
        back_button.setOnClickListener(this)
        play_button.setOnClickListener(this)
    }

    // ユーザーの許可選択の結果を受け取る
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsImageUri()
                } else {
                    next_button.isEnabled = false
                    back_button.isEnabled = false
                    play_button.isEnabled = false
                }
        }
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.next_button -> {
                uriNum++
                if(uriNum >= uriArray.size) uriNum = 0
                imageView.setImageURI(uriArray[uriNum])
            }
            R.id.back_button -> {
                uriNum--
                if(uriNum < 0) uriNum = uriArray.size - 1
                imageView.setImageURI(uriArray[uriNum])
            }
            R.id.play_button -> {
                slideshow()
            }
        }
    }

    private fun getContentsImageUri() {
        val resolver = contentResolver
        var cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor!!.moveToFirst()) {
            do {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                // uriをリストに追加
                uriArray.add(imageUri)

            } while(cursor.moveToNext())
        }
        Log.d("uriArray", "${uriArray}")

        // 画像がない場合、@drawableのuriを入れる
        if(uriArray.size == 0) {
            val uri = Uri.parse("android.resource://jp.techacademy.kenichi04.autoslideshowapp/drawable/no_image")
            Log.d("uriArray", "$uri")
            uriArray.add(uri)
            next_button.isEnabled = false
            back_button.isEnabled = false
            play_button.isEnabled = false
        }

        imageView.setImageURI(uriArray[0])

        cursor.close()
    }

    private fun slideshow() {
        // 再生ボタン
        if (timer == null) {
            play_button.text = "停止"
            next_button.isEnabled = false
            back_button.isEnabled = false

            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        imageView.setImageURI(uriArray[uriNum])
                    }
                    uriNum++
                    if(uriNum >= uriArray.size) uriNum = 0
                }
            }, 2000, 2000)
        // 停止ボタン
        } else {
            play_button.text = "再生"
            next_button.isEnabled = true
            back_button.isEnabled = true

            timer!!.cancel()
            timer = null
        }
    }

}