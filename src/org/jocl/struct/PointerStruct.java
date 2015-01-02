/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 * 
 * 
 * This file is part of JOCL. 
 * 
 * JOCL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JOCL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with JOCL.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jocl.struct;

import java.nio.ByteBuffer;

import org.jocl.struct.CLTypes.*;
import org.jocl.*;

/**
 * A class that offers a functionality similar to the 'Pointer' class
 * of JOCL for the experimental 'struct' support.
 */
public class PointerStruct
{
    /**
     * Creates a new Pointer to the given vector type values.
     * The given value may not be null, must at least contain
     * one element, and may not contain null elements.<br />
     * <br /> 
     * The data of this pointer may not be read on host side.
     * To pass data to a kernel which may later be written into
     * CL vector types, use the 
     * {@link Buffers#allocateBuffer(cl_vector_type...)} / 
     * {@link Buffers#readFromBuffer(ByteBuffer, cl_vector_type...)} /
     * {@link Buffers#writeToBuffer(ByteBuffer, cl_vector_type...)}
     * methods:
     * <pre><code>
     * ByteBuffer buffer = CLTypes.allocateBuffer(values);
     * Buffers.writeToBuffer(buffer, values);
     * passToKernel(Pointer.to(buffer));
     * Buffers.readFromBuffer(buffer, values);
     * </code></pre>
     * 
     * @param values The vector type values
     * @return A pointer to the vector 
     * @throws NullPointerException If the given values
     * array is null, has a length of 0, or contains null elements
     */
    public static <T extends cl_vector_type> Pointer to(T ... values)
    {
        ByteBuffer buffer = Buffers.allocateBuffer(values);
        Buffers.writeToBuffer(buffer, values);
        return Pointer.to(buffer);
    }
    
    
    /**
     * Creates a new Pointer to the given Structures. The array of
     * structures may not be null, must at least contain one
     * element, and may not contain null elements.<br />
     * <br />
     * The data of this pointer may not be read on host side.
     * To pass data to a kernel which may later be written into
     * Structs, use the 
     * {@link Buffers#allocateBuffer(Struct...)} / 
     * {@link Buffers#readFromBuffer(ByteBuffer, Struct...)} /
     * {@link Buffers#writeToBuffer(ByteBuffer, Struct...)}
     * methods:
     * <pre><code>
     * ByteBuffer buffer = Struct.allocateBuffer(structs);
     * Buffers.writeToBuffer(buffer, values);
     * passToKernel(Pointer.to(buffer));
     * Buffers.readFromBuffer(buffer, structs);
     * </code></pre>
     * 
     * @param structs The structures that the pointer will point to
     * @return The pointer
     * @throws IllegalArgumentException If the given array
     * is null, has a length of 0 or contains null objects
     */
    public static Pointer to(Struct ... structs)
    {
        ByteBuffer buffer = Buffers.allocateBuffer(structs);
        Buffers.writeToBuffer(buffer, structs);
        return Pointer.to(buffer);
    }

}
