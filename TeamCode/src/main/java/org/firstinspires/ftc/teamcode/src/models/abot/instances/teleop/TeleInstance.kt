package org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop

//import kotlinx.coroutines.DefaultExecutor.delay
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.teamcode.src.models.abot.cycles.teleop.TeleCycle
import org.firstinspires.ftc.teamcode.src.models.abot.instances.RobotClass
import org.firstinspires.ftc.teamcode.src.models.abot.utils.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

class TeleInstance(Instance: LinearOpMode) {
    val bot = RobotClass(Instance)
    val cupAngle = (bot.arm.currentPosition / 360.0)

    enum class Positions {OPEN, CLOSE, RECEIVED, DUMPED}

    var ticksPerDegree = 288.0 / 360.0
    private val instance = Instance
    val myCycle = TeleCycle(instance, this)
    var freeMove = false

    private val xSliderConst = X_Slider()
    private val ySliderConst = Y_Slider()
    private val armConst = Arm()
    private val xGripConst = X_Grip()
    private val yGripConst = Y_Grip()
    private val zGripConst = Z_Grip()

    private var cupHandPos: Positions = Positions.OPEN
    private var liftGripPos: Positions = Positions.RECEIVED

    var process = false
    private var yPressed = false
    private var isInit = true
    private var freeMovePress = false
    private var cupHandPress = false
    private var liftGripPress = false

    enum class Direction {
        FORWARD, BACKWARD, UP, DOWN
    }

    init {
        bot.zGrip.position = zGripConst.L1
        bot.yGrip.position = 0.45
        bot.xGrip.position = 0.0

        // Behaviour when Motor Power = 0
//        fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }

    fun resetEncoders() {
        bot.fl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.fr.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.bl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.br.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.ySlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.ySlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }
    private fun resetPartEncoders() {
        bot.xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    fun gamePadOne(gamePad: Gamepad = instance.gamepad1) {
        moveControls(gamePad)
//        moveControlsCar(gamePad)
        cupHand(gamePad)
        cupArmMove(gamePad)
        freeMove(gamePad)
    }

    fun gamePadTwo(gamePad: Gamepad = instance.gamepad2) {
        liftMove(gamePad)
        liftHand(gamePad)
        armMove(gamePad)
        cycleInit(gamePad)
        cycle(gamePad)
    }

    fun extArmInit() {
        bot.xSlider.power = -0.1
        while (instance.opModeIsActive() && bot.xSensor.state) { } // Wait for xAxis to be released
        bot.xSlider.power = 0.0

        bot.xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    fun cupHandInit() {
        bot.xGrip.position = 0.2
    }

    fun liftHandInit() {
        bot.yGrip.position = 0.24
    }

    fun cupArmInit() {
        bot.arm.power = 0.9
        while (instance.opModeIsActive() && bot.arm.currentPosition < 200.0) { } // Just go up a little bit
        bot.arm.power = 0.0
        Thread.sleep(250)
        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    private fun cupArm(direction: Direction, power: Double) {
        when (direction) {
            Direction.FORWARD -> {
                if (abs(bot.arm.currentPosition) <= (30 * ticksPerDegree) && !freeMove) {
                    bot.arm.power = 0.0
                    bot.arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
                } else {
                    bot.arm.power = -power
                }
            }
            Direction.BACKWARD -> {
                if (abs(bot.arm.currentPosition) >= (85 * ticksPerDegree) && !freeMove) {
                    bot.arm.power = 0.0
                    bot.arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
                } else {
                    bot.arm.power = power
                }
            }
            else -> {
                return
            }
        }
    }

    private fun lift(direction: Direction, power: Double) {
        when (direction) {
            Direction.UP -> {
                if (abs(bot.ySlider.currentPosition) >= liftDistance(15.0) && !freeMove) {
                    bot.ySlider.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                    bot.ySlider.power = 0.0
                } else {
                    bot.ySlider.power = power
                }
            }
            Direction.DOWN -> {
                if ((bot.ySensor.state || abs(bot.ySlider.currentPosition) <= 10) && !freeMove) {
                    bot.ySlider.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                    bot.ySlider.power = 0.0
                } else {
                    bot.ySlider.power = -power
                }
            }
            else -> {
                bot.ySlider.power = 0.0
            }
        }
    }

    private fun arm(direction: Direction, power: Double) {
        when (direction) {
            Direction.FORWARD -> {
                bot.xSlider.power = -power
            }
            Direction.BACKWARD -> {
                bot.xSlider.power = power
            }
            else -> {
                return
            }
        }
    }

    private fun forward(power: Double) {
        bot.fl.power = power
        bot.fr.power = power
        bot.br.power = power
        bot.bl.power = power
    }

    private fun pivot(power: Double) {
        bot.fl.power = power
        bot.fr.power = -power
        bot.br.power = -power
        bot.bl.power = power
    }

    private fun strafe(power: Double) {
        bot.fl.power = -power
        bot.fr.power = power
        bot.br.power = -power
        bot.bl.power = power
    }

    private fun turn(leftPow: Double, rightPow:Double) {
        bot.fl.power = leftPow
        bot.fr.power = rightPow
        bot.br.power = rightPow
        bot.bl.power = leftPow
    }
    fun setMotorPower(fl: Double, fr: Double, bl: Double, br:Double) {
        bot.fl.power = fl
        bot.fr.power = fr
        bot.bl.power = bl
        bot.br.power = br
    }
    private fun moveControls(gamePad: Gamepad) {
        val y = gamePad.left_stick_y
        val x = gamePad.left_stick_x
        val deadZone = 0.15
        val pow = 0.7

        if (abs(y) > abs(x) && y < -deadZone) {
            forward(pow * abs(y).pow(2f))
        } else if (abs(y) > abs(x) && y > deadZone) {
            forward(pow * -abs(y).pow(2f))
        } else if ((abs(y) < abs(x) && x < -deadZone)) {
            pivot(pow * -abs(y).pow(2f))
        } else if (abs(y) < abs(x) && x > deadZone) {
            pivot(pow * abs(y).pow(2f))
        } else if (gamePad.dpad_up) {
            forward(pow / 2.5)
        } else if (gamePad.dpad_down) {
            forward(-pow / 2.5)
        } else if (gamePad.dpad_left) {
            pivot(-pow / 2.5)
        } else if (gamePad.dpad_right) {
            pivot(pow / 2.5)
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

    private fun moveControlsCar(gp: Gamepad) {
        val leftY = gp.left_stick_y
        val leftX = gp.left_stick_x
        val rightY = gp.right_stick_y
        val rightX = gp.right_stick_x
        val deadZone = 0.2
        val threshHold = 0.15
        val pow = 0.7
        if (gp.right_trigger > threshHold && gp.left_trigger <= threshHold) {
            val power = gp.right_trigger.pow(2f).toDouble() * pow
            forward(power)

        }
        else if (gp.left_trigger > threshHold && gp.right_trigger <= threshHold) {
            val power = -(gp.left_trigger.pow(2f).toDouble() * pow)
            forward(power)
        }
        else if ((abs(leftY) < abs(leftX) && leftX < -deadZone) &&
            gp.left_trigger <= threshHold && gp.right_trigger <= threshHold)
        {
            strafe(pow * abs(leftX))

        }
        else if (abs(leftY) < abs(leftX) && leftX > deadZone &&
            gp.left_trigger <= threshHold && gp.right_trigger <= threshHold)
        {
            strafe(-pow * abs(leftX))

        }
        else if ((abs(rightY) < abs(rightX) && rightX < -deadZone) &&
            gp.left_trigger <= threshHold && gp.right_trigger <= threshHold)
        {
            pivot(-pow * abs(rightX))

        }
        else if (abs(rightY) < abs(rightX) && rightX > deadZone
            && gp.left_trigger <= threshHold && gp.right_trigger <= threshHold) {
            pivot(pow * abs(rightX))

        }
        else {
            forward(0.0)
        }
    }

    private fun moveControlsV2(gp: Gamepad) {
        val leftY = -gp.left_stick_y.toDouble()
        val leftX = gp.left_stick_x.toDouble() * 1.1
        val rightY = -gp.right_stick_y.toDouble()
        val rightX = gp.right_stick_x.toDouble()
        val deadZone = 0.2
        val threshHold = 0.15
        val pow = 0.7

        val denominator = max(abs(leftY) + abs(leftX) + abs(rightX), 1.0)

        setMotorPower(
            (leftY + leftX + rightX) / denominator,
            (leftY - leftX - rightX) / denominator,
            (leftY - leftX + rightX) / denominator,
            (leftY + leftX - rightX) / denominator
        )

    }

    private fun cupArmMove(gamePad: Gamepad) {
        if (gamePad.x && !process) {
            cupArm(Direction.BACKWARD, 0.9)
        } else if (gamePad.b && !process) {
            cupArm(Direction.FORWARD, 0.9)
        } else if (!process) {
            cupArm(Direction.FORWARD, 0.0)
        }
    }

    private fun armMove(gamePad: Gamepad) {
        if (gamePad.dpad_right) {
            arm(Direction.BACKWARD, 0.5)
        } else if (gamePad.dpad_left) {
            arm(Direction.FORWARD, 0.5)
        } else {
            arm(Direction.FORWARD, 0.0)
        }
    }

    private fun cupHand(gamePad: Gamepad) {
        if (gamePad.a && !cupHandPress && cupHandPos == Positions.CLOSE) {
            cupHandPress = true
            bot.xGrip.position = 0.3
            cupHandPos = Positions.OPEN
        } else if (gamePad.a && !cupHandPress && cupHandPos == Positions.OPEN) {
            cupHandPress = true
            bot.xGrip.position = 1.0
            cupHandPos = Positions.CLOSE
        } else if (!gamePad.a && cupHandPress) {
            cupHandPress = false
        }
//        if (gamePad.a) {
//        } else if (!gamePad.start && gamePad.y) {
//            bot.xGrip.position = 1.0
//        }
    }

    private fun liftMove(gamePad: Gamepad) {
        if (gamePad.right_trigger > 0.1 && !freeMove) {
            if (gamePad.a) {
                lift(Direction.UP, 0.9)
            } else if (gamePad.x) {
                if (abs(bot.ySlider.currentPosition) < liftDistance(16.0)) {
                    lift(Direction.UP, 0.9)
                } else {
                    lift(Direction.UP, 0.0)
                }
            } else if (gamePad.y) {
                if (abs(bot.ySlider.currentPosition) < liftDistance(4.5)) {
                    lift(Direction.UP, 0.9)
                } else {
                    lift(Direction.UP, 0.0)
                }
            } else {
                lift(Direction.DOWN, 0.9)
            }
        } else if (gamePad.dpad_up && freeMove) {
            lift(Direction.UP, 0.9)
        } else if (gamePad.dpad_down && freeMove) {
            lift(Direction.DOWN, 0.9)
        } else {
            lift(Direction.UP, 0.0)
        }
    }

    private fun liftHand(gamePad: Gamepad) {
        if (gamePad.left_bumper && !liftGripPress && liftGripPos == Positions.RECEIVED) {
            liftGripPress = true
            bot.yGrip.position = 1.0
            liftGripPos = Positions.DUMPED
        } else if (gamePad.left_bumper && !liftGripPress && liftGripPos == Positions.DUMPED) {
            liftGripPress = true
            bot.yGrip.position = 0.0
            liftGripPos = Positions.RECEIVED
        } else if (!gamePad.left_bumper && liftGripPress) {
            liftGripPress = false
        }

    }

    private fun freeMove(gp: Gamepad) {
        if (gp.share && gp.x && !freeMovePress && !freeMove && !process) {
            freeMovePress = true
            freeMove = true
            gp.rumbleBlips(1)
        } else if (gp.share && gp.x && !freeMovePress && freeMove && !process) {
            freeMovePress = true
            freeMove = false
            resetPartEncoders()
            gp.rumbleBlips(2)
        }else if (!gp.x && freeMovePress) {
            freeMovePress = false
        }
    }

    private fun cycleInit(gamePad: Gamepad) {
        if (gamePad.start && gamePad.x) {
//            cycle.init()
            myCycle.runInit()
            isInit = true
        }
    }

    private fun cycle(gamePad: Gamepad) {
        var processPressed = false
        if (gamePad.start && gamePad.y && !yPressed && !process && isInit) {
            yPressed = true
            isInit = true //false
            process = true
            myCycle.runInit()
            while (process && instance.opModeIsActive()) {
                if (!gamePad.y && yPressed) {
                    yPressed = false
                } else if (gamePad.y && !yPressed && process) {
                    process = false
                    yPressed = true
                    myCycle.robotState = RobotState.DONE
                }
                if (gamePad.dpad_down) {
//                    cycle.EXT_ARM_MIN = 50.0
//                    cycle.EXT_ARM_READY = cycle.EXT_ARM_MIN
//                    cycle.EXT_ARM_MAX = cycle.EXT_ARM_MIN
                    myCycle.coneDistance = false
                } else if (gamePad.dpad_up) {
//                    cycle.EXT_ARM_MIN = 150.0
//                    cycle.EXT_ARM_READY = 118.0 / 3.0 * 6
//                    cycle.EXT_ARM_MAX = 118.0 / 3.0 * 14
                    myCycle.coneDistance = true
                }
//                cycle.scoreCones()
                myCycle.run()
            }
        } else if (!gamePad.y && yPressed) {
            yPressed = false
        }
    }
}