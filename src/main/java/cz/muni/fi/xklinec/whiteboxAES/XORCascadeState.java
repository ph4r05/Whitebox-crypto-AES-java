/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.xklinec.whiteboxAES;

/**
 * Implements basic XOR cascade 16x128 bit -> 128 bit
 *
 * [01] [23] [45] [67] [89] [1011] [1213] [1415]
 *  0     1   2     3   4     5      6      7
 *   \   /     \   /     \   /        \    /
 *  [0123]    [4567]   [891011]    [12131415]
 *     8         9        10          11
 *      \       /          \          /
 *       \     /            \        /
 *      [01234567]       [89101112131415]
 *          12                  13
 *           \                 /
 *            \               /
 *        [0123456789101112131415]
 *                   14
 *
 * @author ph4r05
 */
public class XORCascadeState {
    
}
