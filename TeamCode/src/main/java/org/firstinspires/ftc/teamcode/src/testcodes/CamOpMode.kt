package org.firstinspires.ftc.teamcode.src.testcodes

import android.graphics.Bitmap
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.Cam

@TeleOp(name = "Cam Test", group = "TeleOp Test")
class CamOpMode: LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.frontCam
        val vuforia = Cam(params)
        var bitmap: Bitmap
        telemetry.addData("Status", "Ready")
        telemetry.update()
        waitForStart()

        while(opModeIsActive()) {
            if (gamepad1.a || gamepad2.a) {
//                val target = bot.seeSignal(bitmap, vuforia)
//                telemetry.addData("info", target)
                telemetry.update()
            }
        }
    }
}