package org.firstinspires.ftc.teamcode.src.autonomous

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.cycles.autonomous.AutoCycle
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Cam
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Directions
import org.firstinspires.ftc.teamcode.src.models.abot.utils.X_Slider

@Autonomous(name = "PID Right", group = "Production Autonomous")
class PIDRight : LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, telemetry)
        val cycle = AutoCycle(this, bot)

        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "cameraMonitorViewId",
            "id", hardwareMap.appContext.packageName
        )
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.bot.frontCam
        val vuforia =
            Cam(params)

        telemetry.addData("Info", "Ready")
        telemetry.update()
        bot.initJob()
        bot.startJob(vuforia)

        val pow = 90.0

        waitForStart()
        val target = bot.target

        if (opModeIsActive()) {


            telemetry.addData("info", bot.target)
            telemetry.update()
            cycle.extArmState = X_Slider.States.IN
            bot.resetJob(vuforia)
            bot.resetDriveEncoders()
            bot.resetEncoders()


            sleep(100)
            bot.init()
            bot.readyLift()
            bot.runToPosition(-61.75, pow, 0.0)
            bot.turnToAngle(72.0, pow)
            bot.runToPosition(0.5, pow, 72.0)
            bot.resetSliderEncoders()
            sleep(50)
            cycle.extArm(Directions.READY)
            sleep(100)
            cycle.runApp(target)

            GlobalScope.launch {
                bot.liftHand(AutoInstance.Direction.MIDDLE)
                cycle.extArm(Directions.DONE)
            }

            when (target) {
                1 -> {
                    bot.turnToAngle(110.0, pow)
                    sleep(100)
                    bot.runToPosition(-25.0, pow, 110.0)
                    sleep(100)
                    bot.turnToAngle(180.0, pow)
                    sleep(100)
                    bot.runToPosition(-5.0, pow, 180.0)
                }

                2 -> {
                    bot.turnToAngle(0.0, pow)
                    sleep(100)
                    bot.runToPosition(10.0, pow, 0.0)
                }

                3 -> {
                    bot.turnToAngle(-60.0, pow)
                    sleep(50)
                    bot.runToPosition(27.0, pow, 60.0)
                    sleep(50)
                    bot.turnToAngle(-0.0, pow)
                    sleep(50)
                    bot.runToPosition(7.5, pow, 0.0)
                }
            }
        }
    }
}