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

import java.util.*;

import org.jocl.*;
import org.jocl.struct.CLTypes.*;

/**
 * A class that offers a functionality similar to the 'Sizeof' class
 * of JOCL for the experimental 'struct' support.
 */
public class SizeofStruct
{
    /**
     * A map containing the sizes of pointer and vector types
     */
    private static Map<Class<?>, Integer> sizeMap = 
        new HashMap<Class<?>, Integer>();
    static
    {
        sizeMap.put(cl_char.class,     Sizeof.cl_char);
        sizeMap.put(cl_char2.class,    Sizeof.cl_char2);
        sizeMap.put(cl_char4.class,    Sizeof.cl_char4);
        sizeMap.put(cl_char8.class,    Sizeof.cl_char8);
        sizeMap.put(cl_char16.class,   Sizeof.cl_char16);
        sizeMap.put(cl_uchar.class,    Sizeof.cl_uchar);
        sizeMap.put(cl_uchar2.class,   Sizeof.cl_uchar2);
        sizeMap.put(cl_uchar4.class,   Sizeof.cl_uchar4);
        sizeMap.put(cl_uchar8.class,   Sizeof.cl_uchar8);
        sizeMap.put(cl_uchar16.class,  Sizeof.cl_uchar16);
        sizeMap.put(cl_short.class,    Sizeof.cl_short);
        sizeMap.put(cl_short2.class,   Sizeof.cl_short2);
        sizeMap.put(cl_short4.class,   Sizeof.cl_short4);
        sizeMap.put(cl_short8.class,   Sizeof.cl_short8);
        sizeMap.put(cl_short16.class,  Sizeof.cl_short16);
        sizeMap.put(cl_ushort.class,   Sizeof.cl_ushort);
        sizeMap.put(cl_ushort2.class,  Sizeof.cl_ushort2);
        sizeMap.put(cl_ushort4.class,  Sizeof.cl_ushort4);
        sizeMap.put(cl_ushort8.class,  Sizeof.cl_ushort8);
        sizeMap.put(cl_ushort16.class, Sizeof.cl_ushort16);
        sizeMap.put(cl_int.class,      Sizeof.cl_int);
        sizeMap.put(cl_int2.class,     Sizeof.cl_int2);
        sizeMap.put(cl_int4.class,     Sizeof.cl_int4);
        sizeMap.put(cl_int8.class,     Sizeof.cl_int8);
        sizeMap.put(cl_int16.class,    Sizeof.cl_int16);
        sizeMap.put(cl_uint.class,     Sizeof.cl_uint);
        sizeMap.put(cl_uint2.class,    Sizeof.cl_uint2);
        sizeMap.put(cl_uint4.class,    Sizeof.cl_uint4);
        sizeMap.put(cl_uint8.class,    Sizeof.cl_uint8);
        sizeMap.put(cl_uint16.class,   Sizeof.cl_uint16);
        sizeMap.put(cl_long.class,     Sizeof.cl_long);
        sizeMap.put(cl_long2.class,    Sizeof.cl_long2);
        sizeMap.put(cl_long4.class,    Sizeof.cl_long4);
        sizeMap.put(cl_long8.class,    Sizeof.cl_long8);
        sizeMap.put(cl_long16.class,   Sizeof.cl_long16);
        sizeMap.put(cl_ulong.class,    Sizeof.cl_ulong);
        sizeMap.put(cl_ulong2.class,   Sizeof.cl_ulong2);
        sizeMap.put(cl_ulong4.class,   Sizeof.cl_ulong4);
        sizeMap.put(cl_ulong8.class,   Sizeof.cl_ulong8);
        sizeMap.put(cl_ulong16.class,  Sizeof.cl_ulong16);
        sizeMap.put(cl_float.class,    Sizeof.cl_float);
        sizeMap.put(cl_float2.class,   Sizeof.cl_float2);
        sizeMap.put(cl_float4.class,   Sizeof.cl_float4);
        sizeMap.put(cl_float8.class,   Sizeof.cl_float8);
        sizeMap.put(cl_float16.class,  Sizeof.cl_float16);
        sizeMap.put(cl_double.class,   Sizeof.cl_double);
        sizeMap.put(cl_double2.class,  Sizeof.cl_double2);
        sizeMap.put(cl_double4.class,  Sizeof.cl_double4);
        sizeMap.put(cl_double8.class,  Sizeof.cl_double8);
        sizeMap.put(cl_double16.class, Sizeof.cl_double16);
        
        sizeMap.put(cl_command_queue.class, Sizeof.POINTER);
        sizeMap.put(cl_context.class, Sizeof.POINTER);
        sizeMap.put(cl_context_properties.class, Sizeof.POINTER);
        sizeMap.put(cl_device_id.class, Sizeof.POINTER);
        sizeMap.put(cl_event.class, Sizeof.POINTER);
        sizeMap.put(cl_kernel.class, Sizeof.POINTER);
        sizeMap.put(cl_mem.class, Sizeof.POINTER);
        sizeMap.put(cl_platform_id.class, Sizeof.POINTER);
        sizeMap.put(cl_program.class, Sizeof.POINTER);
        sizeMap.put(cl_sampler.class, Sizeof.POINTER);
        sizeMap.put(Pointer.class, Sizeof.POINTER);
    }
    
    
    /**
     * Returns the size of the given type. If the given type is a
     * type from CLTypes, then this simply returns its size, e.g. <br />
     * Sizeof.type(cl_short4) == Sizeof.cl_short4 <br />
     * <br />
     * If the given type is one of the ports of the other cl_* types
     * (e.g. a cl_device_id or cl_command_queue) then the size of 
     * a native pointer will be returned.
     * <br />
     * If the given type extends the {@link Struct} class, then
     * the size of the whole structure, including padding that
     * is required for the alignment in OpenCL, is computed 
     * and returned. <br />
     * <br />
     * Otherwise, an IllegalArgumentException is thrown
     * 
     * @param c The class
     * @return The size of the given class
     * @throws IllegalArgumentException If the given class is
     * neither a type from CLTypes, nor a cl_* type, nor a Struct.
     */
    @SuppressWarnings("unchecked")
    public static int sizeof(Class<?> c)
    {
        Integer size = sizeMap.get(c);
        if (size != null)
        {
            return size;
        }
        if (Struct.class.isAssignableFrom(c))
        {
            return StructAccess.obtainStructInfo(
                (Class<? extends Struct>)c).getSize();
        }
        throw new IllegalArgumentException("Illegal type: "+c);
    }

}
