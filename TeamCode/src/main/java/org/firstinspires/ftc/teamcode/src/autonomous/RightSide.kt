package org.firstinspires.ftc.teamcode.src.autonomous

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.AutoScoreCycle
import org.firstinspires.ftc.teamcode.src.models.abot.Cam

@Autonomous(name="Right Side Autonomous", group="Production Autonomous")
class RightSide : LinearOpMode(){
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        val pow = 0.8
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.frontCam
        val vuforia = Cam(params)
        val cycle = AutoScoreCycle(this, bot)
        bot.initJob()
        bot.startJob(vuforia)

        waitForStart()

        val target = when (bot.parkingZone) {
            1 -> 1
            2 -> 3
            else -> 2
        }

        if (opModeIsActive()) {
            telemetry.addData("info", bot.parkingZone)
            telemetry.update()
            bot.resetJob(vuforia)
            bot.resetDriveEncoders()
            bot.init()
            sleep(100)
            bot.move(70.0, pow, true, acceleration = true)
            sleep(100)
            bot.move(10.0, -pow, true)
            sleep(100)
            bot.pivot(75, -pow)
            sleep(100)
            bot.strafe(1.5, pow)
            sleep(100)
            bot.move(1.5, -pow, true)
            sleep(100)

            bot.liftMove(AutoInstance.Direction.UP)
            sleep(100)
            bot.liftHand(AutoInstance.Direction.OPEN)
            sleep(1200)
            bot.liftHand(AutoInstance.Direction.MIDDLE)
            sleep(500)
            bot.liftMove(AutoInstance.Direction.DOWN)
            sleep(100)

            //Begin the cycle
//            bot.extArmInit()
//            bot.cupArmMove(AutoInstance.Direction.UP)
//            var i = 0
//            cycle.init()
//            while (i < 5) {
//                cycle.scoreCones()
//                cycle.done()
//                i++
//            }
            //End the Cycle


            when (target) {
                1 -> {
                    bot.pivot(50, -pow)
                    sleep(100)
                    bot.move(5.0, pow, true)
                    sleep(100)
                    bot.pivot(60, -pow)
                    sleep(100)
                    bot.move(22.0, pow, true)
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
                    bot.move(27.0, pow, true)
                    sleep(100)
                    bot.pivot(45, -pow)
                    sleep(100)
                    bot.move(10.0, pow, true)
                }
            }
            sleep(100)
            bot.liftHand(AutoInstance.Direction.CLOSE)
        }
    }
}