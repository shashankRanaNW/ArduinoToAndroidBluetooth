package com.bottlerunner.arduinotoandroidbluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AvailableDevicesActivity : AppCompatActivity(),DeviceAdapter.OnDeviceInfoListener {

    lateinit var rvPairedDevices: RecyclerView
    lateinit var rvAvailableDevices: RecyclerView
    lateinit var blueAdapt :BluetoothAdapter
    lateinit var progressBar: ProgressBar
    lateinit var pairedDevices: MutableList<BluetoothDevice>
    var availDevList = mutableListOf<BluetoothDevice>()

    lateinit var btnDiscover: Button

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_devices)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        blueAdapt = bluetoothManager.adapter
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        rvPairedDevices  =findViewById(R.id.rvPairedDevices)
        rvAvailableDevices = findViewById(R.id.rvAvailableDevices)
        btnDiscover = findViewById(R.id.btnDiscover)

        pairedDevices = blueAdapt.getBondedDevices().toMutableList()
        if(pairedDevices.size != 0 ){
            rvPairedDevices.adapter = DeviceAdapter(this,pairedDevices.toMutableList())
            rvPairedDevices.layoutManager = LinearLayoutManager(this)
        }

        //Now gonna search for new devices

        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothDeviceListener, intentFilter)
        val intentFilter1 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(bluetoothDeviceListener, intentFilter1)

        btnDiscover.setOnClickListener {

            if (blueAdapt.isDiscovering()) {
                blueAdapt.cancelDiscovery()
            }
            blueAdapt.startDiscovery()                                                          //Herein, if we don't use intent filter thing in top
            progressBar.visibility = View.VISIBLE
        }
    }

    private val bluetoothDeviceListener: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let{
                    availDevList.add(device)
                    rvAvailableDevices.adapter = DeviceAdapter(this@AvailableDevicesActivity,availDevList.toMutableList())
                    rvAvailableDevices.layoutManager = LinearLayoutManager(this@AvailableDevicesActivity)
                    Log.d("Dicovery",availDevList.toString())
                }
                Log.d("Dicovery",availDevList.toString())
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                progressBar.setVisibility(View.GONE)
            }
        }
    }

    override fun onDeviceInfoListener(intent: Intent) {
        Log.d("DeviceListActivity",intent.getStringExtra("devName").toString())
        setResult(RESULT_OK, intent)
        finish()
    }

}
