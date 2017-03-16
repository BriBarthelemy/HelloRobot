package opencv;

import opencv.interfaces.iOpenCV;
import org.apache.log4j.Logger;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jolan Stordeur on 15/03/2017.
 */
public class OpenCVImpl implements iOpenCV {


    private Logger logger =  Logger.getLogger(OpenCVImpl.class);;

    // Used to acquire video steam
    private ScheduledExecutorService timer;

    // OpenCV object that performs the video capture.
    private VideoCapture videoCapture;

    private int frameWidth;
    private int frameHeight;

    private ArrayList<Rect> detectedObjects = new ArrayList<>();

    private boolean cameraActive;

    // face cascade classifier
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;
    private static OpenCVImpl Instance;

    public static OpenCVImpl getInstance(){
        System.out.println("Getting OpenCV instance.");
        if(Instance==null){
            System.out.println("OpenCV was null...");
            Instance = new OpenCVImpl();
            return Instance;
        }
        return Instance;
    }

    private OpenCVImpl(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    /**
     * inits the Instance impl with default presets.
     * This will detect faces.
     */
    public void init(){


        this.videoCapture = new VideoCapture();
        this.cascadeClassifier = new CascadeClassifier();

        this.cascadeClassifier.load("./src/main/resources/haarcascades/haarcascade_frontalface_alt.xml");
        if(cascadeClassifier.empty())
            logger.error("Cascade failed to load.");
        logger.info("Initiation of OpenCV object complete.");
    }


    /**
     * Starts the video capture and the detection of objects based on the cascadeClassifier.
     */
    @Override
    public void startCapture(){
        if(!this.cameraActive && !cascadeClassifier.empty()){
            this.videoCapture.open(0);
            if(videoCapture.isOpened()){
                this.cameraActive=true;

                Mat frame = grabFrame();
                this.frameWidth = frame.width();
                this.frameHeight = frame.height();

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(this::grabFrame,0,33, TimeUnit.MILLISECONDS);
            }


        }else {
            logger.error("Failed to open the camera connection.");

        }
    }


    /**
     * Reloads the cascade classifier object, if the path doesn't point to a valid file,
     * it'll throw an invalidstateException.
     * @param path Path of the new classifier.
     * @throws InvalidStateException Thrown if the classifier isn't valid.
     */
    @Override
    public void setClassifierPath(String path) throws InvalidStateException {
        if(cameraActive)
            timer.shutdown();
        // Here we wait until the service is actually shut down to change the path.
        //
        while(!timer.isTerminated())

        this.cascadeClassifier.load(path);
        if(cascadeClassifier.empty())
            throw new InvalidStateException("Invalid path or invalid file.");
    }


    /**
     * Mostly for debugging, returns the cascadeClassfier.ToString.
     * @return CascadeClassifierToString.
     */
    public String getClassifierPathToString() {
        return this.cascadeClassifier.toString();
    }

    @Override
    public int getCountDetectedObjects() {
        return this.detectedObjects.size();
    }

    @Override
    public ArrayList<Rect> getObjects() {
        return this.detectedObjects;
    }

    private Mat grabFrame(){
        Mat frame = new Mat();

        // Check if the capture is open
        if(this.videoCapture.isOpened()){
            try{
                this.cameraActive = true;

                this.videoCapture.read(frame);

                if(!frame.empty()){
                    detect(frame);
                }

            }
            catch (Exception e){
                logger.error("Could not open videocapture." + e);
            }
        }
        return frame;

    }

    private void detect(Mat frame){

        MatOfRect objects = new MatOfRect();

        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame,grayFrame,Imgproc.COLOR_BGR2GRAY);

        Imgproc.equalizeHist(grayFrame,grayFrame);

        if(this.absoluteFaceSize==0){
            int h = grayFrame.rows();
            if(Math.round(h*0.2f)>0)
                this.absoluteFaceSize = Math.round(h*0.2f);
        }

        //Detects the objects
        this.cascadeClassifier.detectMultiScale(grayFrame,objects,1.1,2,0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize,this.absoluteFaceSize),new Size());

        /*
         * This is probably not the most effective method.. Maybe keeping an array would be better?
         */
        this.detectedObjects.clear();
        Collections.addAll(this.detectedObjects, objects.toArray());
    }


    @Override
    public int getFrameWidth() {
        return frameWidth;
    }

    @Override
    public int getFrameHeight() {
        return frameHeight;
    }
}
