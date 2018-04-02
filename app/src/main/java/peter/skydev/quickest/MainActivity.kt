package peter.skydev.quickest

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var TAG: String = "MainActivity"
    var start: Long = 0
    var red: Boolean = false
    var numIter: Int = 5
    var numActualIter: Int = 0
    var arrScore = LongArray(numIter)

    fun init() {
        this.start = 0
        this.red = false
        textToClick.setBackgroundColor(resources.getColor(R.color.colorGreen))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        startIter()

        textToClick.setOnClickListener {
            //if (!red)
            // TODO mensaje de error o reiniciar para que no haya trampas
        }
    }

    fun startIter() {
        var randomMinutes: Double = 5 + (Math.random() * (10 - 5))
        Log.d(TAG, "Tiempo: " + randomMinutes)

        val handler = Handler()
        handler.postDelayed({
            red = true
            textToClick.setBackgroundColor(resources.getColor(R.color.colorRed))
            start = System.currentTimeMillis()
            textToClick.setOnClickListener {
                val elapsedTime = System.currentTimeMillis() - start
                Log.d(TAG, "Tiempo Total: " + elapsedTime)

                arrScore.set(numActualIter, elapsedTime)

                // TODO mostrar tiempo de la iteración

                numActualIter++
                if (numActualIter == numIter) {
                    // TODO mostrar media total de las iteraciones, es la puntuacion que se subirá a FIrebase
                    var totalScore: Long = 0
                    for (scoreIter: Long in arrScore) {
                        totalScore += scoreIter
                    }
                    var finalScore = totalScore/numIter
                    Log.d(TAG, "Tiempo medio: " + finalScore)

                } else {
                    init()
                    startIter()
                }
            }
        }, Math.round(randomMinutes * 1000))
    }
}
