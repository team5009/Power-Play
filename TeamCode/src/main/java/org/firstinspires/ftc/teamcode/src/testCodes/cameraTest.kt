package org.firstinspires.ftc.teamcode.src.testCodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector


@Autonomous(name = "Vuforia Field Nav Webcam", group = "AutoTest")
class cameraTest: LinearOpMode() {

    private val TFOD_Asset = "cones/tflite_quantized/model.tflite"
    private val TFOD_List: Array<String> = arrayOf<String>("BlueCone", "RedCone")

    override fun runOpMode() {
        println("This Tensorflow")
        println(initTensor())
        telemetry.addData("Initialized", "Tensor")
        telemetry.update()

        waitForStart()
    }

    private fun initVuforia(): VuforiaLocalizer {
        val webcam = hardwareMap.get("Webcam") as WebcamName
        val parameters: VuforiaLocalizer.Parameters = VuforiaLocalizer.Parameters()

        parameters.vuforiaLicenseKey = hardwareMap.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }
        parameters.cameraName = webcam

        parameters.useExtendedTracking = false

        return ClassFactory.getInstance().createVuforia(parameters)
    }
    private fun initTensor(): TFObjectDetector {
        val vuforia: VuforiaLocalizer = initVuforia()
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)

        val tfodParameters: TFObjectDetector.Parameters = TFObjectDetector.Parameters(cameraMonitorViewId)
        tfodParameters.minResultConfidence = 0.5f
        tfodParameters.isModelTensorFlow2 = true
        tfodParameters.inputSize = 300
        val tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia)
        tfod.loadModelFromAsset(TFOD_Asset, *TFOD_List)

        return tfod
    }
}