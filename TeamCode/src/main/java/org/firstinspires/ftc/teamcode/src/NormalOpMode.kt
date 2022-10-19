package org.firstinspires.ftc.teamcode.src

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.teamcode.src.models.ABot.TeleInstance


@TeleOp(name = "NormalTeleOp", group = "TeleOp Mode")
class NormalOpMode : LinearOpMode() {
<<<<<<< Updated upstream
//    private var bot: AutoInstance? = null


    override fun runOpMode() {
//        bot = AutoInstance(this, hardwareMap, telemetry)
//        right?.setPower(gamepad1.right_stick_y.toDouble());
//        left?.setPower((-gamepad1.left_stick_y).toDouble());
        waitForStart()
        while(opModeIsActive()){
        telemetry.addData("GamePad 1X %f", gamepad1.left_stick_x)
        telemetry.addData("GamePad 1Y %f", gamepad1.left_stick_y)
        telemetry.update()
        }
=======
//    private var bot: TeleInstance? = null

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

>>>>>>> Stashed changes
    }
}