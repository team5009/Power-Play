package org.firstinspires.ftc.teamcode.src

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.src.models.ABot.TeleInstance
import kotlin.math.abs


@TeleOp(name = "NormalTeleOp", group = "TeleOp Mode")
class NormalOpMode : LinearOpMode() {
    val deadZone = 0.2


    var extState = false

    var pow: Double = 0.9
//    private var bot: AutoInstance? = null



    override fun runOpMode() {
        val bot = TeleInstance(this, hardwareMap, telemetry)
        var cupAngle = (bot.cupTicks/360)
        telemetry.addData(">", "Ready")
        print("Started ----------------------------")
        telemetry.update()
        waitForStart()
        while(opModeIsActive()){
            telemetry.addData("GamePad 1X", gamepad1.left_stick_x)
            telemetry.addData("GamePad 1Y", gamepad1.left_stick_y)
            telemetry.addData("Grip Servo Pos", bot.gripX.position)
            telemetry.addData("Grip Servo Pos", bot.gripY.position)
            telemetry.update()

            val x = gamepad1.left_stick_x
            val y = gamepad1.left_stick_y

            gamepadOne(bot, x, y, telemetry)
            gamepadTwo(bot, telemetry)
        }
    }

    private fun gamepadOne(bot: TeleInstance, x: Float, y: Float, t: Telemetry) {
        if (abs(y) > abs(x) && y < -deadZone) {
            bot.forward(pow)
        } else if (abs(y) > abs(x) && y > deadZone) {
            bot.forward(-pow)
        } else if ((abs(y) < abs(x) && x < -deadZone)) {
            bot.turn(-pow)
        }
        else if (abs(y) < abs(x) && x > deadZone) {
            bot.turn(pow)
        }
        else {
            bot.forward(0.0)
        }

        if (gamepad1.dpad_up) {
            bot.forward(pow/2)
        } else if (gamepad1.dpad_down) {
            bot.forward(-pow/2)
        } else if (gamepad1.dpad_left) {
            bot.turn(-pow/2)
        } else if (gamepad1.dpad_right) {
            bot.turn(pow/2)
        } else {
            bot.forward(0.0)
        }

        if (gamepad1.left_bumper){
            bot.strafe(pow)
        } else if (gamepad1.right_bumper){
            bot.strafe(-pow)
        } else {
            bot.strafe(0.0)
        }
    }
    private fun gamepadTwo(bot: TeleInstance, t: Telemetry) {
        // Controls to the Extender (Horizontal Arm)
        if (gamepad2.dpad_left){
            //Backward Movement
            //bot.gripX.position = 1.0
            bot.pick(-pow)
        } else if (gamepad2.dpad_right){
            //Forward Movement
            bot.pick(pow)
        } else {
            bot.pick(0.0)
        }
        // Controls to the Elevator (Vertical Arm)
        if (gamepad2.dpad_up) {
            //Elevator Up
            bot.extLift.power = -0.8
        } else if (gamepad2.dpad_down){
            //Elevator Down
            bot.extLift.power = 0.8
        } else {
            bot.extLift.power = 0.0
        }

        // Controls to the Grabber(X Axis)
        if(gamepad2.x ) {
            bot.cupArmyUp()
        }else if(gamepad2.b) {
            bot.cupArmyDown()
        }else{
            bot.cupArm.power = 0.0
        }


        if (gamepad2.y) {
            bot.gripX.position = 1.0
        } else if (gamepad2.a) {
            bot.gripX.position = 0.0
        }

        if (gamepad2.left_bumper){
            bot.gripY.position = 1.0
        } else if (gamepad2.right_bumper) {
            bot.gripY.position = 0.0
        }
    }
}
