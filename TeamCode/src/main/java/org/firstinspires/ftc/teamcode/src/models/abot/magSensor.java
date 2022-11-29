package org.firstinspires.ftc.teamcode.src.models.abot;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class magSensor {
    public DigitalChannel get(String name, HardwareMap hardwareMap) {
        DigitalChannel digitalTouch = hardwareMap.get(DigitalChannel.class, name);
        digitalTouch.setMode(DigitalChannel.Mode.INPUT);
        return digitalTouch;
    }
}
