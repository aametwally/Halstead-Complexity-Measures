package com.neogenesis.pfaat.j3d;


/**
 * Spline interpolation.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class Spline {
    
    static final double VALUE_TOL = 0.00000000001;
    double[] x, y, y2;
    int n;
    public boolean lower_interp, upper_interp;
    
    public Spline(double[] y_in) {
        int n = y_in.length;
        double[] x_default = new double[n];

        for (int k = 0; k < n; k++)
            x_default[k] = (double) k;
        setup(x_default, y_in);
    }
    
    public Spline(double[] x_in, double[] y_in) {
        setup(x_in, y_in);
    }
    
    private void setup(double[] x_in, double[] y_in) {
	
        int  i, k;
        double  p, qn, sig, un;
        double[] u;

        x = x_in;
        y = y_in;
        n = x.length;
        y2 = new double[n];
        lower_interp = upper_interp = true; // for now;
	
        p = qn = sig = un = 0.0;
	
        u = new double[n - 1];
        y2[0] = u[0] = 0.0;
	
        /* Decomposition loop of the tridiagonal algorithm. y2 and u are used for
         temporary storage of the decomposed factors */
        for (i = 1; i < n - 1; i++) {
            sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
            p = sig * y2[i - 1] + 2.0;
            y2[i] = (sig - 1.0) / p;
            u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
            u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
        }
        qn = un = 0.0;
        y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
	
        /* Backsubstitution loop of tridiagonal algorithm */
	
        for (k = n - 2; k >= 0; k--)
            y2[k] = y2[k] * y2[k + 1] + u[k];
    }

    public double[] eval(double[] eval_points) {
        int size = eval_points.length;
        double[] result = new double[size];

        for (int i = 0; i < size; i++)
            result[i] = eval(eval_points[i]);
        return result;
    }
    
    public double[] deriv(double[] eval_points) {
        int size = eval_points.length;
        double[] result = new double[size];

        for (int i = 0; i < size; i++)
            result[i] = deriv(eval_points[i]);
        return result;
    }

    public double eval(double eval_point) {
        int    klo, khi, k;
        double    h, b, a;
	
        /* check if we are below or above the table */
        if (!lower_interp && eval_point < x[0]) {
            return y[0];
	    
        }
        if (!upper_interp && eval_point > x[n - 1]) {
            return y[n - 1];
	    
        }

        h = b = a = 0.0;

        /* we will find the right place in the table by bisection. This is optimal
         if sequential calls to this routine are at random values of eval_point. If
         sequential calls are in order, and closely spaced, one would do better
         by storing previous values of klo and khi, and test if they remain
         appropriate on the next call */
	
        klo = 0;
        khi = n - 1;
        while (khi - klo > 1) {
            k = (khi + klo) >> 1;
            if (x[k] > eval_point)
                khi = k;
            else
                klo = k;
        }
	
        /* klo and khi now bracket the input value eval_point */
	
        h = x[khi] - x[klo];
	
        /* The x's must be distinct */
	
        a = (Math.abs(h) > VALUE_TOL ? (x[khi] - eval_point) / h : 0.0);
        b = (Math.abs(h) > VALUE_TOL ? (eval_point - x[klo]) / h : 0.0);
	
        /* Cubic spline polynomial is now evaluated */
	
        return a * y[klo] + b * y[khi] +
            ((a * a * a - a) * y2[klo] + (b * b * b - b) * y2[khi]) * (h * h) / 6.0;
	
    }
    
    /* calculate the derivative of a spline interpolated function */
    
    public double deriv(double eval_point) {
        int    klo, khi, k;
        double    h, b, a;
	
        /* check if we are below or above the table */
        if (!lower_interp && eval_point < x[0]) {
            return 0.0;
	    
        }
        if (!upper_interp && eval_point > x[n - 1]) {
            return 0.0;
        }

        h = b = a = 0.0;

        /* we will find the right place in the table by bisection. This is optimal
         if sequential calls to this routine are at random values of eval_point. If
         sequential calls are in order, and closely spaced, one would do better
         by storing previous values of klo and khi, and test if they remain
         appropriate on the next call */
	
        klo = 0;
        khi = n - 1;
        while (khi - klo > 1) {
            k = (khi + klo) >> 1;
            if (x[k] > eval_point)
                khi = k;
            else
                klo = k;
        }
	
        /* klo and khi now bracket the input value eval_point */
	
        h = x[khi] - x[klo];
	
        /* The x's must be distinct */
        if (Math.abs(h) > VALUE_TOL) {
            a = (x[khi] - eval_point) / h;
            b = (eval_point - x[klo]) / h;
	    
            /* Cubic spline polynomial derivative is now evaluated */
	    
            return (y[khi] - y[klo]) / h +
                ((-3.0 * a * a + a) * y2[klo] + (3.0 * b * b - b) * y2[khi]) * h / 6.0;
        } else
            return 0.0;
	
    }
}

