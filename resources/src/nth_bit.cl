//Derived from http://graphics.stanford.edu/~seander/bithacks.html
long nthBit(unsigned long l, unsigned int n) {
    unsigned long a, b, c, d; // Intermediate temporaries for bit count.

    // Do a normal parallel bit count for a 64-bit integer,
    // but store all intermediate steps.
    // a = (v & 0x5555...) + ((v >> 1) & 0x5555...);
    a =  l - ((l >> 1) & ~0UL/3);
    // b = (a & 0x3333...) + ((a >> 2) & 0x3333...);
    b = (a & ~0UL/5) + ((a >> 2) & ~0UL/5);//2 bit sums (0-4)
    // c = (b & 0x0f0f...) + ((b >> 4) & 0x0f0f...);
    c = (b + (b >> 4)) & ~0UL/0x11;
    // d = (c & 0x00ff...) + ((c >> 8) & 0x00ff...);
    d = (c + (c >> 8)) & ~0UL/0x101;


    unsigned int s = 0; //bit position counter
    unsigned int t = (d + (d >> 16)) & 0xFF;//bit count temporary

    // if (r >= t) {s |= 32; r -= t;}
    s |= (n >= t) << 5; n -= -(n >= t) & t;
    t  = (d >> s) & 0xff;
    // if (r > t) {s |= 16; r -= t;}
    s |= (n >= t) << 4; n -= -(n >= t) & t;
    t  = (c >> s) & 0xf;
    // if (r > t) {s |= 8; r -= t;}
    s |= (n >= t) << 3; n -= -(n >= t) & t;
    t  = (b >> s) & 0x7;
    // if (r > t) {s -= 4; r -= t;}
    s |= (n >= t) << 2; n -= -(n >= t) & t;
    t  = (a >> s) & 0x3;
    // if (r > t) {s -= 2; r -= t;}
    s |= (n >= t) << 1; n -= -(n >= t) & t;
    t  = (l >> s) & 0x1;
    // if (r > t) s++;
    s |= (n >= t);

    l = 1L << s;
    //if (r > 1) v = 0
    l *= n <= 1;
    return l;
}