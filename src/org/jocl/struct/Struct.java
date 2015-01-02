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

import java.lang.reflect.*;
import java.nio.ByteBuffer;

import org.jocl.*;
import org.jocl.struct.CLTypes.cl_vector_type;
import org.jocl.struct.StructAccess.*;



/**
 * This class is the base class for all Java classes that should
 * represent a native 'struct'. A struct class may be created
 * by creating a public class that extends this Struct class,
 * and which contains public accessors. All public non-volatile
 * accessors will be considered as accessors that correspond to the
 * element of native struct. <br />
 * <br />
 * <u>Example:</u><br />
 * <br />
 * The class
 * <code><pre>
 * public class Particle extends Struct
 * {
 *     public float mass;
 *     public cl_float4 position;
 *     public cl_float4 velocity;
 * }
 * </pre></code>
 * Corresponds to a native struct like
 * <code><pre>
 * typedef struct Particle
 * {
 *     float mass;
 *     float4 position;
 *     float4 velocity;
 * } Particle;
 * </pre></code>
 * <br />
 * All accessors of the derived class must either be primitive
 * accessors, one of the OpenCL vector types defined in
 * {@link CLTypes}, other Structs or arrays of these
 * types (except for boolean arrays). Structs containing 
 * instances of themself are not supported and will cause 
 * a CLException to be thrown during the initialization 
 * of the Struct. 
 * <br />
 */
public abstract class Struct extends Bufferable
{
    /**
     * Creates a new instance of the given type, wrapping
     * all possible exceptions into a CLException
     * 
     * @param type The type
     * @return The new instance
     */
    private static Object createObject(Class<?> type)
    {
        try
        {
            Constructor<?> constructor = type.getDeclaredConstructor();
            return constructor.newInstance();
        }
        catch (IllegalArgumentException e)
        {
            // This
            throw new CLException(e.getMessage(), e);
        }
        catch (IllegalAccessException e)
        {
            // may
            throw new CLException(e.getMessage(), e);
        }
        catch (InstantiationException e)
        {
            // go
            throw new CLException(e.getMessage(), e);
        }
        catch (NoSuchMethodException e)
        {
            // terribly
            throw new CLException(e.getMessage(), e);
        }
        catch (InvocationTargetException e)
        {
            // wrong ;-)
            throw new CLException(e.getMessage(), e);
        }
    }
    
    /**
     * Initialize the given object array. If the array contains
     * arrays, this method is called recursively on all contained
     * arrays. When the innermost arrays are reached, they are
     * filled with objects of the given type.<br />
     * <br />
     * The given type must provide a default constructor, none
     * of the arguments may be null, and the array may not 
     * contain null arrays.
     * 
     * @param array The array to fill
     * @param type The type of the objects
     * @throws CLException If the objects can not be instantiated
     */
    private static void initObjectArray(Object array, Class<?> type)
    {
        int length = Array.getLength(array);
        if (array.getClass().getComponentType().isArray())
        {
            for (int i=0; i<length; i++)
            {
                Object subArray = Array.get(array, i);
                initObjectArray(subArray, type);
            }
        }
        else
        {
            for (int i=0; i<length; i++)
            {
                Object object = createObject(type);
                Array.set(array, i, object);
            }
        }
    }
    
    
    /**
     * Creates a new instance of a struct.
     */
    protected Struct()
    {
        initFields();
    }
    
    /**
     * Initialize all non-primitive (public and non-volatile) 
     * accessors of this instance.
     */
    private void initFields()
    {
        StructInfo structInfo = getStructInfo();
        StructAccessor structAccessors[] = structInfo.getStructAccessors();
        for (int i=0; i<structAccessors.length; i++)
        {
            Field field = structAccessors[i].getField();
            if (!field.getType().isPrimitive())
            {
                if (this.getClass().isAssignableFrom(field.getType()))
                {
                    // This has already been checked during the 
                    // initialization of the StructAccessors,
                    // so should never happen here.
                    throw new CLException(
                        "Struct may not contain instances of itself: "+field);
                }
                
                if (CLTypes.cl_vector_type.class.isAssignableFrom(field.getType()))
                {
                    initCLVectorTypeField(field);
                }
                else if (field.getType().isArray())
                {
                    initArrayField(field);
                }
                else if (Struct.class.isAssignableFrom(field.getType()))
                {
                    initStructField(field);
                }
                else
                {
                    // This has already been checked during the 
                    // initialization of the StructAccessors,
                    // so should never happen here.
                    throw new CLException(
                        "Invalid type in struct: "+field);
                }
            }
        }
    }
    
    
    /**
     * Initialize the given field, which must be an OpenCL vector type field.
     * This method will create an instance of this type and assign it to the
     * specified field in this struct.
     * 
     * @param clVectorTypeField The field to initialize
     * @throws CLException If the initialization went wrong...
     */
    private void initCLVectorTypeField(Field clVectorTypeField) throws CLException
    {
        Class<?> type = clVectorTypeField.getType(); 
        cl_vector_type element = (cl_vector_type)createObject(type);
        try
        {
            clVectorTypeField.set(this, element);
        }
        catch (IllegalArgumentException e)
        {
            throw new CLException(e.getMessage(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new CLException(e.getMessage(), e);
        }
    }
    
    
    /**
     * Initialize the given field, which must be an array type field.
     * This method will create an instance of the array and assign 
     * it to the specified field in this struct. The size of the
     * array that is created is taken from the {@link ArrayLength}
     * annotation of the field. If this annotation is not present,
     * a CLException is thrown.
     * 
     * @param arrayField The field to initialize
     * @throws CLException If the field has no ArrayLength 
     * annotation, or the initialization went wrong...
     */
    private void initArrayField(Field arrayField)
    {
        // This has already been checked during the 
        // initialization of the StructAccessors,
        // so should never happen here.
        if (!arrayField.isAnnotationPresent(ArrayLength.class)) 
        {
            throw new CLException(
                "Field "+arrayField+" has no ArrayLength annotation");
        }
        
        // Create the array and assign it to 'this' via the field
        int arrayLengths[] = 
            arrayField.getAnnotation(ArrayLength.class).value();
        Class<?> componentType = 
            StructAccess.getBaseComponentType(arrayField.getType());
        Object array = Array.newInstance(componentType, arrayLengths);
        try
        {
            arrayField.set(this, array);
        }
        catch (IllegalArgumentException e)
        {
            throw new CLException(e.getMessage(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new CLException(e.getMessage(), e);
        }
        
        // Non-primitive arrays will be filled with objects
        if (!componentType.isPrimitive())
        {
            if (cl_vector_type.class.isAssignableFrom(componentType))
            {
                initObjectArray(array, componentType);
            }
            else if (Struct.class.isAssignableFrom(componentType))
            {
                initObjectArray(array, componentType);
            }
            else
            {
                throw new CLException("Invalid type in array: "+componentType);
            }
        }
        
    }
    
    
    /**
     * Initialize the given field, which must be a Struct type field.
     * This method will create an instance of this type and assign 
     * it to the specified field in this struct.
     * 
     * @param structField The field to initialize
     * @throws CLException If the initialization went wrong...
     */
    private void initStructField(Field structField)
    {
        try
        {
            structField.set(this, createObject(structField.getType()));
        }
        catch (IllegalArgumentException e)
        {
            throw new CLException(e.getMessage(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new CLException(e.getMessage(), e);
        }
    }
    
    
    
    /**
     * Returns the StructInfo instance that is associated with
     * the class of this struct. If this StructInfo does not
     * yet exist, it is created and stored for later access.
     * 
     * @return The StructInfo for this struct class.
     */
    private StructInfo getStructInfo()
    {
        return StructAccess.obtainStructInfo(getClass());
    }

    
    /**
     * Return the size of this struct, including paddings that are
     * inserted to obey OpenCL alignment requirements.
     * 
     * @return The size of this struct
     */
    int getSize()
    {
        return getStructInfo().getSize();
    }
    
    
    @Override
    void writeThisToBuffer(ByteBuffer targetBuffer)
    {
        int initialPosition = targetBuffer.position();
        StructAccessor structAccessors[] = 
            getStructInfo().getStructAccessors();
        for (int i=0; i<structAccessors.length; i++)
        {
            StructAccessor structAccessor = structAccessors[i];
            try
            {
                int position = 
                    initialPosition + structAccessor.getOffset();
                
                //System.out.println("For writing "+structAccessor.getField()+" position at "+position);
                
                targetBuffer.position(position);
                structAccessor.writeToBuffer(this, targetBuffer);
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
                throw new CLException(
                    "Could not access field "+structAccessor.getField()+
                    " of structure "+this);
            }
            catch (IllegalAccessException e)
            {
                throw new CLException(
                    "Could not access field "+structAccessor.getField()+
                    " of structure "+this);
            }
        }
        targetBuffer.position(initialPosition+getSize());
    }


    @Override
    void readThisFromBuffer(ByteBuffer sourceBuffer)
    {
        int initialPosition = sourceBuffer.position();
        StructAccessor structAccessors[] = 
            getStructInfo().getStructAccessors();
        for (int i=0; i<structAccessors.length; i++)
        {
            StructAccessor structAccessor = structAccessors[i];
            try
            {
                int position = 
                    initialPosition + structAccessor.getOffset();
                sourceBuffer.position(position);
                
                //System.out.println("For reading "+structAccessor.getField()+" position at "+position);
                
                structAccessor.readFromBuffer(this, sourceBuffer);
            }
            catch (IllegalArgumentException e)
            {
                throw new CLException(
                    "Could not access field "+structAccessor.getField()+
                    " of structure "+this);
            }
            catch (IllegalAccessException e)
            {
                throw new CLException(
                    "Could not access field "+structAccessor.getField()+
                    " of structure "+this);
            }
        }
        sourceBuffer.position(initialPosition+getSize());
    }
    
    

    
    
    
    
    

    /**
     * Debug function which prints the alignment layout of the given
     * struct class.<br />
     * <br />
     * <b><u>This function is for debugging purposes ONLY!</u></b> 
     *  
     * @param structClass The class
     */
    public static void showLayout(Class<? extends Struct> structClass)
    {
        StructInfo structInfo = StructAccess.obtainStructInfo(structClass);
        StructAccessor structAccessors[] = 
            structInfo.getStructAccessors();
        if (structAccessors.length == 0)
        {
            System.out.println("No relevant (public and non-volatile) " +
                    "fields in "+structClass.getSimpleName());
            return;
        }
        
        System.out.println(
            "Aligned field layout for "+structClass.getSimpleName()+": ");
        int currentOffset = 0;
        for (int i=0; i<structAccessors.length; i++)
        {
            StructAccessor structAccessor = structAccessors[i];
            for (int j=currentOffset; j<structAccessor.offset; j++)
            {
                System.out.print("_");
                currentOffset++;
            }
            for (int j=0; j<structAccessor.getSize(); j++)
            {
                System.out.print(i);
                currentOffset++;
            }
        }
        while (currentOffset < structInfo.getSize())
        {
            System.out.print("_");
            currentOffset++;
        }
        System.out.println("");
        for (int i=0; i<structAccessors.length; i++)
        {
            StructAccessor sa = structAccessors[i]; 
            Field f = sa.getField();
            System.out.println(i+": "+
                f.getType().getSimpleName()+" "+f.getName()+
                ", offset "+sa.getOffset()+
                ", size "+sa.getSize()+
                ", alignment "+sa.getAlignment());
            
            int current = sa.getOffset() + sa.getSize();
            int end = 0;
            if (i == structAccessors.length - 1)
            {
                end = structInfo.getSize();
            }
            else
            {
                end = structAccessors[i+1].getOffset();
            }
            if (current < end)
            {
                System.out.println("   padding: "+(end-current)+" bytes");
            }
        }
        System.out.println(
            "Total size: "+structInfo.getSize());
    }
    
    



}


