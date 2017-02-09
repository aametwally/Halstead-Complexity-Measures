package com.neogenesis.pfaat.j3d;


import java.awt.Font;
import javax.media.j3d.*;
import javax.vecmath.*;


/**
 * A graphical primitive representing a 3D label which can rotate to align to 
 * the view plane..
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class RotatingText3D extends TransformGroup {
    public RotatingText3D(Point3d pos, double scale, 
        boolean center_text,
        String text, Font3D f3d, Appearance app) {
        Text3D txt = new Text3D(f3d,
                text,
                center_text 
                ? new Point3f(0.0f, -0.5f, -0.5f)
                : new Point3f(1.0f, 0.0f, -0.5f),
                center_text 
                ? Text3D.ALIGN_CENTER 
                : Text3D.ALIGN_FIRST,
                Text3D.PATH_RIGHT);	    
        Shape3D sh = new Shape3D();

        sh.setGeometry(txt);
        sh.setAppearance(app);
        Transform3D t3d_scale = new Transform3D();

        t3d_scale.setScale(scale);
        TransformGroup tg_scale = new TransformGroup(t3d_scale);

        tg_scale.addChild(sh);

        Transform3D t3d_pos = new Transform3D();

        t3d_pos.setTranslation(new Vector3d(pos));
	
        setTransform(t3d_pos);
        setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
        addChild(tg_scale);
    }

    // temporary variable used for alignment
    private Matrix3d image2world = new Matrix3d();
    private Matrix3d world2local = new Matrix3d();
    private Transform3D temp_t3d = new Transform3D();

    public void alignToImagePlate(Transform3D world2image_t3d) {
        // compute image2world
        world2image_t3d.get(image2world);
        image2world.transpose();
        // compute world2local
        getLocalToVworld(temp_t3d);
        temp_t3d.get(world2local);
        world2local.transpose();
        // compute product
        image2world.mul(world2local);
        // set the rotation
        getTransform(temp_t3d);
        temp_t3d.setRotation(image2world);
        setTransform(temp_t3d);
    }
	
}
