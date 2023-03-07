package org.firstinspires.ftc.teamcode.src.models.abot.cycles.autonomous

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.*
import kotlin.math.abs

class AutoCycle(opMode: LinearOpMode, robot: AutoInstance) {
    private val instance = robot
    private val op = opMode
    private val active = op.opModeIsActive()

    enum class ConeStackState { NONE, ONE, TWO, THREE, FOUR, FIVE, SIX }

    private val timings = Timings()
    private val xSliderConst = X_Slider()
    private val ySliderConst = Y_Slider()
    private val armConst = Arm()
    private val xGripConst = X_Grip()
    private val yGripConst = Y_Grip()
    private val zGripConst = Z_Grip()



    var extArmState: X_Slider.States
    var extLiftState: Y_Slider.States
    var cupArmState: Arm.States
    var armGripState: X_Grip.States
    var liftGripState: Y_Grip.States
    var robotState: RobotState
    var antlerState: Z_Grip.States
    var coneStackState: ConeStackState
    var middle: Boolean
    var done: Boolean
    var horizontalSliderMax = 820
    var addingMax = 0

    init {
        extArmState = X_Slider.States.READY
        extLiftState = Y_Slider.States.BOTTOM
        cupArmState = Arm.States.DOWN
        armGripState = X_Grip.States.OPEN
        liftGripState = Y_Grip.States.RECEIVE
        robotState = RobotState.SCORING
        antlerState = Z_Grip.States.L5
        coneStackState = ConeStackState.SIX
        middle = false
        done = false
    }

    fun testApp() {
        extArmState = X_Slider.States.READY
        extLiftState = Y_Slider.States.BOTTOM
        cupArmState = Arm.States.DOWN
        armGripState = X_Grip.States.OPEN
        liftGripState = Y_Grip.States.RECEIVE
        robotState = RobotState.RETRACTING
        antlerState = Z_Grip.States.L5
        coneStackState = ConeStackState.SIX
        middle = true

        while (op.opModeIsActive() && !done) {
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

    fun runApp(target: Int) {
        extArmState = X_Slider.States.READY
        extLiftState = Y_Slider.States.BOTTOM
        armGripState = X_Grip.States.OPEN
        liftGripState = Y_Grip.States.RECEIVE
        cupArmState = Arm.States.DOWN
        robotState = RobotState.RETRACTING

        antlerState = Z_Grip.States.L5
        coneStackState = ConeStackState.SIX
        middle = target == 2

        while (op.opModeIsActive() && !done) {
            when (robotState) {
                RobotState.DROPPING -> dropping()
                RobotState.RETRACTING -> retracting()
                RobotState.EARLYSCORE -> earlyscore()
                RobotState.SCORING -> scoring()
                RobotState.DONE -> done()
            }
            op.telemetry.addData("Current Angle", instance.getAbsoluteHeading());
            op.telemetry.update()
        }
    }

    // Movement Controls

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
                    instance.bot.xSlider.power = -xSliderConst.power / 1.5
                    extArmState = X_Slider.States.RETRACTING
                }
            }
            Directions.ADJUST -> {
                if (extArmState == X_Slider.States.MIDDLE) {
                    instance.bot.xSlider.power = xSliderConst.power / 1.5
                    extArmState = X_Slider.States.ADJUSTING
                }
                if (extArmState == X_Slider.States.READY) {
                    instance.bot.xSlider.power = xSliderConst.power / 2
                    extArmState = X_Slider.States.ADJUSTING
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
                }
            }
            Directions.RECEIVE -> {
                if (liftGripState == Y_Grip.States.DUMP) {
                    instance.bot.yGrip.position = yGripConst.receive
                    liftGripState = Y_Grip.States.RECEIVING
                }
            }
            else -> {
                return
            }
        }
    }

    private fun antler(level: Z_Grip.States) {
        when (level) {
            Z_Grip.States.L5 -> {
                instance.bot.zGrip.position = zGripConst.L5
                antlerState = Z_Grip.States.L5
            }
            Z_Grip.States.L4 -> {
                instance.bot.zGrip.position = zGripConst.L4
                antlerState = Z_Grip.States.L4
            }
            Z_Grip.States.L3 -> {
                instance.bot.zGrip.position = zGripConst.L3
                antlerState = Z_Grip.States.L3
            }
            Z_Grip.States.L2 -> {
                instance.bot.zGrip.position = zGripConst.L2
                antlerState = Z_Grip.States.L2
            }
            Z_Grip.States.L1 -> {
                instance.bot.zGrip.position = zGripConst.L1
                antlerState = Z_Grip.States.L1
            }
        }
    }

    // Code for States

    private fun dropping() {
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (liftGripState == Y_Grip.States.RECEIVING && System.currentTimeMillis() > timings.gripYTime) {
            liftGripState = Y_Grip.States.RECEIVE
        }
        if (extArmState == X_Slider.States.OUT && coneStackState != ConeStackState.NONE) {
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
            if (cupArmState == Arm.States.ASCENDING && abs(instance.bot.arm.currentPosition) > armConst.middle) {
                timings.extTimeOut = System.currentTimeMillis() + xSliderConst.time
                extArm(Directions.RETRACT)
            }
        }
        if (cupArmState == Arm.States.ASCENDING && abs(instance.bot.arm.currentPosition) > armConst.receive) {
            cupArm(Directions.STOP)
            cupArmState = Arm.States.RECEIVE
        }
        if (extLiftState == Y_Slider.States.DROPPING && (instance.bot.ySensor.state || instance.bot.ySlider.currentPosition < 10)) {
            lift(Directions.STOP)
            extLiftState = Y_Slider.States.BOTTOM
        }

        if (extLiftState == Y_Slider.States.BOTTOM && liftGripState == Y_Grip.States.RECEIVE) {
            if (coneStackState == ConeStackState.NONE) {
                robotState = RobotState.DONE
            } else if (armGripState == X_Grip.States.CLOSED && extArmState == X_Slider.States.RETRACTING && (cupArmState == Arm.States.RECEIVE || cupArmState == Arm.States.ASCENDING)) {
                robotState = RobotState.RETRACTING
            }
        }
    }

    private fun retracting() {
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (cupArmState == Arm.States.ASCENDING && abs(instance.bot.arm.currentPosition) > armConst.receive) {
            cupArm(Directions.STOP)
            cupArmState = Arm.States.RECEIVE
        }
        if (extArmState == X_Slider.States.RETRACTING) {
            changeAntler()
            if (!instance.bot.xSensor.state) {
                extArm(Directions.STOP)
                extArmState = X_Slider.States.READY
            }
        }
        if ((extArmState == X_Slider.States.READY || extArmState == X_Slider.States.ADJUSTING) && liftGripState == Y_Grip.States.RECEIVE) {
            if (instance.bot.xSensor.state) {
                extArm(Directions.ADJUST)
            }
            if (!instance.bot.xSensor.state) {
                extArm(Directions.STOP)
                op.sleep(100)
                extArmState = X_Slider.States.READY
            }
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
                changeConePosition(RobotState.EARLYSCORE)
                robotState = RobotState.EARLYSCORE
            }
        }
    }

    private fun earlyscore() {
        if (!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (!instance.bot.xSensor.state) {
            extArm(Directions.STOP)
            extArmState = X_Slider.States.READY
        }
        if (extLiftState == Y_Slider.States.RISING) {
            if (abs(instance.bot.ySlider.currentPosition) >= (ySliderConst.top - (ySliderConst.top - 500))) {
                if (liftGripState == Y_Grip.States.RECEIVE) {
                    timings.gripYTime = System.currentTimeMillis() + yGripConst.time
                    liftGrip(Directions.DUMP)
                }
            }
            if (abs(instance.bot.ySlider.currentPosition) >= ySliderConst.middle) {
                if (extArmState != X_Slider.States.EXTENDING) {
                    timings.extTimeOut = System.currentTimeMillis() + xSliderConst.time
                    horizontalSliderMax = abs(instance.bot.xSlider.currentPosition) + 350
                    extArm(Directions.EXTEND)
//                    armGrip(Directions.READY)
                }
                if (extArmState == X_Slider.States.EXTENDING && liftGripState == Y_Grip.States.DUMPING) {
                    robotState = RobotState.SCORING
                }
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
//            if (!instance.bot.xSensor.state) {
//                horizontalSliderMax = abs(instance.bot.xSlider.currentPosition) + 285
//                timings.extTimeOut = System.currentTimeMillis() + (xSliderConst.time - 500)
//            }
            if (abs(instance.bot.xSlider.currentPosition) >= horizontalSliderMax || System.currentTimeMillis() > timings.extTimeOut) {
                extArm(Directions.STOP)
                extArmState = X_Slider.States.OUT
            }
        }
        if (extLiftState == Y_Slider.States.DROPPING && instance.bot.ySensor.state) {
            lift(Directions.STOP)
            extLiftState = Y_Slider.States.BOTTOM
        }

        if (extArmState == X_Slider.States.OUT && (extLiftState == Y_Slider.States.DROPPING || extLiftState == Y_Slider.States.BOTTOM)) {
            robotState = RobotState.DROPPING
        }
    }

    private fun done() {
        instance.bot.ySlider.power = 0.0
        instance.bot.xSlider.power = 0.0
        instance.bot.arm.power = 0.0
        done = true
    }

    // Sub Functions

    private fun changeConePosition(state: RobotState) {
        when (coneStackState) {
            ConeStackState.SIX -> {
                coneStackState = ConeStackState.FIVE
                robotState = state
                return
            }
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
//                coneStackState = if (middle) {
                coneStackState = ConeStackState.TWO
//                } else {
//                    ConeStackState.NONE
//                }
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

    private fun changeAntler() {
//        if (middle) {
            when (coneStackState) {
                ConeStackState.SIX -> antler(Z_Grip.States.L5)
                ConeStackState.FIVE -> antler(Z_Grip.States.L5)
                ConeStackState.FOUR -> antler(Z_Grip.States.L4)
                ConeStackState.THREE -> antler(Z_Grip.States.L3)
                ConeStackState.TWO -> antler(Z_Grip.States.L2)
                ConeStackState.ONE -> antler(Z_Grip.States.L1)
                else -> {
                    return
                }
            }
//        } else {
//            when (coneStackState) {
//                ConeStackState.FIVE -> antler(Directions.UP)
//                ConeStackState.FOUR -> antler(Directions.READY)
//                ConeStackState.THREE -> antler(Directions.DOWN)
//                ConeStackState.TWO -> antler(Directions.DOWN)
//                ConeStackState.ONE -> antler(Directions.DOWN)
//                else -> {
//                    return
//                }
//            }
//        }
    }
}