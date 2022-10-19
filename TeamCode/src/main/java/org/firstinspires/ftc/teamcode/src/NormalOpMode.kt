package org.firstinspires.ftc.teamcode.src

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.teamcode.src.models.ABot.TeleInstance


@TeleOp(name = "NormalTeleOp", group = "TeleOp Mode")
class NormalOpMode : LinearOpMode() {
//    private var bot: AutoInstance? = null
    override fun runOpMode() {
//        bot = TeleInstance(this, hardwareMap, telemetry)
        telemetry.addData(">", "Ready")
        print("Started ----------------------------")
        telemetry.update()
        waitForStart()
        while(opModeIsActive()){
            telemetry.addData("GamePad 1X", gamepad1.left_stick_x)
            telemetry.addData("GamePad 1Y", gamepad1.left_stick_y)
            telemetry.update()
        }
    }
}
