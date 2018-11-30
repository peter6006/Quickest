package peter.skydev.quickest

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import java.util.*


class Game2 : Activity() {
    var circleX: Float = 0.0F
    var circleY: Float = 0.0F
    var circleR: Float = 100.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MyView(this))
    }

    inner class MyView(context: Context) : View(context) {
        internal var paint: Paint? = null
        internal var canvas: Canvas? = null

        init {
            paint = Paint()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            this.canvas = canvas
            drawCircle()
        }

        fun drawCircle(){
            val r = Random()
            val r2 = Random()
            circleX = 0 + r.nextFloat() * (height - 0)
            circleY = 0+ r2.nextFloat() * (width - 0)

            if(circleX.equals(0.0))
                circleX = height.toFloat()/2

            if(circleY.equals(0.0))
                circleY = width.toFloat()/2

            canvas!!.drawColor(resources.getColor(R.color.colorGreen))
            paint!!.setColor(resources.getColor(R.color.colorRed))
            canvas!!.drawCircle(circleX, circleY, circleR, paint)

        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    if(isInside(event.getX(), event.getY())){
//                        drawCircle()
                    }
                }
            }
            return false
        }

        fun isInside(x: Float, y: Float): Boolean {
            return (x - circleX) * (x - circleX) + (y - circleY) * (y - circleY) <= circleR * circleR
        }
    }
}
