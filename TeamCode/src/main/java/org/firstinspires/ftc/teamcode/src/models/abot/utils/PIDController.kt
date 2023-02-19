package org.firstinspires.ftc.teamcode.src.models.abot.utils

/**
 * Construct PID controller
 * @param Kp Proportional coefficient
 * @param Kp Integral coefficient
 * @param Kp Derivative coefficient
 */
class PIDController(Kp: Double, Ki: Double, Kd: Double) {
    val p = Kp
    val i = Ki
    val d = Kd
    var t = 0.0

    /**
     * Update the PID Controller Output
     * @param target Reference Position (Where we want to be)
     * @param state Current Position (Where we are)
     * @return Motor Power
     */
    fun update(target: Double, state: Double): Double{
        t = target
        // P
        var error = state

        val out = 0.1

        return out
    }
}