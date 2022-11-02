package org.firstinspires.ftc.teamcode.src.models.ABot

import android.content.Context
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import  org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import kotlin.math.abs

class AutoInstance(Instance:LinearOpMode, hardware: HardwareMap, t: Telemetry) {
    val vuforiaKey: String = hardware.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }
    val fl:      DcMotor =    hardware.get("FL")      as DcMotor
    val fr:      DcMotor =    hardware.get("FR")      as DcMotor
    val br:      DcMotor =    hardware.get("BR")      as DcMotor
    val bl:      DcMotor =    hardware.get("BL")      as DcMotor
    var camera:  CameraName = hardware.get(WebcamName::class.java, "Webcam")
    val extArm:  DcMotor =    hardware.get("Extendo") as DcMotor
    val extLift: DcMotor =    hardware.get("Elevato") as DcMotor
    val cupArm:  DcMotor =    hardware.get("cupArm")  as DcMotor
    val gripX: Servo =        hardware.get("grip")    as Servo
    val gripY: Servo =        hardware.get("dropper") as Servo


    private val telemetry: Telemetry = t
    private val pi: Double = Math.PI
    private val radius: Double = 7.036308765
    private val instance = Instance



    init {
        // Set Each Wheel Direction
        fl.direction = DcMotorSimple.Direction.FORWARD
        fr.direction = DcMotorSimple.Direction.REVERSE
        bl.direction = DcMotorSimple.Direction.FORWARD
        br.direction = DcMotorSimple.Direction.REVERSE

        // Behaviour when Motor Power = 0
        fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }

    fun resetDriveEncoders() {
        fl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        fr.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        br.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }
    private fun stop(bool: Boolean = true) {
        if (bool) {
            fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        } else {
            fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        }
    }

    fun move( Inches: Int, Power: Int, brake: Boolean) {
        try {
            val distance = inchToTick(Inches.toDouble())

            fl.power = Power.toDouble()
            fr.power = Power.toDouble()
            bl.power = Power.toDouble()
            br.power = Power.toDouble()

            while (instance.opModeIsActive() && abs(fr.currentPosition) < distance && abs(fl.currentPosition) < distance){
                telemetry.addData("Target Tics", distance);
                telemetry.addData("FR", fr.currentPosition);
                telemetry.update();
            }
            stop(brake)
            resetDriveEncoders()
        } catch (e: Exception) {
            telemetry.addData("Error: ", e.message)
            print(e.message)
            telemetry.update()
        }
    }

    fun pivot(degrees: Int, power: Int){
        try{
            val degree: Double = targetDegrees(degrees.toDouble())
            fl.power = power.toDouble()
            fr.power = -power.toDouble()
            bl.power = power.toDouble()
            br.power = -power.toDouble()
            while (instance.opModeIsActive() && abs(fr.currentPosition) < degree) {
                telemetry.addData("Target Tics", degree);
                telemetry.addData("FR", fr.currentPosition);
                telemetry.update();
            }
            stop()
            resetDriveEncoders()
        }catch(e: Exception) {
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

