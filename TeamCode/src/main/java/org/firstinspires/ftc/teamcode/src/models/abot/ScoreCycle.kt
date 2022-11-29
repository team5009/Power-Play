package org.firstinspires.ftc.teamcode.src.models.abot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.abs


class ScoreCycle (opMode: LinearOpMode, robot: TeleInstance){
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


    // todo move these to bot
    val LIFT_MIN = 50
    val LIFT_RISING = 118 * 12
    val LIFT_MAX = 118 * 20
    val DUMP_TIME = 1000
    val EXT_ARM_MAX = 118 * 14
    val EXT_ARM_READY = 118 * 4.5
    val EXT_ARM_MIN = 380
    val EXT_ARM_TIME = 1500
    val GRAB_TIME = 1200
    val CUP_ARM_UPTIME = 7000
    val CUP_ARM_DOWNTIME = 800
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
            //RobotState.HANDOVER -> handover()
            //RobotState.CLEARAREA -> clearArea()
            //RobotState.EARLYLIFT -> earlyLift()
            //RobotState.LATELIFT -> lateLift()
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

        bot.cupArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.cupArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        bot.cupArmInit()
//        Thread.sleep(1000)
        bot.extArmInit()
        Thread.sleep(1000)
        bot.cupHandInit()
        Thread.sleep(1000)
        bot.liftInit()
        Thread.sleep(100)
        bot.liftHandInit()
    }

    private fun dumping() {
//        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
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
            robotState = RobotState.PICKUP
            bot.gripY.position = GRIP_Y_RECEIVE
            bot.extLift.power = 0.4
            bot.extArm.power = -0.5
            gripYTime = System.currentTimeMillis() + DUMP_TIME
            extTimeOut = System.currentTimeMillis() + EXT_ARM_TIME
        }
    }
    private fun pickUp() {
//        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.extLift.currentPosition) <= LIFT_MIN) { // todo check touchsensor state
            bot.extLift.power = 0.0
            lift = LiftState.BOTTOM
        }
        if(abs(bot.extArm.currentPosition) > EXT_ARM_MAX || extTimeOut < System.currentTimeMillis()) {
            bot.extArm.power = 0.0
            ext = ExtState.OUT
            if(extTimeOut < System.currentTimeMillis()) {
                robotState = RobotState.GRABBING
                bot.gripX.position = GRIP_X_CLOSED
                gripXTime = System.currentTimeMillis() + GRAB_TIME
            }
        }
    }
    private fun grabbing() {
//        bot.cupArm.power = 0.0
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.extLift.currentPosition) <= LIFT_MIN) { // todo check touchsensor state
            bot.extLift.power = 0.0
            lift = LiftState.BOTTOM
        } else if(abs(bot.extLift.currentPosition) <= (LIFT_MAX - LIFT_MIN) / 2) {
            lift = LiftState.LOWERING
        }
        if(gripXTime < System.currentTimeMillis()) {
            gripX = GripXState.CLOSED
        }
        if(gripX == GripXState.CLOSED && (lift == LiftState.LOWERING || lift == LiftState.BOTTOM)) {
            robotState = RobotState.RETRACTING
            bot.cupArm.power = 0.9
            bot.extArm.power = 0.9
            cupArmTimeOut = System.currentTimeMillis() + CUP_ARM_UPTIME
        }
    }
    private fun retracting() {
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.cupArm.currentPosition) > (82.0 * bot.ticksPerDegree)) { //todo bot.armticks()
            bot.cupArm.power = 0.0
        }
        if(cupArmTimeOut < System.currentTimeMillis()) {
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
        }
        if(gripYTime < System.currentTimeMillis()) {
            gripY = GripYState.RECEIVE
        }
        if(gripY == GripYState.RECEIVE && cupArm == CupArmState.UP && ext == ExtState.IN && lift == LiftState.BOTTOM) {
            robotState = RobotState.HANDOVER
            bot.gripX.position = GRIP_X_OPEN
            gripXTime = System.currentTimeMillis() + GRAB_TIME
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
            bot.extArm.power = -0.9
            bot.cupArm.power = -0.8
            cupArmTimeOut = System.currentTimeMillis() + CUP_ARM_DOWNTIME
            robotState = RobotState.CLEARAREA
        }
    }
    private fun clearArea() {
        if(!op.opModeIsActive()) {
            robotState = RobotState.DONE
            return
        }
        if(abs(bot.cupArm.currentPosition) < 30.0 * bot.ticksPerDegree) { //todo bot.armticks()
            bot.cupArm.power = 0.0
        }
        if(cupArmTimeOut < System.currentTimeMillis()) {
            bot.cupArm.power = 0.0
            cupArm = CupArmState.DOWN
            robotState = RobotState.EARLYLIFT
            bot.extLift.power = -0.9
        }
        if(abs(bot.extArm.currentPosition) >= EXT_ARM_READY) {
            bot.extArm.power = 0.0
            ext = ExtState.READY
        }
    }
    private fun earlyLift() {
//        bot.cupArm.power = 0.0
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
//        bot.cupArm.power = 0.0
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
    private fun done() {
        //stop all motors
        bot.cupArm.power = 0.0
        bot.extLift.power = 0.0
        bot.extArm.power = 0.0
    }
}