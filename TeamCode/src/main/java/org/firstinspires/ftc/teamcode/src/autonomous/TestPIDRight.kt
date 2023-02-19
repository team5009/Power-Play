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

@Autonomous(name = "Test PID Right", group = "Test Autonomous")
class TestPIDRight : LinearOpMode() {
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

        val pow = 65.0

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
//            GlobalScope.launch {
//
//            }
            bot.runToPosition(-61.75, pow)
            bot.turnToPID(-72.0, pow)
            bot.runToPosition(0.5, pow)
            bot.resetSliderEncoders()
            cycle.extArm(Directions.READY)
            sleep(1000)
//            cycle.runApp(target)
            bot.strafeToPosition(10.0, 0.9)
            sleep(5000)

            GlobalScope.launch {
                bot.liftHand(AutoInstance.Direction.MIDDLE)
                cycle.extArm(Directions.DONE)
            }
            if (bot.getAbsoluteHeading() > 90) {
                bot.turnToPID(90.0, pow)
            } else {
                bot.turnToPID(-90.0, pow)
            }

            when (target) {
                1 -> {
                    bot.runToPosition(25.0, pow)
                }

                2 -> {

                }

                3 -> {
                    bot.runToPosition(-25.0, pow)
                }
            }
        }
    }
}