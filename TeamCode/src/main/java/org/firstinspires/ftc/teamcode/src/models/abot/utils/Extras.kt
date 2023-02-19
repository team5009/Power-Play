package org.firstinspires.ftc.teamcode.src.models.abot.utils

private const val BOT_RADIUS = 9.5//8.5
private const val COUNTS_PER_MOTOR_REV = 529.2 // eg: GoBILDA 312 RPM Yellow Jacket
private const val DRIVE_GEAR_REDUCTION = 1.0 // No External Gearing.
private const val WHEEL_DIAMETER_INCHES = 4.0 // For figuring circumference
private const val COUNTS_PER_INCH = COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION / (WHEEL_DIAMETER_INCHES * Math.PI)
fun inchToTick(inches: Double): Double {
    return inches * COUNTS_PER_INCH
}

fun targetDegrees(degrees: Double): Double {
    return inchToTick(((BOT_RADIUS * Math.PI) / 180 ) * degrees) * 4/3
}

fun armDegrees(degree: Double): Double {
    return degree * (288.0 / 360.0)
}

fun arcDistance(degree: Double, radius: Double): Double {
    return targetDegrees(degree) * radius
}

fun liftDistance(inch: Double): Double {
    return inch * (1.5 * 118.0 / 2.4)
}

fun armDistance(inch: Double): Double {
    return inch * (118.0 / 3.0)
}

class Y_Slider {
    enum class States { BOTTOM, RISING, MIDDLE, DROPPING, TOP }
    val middle = liftDistance(4.5)
    val top = liftDistance(17.5)
    val power: Double = 0.9
    val time = 1500
}
class X_Slider {
    enum class States { IN, EXTENDING, READY, MIDDLE, RETRACTING, OUT }
    val middle = armDistance(12.0)
    val outside = armDistance(24.0)
    val time = 1200
    val power: Double = 0.7
}
class Arm {
    enum class States { DOWN, ASCENDING, RECEIVE, DESCENDING }
    val power = 0.65
    val down = armDegrees(30.0)
    val receive = armDegrees(90.0)
}
class X_Grip {
    enum class States { OPEN, OPENING, CLOSED, CLOSING, READY }
    val open = 0.0
    val close = 1.0
    val ready = 0.5
    val time = 666
}
class Y_Grip {
    enum class States { DUMP, DUMPING, MIDDLE, RECEIVE, RECEIVING }
    val dump = 1.0
    val middle = 0.5
    val receive = 0.0
    val time = 1000
}
class Z_Grip {
    enum class States { L1, L2, L3, L4, L5 }
    val L1 = 0.05
    val L2 = 0.28
    val L3 = 0.38
    val L4 = 0.48
    val L5 = 0.59
    val high = 0.55
    val low = 0.45
    val middle = 0.35
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
    UP, DOWN, EXTEND, RETRACT, OPEN, CLOSE, DUMP, RECEIVE, READY, DONE, STOP
}