/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES.generator;

/**
 *
 * @author ph4r05
 */
public interface IOEncoding {
    int allocate(int idx);
    int getIOInputWidth();
    int getIOOutputWidth();
    int getIOInputSlots();
}
