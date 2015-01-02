/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import java.util.Arrays;

import org.jocl.struct.*;

/**
 * Test struct containing arrays of other structs
 */
public class StructStructArrays extends Struct
{
    @ArrayLength(3)
    public StructSimple ass[];

    @ArrayLength({3,3})
    public StructVectorTypes asvt[][];
    
    @Override
    public String toString()
    {
        return "StructArrays[" +
        		"as="+Arrays.toString(ass)+"," +
        		"af="+Arrays.toString(asvt[0])+","+
        		      Arrays.toString(asvt[1])+","+
        		      Arrays.toString(asvt[2])+"]";
    }
}
