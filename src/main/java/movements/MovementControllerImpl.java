package movements;

import opencv.iOpenCV;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Jolan Stordeur on 15/03/2017.
 */
public class MovementControllerImpl implements MovementController {

    private ScheduledExecutorService executorService;

    @Override
    public void init(){
        executorService = Executors.newSingleThreadScheduledExecutor();

    }



    @Override
    public void takeAction(iOpenCV openCV) {

    }
}
