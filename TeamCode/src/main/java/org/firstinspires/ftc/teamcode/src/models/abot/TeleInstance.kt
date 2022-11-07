package org.firstinspires.ftc.teamcode.src.models.abot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import kotlin.math.*

class TeleInstance (Instance: LinearOpMode, hardware: HardwareMap){
    val fl: DcMotor = hardware.get("FL") as DcMotor
    val fr: DcMotor = hardware.get("FR") as DcMotor
    val br: DcMotor = hardware.get("BR") as DcMotor
    val bl: DcMotor = hardware.get("BL") as DcMotor

    val extArm: DcMotor = hardware.get("Extendo") as DcMotor
    val extLift: DcMotor = hardware.get("Elevato") as DcMotor
    val cupArm: DcMotor = hardware.get("cupArm") as DcMotor

    val xAxis = hardware.get("xAxis") as DigitalChannel
    val yAxis = hardware.get("yAxis") as DigitalChannel

    val gripX = hardware.get("grip") as Servo
    val gripY = hardware.get("dropper") as Servo

    var cupAngle = (cupArm.currentPosition / 360)
    private var ticksPerDegree = (288/360)
    private val instance = Instance
    private var process = false
    enum class Direction {
        FORWARD, BACKWARD, UP, DOWN
    }
    init {
        // Set Each Wheel Direction
        fl.direction = DcMotorSimple.Direction.FORWARD
        fr.direction = DcMotorSimple.Direction.REVERSE
        bl.direction = DcMotorSimple.Direction.FORWARD
        br.direction = DcMotorSimple.Direction.REVERSE

        // Behaviour when Motor Power = 0
        fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set Encoder Mode
        extArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        cupArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        extArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        extLift.mode = DcMotor.RunMode.RUN_USING_ENCODER
        cupArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    fun gamePadOne(gamePad: Gamepad = instance.gamepad1) {
        moveControls(gamePad)
        cupArmMove(gamePad)
        cupHand(gamePad)
    }
    fun gamePadTwo(gamePad: Gamepad = instance.gamepad2) {
        liftMove(gamePad)
        liftHand(gamePad)
        armMove(gamePad)
        cycle(gamePad)
    }

    private val cycle = ScoreCycle(instance, this)

    private fun cupArm(direction: Direction, power: Double) {
        when (direction) {
            Direction.FORWARD -> {
                if (cupArm.currentPosition <= 110 * ticksPerDegree) {
                    cupArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
                    cupArm.power = 0.0
                } else {
                    cupArm.power = power
                }
            }
            Direction.BACKWARD -> {
                if (cupArm.currentPosition >= 70 * ticksPerDegree) {
                    cupArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
                    cupArm.power = 0.0
                } else {
                    cupArm.power = power
                }
            }
            else -> {
                cupArm.power = -0.0
            }
        }
    }
    private fun lift(direction: Direction, power: Double) {
        when (direction) {
            Direction.UP -> {
                if (extLift.currentPosition >= 1000) {
                    extLift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                    extLift.power = 0.0
                } else {
                    extLift.power = power
                }
            }
            Direction.DOWN -> {
                if (extLift.currentPosition <= 0) {
                    extLift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                    extLift.power = 0.0
                } else {
                    extLift.power = -power
                }
            }
            else -> {
                extLift.power = -0.0
            }
        }
    }
    private fun arm(direction: Direction, power: Double) {
        when (direction) {
            Direction.FORWARD -> {
                if (extArm.currentPosition >= 1000) {
                    extArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                    extArm.power = 0.0
                } else {
                    extArm.power = power
                }
            }
            Direction.BACKWARD -> {
                if (extArm.currentPosition <= 0) {
                    extArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                    extArm.power = 0.0
                } else {
                    extArm.power = -power
                }
            }
            else -> {
                extArm.power = -0.0
            }
        }
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
    private fun strafe(power: Double){
        fl.power = power
        fr.power = -power
        br.power = power
        bl.power = -power
    }

    private fun moveControls(gamePad: Gamepad) {
        val y = gamePad.left_stick_y
        val x = gamePad.left_stick_x
        val deadZone = 0.2
        val pow = 0.9

        if (abs(y) > abs(x) && y < -deadZone) {
            forward(pow)
        } else if (abs(y) > abs(x) && y > deadZone) {
            forward(-pow)
        } else if ((abs(y) < abs(x) && x < -deadZone)) {
            turn(-pow)
        } else if (abs(y) < abs(x) && x > deadZone) {
            turn(pow)
        } else if (gamePad.dpad_up) {
            forward(pow/2.5)
        } else if (gamePad.dpad_down) {
            forward(-pow/2.5)
        } else if (gamePad.dpad_left) {
            turn(-pow/2.5)
        } else if (gamePad.dpad_right) {
            turn(pow/2.5)
        } else if (gamePad.left_bumper){
            strafe(pow)
        } else if (gamePad.right_bumper){
            strafe(-pow)
        } else if (gamePad.left_trigger > 0.0){
            strafe(pow/2.5 * gamePad.left_trigger)
        } else if (gamePad.right_trigger > 0.0){
            strafe(pow/2.5 * gamePad.left_trigger)
        } else {
            forward(0.0)
        }
    }
    private fun cupArmMove(gamePad: Gamepad) {
        if (gamePad.x) {
            cupArm(Direction.FORWARD, 0.5)
        } else if (gamePad.b) {
            cupArm(Direction.BACKWARD, 0.5)
        } else {
            cupArm(Direction.FORWARD, 0.0)
        }
    }
    private fun armMove(gamePad: Gamepad) {
        if (gamePad.dpad_left) {
            arm(Direction.FORWARD, 0.5)
        } else if (gamePad.dpad_right) {
            arm(Direction.BACKWARD, 0.5)
        } else {
            arm(Direction.FORWARD, 0.0)
        }
    }
    private fun cupHand(gamePad: Gamepad){
        if (gamePad.a){
            gripX.position = 0.0
        } else if (gamePad.y){
            gripX.position = 1.0
        }
    }
    private fun liftMove(gamePad: Gamepad) {
        if (gamePad.dpad_up) {
            lift(Direction.UP, 0.9)
        } else if (gamePad.dpad_down) {
            lift(Direction.DOWN, 0.9)
        } else {
            lift(Direction.UP, 0.0)
        }
    }
    private fun liftHand(gamePad: Gamepad){
        if (gamePad.a){
            gripY.position = 0.0
        } else if (gamePad.y){
            gripY.position = 1.0
        }
    }
    private fun cycle(gamePad: Gamepad) {
        if (gamePad.share && !process) {
            process = true
            cycle.unit_test()
        } else if (gamePad.share && process) {
            process = false
            cycle.robotState = ScoreCycle.RobotState.DONE
        }
    }
}