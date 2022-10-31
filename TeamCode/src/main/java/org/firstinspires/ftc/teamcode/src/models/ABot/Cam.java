package org.firstinspires.ftc.teamcode.src.models.ABot;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;
import com.qualcomm.robotcore.robot.Robot;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.logging.Handler;
import android.graphics.ImageFormat;
//import android.os.Handler;

//import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.android.util.Size;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.hardware.camera.Camera;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraCaptureRequest;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraCaptureSequenceId;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraCaptureSession;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraCharacteristics;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraException;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraFrame;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraManager;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.internal.collections.EvictingBlockingQueue;
import org.firstinspires.ftc.robotcore.internal.network.CallbackLooper;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.ContinuationSynchronizer;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Cam  {

    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    private static final String TAG = "Cam";

    /** How long we are to wait to be granted permission to use the camera before giving up. Here,
     * we wait indefinitely */
    private static final int secondsPermissionTimeout = Integer.MAX_VALUE;

    /** State regarding our interaction with the camera */
    private CameraManager cameraManager;
    private WebcamName cameraName;
    private Camera camera;
    private CameraCaptureSession cameraCaptureSession;

    /** The queue into which all frames from the camera are placed as they become available.
     * Frames which are not processed by the OpMode are automatically discarded. */
    private EvictingBlockingQueue<Bitmap> frameQueue;

    private Bitmap bmp; //just use the one bitmap so you don't have to pass it around
    private Canvas canvas; //canvas to draw on bitmap
    private Paint paint; //paintbrush for drawing lines

    private int left = 55; // pixels from left of image to left edge of ring stack
    private int top = 130; // pixels from top of image to top of a 4 ring stack
    private int right = left+150; // pixels from left of image to right edge of ring stack
    private int bottom = top+150; // pixels from top of image to bottom of ring stack
    private int left2 = 320; // pixels from left of image to left edge of ring stack
    private int top2 = 130; // pixels from top of image to top of a 4 ring stack
    private int right2 = left2+150; // pixels from left of image to right edge of ring stack
    private int bottom2 = top2+150;

    private int rMin = 100; // min red value in orange
    private int gMin = 100; // min green value in orange
    private int bMax = 100; // max blue value in orange

    private double tot = 0;
    private double orange = 0;
    private double pct = 0.0;
    private int result = 0;
    private int rings = 0;

    /** State regarding where and how to save frames when the 'A' button is pressed. */
    private int captureCounter = 0;
    private File captureDirectory = AppUtil.ROBOT_DATA_DIR;

    /** A utility object that indicates where the asynchronous callbacks from the camera
     * infrastructure are to run. In this OpMode, that's all hidden from you (but see {@link #startCamera}
     * if you're curious): no knowledge of multi-threading is needed here. */
    private android.os.Handler callbackHandler;
    private LinearOpMode op;

    // Creator
    public Cam(LinearOpMode newOp) {
        op = newOp;
    }

    public boolean setUp() {
        callbackHandler = CallbackLooper.getDefault().getHandler();

        cameraManager = ClassFactory.getInstance().getCameraManager();
        cameraName = op.hardwareMap.get(WebcamName.class, "Webcam");

        //set up the brush for drawing lines and text
        paint = new Paint();
        paint.setColor(Color.rgb(255,255,255));
        paint.setStrokeWidth(4);
        paint.setTextSize(60);

        initializeFrameQueue(2);
        AppUtil.getInstance().ensureDirectoryExists(captureDirectory);

        // start the camera
        try {
            openCamera();
            if (camera == null) return false;

            startCamera();
            if (cameraCaptureSession == null) return false;
        } finally {
            op.telemetry.addData("Status0", "Setting up Cam");
            return false;
        }
    }

    // start the frame capture process
    public int startStackCount() {
        boolean captureWhenAvailable = true;
        while (captureWhenAvailable) {
            bmp = frameQueue.poll();
            //ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //byte[] byteArray = stream.toByteArray();
            if (bmp != null) {
                captureWhenAvailable = false;
                //System.out.println("BMP: \n" + Arrays.toString(byteArray));
                return 1;
            }
        }
        return 0;
    }

    // when a new frame comes in use countRings to get the number of rings and save the bitmap
    private int onNewFrame() {
        stopCamera();
        op.telemetry.addData("Status3", "In onNewFrame");
        op.telemetry.update();
        if (countRings(left, right, top, bottom)) {
            return 1;
        } if (countRings(left2, right2, top2, bottom2)) {
            return 2;
        } else {
            return 3;
        }

        //frame.recycle(); // not strictly necessary, but helpful
    }

    //----------------------------------------------------------------------------------------------
    // Camera operations
    //----------------------------------------------------------------------------------------------

    private void initializeFrameQueue(int capacity) {
        /** The frame queue will automatically throw away bitmap frames if they are not processed
         * quickly by the OpMode. This avoids a buildup of frames in memory */
        frameQueue = new EvictingBlockingQueue<Bitmap>(new ArrayBlockingQueue<Bitmap>(capacity));
        frameQueue.setEvictAction(new Consumer<Bitmap>() {
            @Override public void accept(Bitmap frame) {
                frame.recycle(); // not strictly necessary, but helpful
            }
        });
    }

    private void openCamera() {
        if (camera != null) return;

        Deadline deadline = new Deadline(secondsPermissionTimeout, TimeUnit.SECONDS);
        camera = cameraManager.requestPermissionAndOpenCamera(deadline, cameraName, null);
        if (camera == null) {
            op.telemetry.addData("camera not found or permission to use not granted: %s", cameraName);
        }
    }



    private void startCamera() {
        if (cameraCaptureSession != null) return; // be idempotent

        /** YUY2 is supported by all Webcams, per the USB Webcam standard: See "USB Device Class Definition
         * for Video Devices: Uncompressed Payload, Table 2-1". Further, often this is the *only*
         * image format supported by a camera */
        final int imageFormat = ImageFormat.YUY2;


        /** Verify that the image is supported, and fetch size and desired frame rate if so */
        CameraCharacteristics cameraCharacteristics = cameraName.getCameraCharacteristics();
        op.telemetry.addData("d1",cameraCharacteristics.getAndroidFormats());
        op.telemetry.update();
        if (!contains(cameraCharacteristics.getAndroidFormats(), imageFormat)) {
            error("image format not supported");
            return;
        }
        final Size size = cameraCharacteristics.getDefaultSize(imageFormat);
        final int fps = cameraCharacteristics.getMaxFramesPerSecond(imageFormat, size);

        /** Some of the logic below runs asynchronously on other threads. Use of the synchronizer
         * here allows us to wait in this method until all that asynchrony completes before returning. */
        final ContinuationSynchronizer<CameraCaptureSession> synchronizer = new ContinuationSynchronizer<>();
        try {
            /** Create a session in which requests to capture frames can be made */
            camera.createCaptureSession(Continuation.create(callbackHandler, new CameraCaptureSession.StateCallbackDefault() {
                @Override public void onConfigured( CameraCaptureSession session) {
                    try {
                        /** The session is ready to go. Start requesting frames */
                        final CameraCaptureRequest captureRequest = camera.createCaptureRequest(imageFormat, size, fps);
                        session.startCapture(captureRequest,
                                new CameraCaptureSession.CaptureCallback() {
                                    @Override public void onNewFrame( CameraCaptureSession session,  CameraCaptureRequest request,  CameraFrame cameraFrame) {
                                        /** A new frame is available. The frame data has <em>not</em> been copied for us, and we can only access it
                                         * for the duration of the callback. So we copy here manually. */
                                        Bitmap bmp = captureRequest.createEmptyBitmap();
                                        cameraFrame.copyToBitmap(bmp);
                                        frameQueue.offer(bmp);
                                    }
                                },
                                Continuation.create(callbackHandler, new CameraCaptureSession.StatusCallback() {
                                    @Override public void onCaptureSequenceCompleted( CameraCaptureSession session, CameraCaptureSequenceId cameraCaptureSequenceId, long lastFrameNumber) {
                                        RobotLog.ii(TAG, "capture sequence %s reports completed: lastFrame=%d", cameraCaptureSequenceId, lastFrameNumber);
                                    }
                                })
                        );
                        synchronizer.finish(session);
                    } catch (CameraException|RuntimeException e) {
                        RobotLog.ee(TAG, e, "exception starting capture");
                        error("exception starting capture");
                        session.close();
                        synchronizer.finish(null);
                    }
                }
            }));
        } catch (CameraException|RuntimeException e) {
            RobotLog.ee(TAG, e, "exception starting camera");
            error("exception starting camera");
            synchronizer.finish(null);
        }

        /** Wait for all the asynchrony to complete */
        try {
            synchronizer.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        /** Retrieve the created session. This will be null on error. */
        cameraCaptureSession = synchronizer.getValue();
    }

    private void stopCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.stopCapture();
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
    }

    public void closeCamera() {
        stopCamera();
        if (camera != null) {
            camera.close();
            camera = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    // Utilities
    //----------------------------------------------------------------------------------------------
    // these error functions write to the log file.
    private void error(String msg) {
        op.telemetry.log().add(msg);
        op.telemetry.update();
    }
    private void error(String format, Object...args) {
        op.telemetry.log().add(format, args);
        op.telemetry.update();
    }

    private boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) return true;
        }
        return false;
    }

    // checkPixel tests to see if the pixel is orange.
    // If so it adds one the count and then turns the pixel yellow for the saved image.
    private void checkPixel(int x, int y){
        int p = bmp.getPixel(x,y);
        int a = (p >> 24) & 0xFF;   //alpha value
        int r = (p >> 16) & 0xFF;   //red value
        int g = (p >> 8) & 0xFF;    //green value
        int b = p & 0xFF;           //blue value
        int rDif = rMin-r;   // red dif if >0 then not orange
        int gDif = gMin-g;      // green dif if >0 then not orange
        int bDif = b-bMax;      // blue dif if >0 then not orange

        tot++;
        if(r >rMin && g > gMin && b < bMax){  // if the pixel is close enough to orange
            orange++;
            bmp.setPixel(x,y,Color.rgb(255,255,0));  // set pixel to yellow
        } else {
            bmp.setPixel(x,y,Color.rgb(rDif>=gDif && rDif>=bDif?255:0,rDif<gDif && gDif >= bDif?255:0,rDif<bDif && gDif<bDif?255:0));   // else set pixel to blue
        }

    }

    // determines the number of rings based on the percentage of orange pixels
    // in the area defined by left,top,right,bottom (see class variables above)
    private boolean countRings(int l, int r, int t, int b){
        orange = 0;
        tot = 0;
        canvas = new Canvas(bmp);

        for(int i = l; i < r; i++){
            for (int j = t; j < b; j++){
                checkPixel(i,j);
            }
        }
        pct = 100*orange/tot;
        String s = String.format("pct: %.1f%%", pct);
        canvas.drawText(s,l, b, paint);
        op.telemetry.addData("Orange:",s);
        return pct > 5;
    }

    /*public int ringCount(){
        return result;
    }*/

    //private void saveBitmap(Bitmap bitmap) {
    public void saveBitmap() {
        //File file = new File(captureDirectory, String.format(Locale.getDefault(), "webcam-frame-%d.jpg", captureCounter++));
        // Color color = bitmap.getColor(0, 0);
        //RobotLog.e(TAG, "dsadssf");
        //int pixel = bmp.getPixel(0, 0);
        //drawRingStackBox();

        //error(String.format("%d %d %d %d", a, r, g, b));
        // error(String.format("%d %d %d", color.red(), color.green(), color.blue()));
        File file = new File(captureDirectory, String.format(Locale.getDefault(), "webcam-frame.jpg"));
        op.telemetry.addData("Status1:","attempt to get %s", file.getName());
        op.telemetry.addData("Status1:","attempt to get %s", file.getPath());


        try {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                op.telemetry.log().add("captured %s", file.getName());
                //op.telemetry.update;
            }
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "exception in saveBitmap()");
            error("exception saving %s", file.getName());
        }
        op.telemetry.update();
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
            if (value < low) { return low; };
            if (value > high) { return high; }
            return value;
        }

        static RGB fromYUV(int y, int u, int v) {
            int rTmp = (int) (y + 1.370705 * (v - 128));
            int gTmp = (int) (y - (0.698001 * (v - 128)) - (0.337633 * (u - 128)));
            int bTmp = (int) (y + (1.732446 * (u - 128)));
            return new RGB(
                    clamp(rTmp, 0, 255),
                    clamp(gTmp, 0, 255),
                    clamp(bTmp, 0, 255)
            );
        }

    }
}
