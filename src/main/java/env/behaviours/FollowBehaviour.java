package env.behaviours;

import actions.movements.interfaces.MovementController;
import env.Environment;
import env.behaviours.interfaces.Behaviour;
import opencv.OpenCVImpl;
import opencv.interfaces.iOpenCV;
import org.apache.log4j.Logger;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by pix on 16.03.17.
 */
public class FollowBehaviour implements Behaviour {
    private static FollowBehaviour Instance;
    private iOpenCV openCV = OpenCVImpl.getInstance();
    private static Logger logger = Logger.getLogger(FollowBehaviour.class);
    private MovementController controller;

    private int buffer = 50;

    private FollowBehaviour(){
        controller = Environment.getController();
    }

    public static Behaviour getInstance() {
        if(Instance ==null){
            logger.info("Initiating Instance.");
            Instance = new FollowBehaviour();
        }
        return Instance;
    }

    private Optional<Rect> getBiggestObject(){
        ArrayList<Rect> objects = openCV.getObjects();
        int biggestObject = 0;
        for(Rect r:objects)
            if(r.height*r.width>biggestObject){
                biggestObject = r.height*r.width;
                return Optional.of(r);
            }
        return Optional.empty();
    }


    private void think(){
        logger.info("Thinking...");
        Optional<Rect> toFollow = getBiggestObject();
        if(toFollow.isPresent()){
            Rect object = toFollow.get();
            int centerOfObject = object.x + object.width/2;
            int offCenter = centerOfObject-openCV.getFrameWidth()/2;
            if(Math.abs(offCenter)>buffer){
                move(centerOfObject,1);
            }
        }
    }

    private void move(int distance, int speed){
        controller.move(distance,speed);
    }


    @Override
    public void run() {
        think();
    }

    @Override
    public String getInfo(){
        return "Following bigger object..";
    }
}
