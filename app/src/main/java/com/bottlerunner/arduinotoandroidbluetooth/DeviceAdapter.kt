package com.bottlerunner.arduinotoandroidbluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class DeviceAdapter(var context: Context, var deviceList: MutableList<BluetoothDevice>)
    : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    var onDeviceInfoListener: OnDeviceInfoListener =context as OnDeviceInfoListener                 //Danger zone

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.device_card, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {

        val tvDeviceName: TextView = holder.itemView.findViewById(R.id.tvName)
        val tvId: TextView = holder.itemView.findViewById(R.id.tvId)

        tvDeviceName.text = deviceList[position].name
        tvId.text =deviceList[position].address

        holder.itemView.setOnClickListener {
            Log.d("Log",deviceList[position].toString())
            val intent = Intent()

            intent.putExtra("devName",deviceList[position].name)
            intent.putExtra("devAddress", deviceList[position].address)

            onDeviceInfoListener.onDeviceInfoListener(intent)

        }

    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    interface OnDeviceInfoListener{
        fun onDeviceInfoListener(intent: Intent)
    }
}