#include <stdio.h>
#include <stdlib.h>

typedef unsigned char BYTE;
typedef union _W32B{
    BYTE B[4];
    unsigned int l;
} W32b;

typedef union _W128B{
    BYTE B[16];
    unsigned int l[4];
} W128b;


int main(){
    W32b test;
    test.B[0] = 0xff;
    test.B[1] = 0xaa;
    test.B[2] = 0xcc;
    test.B[3] = 0x00;;
	
    printf("hexacode: %X\n", test.l);


    return 0;
}
