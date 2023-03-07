package org.firstinspires.ftc.teamcode.src.models.abot.utils

import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.*

/**
 * Construct PID controller
 * @param Kp Proportional coefficient
 * @param Ki Integral coefficient
 * @param Kd Derivative coefficient
 */
class PIDController(Kp: Double, Ki: Double, Kd: Double) {
    private val p = Kp
    private val i = Ki
    private val d = Kd
    private val timer = ElapsedTime(ElapsedTime.Resolution.MILLISECONDS)

    private val a = 0.8
    private val maxIntegralSum = 0

    private var lastError = 0.0
    private var integralSum = 0.0
    private var lastSlope = 0.0
    private var lastTime = 0.0

    private var previousFilterEstimate = 0.0

    fun encoderUpdate(current: Double, reference: Double): Double {
        var error = reference - current
        val errorChange = error - lastError

        val currentFilterEstimate = (a * previousFilterEstimate) + (1-a) * errorChange
        previousFilterEstimate = currentFilterEstimate

        val derivative = currentFilterEstimate / timer.milliseconds()

        integralSum += (error * timer.milliseconds())
        return 0.0
    }

    fun angleUpdate(current: Double, reference: Double): Double {
        // Proportional Math
        var error: Double = reference - current // Difference between current and targeted angle
        error %= 360 // between +/- 360
        error += 360 // make sure error is positive
        error %= 360 // positive value is still in 360
        if (error > 180) {
            error -= 360 // Optimal turning
        }

        // Integral Math
        integralSum += error // accumulate error
        if (abs(error) < 1) integralSum = 0.0 // When the error is close to 1, stop moving
        integralSum = abs(integralSum) * sign(error)

        // Derivative Math
        var slope = 0.0
        if (lastTime > 0) slope = (error - lastError) / (timer.milliseconds() - lastTime)
        lastSlope = slope // Save last slope
        lastError = error // Save last error
        lastTime = timer.milliseconds() // Save last time

        return 0.1 * sign(error) + 0.9 * tanh(p * error + i * integralSum - d * slope)
    }
}