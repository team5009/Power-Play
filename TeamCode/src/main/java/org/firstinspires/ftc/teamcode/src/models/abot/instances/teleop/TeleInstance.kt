package org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop

//import kotlinx.coroutines.DefaultExecutor.delay
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.teamcode.src.models.abot.cycles.teleop.TeleCycle
import org.firstinspires.ftc.teamcode.src.models.abot.instances.RobotClass
import org.firstinspires.ftc.teamcode.src.models.abot.utils.TouchSensor
import kotlin.math.abs

class TeleInstance(Instance: LinearOpMode, hardware: HardwareMap) {
    private val touchSensor =
        TouchSensor()

    val bot = RobotClass(Instance, hardware)
    val cupAngle = (bot.arm.currentPosition / 360.0)

    private var isInit = true
    var process = false
    private var yPressed = false
    var ticksPerDegree = 288.0 / 360.0
    private val instance = Instance
    val myCycle = TeleCycle(instance, this)

    enum class Direction {
        FORWARD, BACKWARD, UP, DOWN
    }

    init {
        bot.zGrip.position = 0.45
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

    fun gamePadOne(gamePad: Gamepad = instance.gamepad1) {
        moveControls(gamePad)
        cupHand(gamePad)
        cupArmMove(gamePad)
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

    fun liftInit() {
        if (!bot.ySensor.state) {
            bot.ySlider.power = 0.1
            while (instance.opModeIsActive() && !bot.ySensor.state) { } // Wait for yAxis to be Pressed
            bot.ySlider.power = 0.0
            bot.ySlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            bot.ySlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }

    private fun extArmBack() {
        bot.xSlider.power = 0.5
        while (instance.opModeIsActive() && bot.xSlider.currentPosition <= 325) {
        }
        bot.xSlider.power = 0.0
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
                if (abs(bot.arm.currentPosition) <= (30 * ticksPerDegree)) {
                    bot.arm.power = 0.0
                    bot.arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
                } else {
                    bot.arm.power = -power
                }
            }
            Direction.BACKWARD -> {
                if (abs(bot.arm.currentPosition) >= (85 * ticksPerDegree)) {
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
                if (abs(bot.ySlider.currentPosition) >= liftDistance(24.0)) {
                    bot.ySlider.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                    bot.ySlider.power = 0.0
                } else {
                    bot.ySlider.power = power
                }
            }
            Direction.DOWN -> {
                if (abs(bot.ySlider.currentPosition) <= 0) {
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

    private fun turn(power: Double) {
        bot.fl.power = -power
        bot.fr.power = power
        bot.br.power = power
        bot.bl.power = -power
    }

    private fun strafe(power: Double) {
        bot.fl.power = power
        bot.fr.power = -power
        bot.br.power = power
        bot.bl.power = -power
    }

    private fun moveControls(gamePad: Gamepad) {
        val y = gamePad.left_stick_y
        val x = gamePad.left_stick_x
        val deadZone = 0.2
        val pow = 0.95

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
        if (gamePad.a) {
            bot.xGrip.position = 0.0
        } else if (!gamePad.start && gamePad.y) {
            bot.xGrip.position = 1.0
        }
    }

    private fun liftMove(gamePad: Gamepad) {
        if (gamePad.dpad_up) {
            lift(Direction.UP, 0.9)
        } else if (gamePad.dpad_down) {
            lift(Direction.DOWN, 0.9)
        } else if (gamePad.right_trigger > 0.3 && abs(bot.ySlider.currentPosition) < liftDistance(15.0)) {
            lift(Direction.UP, gamePad.right_trigger.toDouble())
        } else if (gamePad.left_trigger > 0.3 && abs(bot.ySlider.currentPosition) < liftDistance(3.5)) {
            lift(Direction.UP, gamePad.left_trigger.toDouble())
        } else {
            lift(Direction.UP, 0.0)
        }
    }

    private fun liftHand(gamePad: Gamepad) {
        if (gamePad.left_bumper) {
            bot.yGrip.position = 0.24
        } else if (gamePad.right_bumper) {
            bot.yGrip.position = 1.0
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
                    myCycle.robotState = TeleCycle.RobotState.DONE
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

    private fun liftDistance(distance: Double): Double {
        return distance * (1.5 * 118.0 / 2.4)
    }
}