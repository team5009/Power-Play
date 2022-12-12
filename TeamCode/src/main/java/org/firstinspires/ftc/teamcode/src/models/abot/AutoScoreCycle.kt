package org.firstinspires.ftc.teamcode.src.models.abot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlin.math.abs

class AutoScoreCycle(opMode: LinearOpMode, robot: AutoInstance) {
    enum class GripYState {RECEIVE, DUMPING, DUMP, RESETTING}
    enum class LiftState {BOTTOM, RISING, TOP, LOWERING}
    enum class ExtState {IN, EXTENDING, READY, OUT, RETRACTING}
    enum class CupArmState {UP, GOINGDOWN, DOWN, GOINGUP}
    enum class GripXState {OPEN, CLOSING, CLOSED, OPENING}

    enum class RobotState {DUMPING, PICKUP, GRABBING, RETRACTING, HANDOVER, CLEARAREA, EARLYLIFT, LATELIFT, DONE}

    private val op = opMode
    private val bot = robot

    lateinit var gripY : GripYState
    lateinit var lift : LiftState
    lateinit var ext : ExtState
    lateinit var cupArm : CupArmState
    lateinit var gripX : GripXState
    var robotState : RobotState = RobotState.DUMPING

    var gripYTime : Long = 0
    var gripXTime : Long = 0
    var extTimeOut : Long = 0
    var liftTimeOut : Long = 0
    var cupArmTimeOut : Long = 0

    var cycle = 0

    // todo move these to bot
    val LIFT_MIN = 50
    val LIFT_RISING = 74 * 11
    val LIFT_MAX = 74 * 21
    val DUMP_TIME = 1000
    val EXT_ARM_MAX = 118.0/3.0 * 13
    val EXT_ARM_READY = 118.0/3.0 * 11
    val EXT_ARM_MIN = 50
    val EXT_ARM_TIME = 800
    val GRAB_TIME = 900
    val CUP_ARM_UPTIME = 1600
    val CUP_ARM_DOWNTIME = 1600
    val GRIP_X_OPEN = 0.0
    val GRIP_X_CLOSED = 1.0
    val GRIP_Y_RECEIVE = 0.24
    val GRIP_Y_DUMP = 1.0


    fun unit_test(){
        robotState = RobotState.RETRACTING
        gripY = GripYState.RECEIVE
        lift = LiftState.BOTTOM
        ext = ExtState.IN
        cupArm = CupArmState.UP
        gripX = GripXState.CLOSED
        while(op.opModeIsActive()) {
            when(robotState) {
                RobotState.RETRACTING -> retracting()
                RobotState.HANDOVER -> handover()
                RobotState.CLEARAREA -> clearArea()
                RobotState.EARLYLIFT -> earlyLift()
                RobotState.LATELIFT -> lateLift()
                RobotState.DUMPING -> dumping()
                RobotState.PICKUP -> pickUp()
                RobotState.GRABBING -> grabbing()
                else -> done()
            }
            op.telemetry.addData("Lift pos", bot.extLift.currentPosition)
            op.telemetry.addData("CupArm pos", bot.cupArm.currentPosition)
            op.telemetry.addData("Extendo pos", bot.extArm.currentPosition)
            op.telemetry.addData("Current State", robotState.name)
            op.telemetry.update()
        }
    }

    fun scoreCones(){
        when(robotState) {
            RobotState.DUMPING -> dumping()
            RobotState.PICKUP -> pickUp()
            RobotState.GRABBING -> grabbing()
            RobotState.RETRACTING -> retracting()
            RobotState.HANDOVER -> handover()
            RobotState.CLEARAREA -> clearArea()
            RobotState.EARLYLIFT -> earlyLift()
            RobotState.LATELIFT -> lateLift()
            else -> done()
        }
    }


    fun init() {
        gripY = GripYState.DUMP
        lift = LiftState.TOP
        ext = ExtState.READY
        cupArm = CupArmState.DOWN
        gripX = GripXState.OPEN
        robotState = RobotState.DUMPING
    }


//    fun init() {
//        gripY = GripYState.DUMP
//        lift = LiftState.TOP
//        ext = ExtState.READY
//        cupArm = CupArmState.DOWN
//        gripX = GripXState.OPEN
//        robotState = RobotState.DUMPING
//
//        bot.cupArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        bot.cupArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
////        bot.cupArmInit()
////        Thread.sleep(1000)
//        bot.extArmInit()
//        Thread.sleep(1000)
//        bot.cupHandInit()
//        Thread.sleep(1000)
//        bot.liftInit()
//        Thread.sleep(100)
//        bot.liftHandInit()
//    }



    private fun dumping() {
//        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.extArm.currentPosition) >= EXT_ARM_READY) {
            bot.extArm.power = 0.0
            ext = ExtState.READY
        }
        // previous state must be LATELIFT so lift is running stop it when it reaches the top
        if(abs(bot.extLift.currentPosition) >= LIFT_MAX) {
            bot.extLift.power = 0.0
            lift = LiftState.TOP
        }
        if(gripYTime < System.currentTimeMillis()) {
            gripY = GripYState.DUMP
        }
        if(lift == LiftState.TOP && gripY == GripYState.DUMP) {
            cycle += 1
            robotState = RobotState.PICKUP
            bot.gripY.position = GRIP_Y_RECEIVE
            bot.extLift.power = -0.5
            bot.extArm.power = 0.5
            gripYTime = System.currentTimeMillis() + DUMP_TIME
            extTimeOut = System.currentTimeMillis() + EXT_ARM_TIME
        }
    }
    private fun pickUp() {
        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.extLift.currentPosition) <= LIFT_MIN) { // todo check touchsensor state
            bot.extLift.power = 0.0
            lift = LiftState.BOTTOM
        } else if(abs(bot.extLift.currentPosition) <= (LIFT_MAX - LIFT_MIN) / 2) {
            lift = LiftState.LOWERING
            bot.extLift.power = -0.4
        }
        if(abs(bot.extArm.currentPosition) > EXT_ARM_MAX || extTimeOut < System.currentTimeMillis() || cycle == 6) {
            bot.extArm.power = 0.0
            ext = ExtState.OUT
            robotState = RobotState.GRABBING
            if (cycle != 6) {
                bot.cupArm.power = -0.5
                cupArmTimeOut = System.currentTimeMillis() + CUP_ARM_DOWNTIME
            }
        }
    }
    private fun grabbing() {
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.cupArm.currentPosition) < 40.0 * (288.0/360.0)) { //todo bot.armticks()
            bot.cupArm.power = 0.0
        }
        if(cupArmTimeOut < System.currentTimeMillis()) {
            bot.cupArm.power = 0.0
            bot.gripX.position = GRIP_X_CLOSED
            gripXTime = System.currentTimeMillis() + 500
            cupArm = CupArmState.DOWN
        }
        if(abs(bot.extLift.currentPosition) <= LIFT_MIN) { // todo check touchsensor state
            bot.extLift.power = 0.0
            lift = LiftState.BOTTOM
        } else if(abs(bot.extLift.currentPosition) <= (LIFT_MAX - LIFT_MIN) / 2) {
            lift = LiftState.LOWERING
            bot.extLift.power = -0.5
        }
        if(bot.gripX.position == GRIP_X_CLOSED) {
            gripX = GripXState.CLOSED
        }
        if((gripX == GripXState.CLOSED || cycle == 6) && (lift == LiftState.LOWERING || lift == LiftState.BOTTOM)) {
            robotState = RobotState.RETRACTING
            if (cycle != 6) {
                bot.cupArm.power = 0.9
                cupArmTimeOut = System.currentTimeMillis() + CUP_ARM_UPTIME
            }
        }
    }
    private fun retracting() {
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if (abs(bot.extArm.currentPosition) < EXT_ARM_MIN + (118 * 2) && bot.gripX.position == GRIP_X_CLOSED) {
            bot.gripX.position = GRIP_X_OPEN
            gripXTime = System.currentTimeMillis() + GRAB_TIME
        }
        if(abs(bot.cupArm.currentPosition) > (90.0 * (288.0/360.0))) { //todo bot.armticks()
            bot.cupArm.power = 0.0
        }
        if(cupArmTimeOut < System.currentTimeMillis() || cycle == 6) {
            bot.cupArm.power = 0.0
            cupArm = CupArmState.UP
        }
        if(abs(bot.extLift.currentPosition) <= LIFT_MIN) { // todo check touchsensor state
            bot.extLift.power = 0.0
            lift = LiftState.BOTTOM
        }
        if(abs(bot.extArm.currentPosition) < EXT_ARM_MIN) { // todo check touchsensor state
            bot.extArm.power = 0.0
            ext = ExtState.IN
        } else if (abs(bot.cupArm.currentPosition) > (50.0 * (288.0/360.0))) {
            bot.extArm.power = -0.5
        }
        if(gripYTime < System.currentTimeMillis()) {
            gripY = GripYState.RECEIVE
        }
        if(gripY == GripYState.RECEIVE && cupArm == CupArmState.UP && ext == ExtState.IN && lift == LiftState.BOTTOM) {
            robotState = RobotState.HANDOVER
            if (bot.gripX.position == GRIP_X_CLOSED) {
                bot.gripX.position = GRIP_X_OPEN
                gripXTime = System.currentTimeMillis() + GRAB_TIME
            }
            if (cycle == 6) {
                robotState = RobotState.DONE
            }
        }
    }
    private fun handover() {
//        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(gripXTime < System.currentTimeMillis()) {
            gripX = GripXState.OPEN
            robotState = RobotState.CLEARAREA
            bot.extArm.power = 0.5
        }
    }
    private fun clearArea() {
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.extArm.currentPosition) >= EXT_ARM_READY) {
            bot.extArm.power = 0.0
            ext = ExtState.READY
        }
        if (abs(bot.extArm.currentPosition) >= EXT_ARM_MIN + (118 * 3)) {
            robotState = RobotState.EARLYLIFT
            bot.extLift.power = 0.9
            bot.gripX.position = 0.6
            gripXTime = System.currentTimeMillis() + GRAB_TIME / 2
        }
    }
    private fun earlyLift() {
        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.extArm.currentPosition) >= EXT_ARM_READY) {
            bot.extArm.power = 0.0
            ext = ExtState.READY
        }
        if(abs(bot.extLift.currentPosition) >= LIFT_RISING) {
            lift = LiftState.RISING
            robotState = RobotState.LATELIFT
            bot.gripY.position = GRIP_Y_DUMP
            gripYTime = System.currentTimeMillis() + DUMP_TIME
        }
    }
    private fun lateLift() {
        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.extArm.currentPosition) >= EXT_ARM_READY) {
            bot.extArm.power = 0.0
            ext = ExtState.READY
        }
        if(abs(bot.extLift.currentPosition) >= LIFT_MAX) {
            bot.extLift.power = 0.0 // Maybe can set power to low for holding position
            lift = LiftState.TOP
            robotState = RobotState.DUMPING
        }
    }
    fun done() {
        //stop all motors
        bot.cupArm.power = 0.0
        bot.extLift.power = 0.0
        bot.extArm.power = 0.0
    }
}