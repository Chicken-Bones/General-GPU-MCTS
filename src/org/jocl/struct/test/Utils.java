/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */ 

package org.jocl.struct.test;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * Utilities for the struct support tests
 */
public class Utils
{
    /**
     * Returns whether the given objects are 'deep equal'. The method
     * recursively checks the objects and all their public, non-volatile 
     * fields (down to only primitive fields) for equality. 
     * 
     * @param object0 The first object
     * @param object1 The second object
     * @return Whether the objects are deep equal.
     */
    public static boolean deepEqual(Object object0, Object object1)
    {
        //System.out.println("deepEqual with "+object0+" and "+object1);
        
        if (object0 == object1)
        {
            return true;
        }
        if ((object0 == null) != (object1 == null)) // Love this...
        {
            return false;
        }
        if (!object0.getClass().equals(object1.getClass()))
        {
            return false;
        }
        Class<?> commonClass = object0.getClass();
        
        if (commonClass.isArray())
        {
            return deepEqualArrays(object0, object1);
        }
        
        if (commonClass.isPrimitive() ||
            commonClass.equals(Byte.class) ||
            commonClass.equals(Short.class) ||
            commonClass.equals(Character.class) ||
            commonClass.equals(Integer.class) ||
            commonClass.equals(Long.class) ||
            commonClass.equals(Float.class) ||
            commonClass.equals(Double.class) ||
            commonClass.equals(Boolean.class))
        {
            return object0.equals(object1);
        }
        
        Field fields[] = object0.getClass().getDeclaredFields();
        for (int i=0; i<fields.length; i++)
        {
            Field field = fields[i];
            int modifier = field.getModifiers();
            if ( Modifier.isPublic(modifier) && 
                !Modifier.isVolatile(modifier))
            {
                try
                {
                    Object value0 = field.get(object0);
                    Object value1 = field.get(object1);
                    if (!deepEqual(value0, value1))
                    {
                        return false;
                    }
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                    return false;
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Returns whether the given objects are arrays with equal size and
     * component types that are deep equal. Primitive arrays are 
     * deep equal if they contain equal values. Object arrays are
     * deep equal if their respective elements are deep equal.
     *  
     * @param object0 The first object
     * @param object1 The second object
     * @return Whether the objects are deep equal arrays.
     */
    private static boolean deepEqualArrays(Object object0, Object object1)
    {
        if (!object0.getClass().isArray())
        {
            return false;
        }
        if (!object1.getClass().isArray())
        {
            return false;
        }
        if (!object0.getClass().getComponentType().equals(
             object1.getClass().getComponentType()))
        {
            return false;
        }
        
        int length0 = Array.getLength(object0);
        int length1 = Array.getLength(object0);
        if (length0 != length1)
        {
            return false;
        }
        
        if (object0 instanceof byte[])
        {
            byte array0[] = (byte[])object0;
            byte array1[] = (byte[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }

        else if (object0 instanceof short[])
        {
            short array0[] = (short[])object0;
            short array1[] = (short[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }

        else if (object0 instanceof char[])
        {
            char array0[] = (char[])object0;
            char array1[] = (char[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }

        else if (object0 instanceof int[])
        {
            int array0[] = (int[])object0;
            int array1[] = (int[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }

        else if (object0 instanceof long[])
        {
            long array0[] = (long[])object0;
            long array1[] = (long[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }

        else if (object0 instanceof float[])
        {
            float array0[] = (float[])object0;
            float array1[] = (float[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }

        else if (object0 instanceof double[])
        {
            double array0[] = (double[])object0;
            double array1[] = (double[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }

        else if (object0 instanceof boolean[])
        {
            boolean array0[] = (boolean[])object0;
            boolean array1[] = (boolean[])object1;
            if (!Arrays.equals(array0, array1))
            {
                return false;
            }
        }
        
        else
        {
            for (int i=0; i<length0; i++)
            {
                Object element0 = Array.get(object0, i);
                Object element1 = Array.get(object1, i);
                if (!deepEqual(element0, element1))
                {
                    return false;
                }
            }
        }
        return true;
    }

}
