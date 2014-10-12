package purplehat.fr.purplehat.game;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Ball {
    private Vector2<Double> position;
    private Double radius;

    public Ball() {
        position = new Vector2<Double>(0.0, 0.0);
        radius = 0.0;
    }

    public Ball(Vector2<Double> position, double radius) {
        this.position = position;
        this.radius = radius;
    }

    public Vector2<Double> getPosition() {
        return position;
    }

    public void setPosition(Vector2<Double> position) {
        this.position = position;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
