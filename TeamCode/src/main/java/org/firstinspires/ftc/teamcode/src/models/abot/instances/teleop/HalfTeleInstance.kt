package org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop

//import kotlinx.coroutines.DefaultExecutor.delay
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.teamcode.src.models.abot.utils.TouchSensor
import kotlin.math.abs

class HalfTeleInstance(Instance: LinearOpMode, hardware: HardwareMap) {
    private val touchSensor =
        TouchSensor()
    val fl: DcMotor = hardware.get("FL") as DcMotor
    val fr: DcMotor = hardware.get("FR") as DcMotor
    val br: DcMotor = hardware.get("BR") as DcMotor
    val bl: DcMotor = hardware.get("BL") as DcMotor

    val xAxis: DigitalChannel = touchSensor.get("xAxis", hardware)
    val yAxis: DigitalChannel = touchSensor.get("yAxis", hardware)


    val gripX: Servo = hardware.get("grip") as Servo
    val gripY: Servo = hardware.get("dropper") as Servo
    val gripZ: Servo = hardware.get("antler") as Servo

    var ticksPerDegree = 288.0 / 360.0
    private val instance = Instance

    enum class Direction {
        FORWARD, BACKWARD, UP, DOWN
    }

    init {
        // Set Each Wheel Direction
        fl.direction = DcMotorSimple.Direction.FORWARD
        fr.direction = DcMotorSimple.Direction.REVERSE
        bl.direction = DcMotorSimple.Direction.FORWARD
        br.direction = DcMotorSimple.Direction.REVERSE
//        gripZ.position = .2

        // Behaviour when Motor Power = 0
        fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE


    }

    suspend fun gamePadOne(gamePad: Gamepad = instance.gamepad1) {
        moveControls(gamePad)
        cupHand(gamePad)
    }

    suspend fun gamePadTwo(gamePad: Gamepad = instance.gamepad2) {
        liftHand(gamePad)
    }

    fun cupHandInit() {
        gripX.position = 0.2
    }

    fun liftHandInit() {
        gripY.position = 0.24
    }


    private fun forward(power: Double) {
        fl.power = power
        fr.power = power
        br.power = power
        bl.power = power
    }

    private fun turn(power: Double) {
        fl.power = -power
        fr.power = power
        br.power = power
        bl.power = -power
    }

    private fun strafe(power: Double) {
        fl.power = power
        fr.power = -power
        br.power = power
        bl.power = -power
    }

    private fun moveControls(gamePad: Gamepad) {
        val y = gamePad.left_stick_y
        val x = gamePad.left_stick_x
        val deadZone = 0.2
        val pow = 0.8

        if (abs(y) > abs(x) && y < -deadZone) {
            forward(pow)
        } else if (abs(y) > abs(x) && y > deadZone) {
            forward(-pow)
        } else if ((abs(y) < abs(x) && x < -deadZone)) {
            turn(-pow)
        } else if (abs(y) < abs(x) && x > deadZone) {
            turn(pow)
        } else if (gamePad.dpad_up) {
            forward(pow / 2.5)
        } else if (gamePad.dpad_down) {
            forward(-pow / 2.5)
        } else if (gamePad.dpad_left) {
            turn(-pow / 2.5)
        } else if (gamePad.dpad_right) {
            turn(pow / 2.5)
        } else if (gamePad.left_bumper) {
            strafe(pow)
        } else if (gamePad.right_bumper) {
            strafe(-pow)
        } else if (gamePad.left_trigger > 0.25) {
            strafe(pow / 2.5 * gamePad.left_trigger)
        } else if (gamePad.right_trigger > 0.25) {
            strafe(-(pow / 2.5 * gamePad.right_trigger))
        } else {
            forward(0.0)
        }
    }

    private fun cupHand(gamePad: Gamepad) {
        if (gamePad.a) {
            gripX.position = 0.0
        } else if (!gamePad.start && gamePad.y) {
            gripX.position = 1.0
        }
    }

    private fun liftHand(gamePad: Gamepad) {
        if (gamePad.left_bumper) {
            gripY.position = 0.24
        } else if (gamePad.right_bumper) {
            gripY.position = 1.0
        }
    }

    private fun liftDistance(distance: Double): Double {
        return distance * (1.5 * 118.0 / 2.4)
    }
}