package jp.techacademy.kenichi04.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSION_REQUEST_CODE = 10
    // 画像URIを格納するリスト
    var uriArray = arrayListOf<Uri>()
    // 選択中のuri
    var uriNum = 0

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

                // リストに追加
                uriArray.add(imageUri)

            } while(cursor.moveToNext())
        }
        Log.d("uriArray", "${uriArray}")
        // 最初の画像を表示
        imageView.setImageURI(uriArray[0])

        cursor.close()
    }


}