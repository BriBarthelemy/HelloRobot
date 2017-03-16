package opencv.interfaces;

import org.opencv.core.Rect;

import java.util.ArrayList;

/**
 * Created by Jolan Stordeur on 15/03/2017.
 */
public interface iOpenCV {

    void init();

    void startCapture();

    void setClassifierPath(String path);
    String getClassifierPathToString();

    int getCountDetectedObjects();

    ArrayList<Rect> getObjects();

    int getFrameWidth();

    int getFrameHeight();
}
