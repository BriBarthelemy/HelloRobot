package actions.movements;

import actions.movements.interfaces.MovementController;
import org.apache.log4j.Logger;

/**
 * Created by Jolan Stordeur on 15/03/2017.
 */
public class LaptopMovementController implements MovementController {

    private Logger logger = Logger.getLogger(LaptopMovementController.class);

    public LaptopMovementController(){}


    @Override
    public void init(){

    }

    @Override
    public void move(int a, int b) {
        logger.info("Move, distance:" + a + "at:" +b+" speed.");
        if(a<0)
            logger.info("Please move the laptop to the left...");
        else
            logger.info("Please move the laptop to the right..");
    }


}
