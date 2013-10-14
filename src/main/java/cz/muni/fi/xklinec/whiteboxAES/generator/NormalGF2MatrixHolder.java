/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

/**
 *
 * @author ph4r05
 */
public class NormalGF2MatrixHolder {
    private int rank;
    private int detetrminant;
    private GF2MatrixEx P;
    private GF2MatrixEx Q;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getDetetrminant() {
        return detetrminant;
    }

    public void setDetetrminant(int detetrminant) {
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
