package com.neogenesis.pfaat.j3d;


import java.util.*;
import javax.media.j3d.PickRay;
import javax.vecmath.*;


/**
 * Class for spacially selecting from a list of points.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class PointSet {
    private boolean[] is_visible;
    private Point3d[] points;
    private double distance_threshold2;

    public PointSet(double distance_threshold) { 
        this.distance_threshold2 = distance_threshold * distance_threshold;
    }

    public int size() {
        return points.length;
    }

    public boolean isVisible(int i) {
        return is_visible[i];
    }

    public Point3d getPoint(int i) {
        return points[i];
    }

    public void set(Point3d[] points, boolean[] is_visible) {
        this.points = points;
        this.is_visible = is_visible;
    }

    public void clear() {
        is_visible = null;
        points = null; 
    }

    // get the index to the closest point to a ray
    public int getClosestPoint(PickRay ray) {
        if (points == null || is_visible == null)
            return -1;
	
        Vector3d direction = new Vector3d();
        Point3d ray_origin = new Point3d();
        Vector3d ray_direction = new Vector3d();

        ray.get(ray_origin, ray_direction);
        ray_direction.normalize();
	
        double minimum_distance2 = Double.POSITIVE_INFINITY;
        int minimum_index = -1;
	
        for (int i = points.length - 1; i >= 0; i--) {
            if (is_visible[i]) {
                direction.sub(points[i], ray_origin);
                double dot = direction.dot(ray_direction);

                if (dot >= 0.0) {
                    double dist2 = direction.lengthSquared();

                    if (dist2 < minimum_distance2 && 
                        dist2 - dot * dot < distance_threshold2) {
                        minimum_distance2 = dist2;
                        minimum_index = i;
                    }
                }
            }
        }

        return minimum_index;
    }
    
    public int[] getPointsInRegion(PickRay ray1, PickRay ray2, 
        PickRay ray3, PickRay ray4) {
        if (points == null || is_visible == null)
            return null;

        Point3d ray1_origin = new Point3d();
        Vector3d ray1_direction = new Vector3d();

        ray1.get(ray1_origin, ray1_direction);
	
        Point3d ray2_origin = new Point3d();
        Vector3d ray2_direction = new Vector3d();

        ray2.get(ray2_origin, ray2_direction);
	
        Point3d ray3_origin = new Point3d();
        Vector3d ray3_direction = new Vector3d();

        ray3.get(ray3_origin, ray3_direction);
	
        Point3d ray4_origin = new Point3d();
        Vector3d ray4_direction = new Vector3d();

        ray4.get(ray4_origin, ray4_direction);
	
        if (ray1_origin.distanceSquared(ray2_origin) > 1e-8
            || ray1_origin.distanceSquared(ray3_origin) > 1e-8
            || ray1_origin.distanceSquared(ray4_origin) > 1e-8)
            throw new RuntimeException("rays have differing origins");	
	    
        Vector3d cross1 = new Vector3d();

        cross1.cross(ray1_direction, ray2_direction);
        if (cross1.lengthSquared() < 1e-8)
            return null;

        Vector3d cross2 = new Vector3d();

        cross2.cross(ray2_direction, ray3_direction);
        if (cross2.lengthSquared() < 1e-8)
            return null;

        Vector3d cross3 = new Vector3d();

        cross3.cross(ray3_direction, ray4_direction);
        if (cross3.lengthSquared() < 1e-8)
            return null;

        Vector3d cross4 = new Vector3d();

        cross4.cross(ray4_direction, ray1_direction);	
        if (cross4.lengthSquared() < 1e-8)
            return null;
	
        Vector3d direction = new Vector3d();
        List selected = new ArrayList();

        for (int i = points.length - 1; i >= 0; i--) {
            if (is_visible[i]) {
                direction.sub(points[i], ray1_origin);
                if (direction.dot(cross1) >= 0.0
                    && direction.dot(cross2) >= 0.0
                    && direction.dot(cross3) >= 0.0
                    && direction.dot(cross4) >= 0.0) 
                    selected.add(new Integer(i));
            }
        }
    
        if (selected.size() < 1) return null;

        int[] idxs = new int[selected.size()];

        for (int i = idxs.length - 1; i >= 0; i--)
            idxs[i] = ((Integer) selected.get(i)).intValue();

        return idxs;    
    }
}
