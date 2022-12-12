package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.src.models.abot.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.AutoScoreCycle

@TeleOp(name = "Scoring Test", group = "TeleOp Test")
class ScoringTest : LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        val scoringCycle = AutoScoreCycle(this, bot)

        waitForStart()

        if(opModeIsActive()) {
            bot.init()
            sleep(5000)
            bot.extArmInit()
            bot.cupArmMove(AutoInstance.Direction.UP)
            sleep(100)
            bot.scoreStack()
        }
    }
}