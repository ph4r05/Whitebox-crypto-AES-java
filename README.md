Whitebox-crypto-AES-java
========================

[![Build Status](https://travis-ci.org/ph4r05/Whitebox-crypto-AES-java.svg?branch=master)](https://travis-ci.org/ph4r05/Whitebox-crypto-AES-java)

Whitebox cryptography AES implementation.

This repository contains a Java implementation of a complete whitebox [AES]-128 scheme introduced by [Chow] et al. It implements/uses input/output encodings, mixing bijections, external encodings.

Implementation code contains pure Java implementation of the Chow's whitebox AES scheme instance generator and instance emulator. Generated instance can be serialized. 

You also might be interested in my [C++] implementation of the Chow's generator & emulator. It also contains implementation of Karroumi whitebox scheme and Billet et al. key recovery attack (not implemented in Java version).

[AES]: http://csrc.nist.gov/archive/aes/rijndael/Rijndael-ammended.pdf
[Chow]: http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.59.7710
[C++]: https://github.com/ph4r05/Whitebox-crypto-AES/

Dependencies
=======
* Maven
* BouncyCastle (Maven should handle this dependency)

[BouncyCastle]: https://www.bouncycastle.org/

License
=======
Code is licensed under new BSD license. For further details see LICENSE file.

Contributing
=======
If you would like to improve my code by extending it to AES-256 or implementing other whitebox AES schemes do not hesitate to submit a pull request. Please also consider it if you find some bug in the code. I am not actively developing this code at the moment but I will review the pull requests. Thanks!



