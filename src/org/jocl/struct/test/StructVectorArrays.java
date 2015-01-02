/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import java.util.Arrays;

import org.jocl.struct.*;
import org.jocl.struct.CLTypes.*;

/**
 * Test struct containing arrays of vector types
 */
public class StructVectorArrays extends Struct
{
    @ArrayLength(3)
    public cl_short2 as2[];

    @ArrayLength({3,3})
    public cl_float4 af4[][];
    
    @Override
    public String toString()
    {
        return "StructArrays[" +
        		"as="+Arrays.toString(as2)+"," +
        		"af="+Arrays.toString(af4[0])+","+
        		      Arrays.toString(af4[1])+","+
        		      Arrays.toString(af4[2])+"]";
    }
}
