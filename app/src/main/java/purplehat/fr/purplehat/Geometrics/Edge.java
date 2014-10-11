package purplehat.fr.purplehat.Geometrics;

import android.graphics.PointF;

/**
 * Created by vcaen on 11/10/2014.
 */
public class Edge implements  Comparable<Edge> {

    public PointF p1;
    public PointF p2;

    Edge(PointF p1, PointF p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    Edge(float x1, float y1, float x2, float y2) {
        p1 = new PointF(x1, y1);
        p2 = new PointF(x2, y2);
    }

    @Override
    public boolean equals(Object o) {
        if(! (o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        if(this.p1.equals(edge.p1.x, edge.p1.y) && this.p2.equals(edge.p2.x, edge.p2.y)
                || this.p1.equals(edge.p2.x, edge.p2.y) && this.p2.equals(edge.p1.x, edge.p1.y)) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Edge edge) {
        if(this.p1.equals(edge.p1.x, edge.p1.y) && this.p2.equals(edge.p2.x, edge.p2.y)
                || this.p1.equals(edge.p2.x, edge.p2.y) && this.p2.equals(edge.p1.x, edge.p1.y)) {
            return 0;
        } else if(this.p1.x < edge.p1.x && this.p1.y < edge.p1.y) {
            return -1;
        } else if(this.p2.x < edge.p2.x && this.p2.y < edge.p2.y) {
            return -1;
        }
        return 1;
    }
}
