/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import org.jocl.struct.Struct;

/**
 * Test struct containing other structs
 */
public class StructNested extends Struct
{
    public short s;
    
    public StructSimple ss;

    public byte c;

    public StructVectorTypes svt;

    @Override
    public String toString()
    {
        return "StructNested[" +
        		"s="+s+"," +
        		"ss="+ss+"," +
        		"c="+c+"," +
        		"svt="+svt+"]";
    }
}

