package org.firstinspires.ftc.teamcode.src.models.abot.utils

import kotlin.math.PI

private const val BOT_RADIUS = 9.5//8.5
private const val COUNTS_PER_MOTOR_REV = 529.2 // eg: GoBILDA 312 RPM Yellow Jacket
private const val DRIVE_GEAR_REDUCTION = 1.0 // No External Gearing.
private const val WHEEL_DIAMETER_INCHES = 4.0 // For figuring circumference
private const val COUNTS_PER_INCH = COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION / (WHEEL_DIAMETER_INCHES * Math.PI)
fun inchToTick(inches: Double): Double {
    return inches * COUNTS_PER_INCH
}

fun targetDegrees(degrees: Double): Double {
    return inchToTick(((BOT_RADIUS * PI) / 180 ) * degrees) * 4/3
}

fun armDegrees(degree: Double): Double {
    return degree * (1120.0 / 360.0) //used to be 288/360 for core hex motor
}

fun arcDistance(degree: Double, radius: Double): Double {
    return targetDegrees(degree) * radius
}

fun liftDistance(inch: Double): Double {
    return inch * (1120.0 / (PI * 2.4 * 2.0)) // 1.5 being the diameter of the spool. Previously was 2.4
}

fun armDistance(inch: Double): Double {
    return inch * (118.0 / 3.0)
}

class Y_Slider {
    enum class States { BOTTOM, RISING, MIDDLE, DROPPING, TOP }
    val middle = liftDistance(9.0)
    val top = liftDistance(21.0)
    val power: Double = 1.0
    val time = 1500
}
class X_Slider {
    enum class States { IN, EXTENDING, READY, MIDDLE,  RETRACTING, OUT, ADJUSTING }
    val middle = armDistance(12.0)
    val time = 1200
    val power: Double = 0.75
}
class Arm {
    enum class States { DOWN, ASCENDING, RECEIVE, DESCENDING }
    val power = 0.75
    val down = armDegrees(30.0)
    val middle = armDegrees(50.0)
    val receive = armDegrees(85.0)
}
class X_Grip {
    enum class States { OPEN, OPENING, CLOSED, CLOSING, READY }
    val open = 0.25
    val close = 1.0
    val ready = 0.5
    val time = 666
}
class Y_Grip {
    enum class States { DUMP, DUMPING, RECEIVE, RECEIVING }
    val dump = 1.0
    val receive = 0.0
    val time = 1100
}
class Z_Grip {
    enum class States { L1, L2, L3, L4, L5 }
    val L1 = 1.0
    val L2 = 0.45
    val L3 = 0.35
    val L4 = 0.25
    val L5 = 0.0
}
class Timings {
    var gripYTime: Long = 0
    var gripXTime: Long = 0
    var extTimeOut: Long = 0
    var liftTimeOut: Long = 0
    var cupArmTimeOut: Long = 0
}
enum class RobotState { DROPPING, EARLYSCORE, SCORING, RETRACTING, DONE }
enum class Directions {
    UP, DOWN, EXTEND, RETRACT, OPEN, CLOSE, DUMP, RECEIVE, READY, DONE, STOP, ADJUST
}