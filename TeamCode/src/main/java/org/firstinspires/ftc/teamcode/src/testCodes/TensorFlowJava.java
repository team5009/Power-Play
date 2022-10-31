//package org.firstinspires.ftc.teamcode.src.testCodes;
//
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//
//import org.firstinspires.ftc.robotcore.external.ClassFactory;
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
//import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
//import org.firstinspires.ftc.robotcore.internal.system.Deadline;
//import org.tensorflow.lite.task.core.BaseOptions;
//import org.tensorflow.lite.task.vision.classifier.Classifications;
//import org.tensorflow.lite.task.vision.classifier.ImageClassifier;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@Autonomous(name = "VuTest", group = "AutoTest")
//public class TensorFlowJava extends LinearOpMode {
//    private final String[] TFOD_List = {"BlueCone", "RedCone"};
//    private VuforiaLocalizer vuforia;
//    private TFObjectDetector tfod;
//    private String TFOD_Asset = "cones/tflite_unquant/model_metadata.tflite";
//
//
//    @Override
//    public void runOpMode(){
//        ImageClassifier.ImageClassifierOptions options =
//                ImageClassifier.ImageClassifierOptions.builder()
//                        .setBaseOptions(BaseOptions.builder().useGpu().build())
//                        .setMaxResults(1)
//                        .build();
//        ImageClassifier imageClassifier =
//                null;
//        try {
//            imageClassifier = ImageClassifier.createFromFileAndOptions(this.hardwareMap.appContext, TFOD_Asset, options);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//// Run inference
////        List<Classifications> results = imageClassifier.classify(image);
//
//        if (tfod != null) {
//            tfod.activate();
//            tfod.setZoom(1.0, 16.0/9.0);
//        }
//
//        telemetry.addData(">", "Press Play to start op mode");
//        telemetry.update();
//        waitForStart();
//    }
//
//    private void initVuforia(){
//        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
//
//        String VUFORIA_KEY = "Aaw91aX/////AAABmdsPMOJ+SEw2hbB0KuA91QNdCicETTnXn5+HBhEteAhuFgn/KjtDhaDByyvmLiJRDJrUO1njQVgKy9wvDfxoXptJURnl+R6mpv17sWJCvMYUeFmlgrk/31pTM4aeIdSNn9z+xupD/tgXRS2cQEHAIu+Hha1/EzIF3lbYiRwNhDbG3XtVzmjuBLus3Gx+I75KgUzkPwn0XIFbQ96BZvIF+6cv3bKEWNyUZ7thLBB3OrPP0Fm02W3fmXhxAB4cpvuqUbVOFkOEkrhAyDxtuP28PKchLXjcJD6JMFXYa8K7pGXcuMiNge5M10knHwEWgvSJLuhcC2onFtgnlGwmDswuqki2ynlQPbLQAGwpKNsPSlET";
//        parameters.vuforiaLicenseKey = VUFORIA_KEY;
//        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam");
//
//        //  Instantiate the Vuforia engine
//        vuforia = ClassFactory.getInstance().createVuforia(parameters);
//    }
//
//    private void initTfod() {
//        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
//                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
//        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
//        tfodParameters.minResultConfidence = 0.75f;
//        tfodParameters.isModelTensorFlow2 = true;
//        tfodParameters.inputSize = 300;
//        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
//
//
//        tfod.loadModelFromAsset(TFOD_Asset, TFOD_List);
//    }
//}
