package org.firstinspires.ftc.teamcode.src.models.abot.cycles.teleop

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop.TeleInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.armDegrees
import org.firstinspires.ftc.teamcode.src.models.abot.utils.armDistance
import kotlin.math.abs

class GrabStore(opMode: LinearOpMode, robot: TeleInstance) {
    val op = opMode
    val active = opMode.opModeIsActive()
    val instance = robot

    class ExtArmPositions {
        val inside = armDistance(1.0)

        //        val ready = armDistance(12.0)
        val middle = armDistance(12.0)
        var max = armDistance(24.0)
        val time = 1200
        val power: Double = 0.4
    }

    class CupArmPositions {
        val down = armDegrees(30.0)
        val receive = armDegrees(90.0)
        val power = 0.7
    }

    class GripXPositions {
        val open = 0.0
        val close = 1.0
        val ready = 0.5
        val time = 666
    }

    class Timings {
        var yGripTime: Long = 0
        var xGripTime: Long = 0
        var xSlideTime: Long = 0
        var armTime: Long = 0
    }

    enum class Directions { UP, DOWN, EXTEND, RETRACT, OPEN, CLOSE, DUMP, RECEIVE, READY, DONE, STOP }

    enum class ExtArmState { IN, EXTENDING, READY, MIDDLE, RETRACTING, OUT }
    enum class ArmGripState { OPEN, OPENING, CLOSED, CLOSING, READY }
    enum class CupArmState { DOWN, ASCENDING, RECEIVE, DESCENDING }
    enum class RobotState { EXTENDING, GRABBING, RETRACTING, DONE }

    var xSliderState: ExtArmState = ExtArmState.READY
    var xGripState: ArmGripState = ArmGripState.OPEN
    var armState: CupArmState = CupArmState.DOWN
    var robotState: RobotState = RobotState.GRABBING
    val xSliderPos = ExtArmPositions()
    val xGripPos = GripXPositions()
    val armPos = CupArmPositions()
    val timings = Timings()

    fun xSlider(direction: Directions) {
        val pos = ExtArmPositions()
        when (direction) {
            Directions.READY -> {
                if (xSliderState == ExtArmState.IN) {
                    instance.bot.xSlider.power = pos.power
                    while (active && instance.bot.xSensor.state) { }
                    instance.bot.xSlider.power = 0.0
                    xSliderState = ExtArmState.READY
                }
            }
            Directions.DONE -> {
                if (xSliderState == ExtArmState.READY) {
                    instance.bot.xSlider.power = -0.9
                    while (active && instance.bot.xSensor.state) { }
                    instance.bot.xSlider.power = 0.0
                    xSliderState = ExtArmState.IN
                }
            }
            Directions.EXTEND -> {
                if (xSliderState == ExtArmState.READY) {
                    instance.bot.xSlider.power = pos.power
                    xSliderState = ExtArmState.EXTENDING
                }
            }
            Directions.RETRACT -> {
                if (xSliderState == ExtArmState.OUT) {
                    instance.bot.xSlider.power = -pos.power
                    xSliderState = ExtArmState.RETRACTING
                }
            }
            Directions.STOP -> {
                instance.bot.xSlider.power = 0.0
            }
            else -> {
            }
        }
    }

    private fun arm(direction: Directions) {
        val pos = CupArmPositions()
        when (direction) {
            Directions.UP -> {
                if (armState == CupArmState.DOWN) {
                    instance.bot.arm.power = pos.power
                    armState = CupArmState.ASCENDING
                }
            }
            Directions.DOWN -> {
                if (armState == CupArmState.RECEIVE) {
                    instance.bot.arm.power = -pos.power
                    armState = CupArmState.DESCENDING
                }
            }
            Directions.STOP -> instance.bot.arm.power = 0.0
            else -> instance.bot.arm.power = 0.0
        }
    }

    private fun armGrip(direction: Directions) {
        val pos = GripXPositions()
        when (direction) {
            Directions.OPEN -> {
                if (xGripState == ArmGripState.CLOSED || xGripState == ArmGripState.READY) {
                    instance.bot.xGrip.position = pos.open
                    xGripState = ArmGripState.OPENING
                }
            }
            Directions.CLOSE -> {
                if (xGripState == ArmGripState.OPEN || xGripState == ArmGripState.READY) {
                    instance.bot.xGrip.position = pos.close
                    xGripState = ArmGripState.CLOSING
                }
            }
            Directions.READY -> {
                if (xGripState == ArmGripState.CLOSED || xGripState == ArmGripState.OPEN) {
                    instance.bot.xGrip.position = pos.ready
                    xGripState = ArmGripState.READY
                }
            }
            else -> {
                return
            }
        }
    }

    fun Extend() {

        if (!active) {
            return
        }
        if (xSliderState == ExtArmState.READY && armState == CupArmState.DOWN) {
            xSliderPos.max = instance.bot.xSlider.currentPosition + 500.0
            timings.xSlideTime = System.currentTimeMillis() + xSliderPos.time
            xSlider(Directions.EXTEND)

            robotState = RobotState.GRABBING
        }
    }

    fun Grab() {
        if (!active) {
            return
        }
        if (xSliderState == ExtArmState.EXTENDING) {
            if (abs(instance.bot.xSlider.currentPosition) > xSliderPos.max - (500/2)) {
                armGrip(Directions.READY)
                xGripState = ArmGripState.READY
            }
            if (instance.bot.xSlider.currentPosition > xSliderPos.max || System.currentTimeMillis() > timings.xSlideTime) {
                xSlider(Directions.STOP)
                xSliderState = ExtArmState.OUT
            }
        }
        if (xSliderState == ExtArmState.OUT) {
            if (armState == CupArmState.DOWN && xGripState == ArmGripState.OPEN) {
                timings.xGripTime = System.currentTimeMillis() + xGripPos.time
                armGrip(Directions.CLOSE)
            }
            if (xGripState == ArmGripState.CLOSING && System.currentTimeMillis() > timings.xGripTime) {
                xGripState = ArmGripState.CLOSED
            }
            if (xGripState == ArmGripState.CLOSED && armState == CupArmState.DOWN) {
                arm(Directions.UP)
            }
            if (armState == CupArmState.ASCENDING && abs(instance.bot.arm.currentPosition) > armPos.receive) {
                arm(Directions.STOP)
                armState = CupArmState.RECEIVE
            }
            if (armState == CupArmState.RECEIVE && xGripState == ArmGripState.CLOSED) {
                xSlider(Directions.RETRACT)
                robotState = RobotState.RETRACTING
            }
        }
    }

    fun Retract() {
        if (!active) {
            return
        }
        if (xSliderState == ExtArmState.RETRACTING) {
            if (instance.bot.xSensor.state) {
                xSlider(Directions.STOP)
                xSliderState = ExtArmState.READY
            }
        }
        if (xSliderState == ExtArmState.READY) {
            if (xGripState == ArmGripState.CLOSED) {
                op.sleep(500)
                timings.xGripTime = System.currentTimeMillis() + xGripPos.time
                armGrip(Directions.OPEN)
            }
            if (xGripState == ArmGripState.OPENING && System.currentTimeMillis() > timings.xGripTime) {
                xGripState = ArmGripState.OPEN
            }
            if (xGripState == ArmGripState.OPEN) {
                arm(Directions.DOWN)
            }
            if (armState == CupArmState.DESCENDING && abs(instance.bot.arm.currentPosition) < armPos.down) {
                arm(Directions.STOP)
                robotState = RobotState.DONE
            }
        }
    }

    fun Done() {
        instance.bot.xSlider.power = 0.0
        instance.bot.arm.power = 0.0
    }
}