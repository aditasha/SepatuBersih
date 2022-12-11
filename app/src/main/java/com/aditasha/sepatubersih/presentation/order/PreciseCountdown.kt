package com.aditasha.sepatubersih.presentation.order

import java.util.*

abstract class PreciseCountdown(totalTime: Long, interval: Long, delay: Long = 0) : Timer() {
    private var totalTime = 0L
    private var interval = 0L
    private var delay = 0L
    private var task: TimerTask
    private var startTime = -1L
    private var timeLeft = 0L
    private var restart = false
    private var wasCancelled = false
    private var wasStarted = false

    init {
        this.delay = delay
        this.interval = interval
        this.totalTime = totalTime
        this.task = getTask(totalTime)
    }

    fun start() {
        wasStarted = true
        this.scheduleAtFixedRate(task, delay, interval)
    }

    fun restart() {
        if (!wasStarted) {
            start()
        } else if (wasCancelled) {
            wasCancelled = false
            task = getTask(totalTime)
            start()
        } else {
            restart = true
        }
    }

    fun pause() {
        wasCancelled = true
        task.cancel()
    }

    fun resume() {
        wasCancelled = false
        task = getTask(timeLeft)
        startTime = -1
        start()
    }

    fun stop() {
        wasCancelled = true
        task.cancel()
    }

    // Call this when there's no further use for this timer
    fun dispose() {
        cancel()
        purge()
    }

    private fun getTask(totalTime: Long): TimerTask {
        return object : TimerTask() {
            override fun run() {
                if (startTime < 0 || restart) {
                    startTime = scheduledExecutionTime()
                    timeLeft = totalTime
                    restart = false
                } else {
                    timeLeft = totalTime - (scheduledExecutionTime() - startTime)
                    if (timeLeft <= 0) {
                        this.cancel()
                        startTime = -1
                        onFinished()
                        return
                    }
                }
                onTick(timeLeft)
            }
        }
    }

    abstract fun onTick(timeLeft: Long)
    abstract fun onFinished()
}