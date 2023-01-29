package org.firstinspires.ftc.teamcode.src.autonomous

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.HalfAutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Cam

@Autonomous(name = "Right Side Autonomous", group = "Production Autonomous")
class RightSide : LinearOpMode() {
    override fun runOpMode() {
        val bot = HalfAutoInstance(this, hardwareMap, telemetry)
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
        val vuforia =
            Cam(params)
        bot.initJob()
        bot.startJob(vuforia)

        waitForStart()

        val target = bot.target

        if (opModeIsActive()) {
            telemetry.addData("info", bot.parkingZone)
            telemetry.update()
            bot.resetJob(vuforia)
            bot.resetDriveEncoders()
            sleep(100)
            bot.move(70.0, pow, true, acceleration = true)
            sleep(100)
            bot.move(10.0, -pow, true)
            sleep(100)
            bot.pivot(75, -pow)
            sleep(100)
            bot.strafe(1.5, pow)
            sleep(100)
            bot.move(1.5, -pow, true)
            sleep(100)

            when (target) {
                1 -> {
                    bot.pivot(50, -pow)
                    sleep(100)
                    bot.move(5.0, pow, true)
                    sleep(100)
                    bot.pivot(60, -pow)
                    sleep(100)
                    bot.move(22.0, pow, true)
                    sleep(100)
                    bot.pivot(60, pow)
                    sleep(100)
                    bot.move(5.0, pow, true)
                }
                2 -> {
                    bot.pivot(45, -pow)
                    sleep(100)
                    bot.move(10.0, pow, true)
                }
                3 -> {
                    bot.move(27.0, pow, true)
                    sleep(100)
                    bot.pivot(45, -pow)
                    sleep(100)
                    bot.move(10.0, pow, true)
                }
            }
        }
    }
}