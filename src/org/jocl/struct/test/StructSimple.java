/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import org.jocl.struct.Struct;

/**
 * Test struct containing primitive values
 */
public class StructSimple extends Struct
{
    public short s;
    public float f;
    public byte c;

    @Override
    public String toString()
    {
        return "StructSimple[" +
        		"c="+c+"," +
        		"s="+s+"," +
        		"f="+f+"]";
    }
}
