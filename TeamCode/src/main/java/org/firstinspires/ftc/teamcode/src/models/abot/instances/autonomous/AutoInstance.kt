package org.firstinspires.ftc.teamcode.src.models.abot.instances.autonomous

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import com.qualcomm.robotcore.util.ElapsedTime
import com.qualcomm.robotcore.util.Range
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.*
import org.firstinspires.ftc.teamcode.src.models.abot.instances.RobotClass
import org.firstinspires.ftc.teamcode.src.models.abot.utils.*
import java.lang.Thread.sleep
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class AutoInstance(Instance: LinearOpMode, t: Telemetry) {
    val vuforiaKey: String = Instance.hardwareMap.appContext.assets.open("vuforiaKey.txt").bufferedReader().use { it.readText() }
    private var canvas: Canvas? = null
    val bot = RobotClass(Instance)
    enum class CameraPositions {
        RIGHT, LEFT
    }

    private val cameraMonitorViewId = Instance.hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", Instance.hardwareMap.appContext.packageName)
    private val params: VuforiaLocalizer.Parameters = VuforiaLocalizer.Parameters(cameraMonitorViewId)

    private val telemetry: Telemetry = t
    private val instance = Instance

    private var parkingZone: Int = 2
    var target: Int = -1
    private lateinit var job: CompletableJob
    private lateinit var bitSave: Bitmap

    private var bestStart = 0
    private var bestEnd = 0
    private var diffStartEnd = 0
    private var botPos: Position = Position.CENTER
    var backReady = false
    var forwardReady = false

    private val xSliderConst = X_Slider()
    private val ySliderConst = Y_Slider()
    private val armConst = Arm()
    private val xGripConst = X_Grip()
    private val yGripConst = Y_Grip()
    private val zGripConst = Z_Grip()

    private val P_TURN_GAIN = 0.02
    private val P_DRIVE_GAIN = 0.03

    private var robotHeading = 0.0
    private var headingOffset = 0.0
    private var headingError = 0.0

    private var targetHeading = 0.0
    private var driveSpeed = 0.0
    private var turnSpeed = 0.0
    private var leftSpeed = 0.0
    private var rightSpeed = 0.0

    enum class Direction {
        FORWARD, BACKWARD, OPEN, CLOSE, UP, DOWN, MIDDLE, LEFT, RIGHT
    }

    enum class Position {
        LEFT, CENTER, RIGHT
    }

    init {
        params.vuforiaLicenseKey = vuforiaKey
        params.cameraName = bot.frontCam

        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        bot.zGrip.position = zGripConst.L5
//        bot.yGrip.position = yGripConst.middle
        bot.xGrip.position = xGripConst.open

        val pidfCoefficients = PIDFCoefficients(15.0,0.0,3.0,3.5)
        bot.fl.setPIDFCoefficients(bot.fl.mode, pidfCoefficients)
        bot.fr.setPIDFCoefficients(bot.fr.mode, pidfCoefficients)
        bot.bl.setPIDFCoefficients(bot.bl.mode, pidfCoefficients)
        bot.br.setPIDFCoefficients(bot.br.mode, pidfCoefficients)
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
        CoroutineScope(Main).launch {
            cupArmInit()
//            liftInit()
            liftArmInit()
            bot.xSlider.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            bot.xSlider.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }
    fun doneWithXSlider() {
        CoroutineScope(Main).launch {
            extArm(Direction.BACKWARD)
        }
    }
    fun readyLift() {
        CoroutineScope(Main).launch {
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
    fun startJob(vuforia: Cam, position: CameraPositions) {
        CoroutineScope(Main + job).launch {
            println("Coroutine $this is started with job $job")
            checkTarget(vuforia, position)
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

    fun setMotorPower(fl: Double, fr: Double, bl: Double, br:Double) {
        bot.fl.power = fl
        bot.fr.power = fr
        bot.bl.power = bl
        bot.br.power = br
    }
    fun setAllMotorPower(power: Double) {
        bot.fl.power = power
        bot.fr.power = power
        bot.bl.power = power
        bot.br.power = power
    }
    fun turn(leftPow: Double, rightPow:Double) {
        bot.fl.power = leftPow
        bot.fr.power = rightPow
        bot.bl.power = leftPow
        bot.br.power = rightPow
    }

    suspend fun liftInit() = coroutineScope {
        launch {
            delay(250)
            bot.ySlider.power = 0.5
            while (instance.opModeIsActive() &&
                abs(bot.ySlider.currentPosition) < ySliderConst.top
            ) {}
            bot.ySlider.power = 0.0
        }.join()
    }

    fun extArmInit() {
//        CoroutineScope(Main).launch {
            extArm(Direction.FORWARD)
//        }
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

    fun turnToAngle(angle: Double, power: Double) {

        bot.move(-power/100.0, power/100.0, -power/100.0, power/100.0)

        while (instance.opModeIsActive() &&
            abs(abs(angle) - getAbsoluteHeading()) > 1.0 // used to be 10
        )  {
            telemetry.addData("Current Angle", getAbsoluteHeading());
            telemetry.addData("Current", getAbsoluteHeading())
            telemetry.addData("Target", angle)
            telemetry.addData("Difference", abs(angle - getAbsoluteHeading()))
            telemetry.update()
        }
        bot.move(0.0, 0.0, 0.0, 0.0)
    }

    fun pivotTurn(angle: Double, power: Double, direction: Direction) {

        val pow: Double = if (angle < 0) -(power/100) else (power/100)

        when (direction) {
            Direction.LEFT -> {
                turn(-(pow/2.5), pow)
            }
            Direction.RIGHT -> {
                turn(pow, -(pow/2.5))
            }
        }
        while (instance.opModeIsActive() &&
            abs(abs(angle) - getAbsoluteHeading()) > 1.0 // used to be 10
        )  {
            telemetry.addData("Current", getAbsoluteHeading())
            telemetry.addData("Target", angle)
            telemetry.addData("Difference", abs(angle) - getAbsoluteHeading())
            telemetry.update()
        }
        bot.move(0.0, 0.0, 0.0, 0.0)
    }

    fun strafeToPosition(distance: Double, power:Double) {
        val ticks = inchToTick(distance)
        val targetFL = bot.fl.currentPosition - ticks.toInt()
        val targetFR = bot.fr.currentPosition + ticks.toInt()
        val targetBL = bot.bl.currentPosition + ticks.toInt()
        val targetBR = bot.br.currentPosition - ticks.toInt()

        bot.fl.targetPosition = targetFL
        bot.fr.targetPosition = targetFR
        bot.bl.targetPosition = targetBL
        bot.br.targetPosition = targetBR

        bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION

        bot.move(abs(power/100.0),
            abs(power/100.0),
            abs(power/100.0),
            abs(power/100.0)
        )

        while (instance.opModeIsActive() && (
                    bot.fl.isBusy &&
                    bot.fr.isBusy &&
                    bot.bl.isBusy &&
                    bot.br.isBusy
                    )) {
            telemetry.addData("fl position", bot.fl.currentPosition)
            telemetry.addData("fr position", bot.fr.currentPosition)
            telemetry.addData("bl position", bot.bl.currentPosition)
            telemetry.addData("br position", bot.br.currentPosition)
            telemetry.update()
        }

        bot.fl.power = 0.0
        bot.fr.power = 0.0
        bot.bl.power = 0.0
        bot.br.power = 0.0

        bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER

        sleep(100)
    }

    fun pivotStrafeToPosition(angle: Double, power: Double) {
        val pow = if (angle < 0)
            -power
        else {
            power
        }
        bot.move(pow, -pow, -pow, pow)

        while (instance.opModeIsActive() &&
            abs(abs(angle) - getAbsoluteHeading()) > 1.0
        )  {
            telemetry.addData("Current", getAbsoluteHeading())
            telemetry.addData("Target", angle)
            telemetry.addData("Difference", abs(angle - getAbsoluteHeading()))
            telemetry.update()
        }
        bot.move(0.0, 0.0, 0.0, 0.0)
    }

    fun runToPosition(distance: Double, power:Double) {
        val ticks = inchToTick(distance)
        val targetFL = bot.fl.currentPosition + ticks.toInt()
        val targetFR = bot.fr.currentPosition + ticks.toInt()
        val targetBL = bot.bl.currentPosition + ticks.toInt()
        val targetBR = bot.br.currentPosition + ticks.toInt()

        bot.fl.targetPosition = targetFL
        bot.fr.targetPosition = targetFR
        bot.bl.targetPosition = targetBL
        bot.br.targetPosition = targetBR

        bot.fl.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.fr.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.bl.mode = DcMotor.RunMode.RUN_TO_POSITION
        bot.br.mode = DcMotor.RunMode.RUN_TO_POSITION

        moveBot(abs(power/100.0), 0.0)
        while (instance.opModeIsActive() && (
                    bot.fl.isBusy &&
                            bot.fr.isBusy &&
                            bot.bl.isBusy &&
                            bot.br.isBusy
                    )) {
            telemetry.addData("Current Angle", getAbsoluteHeading());
            telemetry.addData("fl position", bot.fl.currentPosition)
            telemetry.addData("fr position", bot.fr.currentPosition)
            telemetry.addData("bl position", bot.bl.currentPosition)
            telemetry.addData("br position", bot.br.currentPosition)
            telemetry.update()
        }
        moveBot(0.0, 0.0)

        bot.fl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.fr.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.bl.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.br.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    fun moveBot(drive: Double, turn: Double) {
        driveSpeed = drive
        turnSpeed = turn

        leftSpeed = drive - turn
        rightSpeed = drive + turn
        //Scale the speeds down if either one exceeds +/- 1.0;
        val max = max(abs(leftSpeed), abs(rightSpeed))
        if (max > 1.0) {
            leftSpeed /= max
            rightSpeed /= max
        }
        turn(leftSpeed, rightSpeed)
    }

    fun getRawHeading(): Double {
        val angles: Orientation = bot.imu.getAngularOrientation(
            AxesReference.INTRINSIC,
            AxesOrder.ZYX,
            AngleUnit.DEGREES)

        return angles.firstAngle.toDouble()
    }

    fun getAbsoluteHeading(): Float {
        val angle = bot.imu.getAngularOrientation(
            AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES
        ).firstAngle

        return if (angle < 0) {
            360 - abs(angle)
        } else {
            angle
        }
    }

    private fun getSteeringCorrection(target: Double, gain:Double): Double {
        targetHeading = target

        // Get the robot heading by applying an offset to the IMU Heading
        robotHeading = getRawHeading() - headingOffset

        // Determine the heading current error
        headingError = targetHeading - robotHeading

        // Normalize the error to be withing +/- 180 degrees
        while (headingError > 180) headingError -= 360
        while (headingError <= -180) headingError += 360

        // Multiply the error by the gain to determine the required steering correction
        // (Limit the result to be between +/- 1.0)
        return Range.clip(headingError * gain, -1.0, 1.0)
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
        }

        telemetry.addData("CupArm Position", bot.arm.currentPosition)
        telemetry.addData("CupArm movement", cupDegree)
        telemetry.update()
        sleep(100)
        bot.arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        bot.arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
        bot.arm.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    }

    private suspend fun liftArmInit() {
        delay(500)
        liftHand(Direction.MIDDLE)
    }

    suspend fun checkTarget(vuforia: Cam, position: CameraPositions) {
        while (instance.opModeInInit()) {
            if (vuforia.rgb != null) {
                val bmp: Bitmap = Bitmap.createBitmap(
                    vuforia.rgb.width,
                    vuforia.rgb.height,
                    Bitmap.Config.RGB_565
                )
                bmp.copyPixelsFromBuffer(vuforia.rgb.pixels)
                bitSave = bmp
                seeSignal(bitSave, position)
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
                telemetry.addData("Current Angle", getAbsoluteHeading());
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

    fun extArm(direction: Direction) {
        when (direction) {
            Direction.BACKWARD -> {
                val timer = ElapsedTime(ElapsedTime.Resolution.MILLISECONDS)
                if (bot.xSensor.state) {
                    bot.xSlider.power = -1.0
                    while (instance.opModeIsActive() && bot.xSensor.state) { }
                    bot.xSlider.power = 0.0
                }
                backReady = true
                timer.reset()
                bot.xSlider.power = -1.0
                while (instance.opModeIsActive() && timer.milliseconds() < 425) { }
                bot.xSlider.power = 0.0
            }
            Direction.FORWARD -> {
                bot.xSlider.power = 0.4
                while (instance.opModeIsActive() && bot.xSensor.state) {}
                bot.xSlider.power = 0.0
                forwardReady = true
            }
        }
    }

    fun liftHand(type: Direction) {
        when (type) {
            Direction.OPEN -> {
                bot.yGrip.position = yGripConst.receive
            }
            Direction.CLOSE -> {
                bot.yGrip.position = yGripConst.dump
            }
            else -> {
                return
            }
        }
    }

    private fun seeSignal(bitSave: Bitmap, position: CameraPositions) {
        var left = 0 // pixels from left of image to left edge of ring stack (max)
        var top = 0 // pixels from top of image to top of a 4 ring stack

        when (position) {
            CameraPositions.RIGHT -> {
                left = 330
                top = 280
            }
            CameraPositions.LEFT -> {
                left = 330
                top = 170
            }
        }

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

}

