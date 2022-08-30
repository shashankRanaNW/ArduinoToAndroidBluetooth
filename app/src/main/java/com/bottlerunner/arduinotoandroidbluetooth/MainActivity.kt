package com.bottlerunner.arduinotoandroidbluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.bottlerunner.arduinotoandroidbluetooth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var binding: ActivityMainBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding= DataBindingUtil.setContentView(this,R.layout.activity_main)

            //        permissions ki bheek

            requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
            )
        )

            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.getAdapter()
            if (bluetoothAdapter == null) {
                Toast.makeText(
                this,
                "Abe bluetooth he nahi, chala bluetooth se chat karne",
                Toast.LENGTH_SHORT
                ).show()
            }

            binding.btnDiscover.setOnClickListener {
                val intent = Intent(this, DeviceListActivity::class.java)
                startActivityForResult(intent, SELECT_DEVICE)
            }


    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        {

            if( (it[Manifest.permission.BLUETOOTH] == false
                        &&
                        (it[Manifest.permission.BLUETOOTH_CONNECT] == false
                                || it[Manifest.permission.BLUETOOTH_ADVERTISE]==false
                                ||it[Manifest.permission.BLUETOOTH_SCAN]==false))
                || (it[Manifest.permission.ACCESS_COARSE_LOCATION] ==false && it[Manifest.permission.ACCESS_FINE_LOCATION] ==false)){
                Toast.makeText(this,"Please provide required permissions", Toast.LENGTH_SHORT).show()
                finish()
            }
            else{
                Toast.makeText(this,"Permissions granted successfully", Toast.LENGTH_SHORT).show()
                bluetoothAdapter?.enable()
            }
        }

}