package org.firstinspires.ftc.teamcode.src.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.teamcode.src.models.abot.TeleInstance

@TeleOp(name = "Regular TeleOp", group = "TeleOp Production")
class RegularOpMode: LinearOpMode() {
    override fun runOpMode() {
        val bot = TeleInstance(this, hardwareMap)
        telemetry.addData(">", "Ready")
        telemetry.update()
        waitForStart()

        GlobalScope.launch {
            while (opModeIsActive()) {
                bot.gamePadOne()
            }
        }
        GlobalScope.launch {
            while (opModeIsActive()) {
                bot.gamePadTwo()
            }
        }
        while(opModeIsActive()){
            telemetry.addData(">", "Running")
            telemetry.addData("Wheel Encoder", bot.fl.currentPosition)
            telemetry.addData("ExtArm Encoder", bot.extArm.currentPosition)
            telemetry.addData("ExtArm Encoder", bot.extArm.currentPosition <= -1000)
            telemetry.addData("ExtLift Encoder", bot.extLift.currentPosition)
            telemetry.addData("CupArm Encoder", bot.cupArm.power)
            telemetry.addData("CupArm Angle", bot.cupArm.currentPosition * bot.ticksPerDegree)
            telemetry.addData("Grip Servo Pos", bot.gripX.position)
            telemetry.addData("Grip Servo Pos", bot.gripY.position)
            telemetry.addData("xAxis Sensor", bot.xAxis.state)
            telemetry.addData("yAxis Sensor", bot.yAxis.state)
            telemetry.update()
        }
    }
}