package peter.skydev.quickest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import kotlinx.android.synthetic.main.activity_game.*
import org.jetbrains.anko.alert

class Game : Activity() {
    private var TAG: String = "Game"
    private var start: Long = 0
    private var red: Boolean = false
    private var numIter: Int = 5
    private var numActualIter: Int = 1
    private var arrScore = LongArray(numIter)
    private var globalText: String = ""

    private var customHandler = Handler()
    private var startTime = 0L
    private var timeToMilliSeconds = 0L
    private var timeSwapBuff = 0L
    private var updateTime = 0L
    val handler = Handler()

    private fun init() {
        this.start = 0
        this.red = false
        textToClick.setBackgroundColor(resources.getColor(R.color.colorRed))
        gameStep.text = "$numActualIter / $numIter"

        textToClick.setOnClickListener {
            if (!red) {
                alert(resources.getText(R.string.gameMSGError).toString()) {
                    title("Too soon")
                    cancellable(false)
                    positiveButton("OK") { startIter() }
                }.show()
                handler.removeCallbacksAndMessages(null)
            }
        }
    }

    var updateTimeThread: Runnable = object : Runnable {
        override fun run() {
            timeToMilliSeconds = SystemClock.uptimeMillis() - startTime
            updateTime = timeSwapBuff + timeToMilliSeconds
            var secs = updateTime / 1000
            secs %= 60
            var milliseconds = updateTime % 1000
            timerValue.text = "" + String.format("%2d", secs) + "." + String.format("%3d", milliseconds)
            customHandler.postDelayed(this, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        init()

        startIter()
    }

    private fun startIter() {
        val randomMinutes: Double = 5 + (Math.random() * (10 - 5))
        Log.d(this.TAG, "Tiempo: " + randomMinutes)


        handler.postDelayed({
            red = true
            textToClick.setBackgroundColor(resources.getColor(R.color.colorGreen))
            start = System.nanoTime()

            startTime = SystemClock.uptimeMillis()
            customHandler.postDelayed(updateTimeThread, 0)

            textToClick.setOnClickListener {
                textToClick.setOnClickListener {
                    if (!red)
                        alert(resources.getText(R.string.gameMSGError).toString()) {
                            title("Too soon")
                            cancellable(false)
                            positiveButton("OK") { startIter() }
                        }.show()
                    handler.removeCallbacksAndMessages(null)
                }

                val elapsedTime = System.nanoTime() - start
                Log.d(this.TAG, "Tiempo Total: " + elapsedTime)

                customHandler.removeCallbacks(updateTimeThread)

                this.globalText = globalText + "\n" + numActualIter + ": " + elapsedTime

                individualScoreTV.text = this.globalText

                arrScore.set(numActualIter - 1, elapsedTime)

                numActualIter++

                gameStep.text = "$numActualIter / $numIter"

                if (numActualIter > numIter) {
                    var totalScore: Long = 0
                    for (scoreIter: Long in arrScore) {
                        totalScore += scoreIter
                    }
                    val finalScore = totalScore / numIter
                    Log.d(this.TAG, "Tiempo medio: " + finalScore)

                    textToClick.text = resources.getText(R.string.gameMSGFinish)
                    textToClick.textSize = 32F

                    val handler2 = Handler()
                    handler2.postDelayed({
                        finish()

                        val intent = Intent(this, Result::class.java)
                        intent.putExtra("finalScore", finalScore)
                        startActivity(intent)
                    }, Math.round(1000.0))
                } else {
                    init()
                    startIter()
                }
            }
        }, Math.round(randomMinutes * 1000))
    }
}
