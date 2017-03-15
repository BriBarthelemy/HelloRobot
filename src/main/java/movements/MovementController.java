package movements;

import opencv.iOpenCV;
import org.opencv.core.Rect;

import java.util.ArrayList;

/**
 * Created by Jolan Stordeur on 15/03/2017.
 */
public interface MovementController {

    void init();

    /**
     * This would check the openCV object to see what it should do.
     */
    void takeAction(iOpenCV openCV);





}
