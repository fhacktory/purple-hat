package purplehat.fr.purplehat.game;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by jmcomets on 11/10/14.
 */
public class World {
    private Collection<Ball> balls;
    private Collection<Rect2<Integer>> rects;

    public World() {
        balls = new ArrayList<Ball>();
        rects = new ArrayList<Rect2<Integer>>();

        Ball ball = new Ball();
        ball.setPosition(new Vector2<Integer>(10, 20));
        ball.setRadius(30);
        balls.add(ball);
    }

    public Collection<Ball> getBalls() {
        return balls;
    }

    public Collection<Rect2<Integer>> getRects() {
        return rects;
    }
}
