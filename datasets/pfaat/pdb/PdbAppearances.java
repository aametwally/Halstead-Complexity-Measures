package com.neogenesis.pfaat.pdb;


import javax.media.j3d.*;
import javax.vecmath.*;


/**
 * Interface to standard appearances used
 * @author  $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $
 */
public class PdbAppearances {

    /** Creates new Appearances */
    private PdbAppearances() {}

    public static Appearance getShinyAppearance(Color3f color) {
        Appearance appearance = new Appearance();
        Material material = new Material();

        material.setShininess(127.0f);
        material.setEmissiveColor(0.2f, 0.2f, 0.2f);
        material.setAmbientColor(color);
        material.setDiffuseColor(color);
        appearance.setMaterial(material);
        PolygonAttributes poly = new PolygonAttributes();

        poly.setCullFace(PolygonAttributes.CULL_NONE);
        poly.setBackFaceNormalFlip(true);
        appearance.setPolygonAttributes(poly);
        return appearance;
    }

    public static Appearance getLineAppearance(float line_width) {
        Appearance app = new Appearance();

        app.setLineAttributes(new LineAttributes(line_width,
                LineAttributes.PATTERN_SOLID, 
                true));
        return app;
    }
    
    public static Appearance getShinyAppearance() {
        Appearance app = new Appearance();
        Material mat = new Material();

        mat.setShininess(127.0f);
        mat.setEmissiveColor(0.2f, 0.2f, 0.2f);
        app.setMaterial(mat);
        PolygonAttributes poly = new PolygonAttributes();

        poly.setCullFace(PolygonAttributes.CULL_NONE);
        poly.setBackFaceNormalFlip(true);
        app.setPolygonAttributes(poly);
        return app;
    }

}
