package org.firstinspires.ftc.teamcode.src.models.ABot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName

class TeleInstance (Instance: LinearOpMode, hardware: HardwareMap, t: Telemetry){
    val vuforiaKey: String = hardware.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }
    val fl: DcMotor = hardware.get("FL") as DcMotor
    val fr: DcMotor = hardware.get("FR") as DcMotor
    val br: DcMotor = hardware.get("BR") as DcMotor
    val bl: DcMotor = hardware.get("BL") as DcMotor
    val extArm: DcMotor = hardware.get("Extendo") as DcMotor
    val extLift: DcMotor = hardware.get("Elevato") as DcMotor
    val cupArm: DcMotor = hardware.get("cupArm") as DcMotor

    // val xAxis = hardware.get("xAxis") as DigitalChannel
    // val yAxis = hardware.get("yAxis") as DigitalChannel

    val gripX = hardware.get("grip") as Servo
    val gripY = hardware.get("dropper") as Servo

    var camera: CameraName = hardware.get("Webcam") as CameraName
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

    fun forward(power: Double) {
        fl.power = power
        fr.power = power
        br.power = power
        bl.power = power
    }
    fun turn(power: Double) {
        fl.power = -power
        fr.power = power
        br.power = power
        bl.power = -power
    }
    fun pick(power: Double) {
        extArm.power = power
    }
    fun strafe(power: Double){
        fl.power = power
        fr.power = -power
        br.power = power
        bl.power = -power
    }

}