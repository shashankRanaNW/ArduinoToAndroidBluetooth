package com.bottlerunner.arduinotoandroidbluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(
                this,
                "Abe bluetooth he nahi, chala bluetooth se chat karne",
                Toast.LENGTH_SHORT
            ).show()
        }

        //hello mortals


//        permissions ki bheek
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
            }
            else{
                Toast.makeText(this,"Permissions granted successfully", Toast.LENGTH_SHORT).show()

            }
        }

}