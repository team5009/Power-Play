package org.firstinspires.ftc.teamcode.src

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.ABot.AutoInstance

@Autonomous(name = "Auto Test", group = "Autonomous Mode")
class AutoTest: LinearOpMode() {
    private var bot: AutoInstance? = null
    override fun runOpMode() {
        bot = AutoInstance(this, hardwareMap, telemetry)
        telemetry.addData("Status", "Ready to Start")
        telemetry.update()
        waitForStart()
        while(opModeIsActive()) {
            bot?.move("Forward", 10, 1, true)
            sleep(500)
        }

    }
}