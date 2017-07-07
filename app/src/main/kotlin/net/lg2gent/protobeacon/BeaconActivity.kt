package net.lg2gent.protobeacon

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import kotlinx.android.synthetic.main.activity_beacon.*


class BeaconActivity : AppCompatActivity(), SensorEventListener {

    private var screenHeight: Int? = null
    private var screenWidth: Int? = null
    private val FOV_X = 90
    private val FOV_Y = 90

    private lateinit var beaconManager: BeaconManager
    private lateinit var sensorManager: SensorManager
    private lateinit var rotationSensor: Sensor

    private val mRotationMatrixFromVector = FloatArray(9)
    private var smoothRotation: FloatArray? = null
    private val mRotationMatrix = FloatArray(9)
    private val mOrientation = FloatArray(3)

    private val ALPHA = 0.2f

    private var hasOrigin = false
    private var origin = FloatArray(3)

    private var lastAccuracy = 0

    companion object {
        private val TAG = BeaconActivity::class.java.getSimpleName()
        val PERMISSIONS_REQUEST_CODE = 1111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon)

        if (!havePermissions()){
            Log.i(TAG, "Requesting permissions needed for this app.")
            requestPermissions()
        }

        beaconManager = BeaconManager(this)
        beaconManager.setup()
        setupSensor()
        setupScreenDim()

        if (null == savedInstanceState) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit()
        }
    }

    private fun setupScreenDim() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
    }

    private fun setupSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onResume() {
        Log.i(TAG, "onResume()")
        super.onResume()
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)

        if (havePermissions()) {
            beaconManager.setup()
            beaconManager.setIBeaconListener(object : IBeaconListener {
                override fun onIBeaconDiscovered(iBeacon: IBeaconDevice, region: IBeaconRegion) {
                    Log.i(TAG, iBeacon.toString())
                    if (iBeacon.major == BeaconManager.beacon3_major) {
                        beacon_label.text = "Discovered"
                        beacon_status.text = iBeacon.rssi.toString()
                    }
                }

                override fun onIBeaconLost(iBeacon: IBeaconDevice, region: IBeaconRegion) {
                    Log.i(TAG, "iBeacon lost" + iBeacon.toString())
                    if (iBeacon.major == BeaconManager.beacon3_major) {
                        beacon_label.text = "Lost"
                        beacon_status.text = "-"
                    }
                }

                override fun onIBeaconsUpdated(iBeacons: MutableList<IBeaconDevice>, region: IBeaconRegion) {
                    Log.i(TAG, "iBeacons updated")
                    for (iBeacon in iBeacons) {
                        if (iBeacon.major == BeaconManager.beacon3_major) {
                            beacon_label.text = "Updated"
                            beacon_status.text = iBeacon.rssi.toString()
                            iBeacon.distance
                        }
                    }
                }
            })
            beaconManager.startScanning()
        }

        hasOrigin = false
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onStop() {
        Log.i(TAG, "onStop()")
        beaconManager.stopScanning()
        super.onStop()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy()")
        beaconManager.disconnect()
        super.onDestroy()
    }

    private fun havePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), BeaconActivity.PERMISSIONS_REQUEST_CODE)
    }

    private fun showLinkToSettingsSnackbar() {
        if (beaconContainer == null) {
            return
        }
        Snackbar.make(beaconContainer,
                R.string.beacon_permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.beacon_settings, {
                    // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package",
                            BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }).show()
    }

    private fun showRequestPermissionsSnackbar() {
        if (beaconContainer == null) {
            return
        }
        Snackbar.make(beaconContainer, R.string.beacon_permission_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.beacon_ok, {
                    // Request permission.
                    ActivityCompat.requestPermissions(this@BeaconActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            BeaconActivity.PERMISSIONS_REQUEST_CODE)
                }).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != BeaconActivity.PERMISSIONS_REQUEST_CODE) {
            return
        }

        for (i in permissions.indices) {
            val permission = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    Log.i(BeaconActivity.TAG, "Permission denied without 'NEVER ASK AGAIN': " + permission)
                    showRequestPermissionsSnackbar()
                } else {
                    Log.i(BeaconActivity.TAG, "Permission denied with 'NEVER ASK AGAIN': " + permission)
                    showLinkToSettingsSnackbar()
                }
            } else {
                Log.i(BeaconActivity.TAG, "Permission granted")
                beaconManager.startScanning()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (lastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return
        }
        if (event?.sensor == rotationSensor) {
            updateOrientation(event.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (lastAccuracy != accuracy) {
            lastAccuracy = accuracy
        }
    }

    private fun updateOrientation(rotationVector: FloatArray){
        smoothRotation = lowPass(rotationVector, smoothRotation)
        SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, smoothRotation)
        SensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                SensorManager.AXIS_X, SensorManager.AXIS_Z,
                mRotationMatrix)
        SensorManager.getOrientation(mRotationMatrix, mOrientation)

        if (!hasOrigin) {
            for (i in mOrientation.indices){
                origin[i] = Math.toDegrees(mOrientation[i].toDouble()).toFloat()
            }
            hasOrigin = true
        }

        val adjustedOrientation = adjustOrientation(mOrientation)
        val translation = getTranslation(adjustedOrientation)
        compass_angle_z.text = java.lang.String.format("%.2f %.2f %.2f\n%.2f %.2f %.2f",
                mOrientation[0],
                mOrientation[1],
                mOrientation[2],
                Math.toDegrees(mOrientation[0].toDouble()).toFloat(),
                Math.toDegrees(mOrientation[1].toDouble()).toFloat(),
                Math.toDegrees(mOrientation[2].toDouble()).toFloat())
//        compass.rotation = -adjustedDegree
        compass.translationX = translation[0]
        compass.translationY = translation[1]
    }

    private fun adjustOrientation(orientation: FloatArray): FloatArray {
        val newOrientation = FloatArray(3)
        for (i in origin.indices) {
            // Shift to origin's plan
            val degree = Math.toDegrees(orientation[i].toDouble()).toFloat()
            newOrientation[i] = degree - origin[i]
            // Shift between -180 and 180
            if (newOrientation[i] < -180) {
                newOrientation[i] += 360.0f
            } else if (newOrientation[i] > 180) {
                newOrientation[i] -= 360.0f
            }
        }
        return newOrientation
    }

    private fun getTranslation(orientation: FloatArray): FloatArray {
        val translation = FloatArray(2)
        translation[0] = -screenWidth!!.div(FOV_X)*orientation[0]
        translation[1] = -screenHeight!!.div(FOV_Y)*orientation[1]
        return translation
    }

    /**
     * Smoothing function
     */
    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input

        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }
}
