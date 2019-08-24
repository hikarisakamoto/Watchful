package com.hikarisakamoto.watchful

import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_watcher.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class WatcherActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_watcher)

        setScannerProperties()
        barcodeBackImageView.setOnClickListener { onBackPressed() }
        flashOnOffImageView.setOnClickListener {
            if (qrCodeScanner.flash) {
                qrCodeScanner.flash = false
                flashOnOffImageView.background =
                    ContextCompat.getDrawable(this, R.drawable.flash_off_vector_icon)
            } else {
                qrCodeScanner.flash = true
                flashOnOffImageView.background =
                    ContextCompat.getDrawable(this, R.drawable.flash_on_vector_icon)
            }
        }
    }

    /**
     * Set bar code scanner basic properties.
     */

    private fun setScannerProperties() {
        qrCodeScanner.setFormats(listOf(BarcodeFormat.QR_CODE))
        qrCodeScanner.setAutoFocus(true)
        qrCodeScanner.setLaserColor(R.color.colorAccent)
        qrCodeScanner.setMaskColor(R.color.colorAccent)
    }

    /**
     * resume the qr code camera when activity is in onResume state.
     */

    override fun onResume() {
        super.onResume()
        // TODO REQUEST / CHECK PERMISSION
        qrCodeScanner.startCamera()
        qrCodeScanner.setResultHandler(this)
    }

    /**
     * To check if user grant camera permission then called openCamera function.If not then show not granted
     * permission snack bar.
     *
     * @param requestCode  specify which request result came from operating system.
     * @param permissions  to specify which permission result is came.
     * @param grantResults to check if user granted the specific permission or not.
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // TODO REQUEST PERMISSION
        openCamera()
    }

    fun openCamera() {
        qrCodeScanner.startCamera()
        qrCodeScanner.setResultHandler(this)
    }

    /**
     * stop the qr code camera scanner when activity is in onPause state.
     */

    override fun onPause() {
        super.onPause()
        qrCodeScanner.stopCamera()
    }

    override fun handleResult(p0: Result?) {
        if (p0 != null) {
            // TODO CHECK CAMERA? GO TO ANOTHER INTENT?
            resumeCamera()
        }
    }

    /**
     * Resume the camera after 2 seconds when qr code successfully scanned through bar code reader.
     */

    private fun resumeCamera() {
        Toast.LENGTH_LONG
        val handler = Handler()
        // TODO CHECK CAMERA? GO TO ANOTHER INTENT?
        handler.postDelayed({ qrCodeScanner.resumeCameraPreview(this) }, 2000)
    }
}
