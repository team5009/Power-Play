package org.firstinspires.ftc.teamcode.src.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.teamcode.src.models.abot.instances.teleop.HalfTeleInstance

@TeleOp(name = "Half TeleOp", group = "TeleOp Production")
class HalfOpMode : LinearOpMode() {
    override fun runOpMode() {
        val bot = HalfTeleInstance(this, hardwareMap)
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
            sleep(1)
        }

    }
}