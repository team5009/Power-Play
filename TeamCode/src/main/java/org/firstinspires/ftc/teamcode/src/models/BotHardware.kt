package org.firstinspires.ftc.teamcode.src.models

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Gamepad
import  org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

open class BotHardware(Instance:LinearOpMode, hardware: HardwareMap, gamepad: Gamepad? = null, t: Telemetry) {
    protected open val vuforiakey: String = "Aaw91aX/////AAABmdsPMOJ+SEw2hbB0KuA91QNdCicETTnXn5+HBhEteAhuFgn/KjtDhaDByyvmLiJRDJrUO1njQVgKy9wvDfxoXptJURnl+R6mpv17sWJCvMYUeFmlgrk/31pTM4aeIdSNn9z+xupD/tgXRS2cQEHAIu+Hha1/EzIF3lbYiRwNhDbG3XtVzmjuBLus3Gx+I75KgUzkPwn0XIFbQ96BZvIF+6cv3bKEWNyUZ7thLBB3OrPP0Fm02W3fmXhxAB4cpvuqUbVOFkOEkrhAyDxtuP28PKchLXjcJD6JMFXYa8K7pGXcuMiNge5M10knHwEWgvSJLuhcC2onFtgnlGwmDswuqki2ynlQPbLQAGwpKNsPSlET"
    open val fl: DcMotor = hardware.get("FL") as DcMotor
    open val fr: DcMotor = hardware.get("FR") as DcMotor
    open val br: DcMotor = hardware.get("BR") as DcMotor
    open val bl: DcMotor = hardware.get("BL") as DcMotor
    private var camera: WebcamName = hardware.get("Webcam") as WebcamName
    open var forward:Double? = -gamepad?.left_stick_y?.toDouble()!!
    open var pivot: Double? = gamepad?.left_stick_x?.toDouble()!!
    private val telemetry: Telemetry = t
    private val pi: Double = Math.PI
    private val radius: Double = 7.036308765
    private val instance = Instance



    init {
        fl.direction = DcMotorSimple.Direction.FORWARD
        fr.direction = DcMotorSimple.Direction.REVERSE
        bl.direction = DcMotorSimple.Direction.FORWARD
        br.direction = DcMotorSimple.Direction.REVERSE
    }

    fun move(Direction: String = "Forward", Inches: Double, Power: Double) {
        try {
                val dir = Direction
                var power: Double = Power
                val distance = inchToTick(Inches)

                if (Direction == "Backwards"){
                    power = -Power
                }

                fl.power = power
                fr.power = power
                bl.power = power
                br.power = power

//                while (instance.opModeIsActive())
        } catch (e: Exception) {
            telemetry.addData("Error: ", e.message)
            print(e.message)
            telemetry.update()
        }
    }

    private fun inchToTick(inches: Double): Double{
        return inches * 37.9498
    }
    private fun targetDegrees(degrees: Double) : Double {
        return inchToTick((radius * pi * degrees)/180)
    }
}

