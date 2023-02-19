package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance

@Autonomous(name = "Test Drive", group = "Dev Autonomous")
class TestDrive:LinearOpMode(){
    // Step through each leg of the path,
    // Notes:   Reverse movement is obtained by setting a negative distance (not speed)
    //          holdHeading() is used after turns to let the heading stabilize
    override fun runOpMode() {
        val bot = AutoInstance(this, telemetry)
            val timer = ElapsedTime()
        fun move(motor: DcMotorEx, power: Double, time: Int) {
            timer.reset()
            motor.power = power
            while(opModeIsActive() && timer.milliseconds() < time) {
                telemetry.addData("name", motor.deviceName)
                telemetry.addData("encoder", motor.currentPosition)
                telemetry.addData("power", motor.power)
                telemetry.update()
            }
            motor.power = 0.0
            telemetry.addData("Status", "Done")
            telemetry.update()
        }

        while (opModeInInit()) {
            telemetry.addData("Robot Heading", bot.getRawHeading())
            telemetry.update()
        }
//        waitForStart()
        bot.resetEncoders()
//        bot.resetHeading()

        if (opModeIsActive()) {
            move(bot.bot.fl, 0.15, 5000)
            sleep(1000)

            move(bot.bot.fr, 0.15, 5000)
            sleep(1000)

            move(bot.bot.bl, 0.15, 5000)
            sleep(1000)

            move(bot.bot.br, 0.15, 5000)
            sleep(1000)
        }
    }
}