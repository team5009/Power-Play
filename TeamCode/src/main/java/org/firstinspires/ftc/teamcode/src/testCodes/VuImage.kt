package org.firstinspires.ftc.teamcode.src.testCodes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.src.models.ABot.AutoInstance
import org.firstinspires.ftc.teamcode.src.models.ABot.Cam
import kotlin.math.min

@Autonomous(name = "CameraVuTest", group = "AutoTest")
class VuImage : LinearOpMode() {
    private var bitSave : Bitmap ?= null
    private var canvas: Canvas? = null
    private var red = 0
    private var green = 0
    private var blue = 0
    private var tot = 0

    override fun runOpMode() {
        val bot: AutoInstance = AutoInstance(this, hardwareMap, telemetry)
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         * To get an on-phone camera preview, use the code below.
         * If no camera preview is desired, use the parameter-less constructor instead (commented out below).
         */
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)
        val params = VuforiaLocalizer.Parameters(cameraMonitorViewId)
        params.vuforiaLicenseKey = bot.vuforiaKey
        // Connect to the camera we are to use.  This name must match what is set up in Robot Configuration
        params.cameraName = bot.camera
        val vuforia: Cam = Cam(params)
        var bitSave : Bitmap ?= null
        waitForStart()

        while(opModeIsActive()) {
            if(vuforia.rgb != null) {
                val bmp: Bitmap = Bitmap.createBitmap(vuforia.rgb.width,vuforia.rgb.height, Bitmap.Config.RGB_565)
                bmp.copyPixelsFromBuffer(vuforia.rgb.pixels)
                bitSave = bmp
                telemetry.addData("Hello World", bmp.getPixel(30,30))
                telemetry.update()
            }
        }

        vuforia.saveBitmap(this, bitSave)

    }
    fun seeSignal() {
        val left = 361 // pixels from left of image to left edge of ring stack
        val top = 168 // pixels from top of image to top of a 4 ring stack
        val right = left + 100 // pixels from left of image to right edge of ring stack
        val bottom = top + 150 // pixels from top of image to bottom of ring stack

        val paint = Paint()
        val parkingZone: Int = signalType(left, right, top, bottom)

        telemetry.addData("Zone", parkingZone)
        canvas = Canvas(bitSave!!)
        val s = String.format("Zone: %d", parkingZone)
        canvas!!.drawText(s, left.toFloat(),  bottom.toFloat(), paint)
    }

    // checkPixel tests to see if the pixel is orange.
    // If so it adds one the count and then turns the pixel yellow for the saved image.
    private fun checkPixel(x: Int, y: Int) {
        val p: Int = bitSave!!.getPixel(x, y)
        //val a = p shr 24 and 0xFF //alpha value
        val r = p shr 16 and 0xFF //red value
        val g = p shr 8 and 0xFF //green value
        val b = p and 0xFF //blue value

        if(min(min(r,g),b) < 128) {
            tot += 1
            if (r > g && r > b ) {
                red += 1
                bitSave!!.setPixel(x, y, Color.rgb(255, 0, 0)) // set pixel to red
            } else if (g > r && g > b ) {
                green += 1
                bitSave!!.setPixel(x, y, Color.rgb(0, 255, 0)) // set pixel to green
            } else if (b > r && b > g) {
                blue += 1
                bitSave!!.setPixel(x, y, Color.rgb(0, 0, 255)) // set pixel to blue
            }
        } else {
            bitSave!!.setPixel(x, y,Color.rgb(255, 255, 255)) // else set pixel to blue
        }
    }

    private fun signalType(l: Int, r: Int, t: Int, b: Int): Int{
        for (i in l until r) {
            for (j in t until b) {
                checkPixel(i, j)
            }
        }
        if(red >= green) {
            return if (red >= blue) { 1 } else { 2 }
        } else {
            return if(green >= blue) { 3 } else { 2 }
        }
    }
}