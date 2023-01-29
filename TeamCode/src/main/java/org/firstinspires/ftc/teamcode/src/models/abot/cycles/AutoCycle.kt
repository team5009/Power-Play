package org.firstinspires.ftc.teamcode.src.models.abot.cycles

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import kotlin.math.abs

class AutoCycle(opMode: LinearOpMode, robot: AutoInstance) {
    private val bot = robot
    private val op = opMode
    private val active = op.opModeIsActive()

    class LiftPositions {
        val middle = liftDistance(12.0)
        val top = liftDistance(24.0)
        val time = 3500
        val power: Double = 0.9
    }

    class ExtArmPositions {
        val inside = armDistance(1.0)
        val middle = armDistance(12.0)
        val outside = armDistance(24.0)
        val time = 1200
        val power: Double = 0.75
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
        val high = 0.375
        val low = 0.5
        val middle = 0.69
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

    init {
        extArmState = ExtArmState.READY
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
            op.telemetry.addData("Lift pos", bot.extLift.currentPosition)
            op.telemetry.addData("CupArm pos", bot.cupArm.currentPosition)
            op.telemetry.addData("Extendo pos", bot.extArm.currentPosition)
            op.telemetry.addData("", "")
            op.telemetry.addData("yAxis Sensor", bot.yAxis.state)
            op.telemetry.addData("xAxis Sensor", bot.xAxis.state)
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
                    bot.extLift.power = pos.power
                    extLiftState = ExtLiftState.RISING
                }
            }
            Directions.DOWN -> {
                if (extLiftState == ExtLiftState.TOP) {
                    bot.extLift.power = -pos.power
                    extLiftState = ExtLiftState.DROPPING
                }
            }
            Directions.STOP -> {
                bot.extLift.power = 0.0
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
                if (extArmState == ExtArmState.IN) {
                    bot.extArm.power = pos.power
                    while (active && bot.xAxis.state) {
                    }
                    bot.extArm.power = 0.0
                    extArmState = ExtArmState.READY
                }
            }
            Directions.DONE -> {
                if (extArmState == ExtArmState.READY) {
                    bot.extArm.power = -0.9
                    while (active && bot.xAxis.state) {
                    }
                    bot.extArm.power = 0.0
                    extArmState = ExtArmState.IN
                }
            }
            Directions.EXTEND -> {
                if (extArmState == ExtArmState.READY) {
                    bot.extArm.power = pos.power
                    extArmState = ExtArmState.EXTENDING
                }
            }
            Directions.RETRACT -> {
                if (extArmState == ExtArmState.OUT) {
                    bot.extArm.power = -pos.power / 2
                    extArmState = ExtArmState.RETRACTING
                }
            }
            Directions.STOP -> {
                bot.extArm.power = 0.0
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
                    bot.cupArm.power = pos.power
                    cupArmState = CupArmState.ASCENDING
                }
            }
            Directions.DOWN -> {
                if (cupArmState == CupArmState.RECEIVE) {
                    bot.cupArm.power = -pos.power
                    cupArmState = CupArmState.DESCENDING
                }
            }
            Directions.STOP -> bot.cupArm.power = 0.0
            else -> bot.cupArm.power = 0.0
        }
    }

    private fun armGrip(direction: Directions) {
        val pos = GripXPositions()
        when (direction) {
            Directions.OPEN -> {
                if (armGripState == ArmGripState.CLOSED || armGripState == ArmGripState.READY) {
                    bot.gripX.position = pos.open
                    armGripState = ArmGripState.OPENING
                }
            }
            Directions.CLOSE -> {
                if (armGripState == ArmGripState.OPEN || armGripState == ArmGripState.READY) {
                    bot.gripX.position = pos.close
                    armGripState = ArmGripState.CLOSING
                }
            }
            Directions.READY -> {
                if (armGripState == ArmGripState.CLOSED || armGripState == ArmGripState.OPEN) {
                    bot.gripX.position = pos.ready
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
                    bot.gripY.position = pos.dump
                    liftGripState = LiftGripState.DUMPING
                } else if (liftGripState == LiftGripState.MIDDLE) {
                    bot.gripY.position = pos.dump
                    liftGripState = LiftGripState.DUMPING
                }
            }
            Directions.RECEIVE -> {
                if (liftGripState == LiftGripState.DUMP) {
                    bot.gripY.position = pos.receive
                    liftGripState = LiftGripState.RECEIVING
                } else if (liftGripState == LiftGripState.MIDDLE) {
                    bot.gripY.position = pos.receive
                    liftGripState = LiftGripState.RECEIVING
                }
            }
            Directions.READY -> {
                if (liftGripState == LiftGripState.RECEIVE || liftGripState == LiftGripState.DUMP) {
                    bot.gripY.position = pos.middle
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
                    bot.gripZ.position = pos.high
                    antlerState = AntlerState.TOP
                } else if (antlerState == AntlerState.MIDDLE) {
                    bot.gripZ.position = pos.high
                    antlerState = AntlerState.TOP
                }
            }
            Directions.DOWN -> {
                if (antlerState == AntlerState.TOP) {
                    bot.gripZ.position = pos.low
                    antlerState = AntlerState.BOTTOM
                } else if (antlerState == AntlerState.MIDDLE) {
                    bot.gripZ.position = pos.low
                    antlerState = AntlerState.BOTTOM
                }
            }
            Directions.READY -> {
                if (antlerState == AntlerState.TOP || antlerState == AntlerState.BOTTOM) {
                    bot.gripZ.position = pos.middle
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
            if (abs(bot.extArm.currentPosition) > armPos.middle) {
                liftGrip(Directions.RECEIVE)
                liftGripState = LiftGripState.RECEIVE
            }
            if (bot.extArm.currentPosition > armPos.outside || System.currentTimeMillis() > timings.extTimeOut) {
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
            if (cupArmState == CupArmState.ASCENDING && abs(bot.cupArm.currentPosition) > cupPos.receive) {
                cupArm(Directions.STOP)
                cupArmState = CupArmState.RECEIVE
            }
        }
        if (extLiftState == ExtLiftState.DROPPING && bot.yAxis.state) {
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
            if (!bot.xAxis.state) {
                extArm(Directions.STOP)
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
        if (cupArmState == CupArmState.DESCENDING && abs(bot.cupArm.currentPosition) < cupPos.down) {
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
            if (bot.extLift.currentPosition < liftPos.middle && liftGripState != LiftGripState.MIDDLE) {
                liftGrip(Directions.READY)
                liftGripState = LiftGripState.MIDDLE
            }
            if (abs(bot.extLift.currentPosition) >= liftPos.top || System.currentTimeMillis() > timings.liftTimeOut) {
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
        bot.extLift.power = 0.0
        bot.extArm.power = 0.0
        bot.cupArm.power = 0.0
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

fun armDegrees(degree: Double): Double {
    return degree * (288.0 / 360.0)
}

fun liftDistance(inch: Double): Double {
    return inch * (1.5 * 118.0 / 2.4)
}

fun armDistance(inch: Double): Double {
    return inch * (118.0 / 3.0)
}