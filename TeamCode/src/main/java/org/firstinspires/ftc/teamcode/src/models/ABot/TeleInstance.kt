package org.firstinspires.ftc.teamcode.src.models.ABot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName

class TeleInstance (Instance: LinearOpMode, hardware: HardwareMap, t: Telemetry){
    val vuforiaKey: String = hardware.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }
    private val fl: DcMotor = hardware.get("FL") as DcMotor
    private val fr: DcMotor = hardware.get("FR") as DcMotor
    private val br: DcMotor = hardware.get("BR") as DcMotor
    private val bl: DcMotor = hardware.get("BL") as DcMotor
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
}