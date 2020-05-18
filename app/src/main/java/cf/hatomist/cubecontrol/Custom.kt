package cf.hatomist.cubecontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_custom.*
import kotlin.experimental.xor

const val id_append = 37000

fun shl(x: Int, y: Int): Int {
    return x.shl(y)
}

@ExperimentalStdlibApi
class Custom : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        for (i in 0 until Global.cube_size)
        {
            val ll_vert = LinearLayout(this)
            ll_vert.orientation = LinearLayout.HORIZONTAL
            ll.addView(ll_vert)

            for (j in 0 until Global.cube_size)
            {
                val btn = Button(this)
                btn.text = "0"
                btn.id = id_append + j + i * Global.cube_size
                ll_vert.addView(btn)
                btn.layoutParams.width = 1000 / Global.cube_size

                btn.setOnClickListener {
                    val col = (btn.id - id_append).rem(Global.cube_size)
                    val row = (btn.id - id_append).div(Global.cube_size)
                    Global.matrix[Global.cur_layer][row] = Global.matrix[Global.cur_layer][row].xor(
                        shl(0x1, Global.cube_size - col - 1).toByte()
                    )

                    Global.display()

                    for (k in 0 until Global.cube_size * Global.cube_size) {
                        if ((Global.matrix[Global.cur_layer][row].toInt() and 0x1.shl(Global.cube_size - col - 1)) == 0)
                            btn.text = "0"
                        else
                            btn.text = "1"
                    }


                }
            }
        }

        val sb = SeekBar(this)
        val txt = TextView(this)
        txt.text = "Current layer: ${Global.cur_layer + 1}"
        sb.max = Global.cube_size - 1
        sb.progress = 0
        sb.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Global.cur_layer = progress
                txt.text = "Current layer: ${Global.cur_layer + 1}"
                for (i in 0 until Global.cube_size * Global.cube_size) {
                    val btn = findViewById<Button>(id_append + i)
                    val col = (btn.id - id_append).rem(Global.cube_size)
                    val row = (btn.id - id_append).div(Global.cube_size)
                    if ((Global.matrix[Global.cur_layer][row].toInt() and 0x1.shl(Global.cube_size - col - 1)) == 0)
                        btn.text = "0"
                    else
                        btn.text = "1"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        ll.addView(sb)
        ll.addView(txt)
        val btn_clear = Button(this)
        btn_clear.text = "Clear cube"
        btn_clear.setOnClickListener {
            Global.nullify()
            Global.display()
            for (i in 0 until Global.cube_size * Global.cube_size) {
                val btn = findViewById<Button>(id_append + i)
                val col = (btn.id - id_append).rem(Global.cube_size)
                val row = (btn.id - id_append).div(Global.cube_size)
                if ((Global.matrix[Global.cur_layer][row].toInt() and 0x1.shl(Global.cube_size - col - 1)) == 0)
                    btn.text = "0"
                else
                    btn.text = "1"
            }
        }
        ll.addView(btn_clear)

    }
}
