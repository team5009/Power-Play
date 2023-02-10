package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance

@Autonomous(name = "Antler Test Autonomous", group = "Test Autonomous")
class AntlerTest : LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)

        waitForStart()
        if (opModeIsActive()) {
            var i = 1.0
            bot.bot.zGrip.position = i
            sleep(1000)

            while (i > 0.0) {
                bot.bot.zGrip.position = i
                sleep(1000)
                telemetry.addData("Antler Pos", bot.bot.zGrip.position)
                telemetry.update()
                i -= 0.05
            }
        }
    }
}