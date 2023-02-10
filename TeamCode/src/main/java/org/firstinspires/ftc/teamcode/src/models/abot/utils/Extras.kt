package org.firstinspires.ftc.teamcode.src.models.abot.utils

private const val BOT_RADIUS = 9.5//8.5
private const val COUNTS_PER_MOTOR_REV = 1120 // eg: GoBILDA 312 RPM Yellow Jacket
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

class Lift {
    val middle = liftDistance(12.0)
    val top = liftDistance(24.0)
    val power: Double = 0.9
}
class GripX {
    val open = 0.0
    val close = 1.0
    val ready = 0.5
}
class GripY {
    val dump = 1.0
    val middle = 0.45
    val receive = 0.24
}
class GripZ {
    val high = 0.55
    val low = 0.45
    val middle = 0.35
}