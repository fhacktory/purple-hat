package purplehat.fr.purplehat.game;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Ball {
    private Vector2<Double> position;

    public Vector2<Double> getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2<Double> velocity) {
        this.velocity = velocity;
    }

    public Vector2<Double> getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2<Double> acceleration) {
        this.acceleration = acceleration;
    }

    private Vector2<Double> velocity;
    private Vector2<Double> acceleration;
    private Double radius;

    public Ball() {
        position = new Vector2<Double>(0.0, 0.0);
        velocity = new Vector2<Double>(Math.random() * 100, Math.random() * 100);
        acceleration = new Vector2<Double>(0.0, 0.0);
        radius = 0.0;
    }

    public Ball(Vector2<Double> position, double radius) {
        this.position = position;
        velocity = new Vector2<Double>(0.0, 0.0);
        acceleration = new Vector2<Double>(0.0, 0.0);
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
