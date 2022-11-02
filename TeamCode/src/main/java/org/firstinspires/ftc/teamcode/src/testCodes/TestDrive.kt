package org.firstinspires.ftc.teamcode.src.testCodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.src.models.ABot.AutoInstance

@Autonomous(name= "Test Autonomous", group="AutoTest")
class TestDrive: LinearOpMode() {
    private var bot: AutoInstance? = null
    override fun runOpMode() {
        bot = AutoInstance(this, hardwareMap, telemetry)
        val runtime: ElapsedTime = ElapsedTime()

        waitForStart()
        while (opModeIsActive()) {
            bot?.move( 10, 1, true)
            bot?.pivot(90, 1)
        }
    }
}