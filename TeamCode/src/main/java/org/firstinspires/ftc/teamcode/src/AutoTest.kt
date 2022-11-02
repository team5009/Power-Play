package org.firstinspires.ftc.teamcode.src

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.ABot.AutoInstance

@Autonomous(name = "Auto Test", group = "Autonomous Mode")
class AutoTest: LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        bot.resetDriveEncoders()
        telemetry.addData("Status", "Ready to Start")
        telemetry.update()

        waitForStart()
        while(opModeIsActive()) {
            bot.move(10, 1, true)
            sleep(500)

        }

    }
}