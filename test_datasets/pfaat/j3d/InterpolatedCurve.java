package com.neogenesis.pfaat.j3d;


import javax.vecmath.*;


/**
 * A curve interpolated in R^3.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class InterpolatedCurve {
    Spline x, y, z;
    private static final double DT = 1.0e-6;
    
    public InterpolatedCurve(Point3d[] points) {
        int num = points.length;
        double[] x_vals = new double[num];
        double[] y_vals = new double[num];
        double[] z_vals = new double[num];

        for (int i = 0; i < num; i++) {
            x_vals[i] = points[i].x;
            y_vals[i] = points[i].y;
            z_vals[i] = points[i].z;
        }
        x = new Spline(x_vals);
        y = new Spline(y_vals);
        z = new Spline(z_vals);
    }
    
    public Point3d eval(double t) {
        return new Point3d(x.eval(t), y.eval(t), z.eval(t));
    }
    
    public Vector3d velocity(double t) {
        return new Vector3d(x.deriv(t), y.deriv(t), z.deriv(t));
    }
    
    public Vector3d tangent(double t) {
        Vector3d vect = velocity(t);

        vect.normalize();
        return vect;
    }
    
    public Vector3d normal(double t) {
        Vector3d t1 = velocity(t + DT);

        t1.sub(velocity(t - DT));
        t1.normalize();
        return t1;
    }
    
    public Point3d[] eval(double[] t) {
        int size = t.length;
        Point3d[] result = new Point3d[size];

        for (int i = 0; i < size; i++)
            result[i] = eval(t[i]);
        return result;
    }
    
    public Vector3d[] velocity(double[] t) {
        int size = t.length;
        Vector3d[] result = new Vector3d[size];

        for (int i = 0; i < size; i++)
            result[i] = velocity(t[i]);
        return result;
    }
    
    public Vector3d[] tangent(double[] t) {
        int size = t.length;
        Vector3d[] result = new Vector3d[size];

        for (int i = 0; i < size; i++)
            result[i] = tangent(t[i]);
        return result;
    }
    
    public Vector3d[] normal(double[] t) {
        int size = t.length;
        Vector3d[] result = new Vector3d[size];

        for (int i = 0; i < size; i++)
            result[i] = normal(t[i]);
        return result;
    }

}
