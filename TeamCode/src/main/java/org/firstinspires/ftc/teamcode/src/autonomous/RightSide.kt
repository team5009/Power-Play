package org.firstinspires.ftc.teamcode.src.autonomous

import android.graphics.Bitmap
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.Cam

@Autonomous(name="Right Side Autonomous", group="Production Autonomous")
class RightSide : LinearOpMode(){
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.camera
        val vuforia = Cam(params)
        var bitmap: Bitmap? = null
        telemetry.addData("Status", "Ready")
        telemetry.update()
        waitForStart()
        while (bitmap == null) {
            bitmap = bot.checkTarget(vuforia)
        }
        val target = bot.seeSignal(bitmap, vuforia)
        if (opModeIsActive()) {
            telemetry.addData("info", target)
            telemetry.update()
            bot.resetDriveEncoders()
            bot.init()
            sleep(100)
            bot.move(70.0, .9, true)
            sleep(100)
            bot.move(10.0, -.9, true)
            sleep(100)
            bot.pivot(75, -.9)
            sleep(100)
            bot.strafe(2.0, .9)
            sleep(100)
            bot.move(0.5, -.5, true)
            sleep(100)
            bot.liftMove(AutoInstance.Direction.UP)
            sleep(100)
            bot.liftHand(AutoInstance.Direction.OPEN)
            sleep(100)
            bot.liftMove(AutoInstance.Direction.DOWN)
            sleep(100)
            bot.liftHand(AutoInstance.Direction.MIDDLE)
            sleep(100)
            bot.pivot(50, -.9)
            sleep(100)
            bot.move(5.0, .9, true)
            sleep(100)
            when (target) {
                1 -> {
                    bot.cupArmMove(AutoInstance.Direction.UP)
                    sleep(100)
                    bot.strafe(30.0, -.9)
                    sleep(50)
                    bot.cupArmMove(AutoInstance.Direction.DOWN)
                    sleep(100)
                }
                2 -> {
                    bot.move(1.0, .9, true)
                    sleep(50)
                }
                3 -> {
                    bot.cupArmMove(AutoInstance.Direction.UP)
                    sleep(100)
                    bot.strafe(30.0, .9)
                    sleep(50)
                    bot.cupArmMove(AutoInstance.Direction.DOWN)
                    sleep(100)
                }
            }
            bot.liftHand(AutoInstance.Direction.CLOSE)
            sleep(100)
            bot.extArmInit()
        }
    }
}