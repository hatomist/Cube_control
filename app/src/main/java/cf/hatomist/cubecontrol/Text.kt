package cf.hatomist.cubecontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_text.*
import kotlin.concurrent.thread

val char_map = mapOf(
    'A' to byteArrayOf(
        0b00100,
        0b01010,
        0b01010,
        0b01110,
        0b10001
    ),
    'B' to byteArrayOf(
        0b00111,
        0b01001,
        0b00111,
        0b01001,
        0b00111
    ),
    'T' to byteArrayOf(
        0b11111,
        0b00100,
        0b00100,
        0b00100,
        0b00100
    ),
    'K' to byteArrayOf(
        0b01001,
        0b00101,
        0b00011,
        0b00101,
        0b01001
    ),
    '+' to byteArrayOf(
        0b00100,
        0b00100,
        0b11111,
        0b00100,
        0b00100
    ),
    'Y' to byteArrayOf(
        0b10010,
        0b10100,
        0b11000,
        0b10000,
        0b01100
    ),
    'C' to byteArrayOf(
        0b01111,
        0b00001,
        0b00001,
        0b00001,
        0b01111
    ),
    '=' to byteArrayOf(
        0b00000,
        0b11111,
        0b00000,
        0b11111,
        0b00000
    ),
    '1' to byteArrayOf(
        0b010010,
        0b101101,
        0b010010,
        0b001100,
        0b000000
    )
).withDefault {
    byteArrayOf(
        0b000000,
        0b000000,
        0b000000,
        0b000000,
        0b000000
    )
}

@ExperimentalUnsignedTypes
class Text : AppCompatActivity() {

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Global.sleep_time = (50 * (progress + 1)).toLong()
                view_text_status.text = "Delay: ${Global.sleep_time}ms, text: \"${Global.change_text}\", running: ${Global.running}"

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        btn_text_set.setOnClickListener {
            if (editText.text.toString() != "")
                Global.change_text = editText.text.toString().toUpperCase()
            else
                Toast.makeText(applicationContext, "Invalid string", Toast.LENGTH_SHORT).show()
            Global.cur_displayed_symbol = 0
            view_text_status.text = "Delay: ${Global.sleep_time}ms, text: \"${Global.change_text}\", running: ${Global.running}"
        }

        btn_text_stop.setOnClickListener {
            if (Global.running) {
                Toast.makeText(applicationContext, "Stopped!", Toast.LENGTH_SHORT).show()
                Global.running = false
                Thread.sleep(Global.sleep_time + 50)
            } else {
                Toast.makeText(applicationContext, "Thread is not running... already stopped", Toast.LENGTH_SHORT).show()
            }
            view_text_status.text = "Delay: ${Global.sleep_time}ms, text: \"${Global.change_text}\", running: ${Global.running}"

        }

        btn_text_start.setOnClickListener {
            if (Global.running) {
                Toast.makeText(applicationContext, "Have already running thread! Restarting...", Toast.LENGTH_SHORT).show()
                Global.running = false
                Thread.sleep(Global.sleep_time + 50)
            } else {
                Toast.makeText(applicationContext, "Started!", Toast.LENGTH_SHORT).show()
            }

            Global.running = true
            Global.cur_displayed_symbol = 0
            Global.cur_layer = 4
            thread (start = true) {
                while (Global.running) {
                    Global.cur_layer++
                    if (Global.cur_layer == Global.cube_size) {
                        Global.cur_layer = 0
                        Global.cur_displayed_symbol++
                        if (Global.cur_displayed_symbol == Global.change_text.length)
                            Global.cur_displayed_symbol = 0
                    }

                    Global.nullify()
                    for (i in 0 until Global.cube_size)
                        Global.matrix[Global.cur_layer][i] = char_map.get(Global.change_text[Global.cur_displayed_symbol])?.get(i) ?: 0

                    Global.display()

                    Thread.sleep(Global.sleep_time)
                }
                Global.nullify()
                Global.display()
            }
            view_text_status.text = "Delay: ${Global.sleep_time}ms, text: \"${Global.change_text}\", running: ${Global.running}"
        }


    }
}
