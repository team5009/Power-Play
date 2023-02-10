package org.firstinspires.ftc.teamcode.src.models.abot.cycles.teleop

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop.TeleInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.*
import kotlin.math.abs

class TeleCycle(opMode: LinearOpMode, robot: TeleInstance) {
    private val instance = robot
    private val op = opMode
    private val active = op.opModeIsActive()

    class LiftPositions {
        //        val bottom = liftDistance(1.0)
        val middle = liftDistance(12.0)
        val top = liftDistance(24.0)
        val time = 3500
        val power: Double = 0.95
    }

    class ExtArmPositions {
        val inside = armDistance(1.0)

        //        val ready = armDistance(12.0)
        val middle = armDistance(12.0)
        val outside = armDistance(24.0)
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

    class GripYPositions {
        val dump = 1.0
        val middle = 0.45
        val receive = 0.24
        val time = 500
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

    var extArmState: ExtArmState
    var extLiftState: ExtLiftState
    var armGripState: ArmGripState
    var cupArmState: CupArmState
    var liftGripState: LiftGripState
    var robotState: RobotState
    var coneDistance: Boolean
    val timings = Timings()

    init {
        extArmState = ExtArmState.READY
        extLiftState = ExtLiftState.BOTTOM
        armGripState = ArmGripState.OPEN
        liftGripState = LiftGripState.MIDDLE
        cupArmState = CupArmState.DOWN
        robotState = RobotState.SCORING
        coneDistance = true
    }

    fun runApp() {
        extArmState = ExtArmState.READY
        extLiftState = ExtLiftState.BOTTOM
        armGripState = ArmGripState.OPEN
        liftGripState = LiftGripState.MIDDLE
        cupArmState = CupArmState.DOWN
        robotState = RobotState.SCORING
        coneDistance = true

        while (op.opModeIsActive()) {
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
            op.telemetry.addData("Cup Arm State", cupArmState.name)
            op.telemetry.addData("Cone State", coneDistance)
            op.telemetry.addData("", "")
            op.telemetry.addData("Lift pos", instance.bot.ySlider.currentPosition)
            op.telemetry.addData("CupArm pos", instance.bot.arm.currentPosition)
            op.telemetry.addData("Extendo pos", instance.bot.xSlider.currentPosition)
            op.telemetry.addData("", "")
            op.telemetry.addData("ySensor Sensor", instance.bot.ySensor.state)
            op.telemetry.addData("xSensor Sensor", instance.bot.xSensor.state)
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

    fun runInit() {
        extArmState = ExtArmState.READY
        extLiftState = ExtLiftState.BOTTOM
        armGripState = ArmGripState.OPEN
        liftGripState = LiftGripState.RECEIVE
        cupArmState = CupArmState.DOWN
        robotState = RobotState.DROPPING
        coneDistance = true

//        instance.extArmInit()
//        Thread.sleep(100)
//        instance.bot.cupHandInit()
//        Thread.sleep(100)
//        instance.bot.liftInit()
//        Thread.sleep(100)
        instance.liftHandInit()
    }

    fun run() {
        when (robotState) {
            RobotState.DROPPING -> dropping()
            RobotState.EXTENDING -> getReady()
            RobotState.RETRACTING -> retracting()
            RobotState.SCORING -> scoring()
            RobotState.DUMPING -> dumping()
            RobotState.DONE -> done()
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

    fun xSlider(direction: Directions) {
        val pos = ExtArmPositions()
        when (direction) {
            Directions.READY -> {
                if (extArmState == ExtArmState.IN) {
                    instance.bot.xSlider.power = pos.power
                    while (active && instance.bot.xSensor.state) {
                    }
                    instance.bot.xSlider.power = 0.0
                    extArmState = ExtArmState.READY
                }
            }
            Directions.DONE -> {
                if (extArmState == ExtArmState.READY) {
                    instance.bot.xSlider.power = -0.9
                    while (active && instance.bot.xSensor.state) {
                    }
                    instance.bot.xSlider.power = 0.0
                    extArmState = ExtArmState.IN
                }
            }
            Directions.EXTEND -> {
                if (extArmState == ExtArmState.READY) {
                    instance.bot.xSlider.power = pos.power
                    extArmState = ExtArmState.EXTENDING
                }
            }
            Directions.RETRACT -> {
                if (extArmState == ExtArmState.OUT) {
                    instance.bot.xSlider.power = -pos.power
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

    private fun arm(direction: Directions) {
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

    private fun dropping() {
        val armPos = ExtArmPositions()
        val liftPos = LiftPositions()
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extArmState == ExtArmState.EXTENDING && cupArmState != CupArmState.ASCENDING) {
            if (abs(instance.bot.xSlider.currentPosition) > armPos.middle) {
                liftGrip(Directions.RECEIVE)
                liftGripState = LiftGripState.RECEIVE
            }
            if (instance.bot.xSlider.currentPosition > armPos.outside || System.currentTimeMillis() > timings.extTimeOut) {
                xSlider(Directions.STOP)
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
        if (extArmState == ExtArmState.OUT || extArmState == ExtArmState.READY) {
            if (cupArmState == CupArmState.DOWN && armGripState == ArmGripState.OPEN) {
                timings.gripXTime = System.currentTimeMillis() + posXGrip.time
                armGrip(Directions.CLOSE)
            }
            if (armGripState == ArmGripState.CLOSING && System.currentTimeMillis() > timings.gripXTime) {
                armGripState = ArmGripState.CLOSED
            }
            if (armGripState == ArmGripState.CLOSED && cupArmState == CupArmState.DOWN) {
                arm(Directions.UP)
            }
            if (cupArmState == CupArmState.ASCENDING && abs(instance.bot.arm.currentPosition) > cupPos.receive) {
                arm(Directions.STOP)
                cupArmState = CupArmState.RECEIVE
            }
        }
        if (extLiftState == ExtLiftState.DROPPING && instance.bot.ySensor.state) {
            lift(Directions.STOP)
            extLiftState = ExtLiftState.BOTTOM
        }
        if (extLiftState == ExtLiftState.BOTTOM && cupArmState == CupArmState.RECEIVE && armGripState == ArmGripState.CLOSED) {
            if (!coneDistance) {
                timings.extTimeOut = System.currentTimeMillis() + armPos.time
                xSlider(Directions.RETRACT)
            }
            robotState = RobotState.RETRACTING
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
            if (!instance.bot.xSensor.state) {
                xSlider(Directions.STOP)
                extArmState = ExtArmState.READY
            }
        }

        if (extArmState == ExtArmState.READY && liftGripState == LiftGripState.RECEIVE) {
            if (armGripState == ArmGripState.CLOSED) {
                Thread.sleep(500)
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
            arm(Directions.DOWN)
            armGripState = ArmGripState.OPEN
        }
        if (cupArmState == CupArmState.DESCENDING && abs(instance.bot.arm.currentPosition) < cupPos.down) {
            arm(Directions.STOP)
            cupArmState = CupArmState.DOWN
        }
        if (cupArmState == CupArmState.DOWN && extLiftState == ExtLiftState.BOTTOM) {
            timings.liftTimeOut = System.currentTimeMillis() + liftPos.time
            lift(Directions.UP)
            robotState = RobotState.DUMPING
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
                timings.gripYTime = System.currentTimeMillis() + gripYPos.time
                liftGrip(Directions.RECEIVE)
            }
            if (liftGripState == LiftGripState.RECEIVING) {
                if (!coneDistance) {
                    timings.extTimeOut = System.currentTimeMillis() + armPos.time
                    xSlider(Directions.EXTEND)
                }
                robotState = RobotState.DROPPING
            }
        }
    }

    fun done() {
        instance.bot.ySlider.power = 0.0
        instance.bot.xSlider.power = 0.0
        instance.bot.arm.power = 0.0
    }
}

