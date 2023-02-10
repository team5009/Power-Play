package org.firstinspires.ftc.teamcode.src.models.abot.cycles.autonomous

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.*
import org.firstinspires.ftc.teamcode.src.models.abot.utils.liftDistance
import kotlin.math.abs

class AutoCycle(opMode: LinearOpMode, robot: AutoInstance) {
    private val instance = robot
    private val op = opMode
    private val active = op.opModeIsActive()

    class LiftPositions {
        val middle = liftDistance(12.0)
        val top = liftDistance(24.0)
        val time = 1500
        val power: Double = 0.95
    }

    class ExtArmPositions {
        val inside = armDistance(1.0)
        val middle = 400
        val outside = 800
        val time = 1500
        val power: Double = 0.7
    }

    class CupArmPositions {
        val down = armDegrees(30.0)
        val receive = armDegrees(80.0)
        val power = 0.75
    }

    class GripXPositions {
        val open = 0.0
        val close = 1.0
        val ready = 0.5
        val time = 500
    }

    class GripYPositions {
        val dump = 1.0
        val middle = 0.45
        val receive = 0.24
        val time = 500
    }

    class GripZPositions {
        val high = 0.475
        val low = 0.45
        val middle = 0.35
        val time = 1000
    }

    class Timings {
        var gripYTime: Long = 0
        var gripXTime: Long = 0
        var extTimeOut: Long = 0
        var liftTimeOut: Long = 0
        var cupArmTimeOut: Long = 0
    }

    enum class Directions { UP, DOWN, EXTEND, RETRACT, OPEN, CLOSE, DUMP, RECEIVE, READY, DONE, STOP }

    enum class ExtArmState { IN, EXTENDING, READY, MIDDLE, RETRACTING, OUT }
    enum class ExtLiftState { BOTTOM, RISING, MIDDLE, DROPPING, TOP }
    enum class ArmGripState { OPEN, OPENING, CLOSED, CLOSING, READY }
    enum class LiftGripState { DUMP, DUMPING, MIDDLE, RECEIVE, RECEIVING }
    enum class CupArmState { DOWN, ASCENDING, RECEIVE, DESCENDING }
    enum class AntlerState { TOP, MIDDLE, BOTTOM }
    enum class RobotState { EXTENDING, DROPPING, DUMPING, SCORING, RETRACTING, DONE }
    enum class ConeStackState { NONE, ONE, TWO, THREE, FOUR, FIVE, SIX }

    var extArmState: ExtArmState
    var extLiftState: ExtLiftState
    var armGripState: ArmGripState
    var cupArmState: CupArmState
    var antlerState: AntlerState
    var liftGripState: LiftGripState
    var robotState: RobotState
    var coneStackState: ConeStackState
    var middle: Boolean
    val timings = Timings()
    var done: Boolean
    var horizontalSliderMax = 820

    init {
        extArmState = ExtArmState.IN
        extLiftState = ExtLiftState.BOTTOM
        armGripState = ArmGripState.OPEN
        liftGripState = LiftGripState.MIDDLE
        antlerState = AntlerState.TOP
        cupArmState = CupArmState.DOWN
        robotState = RobotState.SCORING
        coneStackState = ConeStackState.FIVE
        middle = false
        done = false
    }

    fun runApp(target: Int) {
        extArmState = ExtArmState.READY
        extLiftState = ExtLiftState.BOTTOM
        armGripState = ArmGripState.OPEN
        liftGripState = LiftGripState.MIDDLE
        antlerState = AntlerState.TOP
        cupArmState = CupArmState.DOWN
        robotState = RobotState.SCORING
        coneStackState = ConeStackState.FIVE
        middle = target == 2

        while (op.opModeIsActive() && !done) {
            when (robotState) {
                RobotState.DROPPING -> dropping()
                RobotState.EXTENDING -> getReady()
                RobotState.RETRACTING -> retracting()
                RobotState.SCORING -> scoring()
                RobotState.DUMPING -> dumping()
                RobotState.DONE -> done()
            }

            op.telemetry.addData("Current State", robotState.name)
            op.telemetry.addData("Ext Arm State", extArmState.name)
            op.telemetry.addData("Ext Lift State", extLiftState.name)
            op.telemetry.addData("Grip X State", armGripState.name)
            op.telemetry.addData("Grip Y State", liftGripState.name)
            op.telemetry.addData("Grip Z State", antlerState.name)
            op.telemetry.addData("Cup Arm State", cupArmState.name)
            op.telemetry.addData("Cone State", coneStackState.name)
            op.telemetry.addData("", "")
            op.telemetry.addData("Y Slider pos", instance.bot.ySlider.currentPosition)
            op.telemetry.addData("X Slider pos", instance.bot.xSlider.currentPosition)
            op.telemetry.addData("Arm pos", instance.bot.arm.currentPosition)
            op.telemetry.addData("", "")
            op.telemetry.addData("yAxis Sensor", instance.bot.ySensor.state)
            op.telemetry.addData("xAxis Sensor", instance.bot.xSensor.state)
            op.telemetry.addData("", "")
            op.telemetry.addData("System Time", System.currentTimeMillis())
            op.telemetry.addData("ExtArm Time", timings.extTimeOut)
            op.telemetry.addData("CupArm Time", timings.cupArmTimeOut)
            op.telemetry.addData("Lift Time", timings.liftTimeOut)
            op.telemetry.addData("Grip X Time", timings.gripXTime)
            op.telemetry.addData("Grip Y Time", timings.gripYTime)
            op.telemetry.update()
        }
    }

    private fun lift(direction: Directions) {
        val pos = LiftPositions()
        when (direction) {
            Directions.UP -> {
                if (extLiftState == ExtLiftState.BOTTOM) {
                    instance.bot.ySlider.power = pos.power
                    extLiftState = ExtLiftState.RISING
                }
            }
            Directions.DOWN -> {
                if (extLiftState == ExtLiftState.TOP) {
                    instance.bot.ySlider.power = -pos.power
                    extLiftState = ExtLiftState.DROPPING
                }
            }
            Directions.STOP -> {
                instance.bot.ySlider.power = 0.0
            }
            else -> {
                return
            }
        }
    }

    fun extArm(direction: Directions) {
        val pos = ExtArmPositions()
        when (direction) {
            Directions.READY -> {
                instance.bot.xSlider.power = pos.power/1.25
                while (op.opModeIsActive() && instance.bot.xSensor.state) { }
                instance.bot.xSlider.power = 0.0
                extArmState = ExtArmState.READY
            }
            Directions.DONE -> {
                val timer = System.currentTimeMillis() + pos.time/2.5
                instance.bot.xSlider.power = -pos.power
                while (op.opModeIsActive() && System.currentTimeMillis() < timer) { }
                instance.bot.xSlider.power = 0.0
                extArmState = ExtArmState.IN
            }
            Directions.EXTEND -> {
                if (extArmState == ExtArmState.READY) {
                    instance.bot.xSlider.power = pos.power
                    extArmState = ExtArmState.EXTENDING
                }
            }
            Directions.RETRACT -> {
                if (extArmState == ExtArmState.OUT) {
                    instance.bot.xSlider.power = -pos.power / 2.5
                    extArmState = ExtArmState.RETRACTING
                }
            }
            Directions.STOP -> {
                instance.bot.xSlider.power = 0.0
            }
            else -> {
            }
        }
    }

    private fun cupArm(direction: Directions) {
        val pos = CupArmPositions()
        when (direction) {
            Directions.UP -> {
                if (cupArmState == CupArmState.DOWN) {
                    instance.bot.arm.power = pos.power
                    cupArmState = CupArmState.ASCENDING
                }
            }
            Directions.DOWN -> {
                if (cupArmState == CupArmState.RECEIVE) {
                    instance.bot.arm.power = -pos.power
                    cupArmState = CupArmState.DESCENDING
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
                if (armGripState == ArmGripState.CLOSED || armGripState == ArmGripState.READY) {
                    instance.bot.xGrip.position = pos.open
                    armGripState = ArmGripState.OPENING
                }
            }
            Directions.CLOSE -> {
                if (armGripState == ArmGripState.OPEN || armGripState == ArmGripState.READY) {
                    instance.bot.xGrip.position = pos.close
                    armGripState = ArmGripState.CLOSING
                }
            }
            Directions.READY -> {
                if (armGripState == ArmGripState.CLOSED || armGripState == ArmGripState.OPEN) {
                    instance.bot.xGrip.position = pos.ready
                    armGripState = ArmGripState.READY
                }
            }
            else -> {
                return
            }
        }
    }

    private fun liftGrip(direction: Directions) {
        val pos = GripYPositions()
        when (direction) {
            Directions.DUMP -> {
                if (liftGripState == LiftGripState.RECEIVE) {
                    instance.bot.yGrip.position = pos.dump
                    liftGripState = LiftGripState.DUMPING
                } else if (liftGripState == LiftGripState.MIDDLE) {
                    instance.bot.yGrip.position = pos.dump
                    liftGripState = LiftGripState.DUMPING
                }
            }
            Directions.RECEIVE -> {
                if (liftGripState == LiftGripState.DUMP) {
                    instance.bot.yGrip.position = pos.receive
                    liftGripState = LiftGripState.RECEIVING
                } else if (liftGripState == LiftGripState.MIDDLE) {
                    instance.bot.yGrip.position = pos.receive
                    liftGripState = LiftGripState.RECEIVING
                }
            }
            Directions.READY -> {
                if (liftGripState == LiftGripState.RECEIVE || liftGripState == LiftGripState.DUMP) {
                    instance.bot.yGrip.position = pos.middle
                    liftGripState = LiftGripState.RECEIVING
                }
            }
            else -> {
                return
            }
        }
    }

    private fun antler(direction: Directions) {
        val pos = GripZPositions()
        when (direction) {
            Directions.UP -> {
                if (antlerState == AntlerState.BOTTOM) {
                    instance.bot.zGrip.position = pos.high
                    antlerState = AntlerState.TOP
                } else if (antlerState == AntlerState.MIDDLE) {
                    instance.bot.zGrip.position = pos.high
                    antlerState = AntlerState.TOP
                }
            }
            Directions.DOWN -> {
                if (antlerState == AntlerState.TOP) {
                    instance.bot.zGrip.position = pos.low
                    antlerState = AntlerState.BOTTOM
                } else if (antlerState == AntlerState.MIDDLE) {
                    instance.bot.zGrip.position = pos.low
                    antlerState = AntlerState.BOTTOM
                }
            }
            Directions.READY -> {
                if (antlerState == AntlerState.TOP || antlerState == AntlerState.BOTTOM) {
                    instance.bot.zGrip.position = pos.middle
                    antlerState = AntlerState.MIDDLE
                }
            }
            else -> {
                return
            }
        }
    }

    private fun dropping() {
        val armPos = ExtArmPositions()
        val liftPos = LiftPositions()
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extArmState == ExtArmState.EXTENDING && cupArmState != CupArmState.ASCENDING) {
            if (abs(instance.bot.arm.currentPosition) > armPos.middle) {
                liftGrip(Directions.RECEIVE)
                liftGripState = LiftGripState.RECEIVE
            }
            if (abs(instance.bot.xSlider.currentPosition) >= horizontalSliderMax || System.currentTimeMillis() > timings.extTimeOut) {
                extArm(Directions.STOP)
                extArmState = ExtArmState.OUT
            }
        }

        if (liftGripState == LiftGripState.RECEIVING && System.currentTimeMillis() > timings.gripYTime) {
            liftGripState = LiftGripState.RECEIVE
        }

        if (liftGripState == LiftGripState.RECEIVE && extLiftState != ExtLiftState.DROPPING && extArmState != ExtArmState.EXTENDING) {
            timings.liftTimeOut = System.currentTimeMillis() + liftPos.time
            lift(Directions.DOWN)
            robotState = RobotState.EXTENDING
        }
    }

    private fun getReady() {
        val liftPos = LiftPositions()
        val armPos = ExtArmPositions()
        val cupPos = CupArmPositions()
        val posXGrip = GripXPositions()
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extArmState == ExtArmState.OUT) {
            if (cupArmState == CupArmState.DOWN && armGripState == ArmGripState.OPEN) {
                timings.gripXTime = System.currentTimeMillis() + posXGrip.time
                armGrip(Directions.CLOSE)
            }
            if (armGripState == ArmGripState.CLOSING && System.currentTimeMillis() > timings.gripXTime) {
                armGripState = ArmGripState.CLOSED
            }
            if (armGripState == ArmGripState.CLOSED && cupArmState == CupArmState.DOWN) {
                cupArm(Directions.UP)
            }
            if (cupArmState == CupArmState.ASCENDING && abs(instance.bot.arm.currentPosition) > cupPos.receive) {
                cupArm(Directions.STOP)
                cupArmState = CupArmState.RECEIVE
            }
        }
        if (extLiftState == ExtLiftState.DROPPING && instance.bot.ySensor.state) {
            lift(Directions.STOP)
            extLiftState = ExtLiftState.BOTTOM
        }
        if (extLiftState == ExtLiftState.BOTTOM && (coneStackState == ConeStackState.NONE || (cupArmState == CupArmState.RECEIVE && armGripState == ArmGripState.CLOSED))) {
            if (coneStackState == ConeStackState.NONE) {
                robotState = RobotState.DONE
            } else {
                extArm(Directions.RETRACT)
                timings.extTimeOut = System.currentTimeMillis() + armPos.time
                robotState = RobotState.RETRACTING
            }
        }
    }

    private fun retracting() {
        val armPos = ExtArmPositions()
        val posXGrip = GripXPositions()
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extArmState == ExtArmState.RETRACTING) {
            if (middle) {
                when (coneStackState) {
                    ConeStackState.FIVE -> antler(Directions.UP)
                    ConeStackState.FOUR -> antler(Directions.READY)
                    ConeStackState.THREE -> antler(Directions.READY)
                    ConeStackState.TWO -> antler(Directions.DOWN)
                    ConeStackState.ONE -> antler(Directions.DOWN)
                    else -> {
                        return
                    }
                }
            } else {
                when (coneStackState) {
                    ConeStackState.FIVE -> antler(Directions.UP)
                    ConeStackState.FOUR -> antler(Directions.READY)
                    ConeStackState.THREE -> antler(Directions.DOWN)
                    ConeStackState.TWO -> antler(Directions.DOWN)
                    ConeStackState.ONE -> antler(Directions.DOWN)
                    else -> {
                        return
                    }
                }
            }
            if (!instance.bot.xSensor.state) {
                extArm(Directions.STOP)
                horizontalSliderMax += 85
                Thread.sleep(250)
                extArmState = ExtArmState.READY
            }
        }
        if (extArmState == ExtArmState.READY && liftGripState == LiftGripState.RECEIVE) {
            if (armGripState == ArmGripState.CLOSED) {
                timings.gripXTime = System.currentTimeMillis() + posXGrip.time
                armGrip(Directions.OPEN)
            }
            robotState = RobotState.SCORING
        }
    }

    private fun scoring() {
        val cupPos = CupArmPositions()
        val liftPos = LiftPositions()
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (cupArmState == CupArmState.RECEIVE && System.currentTimeMillis() > timings.gripXTime) {
            cupArm(Directions.DOWN)
            armGripState = ArmGripState.OPEN
        }
        if (cupArmState == CupArmState.DESCENDING && abs(instance.bot.arm.currentPosition) < cupPos.down) {
            cupArm(Directions.STOP)
            cupArmState = CupArmState.DOWN
        }
        if (cupArmState == CupArmState.DOWN && extLiftState == ExtLiftState.BOTTOM) {
            timings.liftTimeOut = System.currentTimeMillis() + liftPos.time
            lift(Directions.UP)
            changeAntler(RobotState.DUMPING)
        }
    }

    private fun dumping() {
        val armPos = ExtArmPositions()
        val liftPos = LiftPositions()
        val gripYPos = GripYPositions()
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extLiftState == ExtLiftState.RISING) {
            if (instance.bot.ySlider.currentPosition < liftPos.middle && liftGripState != LiftGripState.MIDDLE) {
                liftGrip(Directions.READY)
                liftGripState = LiftGripState.MIDDLE
            }
            if (abs(instance.bot.ySlider.currentPosition) >= liftPos.top || System.currentTimeMillis() > timings.liftTimeOut) {
                lift(Directions.STOP)
                extLiftState = ExtLiftState.TOP
            }
        }
        if (extLiftState == ExtLiftState.TOP) {
            if (liftGripState == LiftGripState.MIDDLE) {
                timings.gripYTime = System.currentTimeMillis() + gripYPos.time
                liftGrip(Directions.DUMP)
            }
            if (liftGripState == LiftGripState.DUMPING && System.currentTimeMillis() > timings.gripYTime) {
                liftGripState = LiftGripState.DUMP
                if (coneStackState != ConeStackState.NONE && extArmState != ExtArmState.EXTENDING) {
                    timings.extTimeOut = System.currentTimeMillis() + armPos.time
                    extArm(Directions.EXTEND)
                }
                timings.gripYTime = System.currentTimeMillis() + gripYPos.time
                liftGrip(Directions.RECEIVE)
                robotState = RobotState.DROPPING
            }

        }
    }

    private fun done() {
        instance.bot.ySlider.power = 0.0
        instance.bot.xSlider.power = 0.0
        instance.bot.arm.power = 0.0
        done = true
    }

    private fun changeAntler(state: RobotState) {
        when (coneStackState) {
            ConeStackState.FIVE -> {
                coneStackState = ConeStackState.FOUR
                robotState = state
                return
            }
            ConeStackState.FOUR -> {
                coneStackState = ConeStackState.THREE
                robotState = state
                return
            }
            ConeStackState.THREE -> {
                coneStackState = if (middle) {
                    ConeStackState.TWO
                } else {
                    ConeStackState.NONE
                }
                robotState = state
                return
            }
            ConeStackState.TWO -> {
                coneStackState = ConeStackState.NONE
                robotState = state
                return
            }
            ConeStackState.ONE -> {
                coneStackState = ConeStackState.NONE
                robotState = state
                return
            }
            ConeStackState.NONE -> {
                robotState = RobotState.DONE
                return
            }
            else -> {
                robotState = RobotState.DONE
                return
            }
        }
    }

}