package net.lg2gent.protobeacon

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainStartButton.setOnClickListener {
            startActivity(Intent(this, BeaconActivity::class.java))
        }

        beaconDistanceButton.setOnClickListener {
            startActivity(Intent(this, BeaconDistanceActivity::class.java))
        }
    }
}
