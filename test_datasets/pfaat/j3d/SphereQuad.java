package com.neogenesis.pfaat.j3d;


import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;


/**
 * A graphical primitive representing a three dimensional sphere
 * rendered from quadrilaterals.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:29:08 $ */

public class SphereQuad extends TransformGroup {
    // geometry cache
    private static HashMap geomCache = new HashMap();

    /**
     * Constructs sphere.
     *
     * @param center the sphere center
     * @param radius the sphere radius
     * @param divisions the number of divisions (higher is better
     * resolution but slower rendering)
     * @param ap the sphere apperance
     * @param capabilities An array of capabilities to apply to the Shape3D.
     *                     May be null.
     */
    public SphereQuad(Point3d center,
        double radius,
        int divisions,
        Appearance ap,
        int capabilities[]) {
        Transform3D t1 = new Transform3D();

        t1.set(radius, new Vector3d(center));
        setTransform(t1);

        Integer nd = new Integer(divisions);
        Geometry g = (Geometry) geomCache.get(nd);

        if (g == null) {
            g = createGeometry(divisions);
            geomCache.put(nd, g);
        }

        Shape3D s = new Shape3D((Geometry) g.cloneNodeComponent(), ap);
	
        if (capabilities != null) {
            for (int i = 0; i < capabilities.length; i++)
                s.setCapability(capabilities[i]);
        }
	
        addChild(s);
    }

    /**
     * Constructs sphere.
     *
     * @param center the sphere center
     * @param radius the sphere radius
     * @param divisions the number of divisions (higher is better
     * resolution but slower rendering)
     * @param ap the sphere apperance
     */
    public SphereQuad(Point3d center,
        double radius,
        int divisions,
        Appearance ap) {
        this(center, radius, divisions, ap, null);
    }

    // create the geometry for a sphere of radius 1 at the origin
    private Geometry createGeometry(int divisions) {
        float scaledVerts[] = new float[divisions * divisions * 12];
        float normals[] = new float[divisions * divisions * 12];

        double dtheta = 2.0 * Math.PI / (double) divisions;
        double drho = Math.PI / (double) divisions;
        int cnt = 0;

        for (int i = 0; i < divisions; i++) {
            double rho = i * drho;
            float sinrho = (float) Math.sin(rho);
            float cosrho = (float) Math.cos(rho);
            float sinrhoplus = (float) Math.sin(rho + drho);
            float cosrhoplus = (float) Math.cos(rho + drho);

            for (int j = 0; j < divisions; j++) {
                double theta = j * dtheta;
                float sintheta = (float) Math.sin(theta);
                float costheta = (float) Math.cos(theta);
                double thetaplus = j != divisions - 1 ? 
                    theta + dtheta : 0.0;
                float sinthetaplus = (float) Math.sin(thetaplus);
                float costhetaplus = (float) Math.cos(thetaplus);

                float vx = -sintheta * sinrho;
                float vy = costheta * sinrho;
                float vz = cosrho;

                normals[cnt] = vx;
                scaledVerts[cnt++] = vx;
                normals[cnt] = vy;
                scaledVerts[cnt++] = vy;
                normals[cnt] = vz;
                scaledVerts[cnt++] = vz;

                vx = -sintheta * sinrhoplus;
                vy = costheta * sinrhoplus;
                vz = cosrhoplus;
		
                normals[cnt] = vx;
                scaledVerts[cnt++] = vx;
                normals[cnt] = vy;
                scaledVerts[cnt++] = vy;
                normals[cnt] = vz;
                scaledVerts[cnt++] = vz;

                vx = -sinthetaplus * sinrhoplus;
                vy = costhetaplus * sinrhoplus;
                vz = cosrhoplus;
		
                normals[cnt] = vx;
                scaledVerts[cnt++] = vx;
                normals[cnt] = vy;
                scaledVerts[cnt++] = vy;
                normals[cnt] = vz;
                scaledVerts[cnt++] = vz;
		
                vx = -sinthetaplus * sinrho;
                vy = costhetaplus * sinrho;
                vz = cosrho;
		
                normals[cnt] = vx;
                scaledVerts[cnt++] = vx;
                normals[cnt] = vy;
                scaledVerts[cnt++] = vy;
                normals[cnt] = vz;
                scaledVerts[cnt++] = vz;
            }
        }
	
        GeometryArray sphere;

        sphere = new QuadArray(divisions * divisions * 4, 
                    QuadArray.COORDINATES 
                    | QuadArray.NORMALS);
        sphere.setCoordinates(0, scaledVerts);
        sphere.setNormals(0, normals);

        return sphere;
    }
}
