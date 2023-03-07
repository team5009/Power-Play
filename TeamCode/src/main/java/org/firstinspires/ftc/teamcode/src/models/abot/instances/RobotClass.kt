package org.firstinspires.ftc.teamcode.src.models.abot.instances

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.teamcode.src.models.abot.utils.TouchSensor

val touchSensor = TouchSensor()
class RobotClass(Instance: LinearOpMode) {
    val fl: DcMotorEx = Instance.hardwareMap.get(DcMotorEx::class.java, "FL")
    val fr: DcMotorEx = Instance.hardwareMap.get(DcMotorEx::class.java, "FR")
    val br: DcMotorEx = Instance.hardwareMap.get(DcMotorEx::class.java, "BR")
    val bl: DcMotorEx = Instance.hardwareMap.get(DcMotorEx::class.java, "BL")
    val xSlider: DcMotorEx = Instance.hardwareMap.get(DcMotorEx::class.java, "Extendo")
    val ySlider: DcMotorEx = Instance.hardwareMap.get(DcMotorEx::class.java, "Elevato")
    val arm: DcMotorEx = Instance.hardwareMap.get(DcMotorEx::class.java, "cupArm")
    val xGrip: Servo = Instance.hardwareMap.get(Servo::class.java, "grip")
    val yGrip: Servo = Instance.hardwareMap.get(Servo::class.java, "dropper")
    val zGrip: Servo = Instance.hardwareMap.get(Servo::class.java, "antler")
    val xSensor: DigitalChannel = touchSensor.get("xAxis", Instance.hardwareMap)
    val ySensor: DigitalChannel = touchSensor.get("yAxis", Instance.hardwareMap)
    val frontCam: CameraName = Instance.hardwareMap.get("FrontCam") as WebcamName
    val imu: BNO055IMU = Instance.hardwareMap.get(BNO055IMU::class.java, "imu")

    init {
        // Set Each Wheel Direction
        fl.direction = DcMotorSimple.Direction.FORWARD
        fr.direction = DcMotorSimple.Direction.REVERSE
        bl.direction = DcMotorSimple.Direction.FORWARD
        br.direction = DcMotorSimple.Direction.REVERSE

        fl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        fr.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        br.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        br.mode = DcMotor.RunMode.RUN_USING_ENCODER

        ySlider.direction = DcMotorSimple.Direction.REVERSE
        arm.direction = DcMotorSimple.Direction.REVERSE

        ySlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        ySlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER

        // Behaviour when Motor Power = 0
        fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        ySlider.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        xSlider.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        // define initialization values for IMU, and then initialize it.
        val params = BNO055IMU.Parameters()
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES
        params.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC
        params.loggingEnabled = true
        params.loggingTag = "IMU"
        params.accelerationIntegrationAlgorithm = JustLoggingAccelerationIntegrator()
        imu.initialize(params)
    }

    fun move(flPower: Double, frPower: Double, blPower: Double, brPower: Double) {
        fl.power = flPower
        fr.power = frPower
        bl.power = blPower
        br.power = brPower
    }
}