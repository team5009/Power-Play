package org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.abot.utils.Cam
import org.firstinspires.ftc.teamcode.src.models.abot.utils.TouchSensor
import java.lang.Thread.sleep
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class TestAutoInstance(Instance: LinearOpMode, hardware: HardwareMap, t: Telemetry) {
    private val touchSensor =
        TouchSensor()
    val vuforiaKey: String =
        hardware.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }
    private var canvas: Canvas? = null
    private var red = 0
    private var green = 0
    private var blue = 0
    private var tot = 0

    private val fl: DcMotor = hardware.get("FL") as DcMotor
    private val fr: DcMotor = hardware.get("FR") as DcMotor
    private val br: DcMotor = hardware.get("BR") as DcMotor
    private val bl: DcMotor = hardware.get("BL") as DcMotor
    val extArm: DcMotor = hardware.get("Extendo") as DcMotor
    val extLift: DcMotor = hardware.get("Elevato") as DcMotor
    val cupArm: DcMotor = hardware.get("cupArm") as DcMotor
    val gripX: Servo = hardware.get("grip") as Servo
    val gripY: Servo = hardware.get("dropper") as Servo
    val gripZ: Servo = hardware.get("antler") as Servo

    var imu: BNO055IMU = hardware.get(BNO055IMU::class.java, "imu")

    val xAxis: DigitalChannel = touchSensor.get("xAxis", hardware)
    val yAxis: DigitalChannel = touchSensor.get("yAxis", hardware)
    var frontCam: CameraName = hardware.get("FrontCam") as WebcamName

//    var backCam:          CameraName     =    hardware.get("BackCam")  as WebcamName

    private val cameraMonitorViewId = hardware.appContext.resources.getIdentifier(
        "cameraMonitorViewId",
        "id",
        hardware.appContext.packageName
    )
    private val params: VuforiaLocalizer.Parameters =
        VuforiaLocalizer.Parameters(cameraMonitorViewId)

    private val telemetry: Telemetry = t
    private val pi: Double = Math.PI
    private val radius: Double = 8.5 //9.097358
    private val instance = Instance

    var parkingZone: Int = 2
    var target: Int = -1
    lateinit var job: CompletableJob
    lateinit var bitSave: Bitmap

    var bestStart = 0
    var bestEnd = 0
    var diffStartEnd = 0
    var botPos: Position = Position.CENTER

    enum class Direction {
        FORWARD, BACKWARD, OPEN, CLOSE, UP, DOWN, MIDDLE, LEFT, RIGHT
    }

    enum class Position {
        LEFT, CENTER, RIGHT
    }

    init {
        // Set Each Wheel Direction
        fl.direction = DcMotorSimple.Direction.FORWARD
        fr.direction = DcMotorSimple.Direction.REVERSE
        bl.direction = DcMotorSimple.Direction.FORWARD
        br.direction = DcMotorSimple.Direction.REVERSE
        extLift.direction = DcMotorSimple.Direction.REVERSE
        extLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extLift.mode = DcMotor.RunMode.RUN_USING_ENCODER
        extArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extArm.mode = DcMotor.RunMode.RUN_USING_ENCODER

        gripZ.position = 0.375
        gripY.position = 0.45
        gripX.position = 0.0

        // Behaviour when Motor Power = 0
        fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        extLift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE


        // Reset Encoders
        resetDriveEncoders()

        params.vuforiaLicenseKey = vuforiaKey
        params.cameraName = frontCam

        imu.parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES

    }

    fun init() {
        GlobalScope.launch {
            cupArmInit()
//            liftInit()
            liftArmInit()
        }
    }

    fun initJob() {
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank()) {
                    msg = "Unknown Error"
                }
            }
        }
    }

    fun startJob(vuforia: Cam) {
        CoroutineScope(Main + job).launch {
            println("Coroutine $this is started with job $job")
            checkTarget(vuforia)
        }
    }

    fun resetJob(vuforia: Cam) {
        vuforia.saveBitmap(instance, bitSave)
        vuforia.close()
        if (job.isActive || job.isCompleted) {
            job.cancel()
        }
    }

    fun resetDriveEncoders() {
        fl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        fr.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        br.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        br.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    private fun stop(bool: Boolean = true) {
        if (bool) {
            fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            extArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        } else {
            fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            extArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        }
    }

    fun move(Inches: Double, Power: Double, brake: Boolean, acceleration: Boolean = false) {
        val distance = inchToTick(Inches)

        if (acceleration) {
            var i = 0.5
            while (instance.opModeIsActive() && i <= Power && abs(fr.currentPosition) < distance) {
                fl.power = i
                fr.power = i
                bl.power = i
                br.power = i
                i += 0.05
                sleep(250)
            }
        }
        fl.power = Power
        fr.power = Power
        bl.power = Power
        br.power = Power
        while (instance.opModeIsActive() && abs(fr.currentPosition) < distance) {
            telemetry.addData("Target Tics", distance)
            telemetry.addData("FR", fr.currentPosition)
            telemetry.addData(
                "Loop Info",
                (instance.opModeIsActive() && abs(fr.currentPosition) < distance)
            )
            telemetry.update()
        }
        fl.power = 0.0
        fr.power = 0.0
        bl.power = 0.0
        br.power = 0.0
        stop(brake)
        resetDriveEncoders()
    }

    fun pivot(degrees: Int, power: Double) {
        val degree: Double = targetDegrees(degrees.toDouble())
        fl.power = power
        fr.power = -power
        bl.power = power
        br.power = -power
        while (instance.opModeIsActive() && abs(fr.currentPosition) < degree) {
        }
        fl.power = 0.0
        fr.power = 0.0
        bl.power = 0.0
        br.power = 0.0
        stop()
        resetDriveEncoders()
    }

    fun strafe(Inches: Double, Power: Double) {
        val distance = inchToTick(Inches)

        fl.power = Power
        fr.power = -Power
        br.power = Power
        bl.power = -Power

        while (instance.opModeIsActive() && abs(fr.currentPosition) < distance) {
            telemetry.addData("Target Tics", distance)
            telemetry.addData("FR", fr.currentPosition)
            telemetry.addData(
                "Loop Info",
                (instance.opModeIsActive() && abs(fr.currentPosition) < distance)
            )
            telemetry.update()
        }
        fl.power = 0.0
        fr.power = 0.0
        bl.power = 0.0
        br.power = 0.0
        stop()
        resetDriveEncoders()
    }

    private suspend fun cupArmInit() {
        delay(500)
        cupArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        cupArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        var prevValue = cupArm.currentPosition
        cupArm.power = -0.5
        val cupDegree = armDegrees(40.0)
        while (instance.opModeIsActive() && abs(cupArm.currentPosition) < cupDegree) {

        }
        cupArm.power = 0.0
        cupArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        while (instance.opModeIsActive() && abs(cupArm.currentPosition - prevValue) > 3.0) {
            prevValue = cupArm.currentPosition
            telemetry.addData("CupArm Position", cupArm.currentPosition)
            telemetry.update()
            sleep(1)
        }

        telemetry.addData("CupArm Position", cupArm.currentPosition)
        telemetry.addData("CupArm movement", cupDegree)
        telemetry.update()
        cupArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        sleep(50)
        cupArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        cupArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    }

    private fun liftInit() {
        extLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extLift.mode = DcMotor.RunMode.RUN_USING_ENCODER
        extLift.power = 0.1
        while (instance.opModeIsActive() && abs(extLift.currentPosition) < liftDistance(1.0)) {
        }
        extLift.power = 0.0
        extLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extLift.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    private suspend fun liftArmInit() {
        delay(50L)
        gripY.position = 0.5
    }

    fun extArmInit() {
        extArm.power = 0.5
        while (instance.opModeIsActive() && xAxis.state) {
        }
        extArm.power = 0.0
        extArm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        extArm.power = 0.3
        while (instance.opModeIsActive() && !xAxis.state) {
        }
        extArm.power = 0.0
    }

    suspend fun checkTarget(vuforia: Cam) {
        while (instance.opModeInInit()) {
            if (vuforia.rgb != null) {
                val bmp: Bitmap = Bitmap.createBitmap(
                    vuforia.rgb.width,
                    vuforia.rgb.height,
                    Bitmap.Config.RGB_565
                )
                bmp.copyPixelsFromBuffer(vuforia.rgb.pixels)
                bitSave = bmp
                seeSignal(bitSave, vuforia)
                checkSignalPosition()
                target = if (parkingZone == 1) {
                    if (diffStartEnd < 40) {
                        3
                    } else {
                        1
                    }
                } else if (parkingZone == 5) {
                    if (diffStartEnd < 40) {
                        3
                    } else {
                        2
                    }
                } else {
                    3
                }
                telemetry.addData("Current Parking Zone:", target)
                telemetry.addData("Best Start", bestStart)
                telemetry.addData("Best End", bestEnd)
                telemetry.addData("Difference", diffStartEnd)
                telemetry.addData("Bot's Position", botPos.name)
                telemetry.update()
            }
            delay(500)
        }
    }

    fun cupArmMove(direction: Direction) {
        cupArm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        cupArm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        when (direction) {
            Direction.UP -> {
                cupArm.power = 0.8
                while (instance.opModeIsActive() && cupArm.currentPosition < armDegrees(110.0)) {
                    telemetry.addData("CupArm Position", cupArm.currentPosition)
                    telemetry.update()
                }
                cupArm.power = 0.0
            }
            Direction.DOWN -> {
                cupArm.power = -0.8
                while (instance.opModeIsActive() && cupArm.currentPosition > armDegrees(70.0)) {
                    telemetry.addData("CupArm Position", cupArm.currentPosition)
                    telemetry.update()
                }
                cupArm.power = 0.0
            }
            else -> {
                return
            }
        }
    }

    fun liftMove(direction: Direction) {
        when (direction) {
            Direction.UP -> {
                extLift.power = 0.9
                while (instance.opModeIsActive() && abs(extLift.currentPosition) < liftDistance(21.5)) {
                    telemetry.addData("Lift Position", extLift.currentPosition)
                    telemetry.addData("Lift Target", liftDistance(15.0))
                    telemetry.update()
                }
                extLift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                extLift.power = 0.0
            }
            Direction.DOWN -> {
                extLift.power = -0.9
                while (instance.opModeIsActive() && abs(extLift.currentPosition) > liftDistance(2.0)) {
                    telemetry.addData("Lift Position", extLift.currentPosition)
                    telemetry.addData("Lift Target", liftDistance(1.0))
                    telemetry.update()
                }
                extLift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                extLift.power = 0.0
            }
            else -> {
                return
            }
        }
    }

    fun extArmMove(direction: Direction) {
        when (direction) {
            Direction.FORWARD -> {
                extArm.power = 0.9
                while (instance.opModeIsActive() && extArm.currentPosition < 810.0) {
                    telemetry.addData("ExtArm Position", extArm.currentPosition)
                    telemetry.update()
                }
                extArm.power = 0.0
            }
            Direction.BACKWARD -> {
                extArm.power = -0.9
                while (instance.opModeIsActive() && extArm.currentPosition > extArm(0.0)) {
                    telemetry.addData("ExtArm Position", extArm.currentPosition)
                    telemetry.update()
                }
                extArm.power = 0.0
            }
            Direction.MIDDLE -> {
                extArm.power = -0.2
                while (instance.opModeIsActive() && xAxis.state) {
                    telemetry.addData("ExtArm Position", extArm.currentPosition)
                    telemetry.update()
                }
                extArm.power = 0.0
            }
            else -> {
                return
            }
        }
    }

    fun cupHand(type: Direction) {
        when (type) {
            Direction.OPEN -> {
                gripX.position = 0.24
            }
            Direction.CLOSE -> {
                gripX.position = 1.0
            }
            else -> {
                return
            }
        }
    }

    fun liftHand(type: Direction) {
        when (type) {
            Direction.OPEN -> {
                gripY.position = 1.0
            }
            Direction.CLOSE -> {
                gripY.position = 0.35
            }
            Direction.MIDDLE -> {
                gripY.position = 0.45
            }
            else -> {
                return
            }
        }
    }


    private fun inchToTick(inches: Double): Double {
        return inches * (280 / pi)
    }

    private fun targetDegrees(degrees: Double): Double {
        return inchToTick((radius * pi * degrees) / 180) * 2
    }

    private fun armDegrees(degree: Double): Double {
        return degree * (288.0 / 360.0)
    }

    private fun liftDistance(inch: Double): Double {
        return inch * (1.5 * 118.0 / 2.4)
    }

    private fun extArm(inch: Double): Double {
        return inch * (118.0 / 3.0)
    }

    private fun seeSignal(bitSave: Bitmap, vuforia: Cam) {
        val left = 330 // pixels from left of image to left edge of ring stack (max)
        val top = 230 // pixels from top of image to top of a 4 ring stack
        val right = left + 25 // pixels from left of image to right edge of ring stack
        val bottom = top + 150 // pixels from top of image to bottom of ring stackstack

        val paint = Paint()
        parkingZone = signalType2(left, right, top, bottom, bitSave)
        canvas = Canvas(bitSave)
        val s = String.format("Zone: %d", parkingZone)
        canvas!!.drawText(s, left.toFloat(), bottom.toFloat(), paint)

    }

    // checkPixel tests to see if the pixel is orange.
    // If so it adds one the count and then turns the pixel yellow for the saved image.
    private fun checkPixel(x: Int, y: Int, bitSave: Bitmap) {
        val p: Int = bitSave.getPixel(x, y)
        //val a = p shr 24 and 0xFF //alpha value
        val r = p shr 16 and 0xFF //red value
        val g = p shr 8 and 0xFF //green value
        val b = p and 0xFF //blue value

        if (min(min(r, g), b) < 128) {
            tot += 1
            if (r > g && r > b) {
                red += 1
                bitSave.setPixel(x, y, Color.rgb(255, 0, 0)) // set pixel to red
            } else if (g > r && g > b) {
                green += 1
                bitSave.setPixel(x, y, Color.rgb(0, 255, 0)) // set pixel to green
            } else if (b > r && b > g) {
                blue += 1
                bitSave.setPixel(x, y, Color.rgb(0, 0, 255)) // set pixel to blue
            }
        } else {
            bitSave.setPixel(x, y, Color.rgb(255, 255, 255)) // else set pixel to blue
        }
    }

    private fun signalType(l: Int, r: Int, t: Int, b: Int, bitSave: Bitmap): Int {
        for (i in l until r) {
            for (j in t until b) {
                checkPixel(i, j, bitSave)
            }
        }
        return if (red >= green) {
            if (red >= blue) {
                1
            } else {
                2
            }
        } else {
            if (green >= blue) {
                3
            } else {
                2
            }
        }
    }

    private fun checkColumn(y: Int, t: Int, b: Int, bitSave: Bitmap): Int {
        val colors = IntArray(7)
        var hsv = FloatArray(3)
        for (x in t until b) {
            val p: Int = bitSave.getPixel(x, y)

            Color.colorToHSV(p, hsv)

            val hue = ((hsv[0] / 60.0).roundToInt() % 6)
            colors[hue + 1] += 1
        }
        var maxColor = colors.indexOf(colors.maxOrNull() ?: 0)

        var dom = Color.rgb(255, 255, 255)
        when (maxColor) {
            1 -> { // seeing red
                dom = Color.rgb(255, 0, 0)
            }
            2 -> { //seeing yellow
                maxColor = -1
            }
            5 -> { //seeing green
                dom = Color.rgb(0, 0, 255)
            }
            else -> {
                maxColor = -1
            }
        }
        for (x in t until b) {
            bitSave.setPixel(x, y, dom)
        }
        return maxColor
    }

    private fun signalType2(l: Int, r: Int, t: Int, b: Int, bitSave: Bitmap): Int {
        var prev = -1
        var best = 0
        var count = 0
        var maxLen = -1
        var start = 0
        for (i in t until b) {
            val cur = checkColumn(i, l, r, bitSave)
            if (cur != -1 && prev == cur) {
                count += 1
                if (best < count) {
                    best = count
                    maxLen = cur
                    bestStart = start
                    bestEnd = i
                }
            } else {
                count = 1
                start = i
            }
            prev = cur
        }
        println("Best Start $bestStart $bestEnd")
        return maxLen
    }

    private fun checkSignalPosition() {
        val min = 290.0 - 5
        val max = 355.0 + 5

        diffStartEnd = bestEnd - bestStart
        if (bestStart >= min && bestEnd <= max) {
            botPos = Position.CENTER
        } else if (bestStart < min) {
            botPos = Position.RIGHT
        } else if (bestEnd > max) {
            botPos = Position.LEFT
        }
    }
}

