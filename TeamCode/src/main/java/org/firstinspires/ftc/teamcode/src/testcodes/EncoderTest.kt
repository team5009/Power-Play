package org.firstinspires.ftc.teamcode.src.testcodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import com.qualcomm.robotcore.util.ElapsedTime
import com.qualcomm.robotcore.util.ElapsedTime.Resolution
import org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.arcDistance
import org.firstinspires.ftc.teamcode.src.models.abot.utils.inchToTick
import org.firstinspires.ftc.teamcode.src.models.abot.utils.targetDegrees
import java.util.*
import kotlin.math.abs

@Autonomous(name = "Encoder Test", group = "Dev Autonomous")
class EncoderTest: LinearOpMode() {
    override fun runOpMode() {
        val instance = AutoInstance(this, telemetry)
        val pidfCoefficients = PIDFCoefficients(15.0,0.0,3.0,3.5)

        instance.bot.fl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        instance.bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        instance.bot.fr.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        instance.bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        instance.bot.bl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        instance.bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        instance.bot.br.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        instance.bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER

        instance.bot.fl.setPIDFCoefficients(instance.bot.fl.mode, pidfCoefficients)
        instance.bot.fr.setPIDFCoefficients(instance.bot.fr.mode, pidfCoefficients)
        instance.bot.bl.setPIDFCoefficients(instance.bot.bl.mode, pidfCoefficients)
        instance.bot.br.setPIDFCoefficients(instance.bot.br.mode, pidfCoefficients)
        telemetry.addData("Info", "Ready")
        telemetry.update()

        while (opModeInInit()) {
            telemetry.addData("Current Angle", instance.getAbsoluteHeading());
//            telemetry.addData("Target Angle", angle);
//            telemetry.addData("Difference", abs(angle - getAbsoluteHeading()))
//            telemetry.addData("Slope", instance.pid.lastSlope);
//            telemetry.addData("Power", power);
            telemetry.update()
        }

        waitForStart()
        if (opModeIsActive()) {
            instance.runToPosition(-60.0, 90.0)
            instance.turnToPID(-72.0, 90.0)
        }

    }


//    private fun pivotToPosition(angle: Double, power: Double) {
//        val ticks = targetDegrees(angle)
//        val targetFL = instance.bot.fl.currentPosition + ticks.toInt()
//        val targetFR = instance.bot.fr.currentPosition - ticks.toInt()
//        val targetBL = instance.bot.bl.currentPosition + ticks.toInt()
//        val targetBR = instance.bot.br.currentPosition - ticks.toInt()
//
//        instance.bot.fl.targetPosition = targetFL
//        instance.bot.fr.targetPosition = targetFR
//        instance.bot.bl.targetPosition = targetBL
//        instance.bot.br.targetPosition = targetBR
//
//        instance.bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
//        instance.bot.fr.mode = DcMotor.RunMode.RUN_TO_POSITION
//        instance.bot.bl.mode = DcMotor.RunMode.RUN_TO_POSITION
//        instance.bot.br.mode = DcMotor.RunMode.RUN_TO_POSITION
//
//        instance.setAllMotorPower(abs(power))
//
//        while (opModeIsActive() && (
//                    instance.bot.fl.isBusy &&
//                            instance.bot.fr.isBusy &&
//                            instance.bot.bl.isBusy &&
//                            instance.bot.br.isBusy
//                    )) {
//            telemetry.addData("fl position", instance.bot.fl.currentPosition)
//            telemetry.addData("fr position", instance.bot.fr.currentPosition)
//            telemetry.addData("bl position", instance.bot.bl.currentPosition)
//            telemetry.addData("br position", instance.bot.br.currentPosition)
//            telemetry.update()
//        }
//
//        instance.setAllMotorPower(0.0)
//
//        instance.bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        instance.bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        instance.bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        instance.bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
//
//        sleep(100)
//    }



//    private fun turnToPosition(radius: Double, angle: Double, power: Double) {
//        val insideTicks = arcDistance(abs(angle), radius - 8)
//        val outsideTicks = arcDistance(abs(angle), radius + 8)
//
//        val timer = ElapsedTime(Resolution.MILLISECONDS)
//
//        var targetFL = 0
//        var targetFR = 0
//        var targetBL = 0
//        var targetBR = 0
//
//        if (angle < 0) {
//            targetFL = instance.bot.fl.currentPosition + outsideTicks.toInt()
//            targetFR = instance.bot.fr.currentPosition + insideTicks.toInt()
//            targetBL = instance.bot.bl.currentPosition + outsideTicks.toInt()
//            targetBR = instance.bot.br.currentPosition + insideTicks.toInt()
//        } else {
//            targetFL = instance.bot.fl.currentPosition + insideTicks.toInt()
//            targetFR = instance.bot.fr.currentPosition + outsideTicks.toInt()
//            targetBL = instance.bot.bl.currentPosition + insideTicks.toInt()
//            targetBR = instance.bot.br.currentPosition + outsideTicks.toInt()
//        }
//
//        instance.bot.fl.targetPosition = targetFL
//        instance.bot.fr.targetPosition = targetFR
//        instance.bot.bl.targetPosition = targetBL
//        instance.bot.br.targetPosition = targetBR
//
//        instance.bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
//        instance.bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
//        instance.bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
//        instance.bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
//
//        instance.bot.fl.power = abs(power/100.0)
//        instance.bot.fr.power = abs(power/100.0)
//        instance.bot.bl.power = abs(power/100.0)
//        instance.bot.br.power = abs(power/100.0)
//        timer.reset()
//        var pastLeft = 0
//        var pastRight = 0
//        var chosen = false
//        var timeInc: Double = 0.0
//
//        while (opModeIsActive() && (
//            instance.bot.fl.isBusy && instance.bot.fr.isBusy && instance.bot.bl.isBusy && instance.bot.br.isBusy
//        )) {
//            if (!chosen) {
//                pastLeft = instance.bot.fl.currentPosition
//                pastRight = instance.bot.fr.currentPosition
//                timeInc = timer.milliseconds() + 1
//                chosen = true
//            }
//            if (chosen && timer.milliseconds() == timeInc) {
//                val diffLeft = instance.bot.fl.currentPosition - pastLeft
//                val diffRight = instance.bot.fl.currentPosition - pastRight
//
//                val speedLeft = targetFL * timer.seconds()
//                val speedRight = targetFR * timer.seconds()
//
//                chosen = false
//            }
//        }
//
//        instance.bot.fl.power = 0.0
//        instance.bot.fr.power = 0.0
//        instance.bot.bl.power = 0.0
//        instance.bot.br.power = 0.0
//
//        instance.bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        instance.bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        instance.bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        instance.bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
//
//        sleep(100)
//    }

}
