package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import javax.vecmath.*;


/**
 * A graphical primitive representing a range bound by four rays.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class RayRange extends Shape3D {
    public RayRange(PickRay ray1, PickRay ray2,
        PickRay ray3, PickRay ray4,
        double max_length,
        Appearance app) {
        Point3d origin = new Point3d();
        Vector3d v1 = new Vector3d();

        ray1.get(origin, v1);
        Vector3d v2 = new Vector3d();

        ray2.get(origin, v2);
        Vector3d v3 = new Vector3d();

        ray3.get(origin, v3);
        Vector3d v4 = new Vector3d();

        ray4.get(origin, v4);

        // double[] vertices = new double [48];
        double[] vertices = new double[24];
        int cnt = 0;

        Vector3d nv = new Vector3d();

        // nv.set(v1);
        // nv.normalize();
        // nv.scale(max_length);
        // nv.add(origin);
        // vertices[cnt++] = origin.x;
        // vertices[cnt++] = origin.y;
        // vertices[cnt++] = origin.z;
        // vertices[cnt++] = nv.x;
        // vertices[cnt++] = nv.y;
        // vertices[cnt++] = nv.z;

	
        // nv.set(v2);
        // nv.normalize();
        // nv.scale(max_length);
        // nv.add(origin);
        // vertices[cnt++] = origin.x;
        // vertices[cnt++] = origin.y;
        // vertices[cnt++] = origin.z;
        // vertices[cnt++] = nv.x;
        // vertices[cnt++] = nv.y;
        // vertices[cnt++] = nv.z;

        // nv.set(v3);
        // nv.normalize();
        // nv.scale(max_length);
        // nv.add(origin);
        // vertices[cnt++] = origin.x;
        // vertices[cnt++] = origin.y;
        // vertices[cnt++] = origin.z;
        // vertices[cnt++] = nv.x;
        // vertices[cnt++] = nv.y;
        // vertices[cnt++] = nv.z;

        // nv.set(v4);
        // nv.normalize();
        // nv.scale(max_length);
        // nv.add(origin);
        // vertices[cnt++] = origin.x;
        // vertices[cnt++] = origin.y;
        // vertices[cnt++] = origin.z;
        // vertices[cnt++] = nv.x;
        // vertices[cnt++] = nv.y;
        // vertices[cnt++] = nv.z;

        vertices[cnt++] = origin.x + v1.x;
        vertices[cnt++] = origin.y + v1.y;
        vertices[cnt++] = origin.z + v1.z;
        vertices[cnt++] = origin.x + v2.x;
        vertices[cnt++] = origin.y + v2.y;
        vertices[cnt++] = origin.z + v2.z;

        vertices[cnt++] = origin.x + v2.x;
        vertices[cnt++] = origin.y + v2.y;
        vertices[cnt++] = origin.z + v2.z;
        vertices[cnt++] = origin.x + v3.x;
        vertices[cnt++] = origin.y + v3.y;
        vertices[cnt++] = origin.z + v3.z;

        vertices[cnt++] = origin.x + v3.x;
        vertices[cnt++] = origin.y + v3.y;
        vertices[cnt++] = origin.z + v3.z;
        vertices[cnt++] = origin.x + v4.x;
        vertices[cnt++] = origin.y + v4.y;
        vertices[cnt++] = origin.z + v4.z;

        vertices[cnt++] = origin.x + v4.x;
        vertices[cnt++] = origin.y + v4.y;
        vertices[cnt++] = origin.z + v4.z;
        vertices[cnt++] = origin.x + v1.x;
        vertices[cnt++] = origin.y + v1.y;
        vertices[cnt++] = origin.z + v1.z;

        if (cnt != vertices.length)
            throw new RuntimeException("geometry size mismatch in RayRange");

        LineArray edges = new LineArray(vertices.length / 2, 
                LineArray.COORDINATES);

        edges.setCoordinates(0, vertices);
        setGeometry(edges);
        setAppearance(app);
    }
}
