package org.firstinspires.ftc.teamcode.src.testCodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaCurrentGame
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.ABot.Cam
import org.firstinspires.ftc.teamcode.src.models.ABot.CamInstance


@Autonomous(name = "Camera", group = "AutoTest")
class Camera: LinearOpMode() {
    lateinit var vuforiaPowerPlay: VuforiaCurrentGame
    lateinit var bot: CamInstance
    val cam = Cam(this)
    var camReady = false
    override fun runOpMode() {
        vuforiaPowerPlay = VuforiaCurrentGame()
        bot = CamInstance(this, hardwareMap, telemetry)
        camReady = cam.setUp()
        waitForStart()
        if (opModeIsActive()){
            val barcode = cam.startStackCount()
            cam.closeCamera()
            telemetry.addData("Info", "Picture Taken")

            telemetry.addData("Status", "Initializing Vuforia. Please wait...");
            telemetry.update();
            initVuforia();
            vuforiaPowerPlay.activate()

            telemetry.addData(">>", "Vuforia initialized, press start to continue...");
            telemetry.update();
            vuforiaPowerPlay.setActiveCamera(bot.camera);
            telemetry.addData("Ring Count", "%d", barcode);
            telemetry.update();
        }
        cam.saveBitmap()

        vuforiaPowerPlay.deactivate()
        vuforiaPowerPlay.close()
    }
    private fun initVuforia() {
        vuforiaPowerPlay.initialize(
            bot.vuforiaKey,  // vuforiaLicenseKey
            bot.camera,  // cameraName
            "",  // webcamCalibrationFilename
            false,  // useExtendedTracking
            true,  // enableCameraMonitoring
            VuforiaLocalizer.Parameters.CameraMonitorFeedback.AXES,  // cameraMonitorFeedback
            0f,  // dx
            0f,  // dy
            0f,  // dz
            0f,  // xAngle
            0f,  // yAngle
            0f,  // zAngle
            true
        ) // useCompetitionFieldTargetLocations

    }
}