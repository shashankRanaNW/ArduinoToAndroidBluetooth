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
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.*
import java.io.*
import java.util.*

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

    val SELECT_DEVICE = 0
    val TAG ="Tag1"
    var currStr=""
    var heartRateStr = ""
    var timeStr = ""

    var ECGDataList = mutableListOf<Int>()
    var timeStampList = mutableListOf<Long>()

    lateinit var py :Python
    lateinit var module : PyObject


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)

        Log.d(TAG,"bef setup")
        //setting up python
        lifecycleScope.launch(Dispatchers.Default) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this@MainActivity))
            }
            py = Python.getInstance()
            module = py.getModule("heartdata_to_heartrate")
            Log.d(TAG,"pyhton set up")
        }

        //       asking for permissions
        Log.d(TAG,"bef permission")
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
        Log.d(TAG,"bef bm")
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(
                this,
                "Abe bluetooth he nahi, chala bluetooth se chat karne",
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.d(TAG,"post bm")

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
                    try {
                        apnaSocket?.connect()
                        Log.d("Log", apnaSocket.toString())

                    }
                    catch(e: IOException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,"Connected to ${apnaSocket?.remoteDevice?.name} \n ${apnaSocket.toString()}",Toast.LENGTH_SHORT).show()
                }
                inStream =apnaSocket?.inputStream
                outStream=apnaSocket?.outputStream

                try{
                    outStream?.write(1)
                    if(outStream==null){
                        Log.d(TAG,"outStream is null")
                    }
                }
                catch (e: IOException) {
                    Log.e(TAG, "Error occurred when sending data", e)
                }
                val reader = BufferedReader(InputStreamReader(inStream))

                var beforeLoopTime = System.currentTimeMillis()
                while (true) {
                    try{

                        currStr = reader.readLine()
                        withContext(Dispatchers.Main) {
                            val compositeData = currStr.toLong()
                            ECGDataList.add((compositeData % 10000).toInt())
                            timeStampList.add(compositeData / 10000)
                        }
                        buffer = ByteArray(1024)                            //we have to clear byteArray
                    }
                    catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@MainActivity,e.message, Toast.LENGTH_LONG).show()
                        }
                        break
                    }

                    if( (System.currentTimeMillis() - beforeLoopTime) > 20000 ){

                        try {

                            Log.d("HeartString",ECGDataList.toString())
                            Log.d("TimeString",timeStampList.toString())

                            withContext(Dispatchers.Main) {
                                val dataList = module.callAttr("get_bpm_metric", ECGDataList.toIntArray(), timeStampList.toLongArray() ).asList()
                                binding.tvHeartRate.text = dataList.get(0).toString()
                                binding.tvSDNN.text = dataList.get(1).toString()
                                Log.d(TAG, dataList.get(2).toString() )
                                Log.d("Contents of List", dataList.toString())
                                ECGDataList.clear()
                                timeStampList.clear()
                            }
                            beforeLoopTime = System.currentTimeMillis()

                        }
                        catch(e: PyException){
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                                Log.e("Error in python script",e.message + "\n" + e.cause + "\n" + e.toString())
                                Log.e("Length",timeStr.length.toString())
                            }
                        }

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