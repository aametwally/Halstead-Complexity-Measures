package com.neogenesis.pfaat.util;


import Jama.*;
import Jama.util.*;


/** Eigenvalues and eigenvectors of a real matrix. 
 * The matrix is assumed to be symmetric and square.
 **/

public class SymmetricEigenvalueDecomposition {

    /* ------------------------
     Class variables
     * ------------------------ */

    /** Row and column dimension (square matrix).
     @serial matrix dimension.
     */
    private int n;
    
    /** Arrays for internal storage of eigenvalues.
     @serial internal storage of eigenvalues.
     */
    private double[] d;
    private double[] e;

    /** Array for internal storage of eigenvectors.
     @serial internal storage of eigenvectors.
     */
    private double[][] V;

    /* ------------------------
     Private Methods
     * ------------------------ */

    // Symmetric Householder reduction to tridiagonal form.

    private void tred2(IterationListener il) throws Exception {

        // This is derived from the Algol procedures tred2 by
        // Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        // Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        // Fortran subroutine in EISPACK.

        for (int j = 0; j < n; j++) {
            d[j] = V[n - 1][j];
        }

        // Householder reduction to tridiagonal form.
   
        for (int i = n - 1; i > 0; i--) {
   
            // Scale to avoid under/overflow.
   
            double scale = 0.0;
            double h = 0.0;

            for (int k = 0; k < i; k++) {
                scale = scale + Math.abs(d[k]);
            }
            if (scale == 0.0) {
                e[i] = d[i - 1];
                for (int j = 0; j < i; j++) {
                    d[j] = V[i - 1][j];
                    V[i][j] = 0.0;
                    V[j][i] = 0.0;
                }
            } else {
   
                // Generate Householder vector.
   
                for (int k = 0; k < i; k++) {
                    d[k] /= scale;
                    h += d[k] * d[k];
                }
                double f = d[i - 1];
                double g = Math.sqrt(h);

                if (f > 0) {
                    g = -g;
                }
                e[i] = scale * g;
                h = h - f * g;
                d[i - 1] = f - g;
                for (int j = 0; j < i; j++) {
                    e[j] = 0.0;
                }
   
                // Apply similarity transformation to remaining columns.
   
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    V[j][i] = f;
                    g = e[j] + V[j][j] * f;
                    for (int k = j + 1; k <= i - 1; k++) {
                        g += V[k][j] * d[k];
                        e[k] += V[k][j] * f;
                    }
                    e[j] = g;
                }
                f = 0.0;
                for (int j = 0; j < i; j++) {
                    e[j] /= h;
                    f += e[j] * d[j];
                }
                double hh = f / (h + h);

                for (int j = 0; j < i; j++) {
                    e[j] -= hh * d[j];
                }
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    g = e[j];
                    for (int k = j; k <= i - 1; k++) {
                        V[k][j] -= (f * e[k] + g * d[k]);
                    }
                    d[j] = V[i - 1][j];
                    V[i][j] = 0.0;
                }
            }
            d[i] = h;

            if (il != null) il.finishedThisIteration();
        }
   
        // Accumulate transformations.
   
        for (int i = 0; i < n - 1; i++) {
            V[n - 1][i] = V[i][i];
            V[i][i] = 1.0;
            double h = d[i + 1];

            if (h != 0.0) {
                for (int k = 0; k <= i; k++) {
                    d[k] = V[k][i + 1] / h;
                }
                for (int j = 0; j <= i; j++) {
                    double g = 0.0;

                    for (int k = 0; k <= i; k++) {
                        g += V[k][i + 1] * V[k][j];
                    }
                    for (int k = 0; k <= i; k++) {
                        V[k][j] -= g * d[k];
                    }
                }
            }
            for (int k = 0; k <= i; k++) {
                V[k][i + 1] = 0.0;
            }

            if (il != null) il.finishedThisIteration();
        }
        for (int j = 0; j < n; j++) {
            d[j] = V[n - 1][j];
            V[n - 1][j] = 0.0;
        }
        V[n - 1][n - 1] = 1.0;
        e[0] = 0.0;
    } 

    // Symmetric tridiagonal QL algorithm.
   
    private void tql2(IterationListener il) throws Exception {

        // This is derived from the Algol procedures tql2, by
        // Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        // Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        // Fortran subroutine in EISPACK.
   
        for (int i = 1; i < n; i++) {
            e[i - 1] = e[i];
        }
        e[n - 1] = 0.0;
   
        double f = 0.0;
        double tst1 = 0.0;
        double eps = Math.pow(2.0, -52.0);

        for (int l = 0; l < n; l++) {

            // Find small subdiagonal element
   
            tst1 = Math.max(tst1, Math.abs(d[l]) + Math.abs(e[l]));
            int m = l;

            while (m < n) {
                if (Math.abs(e[m]) <= eps * tst1) {
                    break;
                }
                m++;
            }
   
            // If m == l, d[l] is an eigenvalue,
            // otherwise, iterate.
   
            if (m > l) {
                int iter = 0;

                do {
                    iter = iter + 1;  // (Could check iteration count here.)
   
                    // Compute implicit shift
   
                    double g = d[l];
                    double p = (d[l + 1] - g) / (2.0 * e[l]);
                    double r = Maths.hypot(p, 1.0);

                    if (p < 0) {
                        r = -r;
                    }
                    d[l] = e[l] / (p + r);
                    d[l + 1] = e[l] * (p + r);
                    double dl1 = d[l + 1];
                    double h = g - d[l];

                    for (int i = l + 2; i < n; i++) {
                        d[i] -= h;
                    }
                    f = f + h;
   
                    // Implicit QL transformation.
   
                    p = d[m];
                    double c = 1.0;
                    double c2 = c;
                    double c3 = c;
                    double el1 = e[l + 1];
                    double s = 0.0;
                    double s2 = 0.0;

                    for (int i = m - 1; i >= l; i--) {
                        c3 = c2;
                        c2 = c;
                        s2 = s;
                        g = c * e[i];
                        h = c * p;
                        r = Maths.hypot(p, e[i]);
                        e[i + 1] = s * r;
                        s = e[i] / r;
                        c = p / r;
                        p = c * d[i] - s * g;
                        d[i + 1] = h + s * (c * g + s * d[i]);
   
                        // Accumulate transformation.
   
                        for (int k = 0; k < n; k++) {
                            h = V[k][i + 1];
                            V[k][i + 1] = s * V[k][i] + c * h;
                            V[k][i] = c * V[k][i] - s * h;
                        }
                    }
                    p = -s * s2 * c3 * el1 * e[l] / dl1;
                    e[l] = s * p;
                    d[l] = c * p;
   
                    // Check for convergence.
   
                } while (Math.abs(e[l]) > eps * tst1);
            }
            d[l] = d[l] + f;
            e[l] = 0.0;

            if (il != null) il.finishedThisIteration();
        }
     
    }

    // Sort eigenvalues and corresponding vectors.
    private void sort(IterationListener il) throws Exception {
        for (int i = 0; i < n - 1; i++) {
            int k = i;
            double p = d[i];

            for (int j = i + 1; j < n; j++) {
                if (d[j] < p) {
                    k = j;
                    p = d[j];
                }
            }
            if (k != i) {
                d[k] = d[i];
                d[i] = p;
                for (int j = 0; j < n; j++) {
                    p = V[j][i];
                    V[j][i] = V[j][k];
                    V[j][k] = p;
                }
            }
        }

        if (il != null)	il.finishedThisIteration();
    }

    /* ------------------------
     Constructor
     * ------------------------ */

    public SymmetricEigenvalueDecomposition(int n) {
        this.n = n;
    }

    public int getTotalIterations() {
        return 2 * (n - 1) + n + 1;
    }

    // computer the eigenvalues
    public void compute(Matrix Arg, IterationListener il) throws Exception {
        double[][] A = Arg.getArray();

        if (n != A.length) 
            throw new Exception("input matrix has incorrect size");
        V = new double[n][n];
        d = new double[n];
        e = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                V[i][j] = A[i][j];
                if (A[i][j] != A[j][i])
                    throw new Exception("input matrix is a symmetric");
            }
        }

        // Tridiagonalize.
        tred2(il);
      
        // Diagonalize.
        tql2(il);

        // sort eigenvalues
        sort(il);
    }

    /* ------------------------
     Public Methods
     * ------------------------ */

    /** Return the eigenvector matrix
     @return     V
     */

    public Matrix getV() {
        return new Matrix(V, n, n);
    }

    /** Return the  eigenvalues
     */

    public double[] getEigenvalues() {
        return d;
    }

    /** Return the block diagonal eigenvalue matrix
     @return     D
     */

    public Matrix getD() {
        Matrix X = new Matrix(n, n);
        double[][] D = X.getArray();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                D[i][j] = 0.0;
            }
            D[i][i] = d[i];
        }
        return X;
    }
}
