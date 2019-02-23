import java.awt.*;
import java.util.ArrayList;
import java.util.*;
import java.util.List;
/**
 *
* */
public class BoardState {
    Point pacPos;
    Point g1Pos;
    Point g2Pos;
    List<Point> food;

    BoardState(List<Point> food,Point pacPos, Point g1Pos, Point g2Pos){
        this.pacPos = pacPos;
        this.g1Pos = g1Pos;
        this.g2Pos = g2Pos;
        this.food = food;

    }
}
