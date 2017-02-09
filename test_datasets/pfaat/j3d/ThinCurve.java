package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.*;


/**
 * Java3d object for interpolated curves.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class ThinCurve extends Shape3D {

    public ThinCurve(Point3d[] points, Color3f[] colors, int knots) {
        init(points, colors, knots);
    }

    public ThinCurve(Point3d[] points, Color3f color) {
        Color3f[] colors = new Color3f[points.length];

        Arrays.fill(colors, color);
        init(points, colors, 0);
    }

    private void init(Point3d[] points, Color3f[] colors, int knots) {
        int num = points.length;
        int num_intp = (num - 1) * (1 + knots) + 1;
        int[] strips = {num_intp};
        LineStripArray geom = new LineStripArray(num_intp, 
                LineStripArray.COORDINATES 
                | LineStripArray.COLOR_3, 
                strips);
        double[] eval_points = new double[num_intp];
        Color3f[] intp_colors = new Color3f[num_intp];

        double knot_d = (double) knots;

        for (int k = 0; k < num - 1; k++) {
            int base_index = k * (1 + knots);

            for (int l = 0; l < knots + 1; l++) {
		
                // linearly interp colors
                double weight1, weight2;

                weight1 = ((knot_d + 1) - ((double) l)) / (knot_d + 1);
                weight2 = ((double) l) / (knot_d + 1);
                Color3f c1 = new Color3f(colors[k]);

                c1.scale((float) weight1);
                c1.scaleAdd((float) weight2, colors[k + 1], c1);
                intp_colors[base_index + l] = c1;
		
                // set up eval points
                double offset = ((double) l) / (knot_d + 1.0);

                eval_points[base_index + l] = offset + k;
            }
        }

        intp_colors[num_intp - 1] = colors[num - 1];
        eval_points[num_intp - 1] = num - 1;
	
        InterpolatedCurve curve = new InterpolatedCurve(points);
        Point3d[] intp_points = curve.eval(eval_points);

        geom.setCoordinates(0, intp_points);
        geom.setColors(0, intp_colors);
        setGeometry(geom);
    }
}
