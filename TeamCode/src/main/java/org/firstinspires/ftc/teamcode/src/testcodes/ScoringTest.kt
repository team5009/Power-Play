package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.src.models.abot.cycles.autonomous.AutoCycle
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance

@Autonomous(name = "Scoring Test", group = "Auto Test")
class ScoringTest : LinearOpMode() {
    override fun runOpMode() {
        val bot = AutoInstance(this, hardwareMap, telemetry)
        val scoringCycle = AutoCycle(this, bot)
        val target = 2

        telemetry.addData("Lift pos", bot.bot.ySlider.currentPosition)
        telemetry.addData("Extendo pos", bot.bot.xSlider.currentPosition)
        telemetry.addData("CupArm pos", bot.bot.arm.currentPosition)
        telemetry.update()

        waitForStart()

        if (opModeIsActive()) {
            scoringCycle.extArm(AutoCycle.Directions.READY)
            sleep(1000)
            scoringCycle.runApp(target)
        }
    }
}