//package net.lg2gent.protobeacon
//
//import android.content.pm.PackageManager
//import android.Manifest
//import android.app.PendingIntent
//import android.os.Bundle
//import android.support.v7.app.AppCompatActivity
//import android.support.design.widget.Snackbar
//import android.support.v4.app.ActivityCompat
//import android.support.v4.content.ContextCompat
//import android.util.Log
//
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.common.api.Status
//import com.google.android.gms.nearby.Nearby
//import com.google.android.gms.nearby.messages.*
//
//import kotlinx.android.synthetic.main.activity_beacon.beaconContainer
//import android.content.Intent
//import android.net.Uri
//import android.provider.Settings
//
//class BeaconActivityOld : AppCompatActivity(),
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener {
//
//    private var mGoogleApiClient: GoogleApiClient? = null
//    private var subscribed = false
//
//    companion object {
//        private val TAG = BeaconActivityOld::class.java.getSimpleName()
//        private val PERMISSIONS_REQUEST_CODE = 1111
//        private val KEY_SUBSCRIBED = "subscribed"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_beacon)
//
//        if (savedInstanceState != null){
//            subscribed = savedInstanceState.getBoolean(KEY_SUBSCRIBED, false)
//            Log.i(TAG, "Restoring instance state: " + subscribed)
//        }
//
//        if (!havePermissions()){
//            Log.i(TAG, "Requesting permissions needed for this app.")
//            requestPermissions()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (havePermissions()){
//            buildGoogleApiClient()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle?) {
//        Log.i(TAG, "Saving instance state.")
//        super.onSaveInstanceState(outState)
//        outState?.putBoolean(KEY_SUBSCRIBED, subscribed)
//    }
//
//    override fun onConnectionFailed(result: ConnectionResult) {
//        if (beaconContainer != null) {
//            Snackbar.make(beaconContainer,
//                    "Exception while connecting to Google Play Services: " + result.errorMessage,
//                    Snackbar.LENGTH_SHORT)
//        }
//    }
//
//    override fun onConnectionSuspended(i: Int) {
//        Log.w(TAG, "Connection suspended. Error code: " + i)
//    }
//
//    override fun onConnected(p0: Bundle?) {
//        Log.i(TAG, "GoogleApiClient connected")
//        subscribe()
//    }
//
//    @Synchronized
//    private fun buildGoogleApiClient() {
//        if (mGoogleApiClient == null) {
//            Log.i(TAG, "Building GoogleApiClient")
//            mGoogleApiClient = GoogleApiClient.Builder(this)
//                    .addApi(Nearby.MESSAGES_API, MessagesOptions.Builder()
//                            .setPermissions(NearbyPermissions.BLE).build())
//                    .addConnectionCallbacks(this)
//                    .enableAutoManage(this, this)
//                    .build()
//        }
//    }
//
//    private fun havePermissions(): Boolean {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
//                PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestPermissions() {
//        ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
//    }
//
//    private fun showLinkToSettingsSnackbar() {
//        if (beaconContainer == null) {
//            return
//        }
//        Snackbar.make(beaconContainer,
//                R.string.beacon_permission_denied_explanation,
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(R.string.beacon_settings, {
//                    // Build intent that displays the App settings screen.
//                    val intent = Intent()
//                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    val uri = Uri.fromParts("package",
//                            BuildConfig.APPLICATION_ID, null)
//                    intent.data = uri
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(intent)
//                }).show()
//
//    }
//
//    private fun showRequestPermissionsSnackbar() {
//        if (beaconContainer == null) {
//            return
//        }
//        Snackbar.make(beaconContainer, R.string.beacon_permission_rationale,
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(R.string.beacon_ok, {
//                    // Request permission.
//                    ActivityCompat.requestPermissions(this@BeaconActivityOld,
//                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                            PERMISSIONS_REQUEST_CODE)
//                }).show()
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>, grantResults: IntArray) {
//        if (requestCode != PERMISSIONS_REQUEST_CODE) {
//            return
//        }
//
//        for (i in permissions.indices) {
//            val permission = permissions[i]
//            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
//                if (shouldShowRequestPermissionRationale(permission)) {
//                    Log.i(TAG, "Permission denied without 'NEVER ASK AGAIN': " + permission)
//                    showRequestPermissionsSnackbar()
//                } else {
//                    Log.i(TAG, "Permission denied with 'NEVER ASK AGAIN': " + permission)
//                    showLinkToSettingsSnackbar()
//                }
//            } else {
//                Log.i(TAG, "Permission granted, building GoogleApiClient")
//                buildGoogleApiClient()
//            }
//        }
//    }
//
//    private fun subscribe() {
//        if (subscribed) {
//            Log.i(TAG, "Already subscribed.")
//            return
//        }
//
//        val options = SubscribeOptions.Builder()
//                .setStrategy(Strategy.BLE_ONLY)
//                .build()
//
//        Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options)
//                .setResultCallback({ status: Status ->
//                    if (status.isSuccess) {
//                        Log.i(TAG, "Subscribed successfully.")
//                        subscribed = true
//                        startService(getBackgroundSubscribeServiceIntent())
//                    } else {
//                        Log.e(TAG, "Operation failed. Error: " +
//                                NearbyMessagesStatusCodes.getStatusCodeString(status.statusCode))
//                    }
//                })
//    }
//
//    private fun getPendingIntent(): PendingIntent {
//        return PendingIntent.getService(this, 0,
//                getBackgroundSubscribeServiceIntent(), PendingIntent.FLAG_UPDATE_CURRENT)
//    }
//
//    private fun getBackgroundSubscribeServiceIntent(): Intent {
//        return Intent(this, BackgroundSubscribeIntentService::class.java)
//    }
//}
