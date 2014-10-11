package purplehat.fr.purplehat.game;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Rect2<T extends Number> {
    private T x1;
    private T x2;
    private T y1;
    private T y2;

    public Rect2(T x1, T x2, T y1, T y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public T getX1() {
        return x1;
    }

    public void setX1(T x1) {
        this.x1 = x1;
    }

    public T getX2() {
        return x2;
    }

    public void setX2(T x2) {
        this.x2 = x2;
    }

    public T getY1() {
        return y1;
    }

    public void setY1(T y1) {
        this.y1 = y1;
    }

    public T getY2() {
        return y2;
    }

    public void setY2(T y2) {
        this.y2 = y2;
    }
}
