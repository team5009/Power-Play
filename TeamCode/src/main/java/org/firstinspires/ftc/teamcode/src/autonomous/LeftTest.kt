package org.firstinspires.ftc.teamcode.src.autonomous

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.cycles.autonomous.AutoCycle
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Cam
import org.firstinspires.ftc.teamcode.src.models.abot.utils.X_Slider

@Autonomous(name = "PID Right", group = "Production Autonomous")
class LeftTest : LinearOpMode() {
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
        bot.startJob(vuforia, AutoInstance.CameraPositions.RIGHT)

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
//            bot.readyLift()
            bot.runToPosition(-61.0, pow)
            bot.turnToAngle(280.0, -pow)
            telemetry.addData("info", "Initing")
            telemetry.update()
            bot.extArmInit()
            bot.liftHand(AutoInstance.Direction.OPEN)
//            bot.runToPosition(-1.5, pow)
            telemetry.addData("info", "running cycle")
            telemetry.update()
//            bot.resetSliderEncoders()
            cycle.runApp(target)

            telemetry.addData("info", "done cycle")
            telemetry.update()

            if (target == 3 || target == 2) {
                bot.liftHand(AutoInstance.Direction.MIDDLE)
                bot.pivotStrafeToPosition(-270.0, pow)
            }
            bot.doneWithXSlider()
            when (target) {
                1 -> {
                    telemetry.addData("info", "Target One")
                    telemetry.update()
                    bot.runToPosition(24.0, pow)
                    bot.turnToAngle(0.1, pow)
                }

                2 -> {
                    telemetry.addData("info", "Target One")
                    telemetry.update()
                    bot.turnToAngle(0.1, pow)
                    bot.runToPosition(10.0, pow)
                }

                3 -> {
                    telemetry.addData("info", "Target One")
                    telemetry.update()
                    bot.runToPosition(-24.0, pow)
                    bot.turnToAngle(0.1, pow)
                }
            }
        }
    }
}