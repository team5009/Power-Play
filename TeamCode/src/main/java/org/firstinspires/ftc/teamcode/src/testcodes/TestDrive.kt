package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance

@Autonomous(name = "Test Drive", group = "Dev Autonomous")
class TestDrive:LinearOpMode(){
    // Step through each leg of the path,
    // Notes:   Reverse movement is obtained by setting a negative distance (not speed)
    //          holdHeading() is used after turns to let the heading stabilize
    override fun runOpMode() {
        val bot = AutoInstance(this,  hardwareMap, telemetry)


        while (opModeInInit()) {
            telemetry.addData("Robot Heading", bot.getRawHeading())
            telemetry.update()
        }
//        waitForStart()
        bot.resetEncoders()
        bot.resetHeading()

        if (opModeIsActive()) {
            bot.move(110.0, 0.9, brake = true)
        }
    }
}