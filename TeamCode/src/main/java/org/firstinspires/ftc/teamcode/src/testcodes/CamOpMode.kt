package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Cam

@Autonomous(name = "Cam Test", group = "TeleOp Test")
class CamOpMode : LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, telemetry)
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "cameraMonitorViewId",
            "id",
            hardwareMap.appContext.packageName
        )
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.bot.frontCam
        val vuforia =
            Cam(params)

        telemetry.addData("Status", "Ready")
        telemetry.update()
        bot.initJob()
        bot.startJob(vuforia, AutoInstance.CameraPositions.RIGHT)
        waitForStart()

        if (opModeIsActive()) {
            bot.resetJob(vuforia)
            telemetry.update()
        }
    }
}