//! Represents the state of a particular generator
typedef struct{ uint x; uint c; } mwc64x_state_t;

enum{ MWC64X_A = 4294883355U };
enum{ MWC64X_M = 18446383549859758079UL };

void MWC64X_Step(mwc64x_state_t *s)
{
    uint X=s->x, C=s->c;
    
    uint Xn=MWC64X_A*X+C;
    uint carry=(uint)(Xn<C);                // The (Xn<C) will be zero or one for scalar
    uint Cn=mad_hi(MWC64X_A,X,carry);  
    
    s->x=Xn;
    s->c=Cn;
}

//! Return a 32-bit integer in the range [0..2^32)
uint MWC64X_NextUint(mwc64x_state_t *s)
{
    uint res=s->x ^ s->c;
    MWC64X_Step(s);
    return res;
}