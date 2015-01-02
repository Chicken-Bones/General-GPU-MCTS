/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import java.util.Arrays;

import org.jocl.struct.*;

/**
 * Test struct containing primtive arrays
 */
public class StructArrays extends Struct
{
    @ArrayLength(3)
    public short as[];

    @ArrayLength({3,3})
    public float af[][];
    
    @Override
    public String toString()
    {
        return "StructArrays[" +
        		"as="+Arrays.toString(as)+"," +
        		"af="+Arrays.toString(af[0])+","+
        		      Arrays.toString(af[1])+","+
        		      Arrays.toString(af[2])+"]";
    }
}
