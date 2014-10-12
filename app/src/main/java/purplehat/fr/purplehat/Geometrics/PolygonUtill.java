package purplehat.fr.purplehat.Geometrics;

import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by vcaen on 11/10/2014.
 */
public class PolygonUtill {

    public static Edge[] edgesFromRectangle(Rect rect) {
        Edge[] edges = new Edge[4];
        edges[1] = new Edge(new PointF(rect.left, rect.bottom), new PointF(rect.left, rect.top));
        edges[0] = new Edge(new PointF(rect.left, rect.top), new PointF(rect.right, rect.top));
        edges[2] = new Edge(new PointF(rect.right, rect.top), new PointF(rect.right, rect.bottom));
        edges[3] = new Edge(new PointF(rect.right, rect.bottom), new PointF(rect.left, rect.bottom));
        return edges;
    }

    public static Edge[] borderOfRectangleUnion(Rect... rects) {
        ArrayList<Edge> edges = new ArrayList<Edge>();
        for(Rect rect : rects) {
              for(Edge edge : edgesFromRectangle(rect)) {
                 edges.add(edge);
              }
        }

        Collections.sort(edges);
        ArrayList<PointF> pointFs = new ArrayList<PointF>();
        for(int i=0; i < edges.size() - 1 ;) {
            if(edges.get(i).equals(edges.get(i+1)) ) {
                edges.remove(i+1);
                edges.remove(i);
            } else {
                i++;
            }

        }

        return edges.toArray(new Edge[edges.size()]);
    }
}
