package com.bottlerunner.arduinotoandroidbluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bottlerunner.arduinotoandroidbluetooth.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

fun getFirst(str: String, n: Int): String {
    var i:Int = 0
    var ans:String=""
    while(i <n){
        ans += str[i]
        i++
    }
    return ans
}


@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    var mSocket: BluetoothSocket? =null
    var MY_UUID :UUID? = UUID.fromString("4cb4cec4-2017-4e54-9ef9-9e4aadaf033e")
    var apnaSocket : BluetoothSocket? = null
    var btDevice: BluetoothDevice? = null
    var apnaServerSocket : BluetoothServerSocket?= null

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var binding: ActivityMainBinding

    var inStream: InputStream? =null
    var outStream: OutputStream? = null
    var buffer: ByteArray? = ByteArray(1024)
    var messageByteArray = "kem palty".toByteArray()

    val SELECT_DEVICE = 0
    val TAG ="Me hoo Gian, me hu bada takatvar"

    var currStr=""


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
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(
                this,
                "Abe bluetooth he nahi, chala bluetooth se chat karne",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnDiscover.setOnClickListener {
            val intent = Intent(this, AvailableDevicesActivity::class.java)
            startActivityForResult(intent, SELECT_DEVICE)
        }

        lifecycleScope.launch(Dispatchers.Main) {
            while(true) {
                if (btDevice == null) {
                    binding.clientButton.visibility = View.GONE
                }
                else {
                    binding.clientButton.visibility = View.VISIBLE
                    break
                }
                delay(500)
            }
        }

        binding.ServerBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {

                if(apnaServerSocket != null){
                    apnaServerSocket!!.close()
                }
                apnaSocket?.close()

                apnaServerSocket =
                    bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                        "BT_SERVICE",
                        MY_UUID
                    )
                var shouldLoop = true
                while (shouldLoop) {
                    Log.d("Log", "Entered loop")
                    apnaSocket = try {
                        Log.d("Log", "inside the try")
                        apnaServerSocket?.accept()
                    } catch (e: IOException) {
                        Log.e("Log", "Socket's accept() method failed", e)
                        shouldLoop = false
                        null
                    }
                    apnaSocket?.also {
                        apnaServerSocket?.close()
                        shouldLoop = false
                    }
                    Log.d(TAG, apnaSocket.toString())
                }

                Log.d("Log", "exited loop")
                withContext(Dispatchers.Main){
                    Toast.makeText(
                        this@MainActivity,
                        "Connected to ${apnaSocket.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                inStream =apnaSocket?.inputStream
                outStream=apnaSocket?.outputStream
                while (true) {
                    // Read from the InputStream.
                    var numBytes = try {
                        inStream?.read(buffer)
                    }
                    catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    Log.d(TAG,"numBytes: $numBytes")
                    Log.d(TAG,"buffer to string is ${buffer?.toString()}")
                    Log.d(TAG,"buffer to string is ${buffer?.decodeToString()}")
                    val readMessage= String(buffer!!, 0, buffer!!.size)
                    Log.d(TAG,"readMessage string is $readMessage")

                }
            }
        }

        binding.clientButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                //code for client
                if(apnaServerSocket != null){
                    apnaServerSocket!!.close()
                }
                apnaSocket?.close()
                bluetoothAdapter?.cancelDiscovery()
                btDevice?.let {
                    apnaSocket = it.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                    apnaSocket?.connect()
                    Log.d("Log", apnaSocket.toString())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,"Connected to ${apnaSocket?.remoteDevice?.name} \n ${apnaSocket.toString()}",Toast.LENGTH_SHORT).show()
                }
                inStream =apnaSocket?.inputStream
                outStream=apnaSocket?.outputStream

                try{
                    outStream?.write(1234)
                    if(outStream==null){
                        Log.d(TAG,"outStream is null")
                    }
                }
                catch (e: IOException) {
                    Log.e(TAG, "Error occurred when sending data", e)
                }
                val reader = BufferedReader(InputStreamReader(inStream))

                while (true) {
                    var currStr=""
                    var numBytes = try {
                        currStr = reader.readLine()
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(this@MainActivity,currStr,Toast.LENGTH_SHORT).show()
//                        }
                        Log.d(TAG,currStr)
                        buffer = ByteArray(1024)                            //we have to clear byteArray
                    }
                    catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }
                }
            }
        }

        binding.btnSendData.setOnClickListener {
            inStream =apnaSocket?.inputStream
            outStream=apnaSocket?.outputStream

            try{
                outStream?.write(0)
                if(outStream==null){
                    Log.d(TAG,"outStream is null")
                }
            }
            catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_DEVICE && resultCode == RESULT_OK) {
            val name = data?.getStringExtra("devName")
            val address = data!!.getStringExtra("devAddress")
            btDevice= bluetoothAdapter?.getRemoteDevice(address)
            if(btDevice ==null){
                Toast.makeText(this,"btDevice is null",Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(this,name +"\n"+address,Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        apnaSocket?.close()
        bluetoothAdapter?.cancelDiscovery()
        bluetoothAdapter?.disable()
    }

}