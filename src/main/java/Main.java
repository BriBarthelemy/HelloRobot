import opencv.OpenCVImpl;
import opencv.iOpenCV;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.opencv.core.Core;

import java.util.concurrent.*;

/**
 * Created by bri on 01-Mar-17.
 */
public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    private static ScheduledExecutorService executorService;


    public static void main(String[] args) {
        BasicConfigurator.configure();
        logger.info("Program started.");


        iOpenCV openCV = new OpenCVImpl();
        openCV.init();
        openCV.startCapture();

        Runnable pokeCount = ()->{
            logger.info(openCV.getCountDetectedObjects());
        };

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(pokeCount,1000,1000, TimeUnit.MILLISECONDS);

    }


}
