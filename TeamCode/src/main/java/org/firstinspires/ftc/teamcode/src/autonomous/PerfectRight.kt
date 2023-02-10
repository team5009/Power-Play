package org.firstinspires.ftc.teamcode.src.autonomous

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.cycles.autonomous.AutoCycle
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Cam

@Autonomous(name = "Perfect Right", group = "Production Autonomous")
class PerfectRight : LinearOpMode() {
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

        val pow = 0.95

        waitForStart()
        val target = bot.target

        if (opModeIsActive()) {


            telemetry.addData("info", bot.target)
            telemetry.update()
            cycle.extArmState = AutoCycle.ExtArmState.IN
            bot.resetJob(vuforia)
            bot.resetDriveEncoders()
            bot.resetEncoders()


            sleep(100)
            bot.init()
            bot.readyLift()
            bot.move(58.5, -pow, true)
//            sleep(100)
//            bot.move(8.0, pow, true)
            sleep(100)
            bot.pivot(77, pow)
            sleep(50)
            bot.move(0.5, pow, true)
            bot.resetSliderEncoders()
            sleep(50)
            cycle.extArm(AutoCycle.Directions.READY)
            sleep(100)
            cycle.runApp(target)

            GlobalScope.launch {
                bot.liftHand(AutoInstance.Direction.MIDDLE)
                cycle.extArm(AutoCycle.Directions.DONE)
            }

            when (target) {
                1 -> {
                    bot.pivot(40, pow)
                    sleep(100)
                    bot.move(25.0, -pow, true)
                    sleep(100)
                    bot.pivot(60, pow)
                    sleep(100)
                    bot.move(5.0, -pow, true)
                }

                2 -> {
                    bot.pivot(75, -pow)
                    sleep(100)
                    bot.move(10.0, pow, true)
                }

                3 -> {
                    sleep(50)
                    bot.pivot(15, -pow)
                    sleep(50)
                    bot.move(27.0, pow, true)
                    sleep(50)
                    bot.pivot(50, -pow)
                    sleep(50)
                    bot.move(7.5, pow, true)
                }
            }
        }
    }
}