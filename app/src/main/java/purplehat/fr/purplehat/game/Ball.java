package purplehat.fr.purplehat.game;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Ball {
    private Vector2<Integer> position;
    private int radius;

    public Ball() {
        position = new Vector2<Integer>(0, 0);
        radius = 0;
    }

    public Ball(Vector2<Integer> position, int radius) {
        this.position = position;
        this.radius = radius;
    }

    public Vector2<Integer> getPosition() {
        return position;
    }

    public void setPosition(Vector2<Integer> position) {
        this.position = position;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
