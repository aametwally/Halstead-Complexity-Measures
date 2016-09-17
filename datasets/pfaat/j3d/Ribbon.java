package com.neogenesis.pfaat.j3d;


import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;
import java.io.*;


/**
 * 3D Ribbon object.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class Ribbon extends Shape3D {

    public Ribbon(Point3d[] points, 
        Color3f[] colors, 
        double[] widths,
        double[] orient, 
        int knots, 
        Appearance app, 
        boolean arrowhead) {
        int num = points.length;
        double knot_d = (double) knots;
        float knot_f = (float) knots;
        int num_intp = (num - 1) * (1 + knots) + 1;
        Color3f[] intp_colors = new Color3f[num_intp];
        double[] eval_points = new double[num_intp];

        // interp colors, setup eval points
        for (int k = 0; k < num - 1; k++) {
            int base_index = k * (1 + knots);

            for (int l = 0; l < knots + 1; l++) {
                double weight1, weight2;

                weight1 = ((knot_d + 1) - ((double) l)) / (knot_d + 1);
                weight2 = ((double) l) / (knot_d + 1);
	       
                Color3f c1 = new Color3f(colors[k]);

                // c1.scale((float) weight1);
                // c1.scaleAdd((float) weight2,colors[k+1], c1);
                if (l + 1 > (knots + 1) / 2.0)
                    intp_colors[base_index + l] = colors[k + 1];
                else
                    intp_colors[base_index + l] = colors[k];
                // set up eval points
                double offset = ((double) l) / (knot_d + 1.0);

                eval_points[base_index + l] = offset + k;
            }
        }

        intp_colors[num_intp - 1] = colors[num - 1];
        eval_points[num_intp - 1] = num - 1;
        // spline widths and coords and orient
        InterpolatedCurve curve = new InterpolatedCurve(points);
        Point3d[] intp_points = curve.eval(eval_points);
        Spline widths_spl = new Spline(widths);
        double[] intp_widths = widths_spl.eval(eval_points);
       
        if (arrowhead) {
            int base_index = (num - 2) * (1 + knots);
            double orig = intp_widths[base_index];

            for (int l = 0; l < knots + 1; l++) {
                double w1 = (knots + 1.0 - l) / (knots + 1.0);

                intp_widths[base_index + l] = orig * 2.0 * w1;
            }
            intp_widths[num_intp - 1] = 0.0001;
        }

        Vector3d[] tangent_bb = new Vector3d[num];
        Vector3d[] normal_bb = new Vector3d[num];
        Vector3d[] binormal_bb = new Vector3d[num];
        Vector3d[] to_up_bb = new Vector3d[num];

        for (int k = 0; k < num; k++) {
            tangent_bb[k] = curve.tangent((double) k);
            normal_bb[k] = curve.normal((double) k);
            binormal_bb[k] = new Vector3d();
            binormal_bb[k].cross(tangent_bb[k], normal_bb[k]);
            to_up_bb[k] = new Vector3d(binormal_bb[k]);
            to_up_bb[k].scale(orient[k]);
            to_up_bb[k].scaleAdd(1.0 - orient[k], normal_bb[k], to_up_bb[k]);
        }
       
        for (int k = 0; k < num - 1; k++) {
            if (to_up_bb[k].dot(to_up_bb[k + 1]) < 0) {
                to_up_bb[k + 1].negate();
            }
        }
       
        InterpolatedVectorCurve to_up_spl = 
            new InterpolatedVectorCurve(to_up_bb);
        Vector3d[] to_up = to_up_spl.eval(eval_points);
        Point3d[] upper_edge, lower_edge;

        upper_edge = new Point3d[num_intp];
        lower_edge = new Point3d[num_intp];
        for (int k = 0; k < num_intp; k++) {
            Point3d p1, p2;

            upper_edge[k] = new Point3d();
            lower_edge[k] = new Point3d();
            upper_edge[k].scaleAdd(intp_widths[k] / 2.0, to_up[k], intp_points[k]);
            lower_edge[k].scaleAdd(-intp_widths[k] / 2.0, to_up[k], intp_points[k]);
        }
        // end set up edges
       
       
        // set up geometry info
        int num_poly = 2 * (knots + 1) * (num - 1);
        int num_verts = num_poly * 3;
        Point3d[] ribbon_edge = new Point3d[num_verts];
        Color3f[] ribbon_colors = new Color3f[num_verts];

        for (int k = 0; k < num_intp - 1; k++) {
            ribbon_edge[k * 6 + 0] = upper_edge[k];
            ribbon_edge[k * 6 + 1] = upper_edge[k + 1];
            ribbon_edge[k * 6 + 2] = lower_edge[k + 1];
            ribbon_edge[k * 6 + 3] = lower_edge[k + 1];
            ribbon_edge[k * 6 + 4] = lower_edge[k];
            ribbon_edge[k * 6 + 5] = upper_edge[k];
	   
            ribbon_colors[k * 6 + 0] = intp_colors[k];
            ribbon_colors[k * 6 + 1] = intp_colors[k + 1];
            ribbon_colors[k * 6 + 2] = intp_colors[k + 1];
            ribbon_colors[k * 6 + 3] = intp_colors[k + 1];
            ribbon_colors[k * 6 + 4] = intp_colors[k];
            ribbon_colors[k * 6 + 5] = intp_colors[k];
        }
        GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);

        gi.setCoordinates(ribbon_edge);
        gi.setColors(ribbon_colors);
        gi.recomputeIndices();
       
        NormalGenerator ng = new NormalGenerator(Math.PI);

        ng.generateNormals(gi);
        gi.recomputeIndices();
       
        Geometry geom = gi.getGeometryArray();

        setAppearance(app);
        setGeometry(geom);
    }
}
