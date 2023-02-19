package org.firstinspires.ftc.teamcode.src.models.abot.utils;

import com.qualcomm.robotcore.util.ElapsedTime;

public class TurnPIDController {
    private double kP, kI, kD;
    private ElapsedTime timer = new ElapsedTime();
    private final double targetAngle;
    private double lastError = 0;
    private double accumulatedError = 0;
    private double lastTime = -1;
    private double lastSlope = 0;

    public TurnPIDController(double target, double p, double i, double d) {
        kP = p;
        kI = i;
        kD = d;
        targetAngle = target;
    }

    public double update(double currentAngle) {
        // TODO: make sure angles are within bounds and are in same format (e.g., 0 <= | angle | <= 180)
        //   and ensure direction is correct

        // P
        double error = targetAngle - currentAngle;
        error %= 360;
        error += 360;
        error %= 360;
        if (error > 180) {
            error -= 360;
        }

        // I
        accumulatedError *= Math.signum(error);
        accumulatedError += error;
        if (Math.abs(error) < 2) {
            accumulatedError = 0;
        }

        // D
        double slope = 0;
        if (lastTime > 0) {
            slope = (error - lastError) / (timer.milliseconds() - lastTime);
        }
        lastSlope = slope;
        lastError = error;
        lastTime = timer.milliseconds();

        return 0.1 * Math.signum(error) + 0.9 * Math.tanh(kP * error + kI * accumulatedError - kD * slope);
    }

    public double getLastSlope() {
        return lastSlope;
    }
}
