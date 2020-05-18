package cf.hatomist.cubecontrol

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_presets.*
import java.lang.Integer.min
import kotlin.concurrent.thread
import kotlin.experimental.xor
import kotlin.random.Random

@ExperimentalUnsignedTypes
class Presets : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presets)

        seekBar2.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Global.sleep_time = (50 * (progress + 1)).toLong()
                view_text_status2.text = "Delay: ${Global.sleep_time}ms, running: ${Global.running}"

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        random_btn.setOnClickListener{  // random
            if (Global.running) {
                Toast.makeText(applicationContext, "Have already running thread! Restarting...", Toast.LENGTH_SHORT).show()
                Global.running = false
                Thread.sleep(Global.sleep_time + 50)
            } else {
                Toast.makeText(applicationContext, "Started!", Toast.LENGTH_SHORT).show()
            }

            Global.running = true

            thread (start = true) {
                Global.nullify()
                while (Global.running) {
                    Global.display()
                    Thread.sleep(Global.sleep_time)
                    val x = Random.nextInt(0, Global.cube_size)
                    val y = Random.nextInt(0, Global.cube_size)
                    val z = Random.nextInt(0, Global.cube_size)

                    Global.matrix[x][y] = Global.matrix[x][y] xor (1 shl z).toByte()

                    Global.display()
                    Thread.sleep(Global.sleep_time)

                }
                Global.nullify()
                Global.display()
            }
        }

        sand_clock_btn.setOnClickListener{  // sand clock
            if (Global.running) {
                Toast.makeText(applicationContext, "Have already running thread! Restarting...", Toast.LENGTH_SHORT).show()
                Global.running = false
                Thread.sleep(Global.sleep_time + 50)
            } else {
                Toast.makeText(applicationContext, "Started!", Toast.LENGTH_SHORT).show()
            }

            Global.running = true

            thread (start = true) {
                Global.nullify()
                while (Global.running) {
                    Global.display()
                    Thread.sleep(Global.sleep_time)
                    for (i in 0 until Global.cube_size) {
                        val left = min(0 + i, Global.cube_size - 1 - i)
                        val right = Global.cube_size - left
                        for (j in left until right)
                            for (k in left until right)
                                Global.setVoxel(i, j, k);
                    }

                    Global.display()
                    Global.nullify()
                    if (!Global.running)
                        return@thread
                    Thread.sleep(Global.sleep_time)

                    for (i in 0 until Global.cube_size) {
                        val left = min(0 + i, Global.cube_size - 1 - i)
                        val right = Global.cube_size - left
                        for (j in left until right)
                            for (k in left until right)
                                Global.setVoxel(j, i, k);
                    }

                    Global.display()
                    Global.nullify()
                    if (!Global.running)
                        return@thread
                    Thread.sleep(Global.sleep_time)

                    for (i in 0 until Global.cube_size) {
                        val left = min(0 + i, Global.cube_size - 1 - i)
                        val right = Global.cube_size - left
                        for (j in left until right)
                            for (k in left until right)
                                Global.setVoxel(j, k, i);
                    }

                    Global.display()
                    Global.nullify()
                    if (!Global.running)
                        return@thread
                    Thread.sleep(Global.sleep_time)

                }
                Global.nullify()
                Global.display()
            }
        }

        rain_btn.setOnClickListener {
            if (Global.running) {
                Toast.makeText(applicationContext, "Have already running thread! Restarting...", Toast.LENGTH_SHORT).show()
                Global.running = false
                Thread.sleep(Global.sleep_time + 50)
            } else {
                Toast.makeText(applicationContext, "Started!", Toast.LENGTH_SHORT).show()
            }

            Global.running = true

            thread (start = true) {
//                Global.nullify()
//                var i = 0
//                var prev_index = Global.cube_size - 1
//                while (Global.running) {
//                    i = (i + 1) % Global.cube_size
//                    Global.setPlane(Global.XAXIS, i)
//                    Global.clearPlane(Global.XAXIS, prev_index)
//                    prev_index = i
//                    Global.display()
//                    Thread.sleep(Global.sleep_time + 50)
//                }
                while (Global.running) {
                    val x = Random.nextInt(0, Global.cube_size)
                    val y = Random.nextInt(0, Global.cube_size)
                    for (z in 0 until Global.cube_size)
                    {
                        Global.setVoxel(Global.cube_size - (z + 1), x, y)
                        if (z != 0)
                            Global.clearVoxel(Global.cube_size - z, x, y)
                        if (!Global.running)
                            return@thread
                        Global.display()
                        Thread.sleep(Global.sleep_time + 50)
                    }

                }

            }
        }

        stop_preset_btn.setOnClickListener {
            if (Global.running) {
                Toast.makeText(applicationContext, "Stopped!", Toast.LENGTH_SHORT).show()
                Global.running = false
                Thread.sleep(Global.sleep_time + 50)
            } else {
                Toast.makeText(applicationContext, "Thread is not running... already stopped", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
