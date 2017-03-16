package env;

import actions.movements.LaptopMovementController;
import actions.movements.interfaces.MovementController;
import actions.movements.PiMovementController;
import env.behaviours.interfaces.Behaviour;
import env.behaviours.FollowBehaviour;
import opencv.OpenCVImpl;
import opencv.interfaces.iOpenCV;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by pix on 16.03.17.
 */
public class Environment {
    private static Environment ourInstance = new Environment();

    private static MovementController controller;
    private static Logger logger = Logger.getLogger(Environment.class);
    private static Behaviour behaviour;


    private static ScheduledExecutorService executorService;



    public static Environment getInstance() {
        System.out.println("Environment called..");
        return ourInstance;
    }

    private Environment() {
    }
    private static iOpenCV openCV = OpenCVImpl.getInstance();

    public static void start(){

        iOpenCV openCV = OpenCVImpl.getInstance();
        openCV.init();
        openCV.startCapture();
        logger.info("FLAG?");
        logger.info("Starting behaviour thread:" + behaviour.getInfo());
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(behaviour,1000,1000, TimeUnit.MILLISECONDS);
    }



    public static MovementController getController() {
        return controller;
    }

    public static void configure(){
        BasicConfigurator.configure();


        String username = System.getProperty("user.name");
        if(username==null) {
            logger.error("The username shouldn't be null...");
            return;
        }
        if(username.equals("pi")){
            logger.info("Username fits a pi, setting controller to pi Controller.");
            controller = new PiMovementController();
        } else {
            logger.info("Username:"+username +"Setting system to a laptop.");
            controller = new LaptopMovementController();
        }
        behaviour = FollowBehaviour.getInstance();
    }

}
