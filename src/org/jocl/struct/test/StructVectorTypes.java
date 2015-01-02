/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import org.jocl.struct.Struct;
import org.jocl.struct.CLTypes.*;

/**
 * Test struct containing vector types
 */
public class StructVectorTypes extends Struct
{
    public cl_char2 c2;
    public cl_float4 f4;
    
    @Override
    public String toString()
    {
        return "StructVectorTypes[" +
        		"c2="+c2+"," +
        		"f4="+f4+"]";
    }
}
