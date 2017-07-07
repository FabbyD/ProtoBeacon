package net.lg2gent.protobeacon

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import kotlinx.android.synthetic.main.activity_beacon_distance.*

class BeaconDistanceActivity : AppCompatActivity() {

    private lateinit var beaconManager: BeaconManager

    companion object {
        private val TAG = BeaconDistanceActivity::class.java.getSimpleName()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon_distance)

        if (!havePermissions()){
            Log.i(TAG, "Requesting permissions needed for this app.")
            requestPermissions()
        }

        beaconManager = BeaconManager(this)
        beaconManager.setup()
    }

    override fun onResume() {
        Log.i(TAG, "onResume()")
        super.onResume()

        if (havePermissions()) {
            beaconManager.setup()
            beaconManager.setIBeaconListener(object : IBeaconListener {
                override fun onIBeaconDiscovered(iBeacon: IBeaconDevice, region: IBeaconRegion) {
                    Log.i(TAG, iBeacon.toString())
                    setBeaconText(iBeacon)
                }

                override fun onIBeaconLost(iBeacon: IBeaconDevice, region: IBeaconRegion) {
                    Log.i(TAG, "iBeacon lost" + iBeacon.toString())
                    setBeaconText(iBeacon)
                }

                override fun onIBeaconsUpdated(iBeacons: MutableList<IBeaconDevice>, region: IBeaconRegion) {
                    Log.i(TAG, "iBeacons updated")
                    for (iBeacon in iBeacons) {
                        setBeaconText(iBeacon)
                    }
                }
            })
            beaconManager.startScanning()
        }
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
                    ActivityCompat.requestPermissions(this@BeaconDistanceActivity,
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
                    Log.i(TAG, "Permission denied without 'NEVER ASK AGAIN': " + permission)
                    showRequestPermissionsSnackbar()
                } else {
                    Log.i(TAG, "Permission denied with 'NEVER ASK AGAIN': " + permission)
                    showLinkToSettingsSnackbar()
                }
            } else {
                Log.i(TAG, "Permission granted")
                beaconManager.startScanning()
            }
        }
    }

    private fun setBeaconText(iBeacon: IBeaconDevice) {
        val rssi = iBeacon.rssi.toString()
        val distance = iBeacon.distance.toString()
        val proximity = iBeacon.proximity.toString()
        val text = arrayOf(rssi,distance,proximity).joinToString(" ")
        when (iBeacon.major) {
            BeaconManager.beacon1_major -> beacon1.text = text
            BeaconManager.beacon2_major -> beacon2.text = text
            BeaconManager.beacon3_major -> beacon3.text = text
        }
    }
}
