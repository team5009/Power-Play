package org.firstinspires.ftc.teamcode.src

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.teamcode.src.models.ABot.AutoInstance


@TeleOp(name = "NormalTeleOp", group = "TeleOp Mode")
class NormalOpMode : LinearOpMode() {
    private var bot: AutoInstance? = null


    override fun runOpMode() {
        bot = AutoInstance(this, hardwareMap, telemetry)
//        right?.setPower(gamepad1.right_stick_y.toDouble());
//        left?.setPower((-gamepad1.left_stick_y).toDouble());
        waitForStart()
            while(OpModeIsActive())
        telemetry.addData("GamePad 1X %f".format(gamepad1.left_stick_x.toDouble()))
        telemetry.addData("GamePad 1Y %f".format(gamepad1.left_stick_y.toDouble()))
        telemetry.update()
    }

    private fun OpModeIsActive(): Boolean {

    }
}