package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.src.models.abot.ScoreCycle
import org.firstinspires.ftc.teamcode.src.testcodes.ScoreCycleTestClass
import org.firstinspires.ftc.teamcode.src.models.abot.TeleInstance

@TeleOp(name = "Scoring Test", group = "TeleOp Test")
class ScoringTest : LinearOpMode() {
    override fun runOpMode() {
        val bot = TeleInstance(this, hardwareMap)
        val scoringCycle = ScoreCycle(this, bot)

        waitForStart()

        if(opModeIsActive()) {
            scoringCycle.unit_test()
        }
    }
}