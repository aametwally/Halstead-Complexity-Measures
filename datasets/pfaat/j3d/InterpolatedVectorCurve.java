package com.neogenesis.pfaat.j3d;


import javax.vecmath.*;


/**
 * Interpolated curve in R^3.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class InterpolatedVectorCurve {
    private Spline x, y, z;

    public InterpolatedVectorCurve(Vector3d[] points) {
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

    public Vector3d eval(double t) {
        return new Vector3d(x.eval(t), y.eval(t), z.eval(t));
    }

    public Vector3d[] eval(double[] t) {
        int size = t.length;
        Vector3d[] result = new Vector3d[size];

        for (int i = 0; i < size; i++)
            result[i] = eval(t[i]);
        return result;
    }
}
