package org.firstinspires.ftc.teamcode.src.models.abot.utils;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.Frame;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.State;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.vuforia.VuforiaLocalizerImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class Cam extends VuforiaLocalizerImpl {

    private final File captureDirectory = AppUtil.ROBOT_DATA_DIR;
    public Image rgb;

    public Cam(VuforiaLocalizer.Parameters parameters) {
        super(parameters);
        stopAR();
        clearGlSurface();

        this.vuforiaCallback = new VuTestSubclass();
        startAR();

        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);

    }

    public void clearGlSurface() {
        if (this.glSurfaceParent != null) {
            appUtil.synchronousRunOnUiThread(new Runnable() {
                @Override
                public void run() {
                    glSurfaceParent.removeAllViews();
                    glSurfaceParent.getOverlay().clear();
                    glSurface = null;
                }
            });
        }
    }

    //private void saveBitmap(Bitmap bitmap) {
    public void saveBitmap(OpMode op, Bitmap bmp) {
        //File file = new File(captureDirectory, String.format(Locale.getDefault(), "webcam-frame-%d.jpg", captureCounter++));
        // Color color = bitmap.getColor(0, 0);
        //RobotLog.e(TAG, "dsadssf");
        //int pixel = bmp.getPixel(0, 0);
        //drawRingStackBox();

        //error(String.format("%d %d %d %d", a, r, g, b));
        // error(String.format("%d %d %d", color.red(), color.green(), color.blue()));
        File file = new File(captureDirectory, String.format(Locale.getDefault(), "webcam-frame.jpg"));
        op.telemetry.addData("Status1:", "attempt to get %s", file.getName());
        op.telemetry.addData("Status1:", "attempt to get %s", file.getPath());


        try {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                op.telemetry.log().add("captured %s", file.getName());
                //op.telemetry.update;
            }
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "exception in saveBitmap()");
            op.telemetry.addData("exception saving %s", file.getName());
        }
        op.telemetry.update();
    }

    static class ClosableFrame extends Frame {
        public ClosableFrame(Frame other) {
            super(other);
        }

        public void close() {
            super.delete();
        }
    }

    static class RGB {
        public int r;
        public int g;
        public int b;

        RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        private static int clamp(int value, int low, int high) {
            if (value < low) {
                return low;
            }
            ;
            if (value > high) {
                return high;
            }
            return value;
        }

        static Cam.RGB fromYUV(int y, int u, int v) {
            int rTmp = (int) (y + 1.370705 * (v - 128));
            int gTmp = (int) (y - (0.698001 * (v - 128)) - (0.337633 * (u - 128)));
            int bTmp = (int) (y + (1.732446 * (u - 128)));
            return new Cam.RGB(
                    clamp(rTmp, 0, 255),
                    clamp(gTmp, 0, 255),
                    clamp(bTmp, 0, 255)
            );
        }

    }

    public class VuTestSubclass extends VuforiaLocalizerImpl.VuforiaCallback {
        @Override
        public synchronized void Vuforia_onUpdate(State state) {
            super.Vuforia_onUpdate(state);

            ClosableFrame frame = new ClosableFrame(state.getFrame());

            long num = frame.getNumImages();
            for (int i = 0; i < num; i++) {
                if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                    rgb = frame.getImage(i);
                    break;
                }
            }

            frame.close();
        }
    }
}
