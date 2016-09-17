package com.neogenesis.pfaat.j3d;


import java.awt.Font;
import javax.media.j3d.*;
import javax.vecmath.*;


/**
 * A graphical primitive representing a three dimensional axes.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:29:08 $ */
public class Axes extends TransformGroup {
    private RotatingText3D x_txt_tg, y_txt_tg, z_txt_tg;

    private static final float[] edgeverts = {
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
        };

    private static Geometry edgegeom = null;

    /**
     * Construct axes with the specified scale.  
     */
    public Axes(Point3d pos, double scale, boolean label, Appearance app) {
        Transform3D t1 = new Transform3D();

        t1.set(scale, new Vector3d(pos));
        setTransform(t1);

        if (edgegeom == null) {
            LineArray edges = new LineArray(24, LineArray.COORDINATES);

            edges.setCoordinates(0, edgeverts);
            edgegeom = edges;
        }
        Shape3D s = new Shape3D((Geometry) edgegeom.cloneNodeComponent(), app);

        addChild(s);

        if (label) {
            Font3D f3d = new Font3D(new Font("TestFont", Font.PLAIN, 1),
                    new FontExtrusion());
            Appearance f3d_app = new Appearance();
            Material mm = new Material();

            mm.setLightingEnable(true);
            f3d_app.setMaterial(mm);
	    
            x_txt_tg = new RotatingText3D(new Point3d(1.1, 0.0, 0.0),
                        0.1,
                        true,
                        "X",
                        f3d,
                        f3d_app);
            addChild(x_txt_tg);

            y_txt_tg = new RotatingText3D(new Point3d(0.0, 1.1, 0.0),
                        0.1,
                        true,
                        "Y",
                        f3d,
                        f3d_app);
            addChild(y_txt_tg);

            z_txt_tg = new RotatingText3D(new Point3d(0.0, 0.0, 1.1),
                        0.1,
                        true,
                        "Z",
                        f3d,
                        f3d_app);
            addChild(z_txt_tg);

        }
	
    }

    public RotatingText3D getXLabel() {
        return x_txt_tg;
    }

    public RotatingText3D getYLabel() {
        return y_txt_tg;
    }

    public RotatingText3D getZLabel() {
        return z_txt_tg;
    }

}
