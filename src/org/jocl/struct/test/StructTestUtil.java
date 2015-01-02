/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import java.lang.reflect.*;
import java.util.*;

import org.jocl.struct.*;
import org.jocl.struct.CLTypes.*;

/**
 * Wierd utility class for the struct support tests. The methods in this
 * class are mainly used to apply a function to all accessors of
 * struct objects, be it primitive- vector- array- or other
 * struct accessors.
 */
public class StructTestUtil
{
    /**
     * Array of all vector classes of {@link CLTypes}
     */
    private static Class<?> vectorClasses[] = new Class<?>[]
    {
        cl_char.class,
        cl_char2.class,
        cl_char4.class,
        cl_char8.class,
        cl_char16.class,
        cl_uchar.class,
        cl_uchar2.class,
        cl_uchar4.class,
        cl_uchar8.class,
        cl_uchar16.class,

        cl_short.class,
        cl_short2.class,
        cl_short4.class,
        cl_short8.class,
        cl_short16.class,
        cl_ushort.class,
        cl_ushort2.class,
        cl_ushort4.class,
        cl_ushort8.class,
        cl_ushort16.class,

        cl_int.class,
        cl_int2.class,
        cl_int4.class,
        cl_int8.class,
        cl_int16.class,
        cl_uint.class,
        cl_uint2.class,
        cl_uint4.class,
        cl_uint8.class,
        cl_uint16.class,

        cl_long.class,
        cl_long2.class,
        cl_long4.class,
        cl_long8.class,
        cl_long16.class,
        cl_ulong.class,
        cl_ulong2.class,
        cl_ulong4.class,
        cl_ulong8.class,
        cl_ulong16.class,

        cl_float.class,
        cl_float2.class,
        cl_float4.class,
        cl_float8.class,
        cl_float16.class,

        cl_double.class,
        cl_double2.class,
        cl_double4.class,
        cl_double8.class,
        cl_double16.class
    };    

    /**
     * Map from the vector classes of {@link CLTypes} to their respective
     * base (component) type
     */
    private static Map<Class<?>, Class<?>> vectorClassBaseTypes = 
        new HashMap<Class<?>, Class<?>>();
    static
    {
        vectorClassBaseTypes.put(cl_char.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_char2.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_char4.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_char8.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_char16.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_uchar.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_uchar2.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_uchar4.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_uchar8.class, Byte.TYPE);
        vectorClassBaseTypes.put(cl_uchar16.class, Byte.TYPE);

        vectorClassBaseTypes.put(cl_short.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_short2.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_short4.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_short8.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_short16.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_ushort.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_ushort2.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_ushort4.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_ushort8.class, Short.TYPE);
        vectorClassBaseTypes.put(cl_ushort16.class, Short.TYPE);

        vectorClassBaseTypes.put(cl_int.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_int2.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_int4.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_int8.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_int16.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_uint.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_uint2.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_uint4.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_uint8.class, Integer.TYPE);
        vectorClassBaseTypes.put(cl_uint16.class, Integer.TYPE);

        vectorClassBaseTypes.put(cl_long.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_long2.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_long4.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_long8.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_long16.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_ulong.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_ulong2.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_ulong4.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_ulong8.class, Long.TYPE);
        vectorClassBaseTypes.put(cl_ulong16.class, Long.TYPE);

        vectorClassBaseTypes.put(cl_float.class, Float.TYPE);
        vectorClassBaseTypes.put(cl_float2.class, Float.TYPE);
        vectorClassBaseTypes.put(cl_float4.class, Float.TYPE);
        vectorClassBaseTypes.put(cl_float8.class, Float.TYPE);
        vectorClassBaseTypes.put(cl_float16.class, Float.TYPE);

        vectorClassBaseTypes.put(cl_double.class, Double.TYPE);
        vectorClassBaseTypes.put(cl_double2.class, Double.TYPE);
        vectorClassBaseTypes.put(cl_double4.class, Double.TYPE);
        vectorClassBaseTypes.put(cl_double8.class, Double.TYPE);
        vectorClassBaseTypes.put(cl_double16.class, Double.TYPE);
    }

    /**
     * Map from the vector classes of {@link CLTypes} to their 
     * dimension
     */
    private static Map<Class<?>, Integer> vectorDimensions = 
        new HashMap<Class<?>, Integer>();
    static
    {
        vectorDimensions.put(cl_char.class, 1);
        vectorDimensions.put(cl_char2.class, 2);
        vectorDimensions.put(cl_char4.class, 4);
        vectorDimensions.put(cl_char8.class, 8);
        vectorDimensions.put(cl_char16.class, 16);

        vectorDimensions.put(cl_uchar.class, 1);
        vectorDimensions.put(cl_uchar2.class, 2);
        vectorDimensions.put(cl_uchar4.class, 4);
        vectorDimensions.put(cl_uchar8.class, 8);
        vectorDimensions.put(cl_uchar16.class, 16);

        vectorDimensions.put(cl_short.class, 1);
        vectorDimensions.put(cl_short2.class, 2);
        vectorDimensions.put(cl_short4.class, 4);
        vectorDimensions.put(cl_short8.class, 8);
        vectorDimensions.put(cl_short16.class, 16);

        vectorDimensions.put(cl_ushort.class, 1);
        vectorDimensions.put(cl_ushort2.class, 2);
        vectorDimensions.put(cl_ushort4.class, 4);
        vectorDimensions.put(cl_ushort8.class, 8);
        vectorDimensions.put(cl_ushort16.class, 16);

        vectorDimensions.put(cl_int.class, 1);
        vectorDimensions.put(cl_int2.class, 2);
        vectorDimensions.put(cl_int4.class, 4);
        vectorDimensions.put(cl_int8.class, 8);
        vectorDimensions.put(cl_int16.class, 16);

        vectorDimensions.put(cl_uint.class, 1);
        vectorDimensions.put(cl_uint2.class, 2);
        vectorDimensions.put(cl_uint4.class, 4);
        vectorDimensions.put(cl_uint8.class, 8);
        vectorDimensions.put(cl_uint16.class, 16);

        vectorDimensions.put(cl_long.class, 1);
        vectorDimensions.put(cl_long2.class, 2);
        vectorDimensions.put(cl_long4.class, 4);
        vectorDimensions.put(cl_long8.class, 8);
        vectorDimensions.put(cl_long16.class, 16);

        vectorDimensions.put(cl_ulong.class, 1);
        vectorDimensions.put(cl_ulong2.class, 2);
        vectorDimensions.put(cl_ulong4.class, 4);
        vectorDimensions.put(cl_ulong8.class, 8);
        vectorDimensions.put(cl_ulong16.class, 16);

        vectorDimensions.put(cl_float.class, 1);
        vectorDimensions.put(cl_float2.class, 2);
        vectorDimensions.put(cl_float4.class, 4);
        vectorDimensions.put(cl_float8.class, 8);
        vectorDimensions.put(cl_float16.class, 16);

        vectorDimensions.put(cl_double.class, 1);
        vectorDimensions.put(cl_double2.class, 2);
        vectorDimensions.put(cl_double4.class, 4);
        vectorDimensions.put(cl_double8.class, 8);
        vectorDimensions.put(cl_double16.class, 16);
    }


    /**
     * Interface for functions that may be applied to objects and
     * return objects.
     */
    public interface Function
    {
        Object apply(Object object);
    }
    
    /**
     * Apply the given function to all public non-volatile 
     * accessors of the given struct
     * 
     * @param struct The struct
     * @param function The function to apply
     * @throws Exception If something goes wrong
     */
    public static void applyToStruct(Object struct, Function function) 
        throws Exception
    {
        Field fields[] = struct.getClass().getFields();
        for (int j=0; j<fields.length; j++)
        {
            Field field = fields[j];
            int modifier = field.getModifiers();
            if ( Modifier.isPublic(modifier) && 
                !Modifier.isVolatile(modifier))
            {
                applyToField(struct, field, function);
            }
        }
    }
    
    /**
     * Applies the given function to the specified field of the given struct
     * 
     * @param struct The struct
     * @param field The field
     * @param function The function
     * @throws Exception If something goes wrong
     */
    private static void applyToField(
        Object struct, Field field, Function function)  
        throws Exception
    {
        if (field.getType().isPrimitive())
        {
            applyToPrimitiveField(struct, field, function);
            return;
        }
        if (Struct.class.isAssignableFrom(field.getType()))
        {
            applyToStruct(field.get(struct), function);
            return;
        }
        if (field.getType().isArray())
        {
            applyToArrayField(struct, field, function);
            return;
        }

        Object object = field.get(struct);
        boolean vectorInitialized = applyToVectorObject(object, function);
        if (vectorInitialized)
        {
            return;
        }
        throw new RuntimeException("Failed to apply to field "+field);
    }

    
    /**
     * Applies the given function to all elements of the given vector object
     * 
     * @param object The vector object
     * @param function The function
     * @return Whether the function was applied (i.e. if the object really
     * was a vector object)
     * @throws Exception If something goes wrong
     */
    private static boolean applyToVectorObject(
        Object object, Function function) 
        throws Exception
    {
        for (int j=0; j<vectorClasses.length; j++)
        {
            Class<?> vectorClass = vectorClasses[j];
            
            if (object.getClass().isAssignableFrom(vectorClass))
            {
                int arity = vectorDimensions.get(vectorClass);
                Class<?> baseClass = vectorClassBaseTypes.get(vectorClass);
                for (int i=0; i<arity; i++)
                {
                    applyToVectorFieldElement(
                        object, vectorClass, baseClass, i, function);
                }
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Applies the given function to the element of the given vector
     * with the given index.
     * 
     * @param vector The vector object
     * @param vectorClass The type of the vector object
     * @param baseType The base (component) type of the vector
     * @param index The index of the vector component
     * @param function The function to apply
     * @throws Exception If something goes wrong
     */
    private static void applyToVectorFieldElement(
        Object vector, Class<?> vectorClass, Class<?> baseType, 
        int index, Function function) 
        throws Exception
    {
        Method getMethod = vectorClass.getMethod(
            "get", new Class<?>[]{Integer.TYPE});
        getMethod.setAccessible(true);
        Number oldValue = (Number)getMethod.invoke(vector, index);
        Number value = (Number)function.apply(oldValue);
        
        Method setMethod = vectorClass.getMethod(
            "set", new Class<?>[]{Integer.TYPE, baseType});
        setMethod.setAccessible(true);
        if (baseType.equals(Byte.TYPE))
        {
            setMethod.invoke(vector, index, value.byteValue());
        }
        if (baseType.equals(Short.TYPE))
        {
            setMethod.invoke(vector, index, value.shortValue());
        }
        if (baseType.equals(Integer.TYPE))
        {
            setMethod.invoke(vector, index, value.intValue());
        }
        if (baseType.equals(Long.TYPE))
        {
            setMethod.invoke(vector, index, value.longValue());
        }
        if (baseType.equals(Float.TYPE))
        {
            setMethod.invoke(vector, index, value.floatValue());
        }
        if (baseType.equals(Double.TYPE))
        {
            setMethod.invoke(vector, index, value.doubleValue());
        }
    }
    
    /**
     * Apply the given function to the given (primitive) field
     * of the given struct
     *  
     * @param struct The struct
     * @param field The field
     * @param function The function to apply
     * @throws Exception If something goes wrong
     */
    private static void applyToPrimitiveField(
        Object struct, Field field, Function function) 
        throws Exception
    {
        Number oldValue = (Number)field.get(struct);
        Number value = (Number)function.apply(oldValue);
        
        if (field.getType().equals(Byte.TYPE))
        {
            field.setByte(struct, value.byteValue());
        }
        if (field.getType().equals(Short.TYPE))
        {
            field.setShort(struct, value.shortValue());
        }
        if (field.getType().equals(Character.TYPE))
        {
            field.setChar(struct, (char)value.shortValue());
        }
        if (field.getType().equals(Integer.TYPE))
        {
            field.setInt(struct, value.intValue());
        }
        if (field.getType().equals(Long.TYPE))
        {
            field.setLong(struct, value.longValue());
        }
        if (field.getType().equals(Float.TYPE))
        {
            field.setFloat(struct, value.floatValue());
        }
        if (field.getType().equals(Double.TYPE))
        {
            field.setDouble(struct, value.doubleValue());
        }
    }

    /**
     * Apply the given function to the given (array) field
     * of the given struct
     *  
     * @param struct The struct
     * @param field The field
     * @param function The function to apply
     * @throws Exception If something goes wrong
     */
    private static void applyToArrayField(
        Object struct, Field field, Function function) 
        throws Exception
    {
        Object array = field.get(struct);
        applyToArray(array, function);
    }

    /**
     * Apply the given function to all elements of the given array
     * 
     * @param array The array
     * @param function The function to apply
     * @throws Exception If something goes wrong
     */
    @SuppressWarnings("cast")
    private static void applyToArray(Object array, Function function) 
        throws Exception
    {
        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);
        if (componentType.isArray())
        {
            for (int i=0; i<length; i++)
            {
                Object subArray = Array.get(array, i);
                applyToArray(subArray, function);
            }
            return;
        }
        
        if (componentType.equals(Byte.TYPE))
        {
            for (int i=0; i<length; i++)
            {
                Number oldValue = (Number)Array.getByte(array, i);
                Number value = (Number)function.apply(oldValue);
                Array.setByte(array, i, value.byteValue());
            }
        }
        else if (componentType.equals(Short.TYPE))
        {
            for (int i=0; i<length; i++)
            {
                Number oldValue = (Number)Array.getShort(array, i);
                Number value = (Number)function.apply(oldValue);
                Array.setShort(array, i, value.shortValue());
            }
        }
        else if (componentType.equals(Character.TYPE))
        {
            for (int i=0; i<length; i++)
            {
                Number oldValue = (Number)((int)Array.getChar(array, i));
                Number value = (Number)function.apply(oldValue);
                Array.setChar(array, i, (char)value.intValue());
            }
        }
        else if (componentType.equals(Integer.TYPE))
        {
            for (int i=0; i<length; i++)
            {
                Number oldValue = (Number)Array.getInt(array, i);
                Number value = (Number)function.apply(oldValue);
                Array.setInt(array, i, value.intValue());
            }
        }
        else if (componentType.equals(Long.TYPE))
        {
            for (int i=0; i<length; i++)
            {
                Number oldValue = (Number)Array.getLong(array, i);
                Number value = (Number)function.apply(oldValue);
                Array.setLong(array, i, value.longValue());
            }
        }
        else if (componentType.equals(Float.TYPE))
        {
            for (int i=0; i<length; i++)
            {
                Number oldValue = (Number)Array.getFloat(array, i);
                Number value = (Number)function.apply(oldValue);
                Array.setFloat(array, i, value.floatValue());
            }
        }
        else if (componentType.equals(Double.TYPE))
        {
            for (int i=0; i<length; i++)
            {
                Number oldValue = (Number)Array.getDouble(array, i);
                Number value = (Number)function.apply(oldValue);
                Array.setDouble(array, i, value.doubleValue());
            }
        }
        else if (Struct.class.isAssignableFrom(componentType)) 
        {
            for (int i=0; i<length; i++)
            {
                Object object = Array.get(array, i);
                applyToStruct((Struct)object, function);
            }
        }
        else
        {
            boolean processed = false;
            for (int i=0; i<length; i++)
            {
                Object object = Array.get(array, i);
                processed = applyToVectorObject(object, function);
                if (!processed)
                {
                    break;
                }
            }
            
            if (!processed)
            {
                throw new RuntimeException(
                    "Invalid array component type: "+ componentType);
            }
        }
    }

}
