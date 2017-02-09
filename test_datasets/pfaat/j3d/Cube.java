package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import javax.vecmath.*;


/**
 * A graphical primitive representing a three dimensional cube.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:29:08 $ */
public class Cube extends TransformGroup {
    // cube vertices
    private static final float[] verts = {
            // front face
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            // back face
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            // right face
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            // left face
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            // top face
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            // bottom face
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
        };

    // cube vertex normals
    private static final float[] vertnormals = {
            // front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            // back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            // right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            // left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            // top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            // bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
        };

    // cube edges
    private static final float[] edgeverts = {
            // front face
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            // back face
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            // cross edges
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f
        };

    // cached cube geometry
    private static Geometry edgegeom = null;
    private static Geometry facegeom = null;

    private Point3d pos = null;
    private double scale;

    /**
     * Constructs a color cube with the specified scale.  The corners of the
     * color cube are [-scale,-scale,-scale] and [scale,scale,scale].
     *
     * @param scale the scale of the cube
     * @param if <code>true</code>, cube edges only are drawn, otherwise
     * cube faces only are drawn
     * @param app the appearance to use for the cube
     */
    public Cube(double scale, boolean outline, Appearance app) {
        Transform3D t1 = new Transform3D();

        t1.set(scale);
        setTransform(t1);
	
        this.scale = scale;
	
        initGeom(outline, app, null);
	
    }
    
    /**
     * Constructs a color cube with the specified scale and offset.  
     * The corners of the color cube are [-scale,-scale,-scale] 
     * and [scale,scale,scale] in the coordinate space [-pos, -pos, -pos]
     *
     * @param pos Position of the cube
     * @param scale the scale of the cube
     * @param if <code>true</code>, cube edges only are drawn, otherwise
     * cube faces only are drawn
     * @param app the appearance to use for the cube
     * @param capabilities An array of capabilities to apply to the Shape3D.
     *                     May be null.
     */
    public Cube(Point3d pos, double scale, boolean outline, Appearance app,
        int capabilities[]) {
        Transform3D t1 = new Transform3D();

        t1.set(scale, new Vector3d(pos));
        setTransform(t1);

        this.pos = pos;
        this.scale = scale;
	
        initGeom(outline, app, capabilities);
    }
    
    public Point3d getPosition() {
        return pos;
    }
    
    public double getScale() {
        return scale;
    }
    
    private void initGeom(boolean outline, Appearance app, 
        int capabilities[]) {
        Geometry geom;

        if (outline) {
            if (edgegeom == null) {
                LineArray edges = new LineArray(24, LineArray.COORDINATES);

                edges.setCoordinates(0, edgeverts);
                edgegeom = edges;
            }
            geom = (Geometry) edgegeom.cloneNodeComponent();
        } else {
            if (facegeom == null) {
                QuadArray cube = new QuadArray(24, QuadArray.COORDINATES
                        | QuadArray.NORMALS);

                cube.setCoordinates(0, verts);
                cube.setNormals(0, vertnormals);
                facegeom = cube;
            }
            geom = (Geometry) facegeom.cloneNodeComponent();
        }

        Shape3D s = new Shape3D(geom, app);
	
        if (capabilities != null) {
            for (int i = 0; i < capabilities.length; i++)
                s.setCapability(capabilities[i]);
        }
	
        addChild(s);
    }
}
