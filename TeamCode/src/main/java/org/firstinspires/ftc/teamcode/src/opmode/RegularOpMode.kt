package org.firstinspires.ftc.teamcode.src.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop.TeleInstance

@TeleOp(name = "Regular TeleOp", group = "TeleOp Production")
class RegularOpMode : LinearOpMode() {
    override fun runOpMode() {
        val bot = TeleInstance(this, hardwareMap)
        telemetry.addData(">", "Ready")
        telemetry.update()
        waitForStart()
        GlobalScope.launch {
            while (opModeIsActive()) {
                bot.gamePadOne()
            }
        }
        GlobalScope.launch {
            while (opModeIsActive()) {
                bot.gamePadTwo()
            }
        }
        while (opModeIsActive()) {
        }

    }
}