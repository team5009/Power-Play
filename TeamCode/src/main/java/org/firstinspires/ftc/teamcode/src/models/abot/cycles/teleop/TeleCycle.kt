package org.firstinspires.ftc.teamcode.src.models.abot.cycles.teleop

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop.TeleInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.*
import kotlin.math.abs

class TeleCycle(opMode: LinearOpMode, robot: TeleInstance) {
    private val instance = robot
    private val op = opMode
    private val active = op.opModeIsActive()
    private val timings = Timings()
    private val xSliderConst = X_Slider()
    private val ySliderConst = Y_Slider()
    private val armConst = Arm()
    private val xGripConst = X_Grip()
    private val yGripConst = Y_Grip()

    var extArmState: X_Slider.States
    var extLiftState: Y_Slider.States
    var cupArmState: Arm.States
    var armGripState: X_Grip.States
    var liftGripState: Y_Grip.States
    var robotState: RobotState
    var coneDistance: Boolean
    var horizontalSliderMax = 830



    init {
        extArmState = X_Slider.States.READY
        extLiftState = Y_Slider.States.BOTTOM
        cupArmState = Arm.States.DOWN
        armGripState = X_Grip.States.OPEN
        liftGripState = Y_Grip.States.MIDDLE
        robotState = RobotState.SCORING
        coneDistance = false
    }

    fun runApp() {
        extArmState = X_Slider.States.READY
        extLiftState = Y_Slider.States.BOTTOM
        cupArmState = Arm.States.DOWN
        armGripState = X_Grip.States.OPEN
        liftGripState = Y_Grip.States.RECEIVE
        robotState = RobotState.RETRACTING
        coneDistance = true

        while (op.opModeIsActive()) {
            when (robotState) {
                RobotState.DROPPING -> dropping()
                RobotState.RETRACTING -> retracting()
                RobotState.EARLYSCORE -> earlyscore()
                RobotState.SCORING -> scoring()
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
            op.telemetry.addData("Lift power", instance.bot.ySlider.power)
            op.telemetry.addData("CupArm power", instance.bot.arm.power)
            op.telemetry.addData("Extendo power", instance.bot.xSlider.power)
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
        extArmState = X_Slider.States.READY
        extLiftState = Y_Slider.States.BOTTOM
        cupArmState = Arm.States.DOWN
        armGripState = X_Grip.States.OPEN
        liftGripState = Y_Grip.States.RECEIVE
        robotState = RobotState.RETRACTING
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
            RobotState.RETRACTING -> retracting()
            RobotState.EARLYSCORE -> earlyscore()
            RobotState.SCORING -> scoring()
            RobotState.DONE -> done()
        }
    }

    private fun lift(direction: Directions) {
        when (direction) {
            Directions.UP -> {
                if (extLiftState == Y_Slider.States.BOTTOM) {
                    instance.bot.ySlider.power = ySliderConst.power
                    extLiftState = Y_Slider.States.RISING
                }
            }
            Directions.DOWN -> {
                if (extLiftState == Y_Slider.States.TOP) {
                    instance.bot.ySlider.power = -ySliderConst.power
                    extLiftState = Y_Slider.States.DROPPING
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
        when (direction) {
            Directions.READY -> {
                if (extArmState == X_Slider.States.IN) {
                    instance.bot.xSlider.power = xSliderConst.power
                    while (active && instance.bot.xSensor.state) {
                    }
                    instance. bot.xSlider.power = 0.0
                    extArmState = X_Slider.States.READY
                }
            }
            Directions.DONE -> {
                if (extArmState == X_Slider.States.READY) {
                    instance.bot.xSlider.power = -xSliderConst.power
                    while (active && instance.bot.xSensor.state) {
                    }
                    instance.bot.xSlider.power = 0.0
                    extArmState = X_Slider.States.IN
                }
            }
            Directions.EXTEND -> {
                if (extArmState == X_Slider.States.READY) {
                    instance.bot.xSlider.power = xSliderConst.power
                    extArmState = X_Slider.States.EXTENDING
                }
            }
            Directions.RETRACT -> {
                if (extArmState == X_Slider.States.OUT) {
                    instance.bot.xSlider.power = -xSliderConst.power
                    extArmState = X_Slider.States.RETRACTING
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
        when (direction) {
            Directions.UP -> {
                if (cupArmState == Arm.States.DOWN) {
                    instance.bot.arm.power = armConst.power
                    cupArmState = Arm.States.ASCENDING
                }
            }
            Directions.DOWN -> {
                if (cupArmState == Arm.States.RECEIVE) {
                    instance.bot.arm.power = -armConst.power
                    cupArmState = Arm.States.DESCENDING
                }
            }
            Directions.STOP -> instance.bot.arm.power = 0.0
            else -> instance.bot.arm.power = 0.0
        }
    }

    private fun armGrip(direction: Directions) {
        when (direction) {
            Directions.OPEN -> {
                if (armGripState == X_Grip.States.CLOSED || armGripState == X_Grip.States.READY) {
                    instance.bot.xGrip.position = xGripConst.open
                    armGripState = X_Grip.States.OPENING
                }
            }
            Directions.CLOSE -> {
                if (armGripState == X_Grip.States.OPEN || armGripState == X_Grip.States.READY) {
                    instance.bot.xGrip.position = xGripConst.close
                    armGripState = X_Grip.States.CLOSING
                }
            }
            Directions.READY -> {
                if (armGripState == X_Grip.States.CLOSED || armGripState == X_Grip.States.OPEN) {
                    instance.bot.xGrip.position = xGripConst.ready
                    armGripState = X_Grip.States.READY
                }
            }
            else -> {
                return
            }
        }
    }

    private fun liftGrip(direction: Directions) {
        when (direction) {
            Directions.DUMP -> {
                if (liftGripState == Y_Grip.States.RECEIVE) {
                    instance.bot.yGrip.position = yGripConst.dump
                    liftGripState = Y_Grip.States.DUMPING
                } else if (liftGripState == Y_Grip.States.MIDDLE) {
                    instance.bot.yGrip.position = yGripConst.dump
                    liftGripState = Y_Grip.States.DUMPING
                }
            }
            Directions.RECEIVE -> {
                if (liftGripState == Y_Grip.States.DUMP) {
                    instance.bot.yGrip.position = yGripConst.receive
                    liftGripState = Y_Grip.States.RECEIVING
                } else if (liftGripState == Y_Grip.States.MIDDLE) {
                    instance.bot.yGrip.position = yGripConst.receive
                    liftGripState = Y_Grip.States.RECEIVING
                }
            }
            Directions.READY -> {
                if (liftGripState == Y_Grip.States.RECEIVE || liftGripState == Y_Grip.States.DUMP) {
                    instance.bot.yGrip.position = yGripConst.middle
                    liftGripState = Y_Grip.States.RECEIVING
                }
            }
            else -> {
                return
            }
        }
    }

    private fun dropping() {
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (liftGripState == Y_Grip.States.RECEIVING && System.currentTimeMillis() > timings.gripYTime) {
            liftGripState = Y_Grip.States.RECEIVE
        }
        if (extArmState == X_Slider.States.OUT) {
            if (cupArmState == Arm.States.DOWN && (armGripState == X_Grip.States.OPEN || armGripState == X_Grip.States.READY)) {
                timings.gripXTime = System.currentTimeMillis() + xGripConst.time
                armGrip(Directions.CLOSE)
            }
            if (armGripState == X_Grip.States.CLOSING && System.currentTimeMillis() > timings.gripXTime) {
                armGripState = X_Grip.States.CLOSED
            }
            if (armGripState == X_Grip.States.CLOSED && cupArmState == Arm.States.DOWN) {
                cupArm(Directions.UP)
            }
            if (cupArmState == Arm.States.ASCENDING && abs(instance.bot.arm.currentPosition) > armConst.receive) {
                cupArm(Directions.STOP)
                cupArmState = Arm.States.RECEIVE
            }
        }
        if (extLiftState == Y_Slider.States.DROPPING && (instance.bot.ySensor.state || instance.bot.ySlider.currentPosition < 10)) {
            lift(Directions.STOP)
            extLiftState = Y_Slider.States.BOTTOM
        }
        if (extLiftState == Y_Slider.States.BOTTOM && liftGripState == Y_Grip.States.RECEIVE && armGripState == X_Grip.States.CLOSED && cupArmState == Arm.States.RECEIVE) {
            extArm(Directions.RETRACT)
            timings.extTimeOut = System.currentTimeMillis() + xSliderConst.time
            robotState = RobotState.RETRACTING
        }
    }

    private fun retracting() {
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extArmState == X_Slider.States.RETRACTING) {
            if (!instance.bot.xSensor.state) {
                extArm(Directions.STOP)
                horizontalSliderMax += 85
                Thread.sleep(250)
                extArmState = X_Slider.States.READY
            }
        }
        if (extArmState == X_Slider.States.READY && liftGripState == Y_Grip.States.RECEIVE) {
            if (armGripState == X_Grip.States.CLOSED) {
                timings.gripXTime = System.currentTimeMillis() + xGripConst.time
                armGrip(Directions.OPEN)
            }
            if (System.currentTimeMillis() > timings.gripXTime) {
                armGripState = X_Grip.States.OPEN
            }
            if (cupArmState == Arm.States.RECEIVE && armGripState == X_Grip.States.OPEN) {
                cupArm(Directions.DOWN)
            }
            if (cupArmState == Arm.States.DESCENDING && abs(instance.bot.arm.currentPosition) < armConst.down) {
                cupArm(Directions.STOP)
                cupArmState = Arm.States.DOWN
            }
            if (cupArmState == Arm.States.DOWN && extLiftState == Y_Slider.States.BOTTOM) {
                timings.liftTimeOut = System.currentTimeMillis() + ySliderConst.time
                lift(Directions.UP)
                robotState = RobotState.EARLYSCORE
            }
        }
    }

    private fun earlyscore() {
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extLiftState == Y_Slider.States.RISING && abs(instance.bot.ySlider.currentPosition) >= ySliderConst.middle) {
            if (liftGripState == Y_Grip.States.RECEIVE) {
                timings.gripYTime = System.currentTimeMillis() + yGripConst.time
                liftGrip(Directions.DUMP)
            }
            if (extArmState != X_Slider.States.EXTENDING) {
                timings.extTimeOut = System.currentTimeMillis() + xSliderConst.time
                horizontalSliderMax = abs(instance.bot.xSlider.currentPosition) + 200
                extArm(Directions.EXTEND)
                armGrip(Directions.READY)
            }
            if (extArmState == X_Slider.States.EXTENDING && liftGripState == Y_Grip.States.DUMPING) {
                robotState = RobotState.SCORING
            }
        }
    }

    private fun scoring() {
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (extLiftState == Y_Slider.States.RISING) {
            if (abs(instance.bot.ySlider.currentPosition) >= ySliderConst.top || System.currentTimeMillis() > timings.liftTimeOut) {
                lift(Directions.STOP)
                extLiftState = Y_Slider.States.TOP
            }
        }
        if (extLiftState == Y_Slider.States.TOP && liftGripState == Y_Grip.States.DUMPING && System.currentTimeMillis() > timings.gripYTime) {
            liftGripState = Y_Grip.States.DUMP
            timings.gripYTime = System.currentTimeMillis() + yGripConst.time
            liftGrip(Directions.RECEIVE)
            lift(Directions.DOWN)
        }
        if (extArmState == X_Slider.States.EXTENDING && cupArmState != Arm.States.ASCENDING) {
            if (!instance.bot.xSensor.state) {
                horizontalSliderMax = abs(instance.bot.xSlider.currentPosition) + 200
                timings.extTimeOut = System.currentTimeMillis() + (xSliderConst.time - 500)
            }
            if (abs(instance.bot.xSlider.currentPosition) >= horizontalSliderMax || System.currentTimeMillis() > timings.extTimeOut) {
                armGrip(Directions.READY)
                extArm(Directions.STOP)
                extArmState = X_Slider.States.OUT
            }
        }
        if (extLiftState == Y_Slider.States.DROPPING && (instance.bot.ySensor.state || instance.bot.ySlider.currentPosition < 10)) {
            lift(Directions.STOP)
            extLiftState = Y_Slider.States.BOTTOM
        }
        if (extArmState == X_Slider.States.OUT && (extLiftState == Y_Slider.States.DROPPING || extLiftState == Y_Slider.States.BOTTOM)) {
            robotState = RobotState.DROPPING
        }
    }

    fun done() {
        instance.bot.ySlider.power = 0.0
        instance.bot.xSlider.power = 0.0
        instance.bot.arm.power = 0.0
    }
}

