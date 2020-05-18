package cf.hatomist.cubecontrol

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor



abstract class Global: Application() {
    @ExperimentalUnsignedTypes
    companion object {
        private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val cube_size = 5
        private const val bt_debug = true
        private var bt_adapter: BluetoothAdapter? = null
        private var bt_device: BluetoothDevice? = null
        private var bt_socket: BluetoothSocket? = null
        val matrix = Array(cube_size, {ByteArray(cube_size)})
        private val socket = Socket()
        var running = false
        var sleep_time: Long = 1000
        var change_text = " "
        var cur_displayed_symbol = 0
        var cur_layer = 0

        fun nullify(){
            for (i in 0 until cube_size)
                for (j in 0 until cube_size)
                    matrix[i][j] = 0
        }

        fun setVoxel(x: Int, y: Int, z: Int){
            matrix[cube_size-1 - z][cube_size-1 - x] = matrix[cube_size-1 - z][cube_size-1 - x] or ((0x01 shl y).toByte())
        }

        fun clearVoxel(x: Int, y: Int, z: Int){
            matrix[cube_size-1 - z][cube_size-1 - x] = matrix[cube_size-1 - z][cube_size-1 - x] xor ((0x01 shl y).toByte())
        }

        fun getVoxel(x: Int, y: Int, z: Int): Boolean {
            return (matrix[cube_size-1 - z][cube_size-1 - x] and matrix[cube_size-1 - z][cube_size-1 - x] xor ((0x01 shl y).toByte())) == ((0x01 shl y).toByte())
        }

        const val XAXIS = 0
        const val YAXIS = 1
        const val ZAXIS = 2

        fun setPlane(axis: Int, i: Int) {
            for (j in 0 until cube_size) {
                for (k in 0 until cube_size) {
                if (axis == XAXIS) {
                    setVoxel(i, j, k)
                } else if (axis == YAXIS) {
                    setVoxel(j, i, k)
                } else if (axis == ZAXIS) {
                    setVoxel(j, k, i)
                }
            }
            }
        }

        fun clearPlane(axis: Int, i: Int) {
            for (j in 0 until cube_size) {
                for (k in 0 until cube_size) {
                if (axis == XAXIS) {
                    clearVoxel(i, j, k)
                } else if (axis == YAXIS) {
                    clearVoxel(j, i, k)
                } else if (axis == ZAXIS) {
                    clearVoxel(j, k, i)
                }
            }
            }
        }

        fun connect()
        {
            bt_adapter = BluetoothAdapter.getDefaultAdapter()
            if (bt_adapter == null) {
                Log.println(Log.ERROR, "[Cube]", "No bluetooth adapters found!")
                return
            }
            if (bt_adapter?.isEnabled == false) {
                Log.println(Log.ERROR, "[Cube]", "Bluetooth adapter is not enabled!")
                return
            }
            Log.println(Log.INFO, "[Cube]", "Bluetooth is enabled.")

            val bondedDevices: Set<BluetoothDevice>? = bt_adapter?.bondedDevices

            if (bondedDevices != null) {
                for (dev in bondedDevices) {
                    Log.println(Log.INFO, "[Cube]", "Paired device: " + dev.name + " (" + dev.address + ")")
                if (dev.name == "LED CUBE") {
                        bt_device = dev
                        Log.println(Log.INFO, "[Cube]", "Got " + dev.name)
                    }
                }
            }

            if (bt_device == null) {
                Log.println(Log.ERROR, "[Cube]", "Target device not found.")
                return
            }

            try {
                bt_socket = bt_device?.createRfcommSocketToServiceRecord(sppUuid)
            } catch (ex: IOException) {
                Log.println(Log.ERROR, "[Cube]", "Failed to create RfComm socket.")
                return
            }
            Log.println(Log.INFO, "[Cube]", "Created RfComm socket")

            if (!bt_debug)
                for (i in 1..5) {
                    try {
                        bt_socket?.connect()
                    } catch (ex: IOException) {
                        if (i < 5) {
                            //                if (i < 0) {
                            Log.println(Log.ERROR, "[Cube]", "Failed to connect. Retrying: $ex")
                            continue
                        }
                        Log.println(Log.ERROR, "[Cube]", "Failed to connect $ex")
                        return
                    }
                    break
                }

            if (Global.bt_debug)
            {
                val policy =
                    StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                Global.socket.connect(InetSocketAddress("192.168.1.108", 8070))
                Log.println(Log.INFO, "[Cube]", Global.socket.isConnected.toString())
            }

            Log.println(Log.INFO, "[Cube]", "Connected to Bluetooth socket!")

        }

        private fun genCommand(): UByteArray {
            val tmp_list = mutableListOf<UByte>()
            tmp_list.add(0x01.toUByte())
            for (x in matrix) {
                for (y in x) {
                    tmp_list.add((y.toUInt() shl 3).toUByte())
                }
            }
            return tmp_list.toUByteArray()
        }

        fun display() {
            if (!bt_debug) {
//                bt_socket?.outputStream?.write(0x01)
//                Thread.sleep(50)

                bt_socket?.outputStream?.write(genCommand().toByteArray())

            }
            if (bt_debug) {
                for (elem in genCommand()) {
                    print(elem.toUByte().toString(2))
                    print(' ')
                }
                println()
                try {
                    socket.getOutputStream().apply {
                        write(genCommand().toByteArray())
                        flush()
                    }
                } catch (e: SocketException){}

            }
        }


    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        main_text_btn.setOnClickListener {
            val intent = Intent(this, Text::class.java)
            startActivity(intent)
        }
        main_custom_btn.setOnClickListener {
            val intent = Intent(this, Custom::class.java)
            startActivity(intent)
        }

        main_preset_btn.setOnClickListener {
            val intent = Intent(this, Presets::class.java)
            startActivity(intent)
        }

        Global.connect()

        Global.setVoxel(1,1,1)
        Global.display()
//        Global.write_bytes_array(byteArrayOf(-128, 127, 127))
        Toast.makeText(applicationContext, "Connected to Bluetooth socket!", Toast.LENGTH_SHORT).show()

//        for (x in Global.matrix) {
//            for (y in x) {
//                print(y)
//                print(' ')
//            }
//            println()
//        }
//        println("Flattened:")
//
//        Global.display()

    }
    


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
