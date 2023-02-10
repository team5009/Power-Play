package org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime
import com.qualcomm.robotcore.util.Range
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.*
import org.firstinspires.ftc.teamcode.src.models.abot.instances.RobotClass
import org.firstinspires.ftc.teamcode.src.models.abot.utils.*
import java.lang.Thread.sleep
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class AutoInstance(Instance: LinearOpMode, hardware: HardwareMap, t: Telemetry) {
    val vuforiaKey: String = hardware.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }
    private var canvas: Canvas? = null
    val bot = RobotClass(Instance, hardware)
    var frontCam: CameraName = hardware.get("FrontCam") as WebcamName

    private val cameraMonitorViewId = hardware.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardware.appContext.packageName)
    private val params: VuforiaLocalizer.Parameters = VuforiaLocalizer.Parameters(cameraMonitorViewId)

    private val telemetry: Telemetry = t
    private val instance = Instance


    private var targetHeading = 0.0
    private var driveSpeed = 0.0
    private var turnSpeed = 0.0
    private var leftSpeed = 0.0
    private var rightSpeed = 0.0

    private var targetRight = 0
    private var targetLeft = 0

    private var headingOffset = 0.0
    private var robotHeading = 0.0
    private var headingError = 0.0

    /*
    Gain is used to control the amount of adjustment to your "heading control"
    We define one value when Turning (larger errors), and the other is used when Driving straight (smaller errors).
    Increase these numbers if the heading does not corrects strongly enough (eg: a heavy robot or using tracks)
    Decrease these numbers if the heading does not settle on the correct value (eg: very agile robot with omni wheels)
    */
    private val P_TURN_GAIN = 0.02
    private val P_DRIVE_GAIN = 0.03

    private val HEADING_THRESHOLD = 1.0


    var parkingZone: Int = 2
    var target: Int = -1
    lateinit var job: CompletableJob
    lateinit var bitSave: Bitmap

    private var bestStart = 0
    private var bestEnd = 0
    var diffStartEnd = 0
    var botPos: Position = Position.CENTER

    enum class Direction {
        FORWARD, BACKWARD, OPEN, CLOSE, UP, DOWN, MIDDLE, LEFT, RIGHT
    }

    enum class Position {
        LEFT, CENTER, RIGHT
    }

    init {
        params.vuforiaLicenseKey = vuforiaKey
        params.cameraName = frontCam

        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER

        bot.zGrip.position = 0.475
        bot.yGrip.position = 0.45
        bot.xGrip.position = 0.1

        resetHeading()
    }

    fun resetEncoders() {
        bot.fl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.fr.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.bl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.br.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.ySlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.ySlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }
    fun resetSliderEncoders() {
        bot.ySlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.ySlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    fun init() {
        GlobalScope.launch {
            cupArmInit()
//            liftInit()
            liftArmInit()
            bot.xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            bot.xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }
    fun readyLift() {
        GlobalScope.launch {
            liftInit()
        }
    }
    // Create and Stop a thread process
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
        bot.fl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.fr.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.bl.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.br.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }
    fun setToRunToPosition() {
        bot.fr.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.br.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.bl.mode = DcMotor.RunMode.RUN_TO_POSITION
    }
    fun setToRunUsingEncoders() {
        bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    suspend fun liftInit() {
        delay(250)
        bot.ySlider.power = 0.9
        while (instance.opModeIsActive() &&
            abs(bot.ySlider.currentPosition) < liftDistance(24.0)
        ) {}
        bot.ySlider.power = 0.0
    }

    private fun stop(bool: Boolean = true) {
        if (bool) {
            bot.fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            bot.fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            bot.bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            bot.br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        } else {
            bot.fl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            bot.fr.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            bot.bl.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            bot.br.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        }
    }

    fun move(Inches: Double, Power: Double, brake: Boolean, acceleration: Boolean = false) {
        val distance = inchToTick(Inches)

        if (acceleration) {
            var i = 0.5
            while (instance.opModeIsActive() && i <= Power && abs(bot.fr.currentPosition) < distance) {
                bot.fl.power = i
                bot.fr.power = i
                bot.bl.power = i
                bot.br.power = i
                i += 0.05
                sleep(250)
            }
        }
        bot.fl.power = Power
        bot.fr.power = Power
        bot.bl.power = Power
        bot.br.power = Power
        while (instance.opModeIsActive() && abs(bot.fr.currentPosition) < distance) {
            telemetry.addData("Target Tics", distance)
            telemetry.addData("FR", bot.fr.currentPosition)
            telemetry.addData(
                "Loop Info",
                (instance.opModeIsActive() && abs(bot.fr.currentPosition) < distance)
            )
            telemetry.update()
        }
        bot.fl.power = 0.0
        bot.fr.power = 0.0
        bot.bl.power = 0.0
        bot.br.power = 0.0
        stop(brake)
        resetDriveEncoders()
    }

    fun moveBot(drive: Double, turn: Double) {

        driveSpeed = drive // save this value as a class member so it can be used by telemetry.
        turnSpeed = turn

        leftSpeed = drive - turn
        rightSpeed = drive + turn

        val max: Double = max(abs(leftSpeed), abs(rightSpeed))
        if (max > 1.0) {
            leftSpeed /= max
            rightSpeed /= max
        }

        bot.fl.power = leftSpeed
        bot.bl.power = leftSpeed
        bot.fr.power = rightSpeed
        bot.br.power = rightSpeed

    }

    fun moveStraight(speed: Double, distance: Double, heading: Double) {
        if (instance.opModeIsActive()) {
            val moveCounts = inchToTick(distance).toInt()
            targetRight = bot.fr.currentPosition + moveCounts
            targetLeft = bot.fl.currentPosition + moveCounts

            bot.fr.targetPosition = targetRight
            bot.fl.targetPosition = targetLeft
            bot.br.targetPosition = targetRight
            bot.bl.targetPosition = targetLeft

            bot.fr.mode = DcMotor.RunMode.RUN_TO_POSITION
            bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
            bot.br.mode = DcMotor.RunMode.RUN_TO_POSITION
            bot.bl.mode = DcMotor.RunMode.RUN_TO_POSITION

            moveBot(abs(speed), 0.0)

            while (instance.opModeIsActive() &&
                    bot.fr.isBusy &&
                    bot.fl.isBusy &&
                    bot.br.isBusy &&
                    bot.bl.isBusy
                    ) {

                turnSpeed = getSteeringCorrection(heading, P_DRIVE_GAIN )

                if (distance < 0)
                    turnSpeed *= -1.0

                moveBot(driveSpeed, turnSpeed)

                sendTelemetry(true)
            }
            moveBot(0.0, 0.0)
            bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
            bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
            bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
            bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }

    fun holdHeading(speed: Double, heading: Double, holdTime: Double) {
        val time = System.currentTimeMillis() + holdTime

        while (instance.opModeIsActive() && System.currentTimeMillis() < time) {
            turnSpeed = getSteeringCorrection(heading, P_TURN_GAIN)

            turnSpeed = Range.clip(turnSpeed, -speed, speed)

            moveBot(0.0, turnSpeed)

            sendTelemetry(false)
        }

        moveBot(0.0, 0.0)
    }

    fun turnHeading(speed: Double, heading: Double) {
        getSteeringCorrection(heading, P_DRIVE_GAIN)

        while (instance.opModeIsActive() && abs(headingError) > HEADING_THRESHOLD) {
            turnSpeed = getSteeringCorrection(heading, P_TURN_GAIN)

            turnSpeed = Range.clip(turnSpeed, -speed, speed)

            moveBot(0.0, turnSpeed)

            sendTelemetry(false)
        }
        moveBot(0.0, 0.0)
    }

    private fun getSteeringCorrection(desiredHeading: Double, proportionalGain: Double): Double {
        targetHeading = desiredHeading
        robotHeading = getRawHeading() - 0

        headingError = targetHeading - robotHeading

        while (headingError > 180) headingError -= 360
        while (headingError <= -180) headingError += 360

        return Range.clip(headingError * proportionalGain, -1.0, 1.0)
    }

    fun getRawHeading(): Double {
        val angles: Orientation = bot.imu.getAngularOrientation(
            AxesReference.INTRINSIC,
            AxesOrder.ZYX,
            AngleUnit.DEGREES)

        return angles.firstAngle.toDouble()
    }

    fun resetHeading() {
        headingOffset = getRawHeading()
        robotHeading = 0.0
    }

    fun pivot(degrees: Int, power: Double) {
        val degree: Double = targetDegrees(degrees.toDouble())
        bot.fl.power = power
        bot.fr.power = -power
        bot.bl.power = power
        bot.br.power = -power
        while (instance.opModeIsActive() && abs(bot.fr.currentPosition) < degree) { }
        bot.fl.power = 0.0
        bot.fr.power = 0.0
        bot.bl.power = 0.0
        bot.br.power = 0.0
        stop()
        resetDriveEncoders()
    }

    fun strafe(Inches: Double, Power: Double) {
        val distance = inchToTick(Inches)

        bot.fl.power = Power
        bot.fr.power = -Power
        bot.br.power = Power
        bot.bl.power = -Power

        while (instance.opModeIsActive() && abs(bot.fr.currentPosition) < distance) {
            telemetry.addData("Target Tics", distance)
            telemetry.addData("FR", bot.fr.currentPosition)
            telemetry.addData(
                "Loop Info",
                (instance.opModeIsActive() && abs(bot.fr.currentPosition) < distance)
            )
            telemetry.update()
        }
        bot.fl.power = 0.0
        bot.fr.power = 0.0
        bot.bl.power = 0.0
        bot.br.power = 0.0
        stop()
        resetDriveEncoders()
    }

    private suspend fun cupArmInit() {
        delay(500)
        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        var prevValue = bot.arm.currentPosition
        bot.arm.power = -0.5
        val cupDegree = armDegrees(40.0)
        while (instance.opModeIsActive() && abs(bot.arm.currentPosition) < cupDegree) {

        }
        bot.arm.power = 0.0
        bot.arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        while (instance.opModeIsActive() && abs(bot.arm.currentPosition - prevValue) > 3.0) {
            prevValue = bot.arm.currentPosition
            telemetry.addData("CupArm Position", bot.arm.currentPosition)
            telemetry.update()
            sleep(1)
        }

        telemetry.addData("CupArm Position", bot.arm.currentPosition)
        telemetry.addData("CupArm movement", cupDegree)
        telemetry.update()
        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        sleep(50)
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    }

    private suspend fun liftArmInit() {
        delay(50L)
        bot.yGrip.position = 0.5
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

    fun liftMove(direction: Direction) {
        when (direction) {
            Direction.UP -> {
                bot.ySlider.power = 0.9
                while (instance.opModeIsActive() && abs(bot.ySlider.currentPosition) < liftDistance(21.5)) {
                    telemetry.addData("Lift Position", bot.ySlider.currentPosition)
                    telemetry.addData("Lift Target", liftDistance(15.0))
                    telemetry.update()
                }
                bot.ySlider.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                bot.ySlider.power = 0.0
            }
            Direction.DOWN -> {
                bot.ySlider.power = -0.9
                while (instance.opModeIsActive() && abs(bot.ySlider.currentPosition) > liftDistance(2.0)) {
                    telemetry.addData("Lift Position", bot.ySlider.currentPosition)
                    telemetry.addData("Lift Target", liftDistance(1.0))
                    telemetry.update()
                }
                bot.ySlider.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                bot.ySlider.power = 0.0
            }
            else -> {
                return
            }
        }
    }

    fun liftHand(type: Direction) {
        when (type) {
            Direction.OPEN -> {
                bot.yGrip.position = 1.0
            }
            Direction.CLOSE -> {
                bot.yGrip.position = 0.35
            }
            Direction.MIDDLE -> {
                bot.yGrip.position = 0.45
            }
            else -> {
                return
            }
        }
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
        var prev = -1; var best = 0; var count = 0; var maxLen = -1; var start = 0
        for (i in t until b) {
            val cur = checkColumn(i, l, r, bitSave)
            if (cur != -1 && prev == cur) {
                count += 1
                if (best < count) {
                    best = count;
                    maxLen = cur;
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

    private fun sendTelemetry(straight: Boolean) {
        if (straight) {
            telemetry.addData("Motion", "Drive Straight")
            telemetry.addData("Target Pos Left | Right", "$targetLeft | $targetRight")
            telemetry.addData("Actual Pos FL | FR", "${bot.fl.currentPosition} | ${bot.fr.currentPosition}")
            telemetry.addData("Actual Pos BL | BR", "${bot.bl.currentPosition} | ${bot.br.currentPosition}")
        } else {
            telemetry.addData("Motion", "Turning")
        }
        telemetry.addData("Angle Target:Current", "%5.2f:%5.0f", targetHeading, robotHeading)
        telemetry.addData("Error:Steer", "%5.1f:%5.1f", headingError, turnSpeed)
        telemetry.addData("Wheel Speeds L:R.", "%5.2f : %5.2f", leftSpeed, rightSpeed)
        telemetry.update()
    }
}

