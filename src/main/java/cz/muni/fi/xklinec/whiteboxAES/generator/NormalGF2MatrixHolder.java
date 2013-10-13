/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;

/**
 *
 * @author ph4r05
 */
public class NormalGF2MatrixHolder {
    private long rank;
    private long detetrminant;
    private GF2MatrixEx P;
    private GF2MatrixEx Q;

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public long getDetetrminant() {
        return detetrminant;
    }

    public void setDetetrminant(long detetrminant) {
        this.detetrminant = detetrminant;
    }

    public GF2MatrixEx getP() {
        return P;
    }

    public void setP(GF2MatrixEx P) {
        this.P = P;
    }

    public GF2MatrixEx getQ() {
        return Q;
    }

    public void setQ(GF2MatrixEx Q) {
        this.Q = Q;
    }
}
