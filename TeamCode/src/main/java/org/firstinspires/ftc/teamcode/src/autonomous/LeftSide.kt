package org.firstinspires.ftc.teamcode.src.autonomous

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.Cam

@Autonomous(name="Left Side Autonomous", group="Production Autonomous")
class LeftSide : LinearOpMode(){

    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "cameraMonitorViewId",
            "id",
            hardwareMap.appContext.packageName
        )
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        val pow = 0.8
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.frontCam
        val vuforia = Cam(params)
        GlobalScope.launch {
            bot.checkTarget(vuforia)
        }

        waitForStart()

        val target = when (bot.parkingZone) {
            1 -> 1
            2 -> 3
            else -> 2
        }

        if (opModeIsActive()) {
            telemetry.addData("info", target)
            telemetry.update()
            bot.resetDriveEncoders()
            bot.init()
            sleep(100)
            bot.move(70.0, .9, true, acceleration = true)
            sleep(100)
            bot.move(10.0, -.9, true)
            sleep(100)
            bot.pivot(75, .9)
            sleep(100)
            bot.strafe(2.0, -.9)
            sleep(100)
            bot.move(0.5, -.5, true)
            sleep(100)


            bot.liftMove(AutoInstance.Direction.UP)
            sleep(100)
            bot.liftHand(AutoInstance.Direction.OPEN)
            sleep(1200)
            bot.liftHand(AutoInstance.Direction.MIDDLE)
            sleep(500)
            bot.liftMove(AutoInstance.Direction.DOWN)
            sleep(100)

            when (target) {
                3 -> {
                    bot.pivot(50, pow)
                    sleep(100)
                    bot.move(5.0, pow, true)
                    sleep(100)
                    bot.pivot(60, pow)
                    sleep(100)
                    bot.move(22.0, pow, true)
                    sleep(100)
                    bot.pivot(60, -pow)
                    sleep(100)
                    bot.move(5.0, pow, true)
                }
                2 -> {
                    bot.pivot(45, pow)
                    sleep(100)
                    bot.move(10.0, pow, true)
                }
                1 -> {
                    bot.move(27.0, pow, true)
                    sleep(100)
                    bot.pivot(45, pow)
                    sleep(100)
                    bot.move(10.0, pow, true)
                }
            }
            sleep(100)
            bot.liftHand(AutoInstance.Direction.CLOSE)
        }
    }
}