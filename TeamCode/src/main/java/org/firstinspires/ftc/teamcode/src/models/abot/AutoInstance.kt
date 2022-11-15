package org.firstinspires.ftc.teamcode.src.models.abot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import kotlinx.coroutines.*
import  org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import java.lang.Thread.sleep
import kotlin.math.PI
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import kotlin.math.abs
import kotlin.math.min

class AutoInstance(Instance:LinearOpMode, hardware: HardwareMap, t: Telemetry) {
    val vuforiaKey: String = hardware.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }


    private var canvas: Canvas? = null
    private var red = 0
    private var green = 0
    private var blue = 0
    private var tot = 0

    private val fl:      DcMotor        =    hardware.get("FL")      as DcMotor
    private val fr:      DcMotor        =    hardware.get("FR")      as DcMotor
    private val br:      DcMotor        =    hardware.get("BR")      as DcMotor
    private val bl:      DcMotor        =    hardware.get("BL")      as DcMotor
    private val extArm:  DcMotor        =    hardware.get("Extendo") as DcMotor
    private val extLift: DcMotor        =    hardware.get("Elevato") as DcMotor
    private val cupArm:  DcMotor        =    hardware.get("cupArm")  as DcMotor
    private val gripX:   Servo          =    hardware.get("grip")    as Servo
    private val gripY:   Servo          =    hardware.get("dropper") as Servo
    private val xAxis:   DigitalChannel =    hardware.get("xAxis")   as DigitalChannel
    private val yAxis:   DigitalChannel =    hardware.get("yAxis")   as DigitalChannel
    var camera:          CameraName     =    hardware.get("Webcam")  as WebcamName

    private val cameraMonitorViewId = hardware.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardware.appContext.packageName)
    private val params: VuforiaLocalizer.Parameters = VuforiaLocalizer.Parameters(cameraMonitorViewId)

    private val telemetry: Telemetry = t
    private val pi: Double = Math.PI
    private val radius: Double = 8.5 //9.097358
    private val instance = Instance

    enum class Direction {
        FORWARD, BACKWARD, OPEN, CLOSE, UP, DOWN, MIDDLE
    }


    init {
        // Set Each Wheel Direction
        fl.direction = DcMotorSimple.Direction.FORWARD
        fr.direction = DcMotorSimple.Direction.REVERSE
        bl.direction = DcMotorSimple.Direction.FORWARD
        br.direction = DcMotorSimple.Direction.REVERSE
        extLift.direction = DcMotorSimple.Direction.REVERSE

        // Behaviour when Motor Power = 0
        fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        extLift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        params.vuforiaLicenseKey = vuforiaKey
        params.cameraName = camera
    }

    fun init() {
        GlobalScope.launch {
            cupArmInit()
            liftInit()
            liftArmInit()
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

    fun move(Inches: Double, Power: Double, brake: Boolean) {
        try {
            val distance = inchToTick(Inches)

            fl.power = Power
            fr.power = Power
            bl.power = Power
            br.power = Power

            while (instance.opModeIsActive() && abs(fr.currentPosition) < distance){
                telemetry.addData("Target Tics", distance)
                telemetry.addData("FR", fr.currentPosition)
                telemetry.addData("Loop Info", (instance.opModeIsActive() && abs(fr.currentPosition) < distance))
                telemetry.update()
            }
            fl.power = 0.0
            fr.power = 0.0
            bl.power = 0.0
            br.power = 0.0
            stop(brake)
            resetDriveEncoders()
        } catch (e: Exception) {
            telemetry.addData("Error: ", e.message)
            print(e.message)
            telemetry.update()
        }
    }
    fun pivot(degrees: Int, power: Double){
        try{
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
        }catch(e: Exception) {
            telemetry.addData("Error: ", e.message)
            print(e.message)
            telemetry.update()
        }
    }
    fun strafe(Inches: Double, Power: Double){
        try {
            val distance = inchToTick(Inches)

            fl.power = Power
            fr.power = -Power
            br.power = Power
            bl.power = -Power

            while (instance.opModeIsActive() && abs(fr.currentPosition) < distance){
                telemetry.addData("Target Tics", distance)
                telemetry.addData("FR", fr.currentPosition)
                telemetry.addData("Loop Info", (instance.opModeIsActive() && abs(fr.currentPosition) < distance))
                telemetry.update()
            }
            fl.power = 0.0
            fr.power = 0.0
            bl.power = 0.0
            br.power = 0.0
            stop()
            resetDriveEncoders()
        } catch (e: Exception) {
            telemetry.addData("Error: ", e.message)
            print(e.message)
            telemetry.update()
        }
    }

    private fun cupArmInit() {
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
        extLift.power = 1.0
        while (instance.opModeIsActive() && extLift.currentPosition < liftDistance(1.0)){}
        extLift.power = 0.0
        extLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extLift.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }
    private suspend fun liftArmInit() {
        delay(50L)
        gripY.position = 0.3
    }
    fun extArmInit() {
        extArm.power = 0.9
        while (instance.opModeIsActive() && extArm.currentPosition < extArm(10.0)) {}
        extArm.power = 0.0
    }

    fun checkTarget(vuforia: Cam): Bitmap?{
        val bitSave : Bitmap
        if(vuforia.rgb != null) {
            val bmp: Bitmap = Bitmap.createBitmap(vuforia.rgb.width,vuforia.rgb.height, Bitmap.Config.RGB_565)
            bmp.copyPixelsFromBuffer(vuforia.rgb.pixels)
            bitSave = bmp
            telemetry.addData("Hello World", bmp.getPixel(30,30))
            telemetry.update()
            return bitSave
        }
        return null
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
                while (instance.opModeIsActive() && extLift.currentPosition < liftDistance(20.0)) {
                    telemetry.addData("Lift Position", extLift.currentPosition)
                    telemetry.update()
                }
                extLift.power = 0.0
            }
            Direction.DOWN -> {
                extLift.power = -0.9
                while (instance.opModeIsActive() && extLift.currentPosition > liftDistance(1.0)) {
                    telemetry.addData("Lift Position", extLift.currentPosition)
                    telemetry.update()
                }
                extLift.power = 0.0
            }
            else -> {
                return
            }
        }
    }
    fun extArmMove(direction: Direction) {
        when (direction) {
            Direction.UP -> {
                extArm.power = 0.9
                while (instance.opModeIsActive() && extArm.currentPosition < extArm(10.0)) {
                    telemetry.addData("ExtArm Position", extArm.currentPosition)
                    telemetry.update()
                }
                extArm.power = 0.0
            }
            Direction.DOWN -> {
                extArm.power = -0.9
                while (instance.opModeIsActive() && extArm.currentPosition > extArm(0.0)) {
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
                gripY.position = 0.0
            }
            Direction.MIDDLE -> {
                gripY.position = 0.7
            }
            else -> {
                return
            }
        }
    }

    private fun inchToTick(inches: Double): Double{
        return inches * (280 / pi)
    }
    private fun targetDegrees(degrees: Double) : Double {
        return inchToTick((radius * pi * degrees)/180) * 2
    }
    fun liftMax(brake: Boolean) {
         val distance = ((1.5 * PI)*2)

         extLift.power = -0.6
         while (instance.opModeIsActive() && abs(extLift.currentPosition) < distance && abs(extLift.currentPosition) < distance){
             telemetry.addData("Target Tics", distance)
             telemetry.addData("ExtArm", fr.currentPosition)
             telemetry.update()
         }
         stop(brake)

         extLift.power = 0.6
         while (instance.opModeIsActive() && abs(extLift.currentPosition) < distance && abs(extLift.currentPosition) < distance){
             telemetry.addData("Target Tics", distance)
             telemetry.addData("extArm", fr.currentPosition)
             telemetry.update()}
         stop(brake)


     }

    fun handY(){
        gripY.position = 1.0
        sleep(500)
        gripY.position = 0.0
    }

    private fun armDegrees(degree: Double) : Double {
        return degree * (288.0/360.0)
    }
    private fun liftDistance(inch: Double) : Double{
        return inch * 118
    }
    private fun extArm(inch: Double): Double {
        return inch * ((1.5 * pi)*2)
    }
    fun seeSignal(bitSave: Bitmap, vuforia: Cam): Int {
        val left = 361 + 35 - 76 - 160 + 130 // pixels from left of image to left edge of ring stack
        val top = 168 // pixels from top of image to top of a 4 ring stack
        val right = left + 75 // pixels from left of image to right edge of ring stack
        val bottom = top + 150 // pixels from top of image to bottom of ring stack

        val paint = Paint()
        val parkingZone: Int = signalType(left, right, top, bottom, bitSave)

        telemetry.addData("Zone", parkingZone)
        canvas = Canvas(bitSave)
        val s = String.format("Zone: %d", parkingZone)
        canvas!!.drawText(s, left.toFloat(),  bottom.toFloat(), paint)
        vuforia.saveBitmap(instance, bitSave)
        return parkingZone
    }

    // checkPixel tests to see if the pixel is orange.
    // If so it adds one the count and then turns the pixel yellow for the saved image.
    private fun checkPixel(x: Int, y: Int, bitSave: Bitmap) {
        val p: Int = bitSave.getPixel(x, y)
        //val a = p shr 24 and 0xFF //alpha value
        val r = p shr 16 and 0xFF //red value
        val g = p shr 8 and 0xFF //green value
        val b = p and 0xFF //blue value

        if(min(min(r,g),b) < 128) {
            tot += 1
            if (r > g && r > b ) {
                red += 1
                bitSave.setPixel(x, y, Color.rgb(255, 0, 0)) // set pixel to red
            } else if (g > r && g > b ) {
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
    private fun signalType(l: Int, r: Int, t: Int, b: Int, bitSave: Bitmap): Int{
        for (i in l until r) {
            for (j in t until b) {
                checkPixel(i, j, bitSave)
            }
        }
        return if(red >= green) {
            if (red >= blue) { 1 } else { 2 }
        } else {
            if(green >= blue) { 3 } else { 2 }
        }
    }
}

