package net.lg2gent.protobeacon

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.kontakt.sdk.android.ble.configuration.ScanPeriod
import com.kontakt.sdk.android.ble.manager.ProximityManager
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.common.KontaktSDK

class BeaconManager(val context: AppCompatActivity) {
    private var proximityManager: ProximityManager? = null

    companion object {
        private val TAG = BeaconActivity::class.java.getSimpleName()
        val beacon1_major = 7344  // 1w6S
        val beacon2_major = 39271 // 7697
        val beacon3_major = 12442 // z4SI
    }

    fun setup(){
        if (proximityManager != null) {
            Log.i(TAG, "Proximity manager is already set up")
            return
        }

        Log.i(TAG, "Setting up proximity manager")
        try {
            val packageManager = this.context.packageManager
            val packageName = this.context.packageName
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val api_key = ai.metaData.getString("KONTAKT_API_KEY")

            KontaktSDK.initialize(api_key)
            proximityManager = ProximityManagerFactory.create(this.context)
            proximityManager!!.configuration()
                    .scanPeriod(ScanPeriod.RANGING)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Could not find Kontakt API Key.")
        }
    }

    fun setIBeaconListener(listener: IBeaconListener) {
        proximityManager!!.setIBeaconListener(listener)
    }

    fun startScanning() {
        Log.i(TAG, "Start scanning...")
        proximityManager!!.connect({
            proximityManager!!.startScanning()
        })
    }

    fun stopScanning() {
        proximityManager?.stopScanning()
    }

    fun disconnect() {
        proximityManager?.disconnect()
        proximityManager = null
    }
}