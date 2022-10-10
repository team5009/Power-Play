package org.firstinspires.ftc.teamcode.src

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.BotHardware

@Autonomous(name = "Auto Test", group = "Autonomous Mode")
class AutoTest: LinearOpMode() {
    private var bot: BotHardware? = null
    override fun runOpMode() {
        bot = BotHardware(this, hardwareMap, gamepad1, telemetry)
        telemetry.addData("Status", "Ready to Start")
        telemetry.update()
        waitForStart()
        while(opModeIsActive()) {
            bot?.move("Forward", 10.toDouble(), 1.toDouble())
            sleep(500)
        }

    }
}