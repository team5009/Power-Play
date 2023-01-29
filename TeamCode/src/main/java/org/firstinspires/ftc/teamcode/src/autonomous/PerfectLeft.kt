package org.firstinspires.ftc.teamcode.src.autonomous

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.cycles.AutoCycle
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Cam

@Autonomous(name = "Perfect Right", group = "Production Autonomous")
class PerfectLeft : LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        val cycle = AutoCycle(this, bot)
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "cameraMonitorViewId",
            "id", hardwareMap.appContext.packageName
        )
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.frontCam
        val vuforia =
            Cam(params)

        telemetry.addData("Info", "Ready")
        telemetry.update()
        bot.initJob()
        bot.startJob(vuforia)

        val pow = 0.9

        waitForStart()
        val target = bot.target

        if (opModeIsActive()) {
            telemetry.addData("info", bot.target)
            telemetry.update()
            cycle.extArmState = AutoCycle.ExtArmState.IN
            bot.resetJob(vuforia)
            bot.resetDriveEncoders()
            sleep(100)
            bot.init()
            bot.move(70.0, -pow, true, true)
            sleep(100)
            bot.move(10.0, pow, true)
            sleep(100)
            bot.pivot(57, pow)
            sleep(100)
            bot.strafe(2.75, pow)
            sleep(50)
//            bot.move(1.5, -pow, true)
//            sleep(50)
            cycle.extArm(AutoCycle.Directions.READY)
            sleep(100)
            cycle.runApp(target)


            when (target) {
                1 -> {
//                    bot.strafe(5.0, -0.9)
                    bot.pivot(50, -pow)
                    sleep(100)
                    bot.move(5.0, pow, true)
                    sleep(100)
                    bot.pivot(60, -pow)
                    sleep(100)
                    bot.move(24.0, pow, true)
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
                    bot.pivot(10, -pow)
                    sleep(50)
                    bot.move(27.0, pow, true)
                    sleep(50)
                    bot.pivot(35, -pow)
                    sleep(50)
                    bot.move(7.5, pow, true)
//                    bot.strafe(1.0, 0.9)
//                    bot.move(27.0, pow, true)
//                    bot.pivot(45, -pow)
//                    sleep(100)
                }
            }
            sleep(100)
            bot.liftHand(AutoInstance.Direction.CLOSE)
        }
    }
}