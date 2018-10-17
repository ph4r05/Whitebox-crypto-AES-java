Whitebox-crypto-AES-java
========================

[![Build Status](https://travis-ci.org/ph4r05/Whitebox-crypto-AES-java.svg?branch=master)](https://travis-ci.org/ph4r05/Whitebox-crypto-AES-java)
[![Coverity Status](https://scan.coverity.com/projects/7189/badge.svg)](https://scan.coverity.com/projects/ph4r05-whitebox-crypto-aes-java)

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


Donating
========

This implementation is an open source. If you like the code or you do find it useful please feel free to donate to the
author whatever amount you would like by clicking on the paypal button below.
And if you don't feel like donating, that's OK too.

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=XK6RLD768RGGJ&lc=SK&item_name=ph4r05&item_number=Whitebox%2dcrypto%2dAES%2djava%2egit&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)

Bitcoin:

![1342dgQqohM9PMLof7RaFK51B5tCiMW2sp](https://deadcode.me/btc-whitebox-java.png)<br />1342dgQqohM9PMLof7RaFK51B5tCiMW2sp

Monero:
47BEukN83whUdvuXbaWmDDQLYNUpLsvFR2jioQtpP5vD8b3o74b9oFgQ3KFa3ibjbwBsaJEehogjiUCfGtugUGAuJAfbh1Z



