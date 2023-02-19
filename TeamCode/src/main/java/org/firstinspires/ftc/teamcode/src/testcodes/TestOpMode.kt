package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop.TeleInstance

@TeleOp(name = "Test TeleOp", group = "TeleOp Test")
class TestOpMode : LinearOpMode() {
    override fun runOpMode() {
        val instance = TeleInstance(this)
        telemetry.addData(">", "Ready")
        telemetry.update()
        instance.resetEncoders()

        waitForStart()

        GlobalScope.launch {
            while (opModeIsActive()) {
                instance.gamePadOne()
            }
        }
        GlobalScope.launch {
            while (opModeIsActive()) {
                instance.gamePadTwo()
            }
        }
        while (opModeIsActive()) {
            telemetry.addData(">", "Running")
            telemetry.addData("Wheel Encoder", instance.bot.fl.currentPosition)
            telemetry.addData("ExtArm Encoder", instance.bot.xSlider.currentPosition)
            telemetry.addData("ExtArm Encoder", instance.bot.xSlider.currentPosition <= -1000)
            telemetry.addData("ExtLift Encoder", instance.bot.ySlider.currentPosition)
            telemetry.addData("CupArm Power", instance.bot.arm.power)
            telemetry.addData("Lift Power", instance.bot.ySlider.power)
            telemetry.addData("Arm Power", instance.bot.xSlider.power)
            telemetry.addData("CupArm Angle", instance.bot.arm.currentPosition * instance.ticksPerDegree)
            telemetry.addData("GripX Servo Pos", instance.bot.xGrip.position)
            telemetry.addData("GripY Servo Pos", instance.bot.yGrip.position)
            telemetry.addData("GripZ Servo Pos", instance.bot.zGrip.position)
            telemetry.addData("xAxis Sensor", instance.bot.xSensor.state)
            telemetry.addData("yAxis Sensor", instance.bot.ySensor.state)
            telemetry.addData("", "")
            telemetry.addData("yAxis Sensor", instance.process)
            telemetry.update()
        }
    }
}