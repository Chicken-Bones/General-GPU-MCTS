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
import java.util.*;

import org.jocl.*;
import org.jocl.struct.CLTypes.cl_vector_type;

/**
 * Package-private class which summarizes helper methods and classes
 * for accessing accessors of structs.
 */
public class StructAccess
{
    /**
     * The map from the (Struct) class to the information about
     * the respective struct class. See {@link StructInfo}. 
     */
    private static Map<Class<? extends Struct>, StructInfo> structInfoMap = 
        new HashMap<Class<? extends Struct>, StructInfo>();

    
    /**
     * Returns the StructInfo instance that is associated with
     * the given class. If this StructInfo does not yet exist, 
     * it is created and stored for later access.
     * 
     * @param c The Struct class
     * @return The StructInfo for this struct class.
     */
    static StructInfo obtainStructInfo(Class<? extends Struct> c)
    {
        StructInfo structInfo = structInfoMap.get(c);
        if (structInfo == null)
        {
            structInfo = new StructInfo(c);
            structInfoMap.put(c, structInfo);
        }
        return structInfo;
    }
    
    /**
     * Computes the lowest common multiple (LCM) of the
     * given values
     * 
     * @param a The first value
     * @param b The second value
     * @return The LCM
     */
    private static int lcm(int a, int b)
    {
        return (a*b) / gcd(a,b);
    }
    
    /**
     * Computes the greatest common divisor (GCD) of the 
     * given values.
     * 
     * @param a The first value
     * @param b The second value
     * @return The GCD
     */
    private static int gcd(int a, int b)
    {
        while (b != 0)
        {
           int t = b;
           b = a % b;
           a = t;
        }
        return a;
    }

    /**
     * Returns the base component type of the given (array) type,
     * or the type itself if it is not an array type. That is,
     * for multidimensional arrays, this will return the 
     * innermost type, e.g. the type 'String' for a String[][]
     * array.
     * 
     * @param type The type
     * @return The base component type
     */
    static Class<?> getBaseComponentType(Class<?> type)
    {
        if (!type.isArray())
        {
            return type;
        }
        return getBaseComponentType(type.getComponentType());
    }
    
    /**
     * Returns the dimension of the given (array) type, or
     * 0 if the given type is not an array type. E.g. it
     * will return 1 for a byte[] array, and 3 for a
     * byte[][][] array.
     * 
     * @param type The array type.
     * @return The dimension
     */
    private static int computeArrayDimension(Class<?> type)
    {
        if (!type.isArray())
        {
            return 0;
        }
        return 1 + computeArrayDimension(type.getComponentType());
    }
    
    /**
     * Computes the lowest common multiple (LCM) of the alignments of the
     * given struct accessors.
     * 
     * @param structAccessors The struct accessors
     * @return The lowest common multiple of the alignments
     */
    private static int computeAlignmentLCM(StructAccessor structAccessors[])
    {
        if (structAccessors.length == 0)
        {
            return 0;
        }
        if (structAccessors.length == 1)
        {
            return structAccessors[0].getAlignment();
        }
        int lcm = structAccessors[0].getAlignment();
        for (int i=1; i<structAccessors.length; i++)
        {
            lcm = lcm(lcm, structAccessors[i].getAlignment());
        }
        return lcm;
    }
    
    /**
     * Computes the maximum alignment of any of the given 
     * accessors.
     * 
     * @param structAccessors The accessors for the accessors
     * @return The maximum alignment
     */
    private static int computeMaxAlignment(StructAccessor structAccessors[])
    {
        int max = 0;
        for (StructAccessor structAccessor : structAccessors)
        {
            max = Math.max(max, structAccessor.getAlignment());
        }
        return max;
    }
    
    /**
     * Compute all accessors of the given (Struct) class which will be
     * considered, i.e. all public non-volatile accessors.
     * 
     * @param structClass The class
     * @return The public non-volatile accessors of the class.
     */
    private static Field[] computeStructFields(
        Class<? extends Struct> structClass)
    {
        Field fields[] = structClass.getDeclaredFields();
        List<Field> structFieldList = new ArrayList<Field>();
        for (int i=0; i<fields.length; i++)
        {
            Field field = fields[i];
            int modifier = field.getModifiers();
            if ( Modifier.isPublic(modifier) && 
                !Modifier.isVolatile(modifier))
            {
                structFieldList.add(field);
            }
        }
        Field structFields[] = 
            structFieldList.toArray(new Field[structFieldList.size()]);
        return structFields;
    }
    
    /**
     * Returns whether the given AnnotatedElement has an 
     * {@link __attribute__} annotation which declares
     * this element as 'packed:
     * 
     * @param annotatedElement The element
     * @return Whether the element is packed
     */
    public static boolean isPacked(AnnotatedElement annotatedElement)
    {
        if (annotatedElement.isAnnotationPresent(__attribute__.class))
        {
            __attribute__ attribute = 
                annotatedElement.getAnnotation(__attribute__.class); 
            return attribute.packed();
        }
        return false;
    }
    
    /**
     * Returns the alignment of the given AnnotatedElement. If the
     * given element has no {@link __attribute__} annotation, then
     * 0 is returned. 
     * 
     * @param annotatedElement The element
     * @return The alignment
     */
    public static int obtainAlignment(AnnotatedElement annotatedElement)
    {
        int alignment = 0;
        if (annotatedElement.isAnnotationPresent(__attribute__.class))
        {
            __attribute__ attribute = 
                annotatedElement.getAnnotation(__attribute__.class); 
            alignment = attribute.align();
        }
        return alignment;
    }
    
    
    /**
     * Creates the struct accessors (see {@link StructAccessor}) for the
     * given accessors.
     * 
     * @param structFields The accessors
     * @return The struct accessors for the given accessors.
     * @throws CLException If one of the accessors is
     * either an instance of this struct itself, or of a type that
     * is not supported. 
     */
    @SuppressWarnings("unchecked")
    private static StructAccessor[] computeStructAccessors(
        Class<? extends Struct> structClass, Field structFields[])
    {
        boolean isPackedStruct = isPacked(structClass);
        
        StructAccessor structAccessors[]  = 
            new StructAccessor[structFields.length];
        int currentOffset = 0;
        for (int i=0; i<structFields.length; i++)
        {
            Field field = structFields[i];
            int fieldAlignment = obtainAlignment(field);

            // According to the OpenCL spec, declaring a struct
            // as 'packed' is equivalent to declaring each field
            // of the struct as 'packed'
            boolean isPackedField = isPackedStruct;
            if (!isPackedField)
            {
                isPackedField |= isPacked(field);
            }

            // Create a StructAccessor depending on the
            // type of the field.
            StructAccessor structAccessor = null;
            if (field.getType().isPrimitive())
            {
                structAccessor = 
                    StructAccess.createPrimitiveStructAccessor(
                        field, currentOffset, fieldAlignment, 
                        isPackedField);
            }
            else if (cl_vector_type.class.isAssignableFrom(field.getType()))
            {
                structAccessor = 
                    new StructAccessor_cl_vector_type(
                        field, currentOffset, 
                        SizeofStruct.sizeof(field.getType()),
                        fieldAlignment, isPackedField);
            }
            else if (field.getType().isArray())
            {
                if (!field.isAnnotationPresent(ArrayLength.class)) 
                {
                    throw new CLException(
                        "Field "+field+" has no ArrayLength annotation. " +
                   		"The length of an array inside a struct must be " +
                   		"specified by annotating the array with the " +
                   		"@ArrayLength(...) annotation. ");
                }
                int arrayLengths[] = 
                    field.getAnnotation(ArrayLength.class).value();
                int dim = computeArrayDimension(field.getType());
                if (dim != arrayLengths.length)
                {
                    throw new CLException(
                        "Field "+field+" is array with dimension "+
                        dim+" but only "+arrayLengths.length+
                        " dimensions have been specified in the " +
                        "ArrayLength annotation");
                }
                
                Class<?> componentType = 
                    getBaseComponentType(field.getType());
                structAccessor = 
                    StructAccess.createArrayStructAccessor(
                        field, componentType, currentOffset, 
                        arrayLengths, fieldAlignment, isPackedField);
            }
            else if (structClass.isAssignableFrom(field.getType()))
            {
                throw new CLException(
                    "Struct may not contain instances of itself: "+field);
            }
            else if (Struct.class.isAssignableFrom(field.getType()))
            {
                Class<? extends Struct> structType = 
                    (Class<? extends Struct>)field.getType();
                StructInfo structInfo = obtainStructInfo(structType);
                
                if (fieldAlignment == 0)
                {
                    fieldAlignment = computeMaxAlignment(
                        structInfo.getStructAccessors());
                }
                structAccessor = 
                    new StructAccessor_Struct(
                        field, currentOffset, 
                        structInfo.getSize(), 
                        fieldAlignment, isPackedField);
            }
            else
            {
                throw new CLException(
                    "Invalid type in struct: "+field);
            }
            currentOffset = 
                structAccessor.getOffset() + structAccessor.getSize();
            structAccessors[i] = structAccessor;

            //System.out.println("For field "+field.getName()+": "+structAccessor);
        }
        return structAccessors;
    }
    
    /**
     * Compute the size of a struct with the given struct accessors.
     * The final size of the struct will be a multiple of the largest
     * field alignment. 
     *  
     * @param structClass The class of the struct
     * @param structAccessors The struct accessors
     * @return The size of the struct
     */
    private static int computeSize(Class<? extends Struct> structClass, 
        StructAccessor structAccessors[])
    {
        if (structAccessors.length == 0)
        {
            return 0;
        }
        
        // If the struct is 'packed', then its size is simply
        // the sum of all the sizes of its accessors
        boolean isPacked = isPacked(structClass);
        if (isPacked)
        {
            int computedSize = 0;
            for (int i=0; i<structAccessors.length; i++)
            {
                computedSize += structAccessors[i].getSize();
            }
            return computedSize;
        }
        
        // The size of a struct must at least be a perfect multiple
        // of the lowest common multiple (LCM) of its elements
        int alignmentLCM = computeAlignmentLCM(structAccessors);
        
        // The unaligned size of the struct is given by
        // the offset and size of the last field
        StructAccessor structAccessor = 
            structAccessors[structAccessors.length-1];
        int unalignedSize = 
            structAccessor.getOffset()+structAccessor.getSize();

        // Compute the size to be a perfect multiple of the LCM
        int computedSize = 0;
        if (unalignedSize % alignmentLCM == 0)
        {
            computedSize = unalignedSize;
        }
        else
        {
            int n = unalignedSize / alignmentLCM;
            int paddedSize = n * alignmentLCM + alignmentLCM;
            computedSize = paddedSize;
        }
        return computedSize;
    }
    
    
    /**
     * Returns the next power of 2 that is greater or equal to the
     * given value.<br />
     * 
     * @param v The value
     * @return The next power of 2
     */
    /* 
     * According to the OpenCL specification, ...
     * "The alignment of any given struct type is required by the
     * ISO C standard to be at least a perfect multiple of the 
     * lowest common multiple of the alignments of all of the 
     * members of the struct in question and must also be a 
     * power of two. "        
     * The necessity that the alignment must be a power
     * of 2 seems dubious, since no OpenCL compiler seems
     * to obey this rule... 
    private static int nextPow2(int v)
    {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;
        return v;
    }
    */ 
    
    
    /**
     * Creates the struct accessor for the given <b>primitive</b> field with 
     * the given byte offset inside the struct and the given alignment.
     * 
     * @param field The field
     * @param offset The byte offset of the field
     * @param alignment The alignment for the field
     * @param packed If the field is packed
     * @return The StructAccessor
     */
    public static StructAccessor createPrimitiveStructAccessor(
            Field field, int offset, int alignment, boolean packed)
    {
        if (field.getType().equals(Byte.TYPE))
        {
            return new StructAccessor_byte(
                field, offset, alignment, packed);
        }
        else if (field.getType().equals(Short.TYPE))
        {
            return new StructAccessor_short(
                field, offset, alignment, packed);
        }
        else if (field.getType().equals(Character.TYPE))
        {
            return new StructAccessor_char(
                field, offset, alignment, packed);
        }
        else if (field.getType().equals(Integer.TYPE))
        {
            return new StructAccessor_int(
                field, offset, alignment, packed);
        }
        else if (field.getType().equals(Long.TYPE))
        {
            return new StructAccessor_long(
                field, offset, alignment, packed);
        }
        else if (field.getType().equals(Float.TYPE))
        {
            return new StructAccessor_float(
                field, offset, alignment, packed);
        }
        else if (field.getType().equals(Double.TYPE))
        {
            return new StructAccessor_double(
                field, offset, alignment, packed);
        }
        else if (field.getType().equals(Boolean.TYPE))
        {
            return new StructAccessor_boolean(
                field, offset, alignment, packed);
        }
        else 
        {
            throw new CLException("Invalid field type: "+field);
        }
    }

    public static StructAccessor createVectorTypeAccessor(
            Field field, int offset, int size, int alignment, boolean packed) {
        return new StructAccessor_cl_vector_type(field, offset, size, alignment, packed);
    }
    
    /**
     * Computes the product of the given array lengths
     * 
     * @param arrayLengths The array lengths
     * @return The product of the array lengths
     */
    private static int computeTotalArrayLength(int arrayLengths[])
    {
        int total = arrayLengths[0];
        for (int i=1; i<arrayLengths.length; i++)
        {
            total *= arrayLengths[i];
        }
        return total;
    }
    
    /**
     * Creates the struct accessor for the given <b>array</b> 
     * field with the given byte offset inside the struct and 
     * the given alignment.
     * 
     * @param field The field
     * @param offset The byte offset of the field
     * @param arrayLengths The array dimensions
     * @param alignment The alignment for the field
     * @param packed If the field is packed
     * @return The StructAccessor
     */
    public static StructAccessor createArrayStructAccessor(
            Field field, Class<?> componentType, int offset,
            int arrayLengths[], int alignment, boolean packed)
    {
        int totalArrayLength = computeTotalArrayLength(arrayLengths);
        int componentTypeSize = 0;
        ArrayProcessor readProcessor = null;
        ArrayProcessor writeProcessor = null;
        
        if (componentType.equals(Byte.TYPE))
        {
            componentTypeSize = Sizeof.cl_char;
            readProcessor = new ArrayReader_byte();
            writeProcessor = new ArrayWriter_byte();
        }
        else if (componentType.equals(Short.TYPE))
        {
            componentTypeSize = Sizeof.cl_short;
            readProcessor = new ArrayReader_short();
            writeProcessor = new ArrayWriter_short();
        }
        else if (componentType.equals(Character.TYPE))
        {
            componentTypeSize = Sizeof.cl_short;
            readProcessor = new ArrayReader_char();
            writeProcessor = new ArrayWriter_char();
        }
        else if (componentType.equals(Integer.TYPE))
        {
            componentTypeSize = Sizeof.cl_int;
            readProcessor = new ArrayReader_int();
            writeProcessor = new ArrayWriter_int();
        }
        else if (componentType.equals(Long.TYPE))
        {
            componentTypeSize = Sizeof.cl_long;
            readProcessor = new ArrayReader_long();
            writeProcessor = new ArrayWriter_long();
        }
        else if (componentType.equals(Float.TYPE))
        {
            componentTypeSize = Sizeof.cl_float;
            readProcessor = new ArrayReader_float();
            writeProcessor = new ArrayWriter_float();
        }
        else if (componentType.equals(Double.TYPE))
        {
            componentTypeSize = Sizeof.cl_double;
            readProcessor = new ArrayReader_double();
            writeProcessor = new ArrayWriter_double();
        }
        else if (componentType.equals(Boolean.TYPE))
        {
            // TODO: Support this?
            throw new CLException(
                "Boolean arrays in structs are not supported: "+field);
        }
        else if (cl_vector_type.class.isAssignableFrom(componentType))
        {
            componentTypeSize = SizeofStruct.sizeof(componentType);
            readProcessor = new ArrayReader_cl_vector_type();
            writeProcessor = new ArrayWriter_cl_vector_type();
        }
        else if (Struct.class.isAssignableFrom(componentType))
        {
            componentTypeSize = SizeofStruct.sizeof(componentType);
            readProcessor = new ArrayReader_Struct();
            writeProcessor = new ArrayWriter_Struct();
        }
        else
        {
            throw new CLException(
                "Invalid field type: "+field);
        }
        
        if (alignment == 0)
        {
            alignment = componentTypeSize;
        }
        StructAccessor result = new StructAccessor_Array(
            field, offset, totalArrayLength * componentTypeSize, 
            alignment, packed, readProcessor, writeProcessor);
        
        //System.out.println("createPrimitiveArrayStructAccessor: "+result);
        return result;
    }


    /**
     * A class storing the information that is specific
     * for one class that extends the Struct class, 
     * namely the size of the struct, and the 
     * {@link StructAccessor}s
     */
    static class StructInfo
    {
        /**
         * The size of the struct, including paddings that are
         * inserted to obey alignment requirements.
         */
        private int size;

        /**
         * The StructAccessors for the accessors of the struct.
         * See {@link StructAccessor}.
         */
        private StructAccessor structAccessors[];

        /**
         * Creates a new StructInfo for the given class.
         * 
         * @param c The struct class
         */
        StructInfo(Class<? extends Struct> c)
        {
            Field structFields[] = StructAccess.computeStructFields(c);
            structAccessors = 
                StructAccess.computeStructAccessors(c, structFields);
            size = StructAccess.computeSize(c, structAccessors);
        }
        
        /**
         * Return the size of the struct, including paddings that are
         * inserted to obey alignment requirements.
         * 
         * @return The size of the struct
         */
        int getSize()
        {
            return size;
        }

        /**
         * Return the default struct accessors for instances of the struct.
         * See {@link StructAccessor}.
         * 
         * @return The struct accessors.
         */
        StructAccessor[] getStructAccessors()
        {
            return structAccessors;
        }
    }
    
    
    
    /**
     * Abstract base class for all StructAccessors. A struct accessor is
     * an entity that allows accessing (reading or writing) a certain
     * field inside a Struct. 
     */
    public static abstract class StructAccessor
    {
        /**
         * The field that is accessed
         */
        protected Field field;
        
        /**
         * The offset from the beginning of the struct, in bytes
         */
        protected int offset;
        
        /**
         * The size of the field, in bytes
         */
        private int size;
        
        /**
         * The alignment required for the field. 
         */
        private int alignment;

        /**
         * Whether this field is 'packed' via the corresponding
         * attribute as described in the OpenCL specification
         */
        private boolean packed;
        

        /**
         * Creates a new StructAccessor for the given field, which 
         * has the given offset from the beginning of the struct,
         * and the given size (in bytes). <br />
         * <br />
         * If 'packed' is true, then the alignment will be set 
         * to 1. Otherwise, if the given alignment is 0, then 
         * the default alignment will be used, which is equal 
         * to the size. Otherwise, the field will be aligned 
         * according to the given alignment value. 
         * 
         * @param field The field
         * @param offset The offset
         * @param size The size
         * @param alignment The alignment
         * @param packed If the field is packed
         */
        protected StructAccessor(
            Field field, int offset, int size, int alignment, boolean packed)
        {
            this.field = field;
            if (packed)
            {
                this.offset = offset;
                this.alignment = 1;
            }
            else
            {
                if (alignment == 0)
                {
                    this.alignment = size;
                }
                else
                {
                    this.alignment = alignment;
                }
                this.offset = computeAligmentOffset(offset, this.alignment);
            }
            this.size = size;
            this.packed = packed;
        }
        
        /**
         * Compute the offset that is necessary to achieve
         * the specified alignment for the field.
         * 
         * @param offset The current offset
         * @param alignment The desired alignment
         * @return The total offset to achieve the alignment
         */
        protected static int computeAligmentOffset(int offset, int alignment)
        {
            return (offset + alignment - 1) & ~(alignment - 1);
        }
        
        /**
         * Writes the field that is represented by this StructAccessor
         * to the given buffer.
         * 
         * @param object The object to obtain the field value from
         * @param targetBuffer The buffer to write the field to.
         * @throws IllegalArgumentException If the field can not be accessed
         * @throws IllegalAccessException If the field can not be accessed 
         */
        public abstract void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException;

        /**
         * Reads the field that is represented by this StructAccessor
         * from the given buffer.
         * 
         * @param object The object to assign the field value to
         * @param sourceBuffer The buffer to read the field from.
         * @throws IllegalArgumentException If the field can not be accessed
         * @throws IllegalAccessException If the field can not be accessed 
         */
        public abstract void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException;

        /**
         * Returns the field of this StructAccessor
         * 
         * @return The field of this StructAccessor
         */
        public Field getField()
        {
            return field;
        }
        
        /**
         * Returns the offset of the field represented by this 
         * StructAccessor, from the beginning of the struct, 
         * in bytes.
         * 
         * @return The offset of the field.
         */
        public int getOffset()
        {
            return offset;
        }

        /**
         * Returns the size of the field represented by this
         * StructAccessor, in bytes.
         * 
         * @return The size of the field.
         */
        public int getSize()
        {
            return size;
        } 
        
        /**
         * Returns whether this field is packed
         * 
         * @return Whether this field is packed
         */
        public boolean isPacked()
        {
            return packed;
        }
        
        
        /**
         * Returns the alignment required for the field
         * represented by this StructAccessor, in bytes.
         * 
         * @return The alignment for the field
         */
        public int getAlignment()
        {
            return alignment;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName()+
                "[field="+field+
                ",offset="+offset+
                ",size="+size+
                ",alignment="+alignment+
                ",packed="+packed+"]";
        }
    }

    
    
    /**
     * Implementation of a StructAccessor for byte accessors
     */
    static class StructAccessor_byte extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_byte(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_char, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            //System.out.println("write "+field+" to   "+targetBuffer+" value "+field.getByte(object));
            targetBuffer.put(field.getByte(object));
        }
        
        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            //System.out.println("read  "+field+" from "+sourceBuffer+" value "+sourceBuffer.get(sourceBuffer.position()));
            field.setByte(object, sourceBuffer.get());
        }
        
    }

    /**
     * Implementation of a StructAccessor for short accessors
     */
    static class StructAccessor_short extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_short(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_short, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            targetBuffer.putShort(field.getShort(object));
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            field.setShort(object, sourceBuffer.getShort());
        }

    }

    /**
     * Implementation of a StructAccessor for char accessors
     */
    static class StructAccessor_char extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_char(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_short, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            targetBuffer.putChar(field.getChar(object));
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            field.setChar(object, sourceBuffer.getChar());
        }

    }


    /**
     * Implementation of a StructAccessor for int accessors
     */
    static class StructAccessor_int extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_int(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_int, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            //System.out.println("write "+field+" to   "+targetBuffer+" value "+field.getInt(object));
            targetBuffer.putInt(field.getInt(object));
            //System.out.println("In buffer now "+targetBuffer.getInt(targetBuffer.position()-4));
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            //System.out.println("read  "+field+" from "+sourceBuffer+" value "+sourceBuffer.getInt(sourceBuffer.position()));
            field.setInt(object, sourceBuffer.getInt());
        }

    }

    /**
     * Implementation of a StructAccessor for long accessors
     */
    static class StructAccessor_long extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_long(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_long, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            targetBuffer.putLong(field.getLong(object));
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            field.setLong(object, sourceBuffer.getLong());
        }

    }



    /**
     * Implementation of a StructAccessor for float accessors
     */
    static class StructAccessor_float extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_float(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_float, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            targetBuffer.putFloat(field.getFloat(object));
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            //System.out.println("Setting "+field+" (offset "+offset+") to "+sourceBuffer.getFloat(offset));
            field.setFloat(object, sourceBuffer.getFloat());
        }

    }


    /**
     * Implementation of a StructAccessor for double accessors
     */
    static class StructAccessor_double extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_double(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_double, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            targetBuffer.putDouble(field.getDouble(object));
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            field.setDouble(object, sourceBuffer.getDouble());
        }

    }


    /**
     * Implementation of a StructAccessor for boolean accessors
     */
    static class StructAccessor_boolean extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_boolean(
            Field field, int offset, int alignment, boolean packed)
        {
            super(field, offset, Sizeof.cl_int, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            int value = 0;
            if (field.getBoolean(object))
            {
                value = 0xFFFFFFFF;
            }
            targetBuffer.putInt(value);
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            
            int i = sourceBuffer.getInt();
            if (i==0)
            {
                field.setBoolean(object, false);
            }
            else
            {
                field.setBoolean(object, true);
            }
        }
    }





    /**
     * Implementation of a StructAccessor for OpenCL vector type 
     * accessors (e.g. {@link CLTypes.cl_float4}).
     */
    static class StructAccessor_cl_vector_type extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param size The size of the type that is accessed.
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_cl_vector_type(
            Field field, int offset, int size, int alignment, boolean packed)
        {
            super(field, offset, size, alignment, packed);
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            cl_vector_type value = (cl_vector_type)field.get(object);
            if (value == null)
            {
                throw new NullPointerException(
                    "Field "+field.getName()+" was null in structure "+object);
            }
            value.writeThisToBuffer(targetBuffer);
        }
        
        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            cl_vector_type value = (cl_vector_type)field.get(object);
            if (value == null)
            {
                throw new NullPointerException(
                    "Field "+field.getName()+" was null in structure "+object);
            }
            value.readThisFromBuffer(sourceBuffer);
        }
    }
    
    
    
    /**
     * Implementation of a StructAccessor for nested Structs
     */
    static class StructAccessor_Struct extends StructAccessor
    {
        /**
         * Creates a new StructAccessor for the given field
         * 
         * @param field The field to access
         * @param offset The offset
         * @param size The size of the type that is accessed.
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         */
        StructAccessor_Struct(
            Field field, int offset, int size, int alignment, boolean packed)
        {
            super(field, offset, size, alignment, packed);  
        }
        
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            Struct value = (Struct)field.get(object);
            if (value == null)
            {
                throw new NullPointerException(
                    "Field "+field.getName()+" was null in structure "+object);
            }
            //System.out.println("Writing "+field+" into "+targetBuffer);
            value.writeThisToBuffer(targetBuffer);
        }
        
        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            Struct value = (Struct)field.get(object);
            if (value == null)
            {
                throw new NullPointerException(
                    "Field "+field.getName()+" was null in structure "+object);
            }
            //System.out.println("Reading "+field+" from "+sourceBuffer);
            value.readThisFromBuffer(sourceBuffer);
        }
    }
    
    
    
    
    
    /**
     * Implementation of a StructAccessor for arrays
     */
    static class StructAccessor_Array extends StructAccessor
    {
        /**
         * The ArrayProcessor which will read the contents 
         * of the array from a buffer
         */
        private ArrayProcessor readProcessor;

        /**
         * The ArrayProcessor which will write the contents 
         * of the array to a buffer
         */
        private ArrayProcessor writeProcessor;
        
        
        /**
         * Creates a new StructAccessor for the given field. The given 
         * ArrayProcessors will be used to process the arrays that
         * are represented by this field.
         * 
         * @param field The field to access
         * @param offset The offset
         * @param size The length of the array
         * @param alignment The alignment for the field
         * @param packed If the field is packed
         * @param readProcessor The readProcessor
         * @param writeProcessor The writeProcessor
         */
        StructAccessor_Array(
            Field field, int offset, int size, int alignment, boolean packed, 
            ArrayProcessor readProcessor, ArrayProcessor writeProcessor)
        {
            super(field, offset, size, alignment, packed);
            this.readProcessor = readProcessor;
            this.writeProcessor = writeProcessor;
        }
        
        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            Object array = field.get(object);
            writeProcessor.process(array, targetBuffer);
        }
        
        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer)
            throws IllegalArgumentException, IllegalAccessException
        {
            Object array = field.get(object);
            readProcessor.process(array, sourceBuffer);
        }
        
    }

    
    /**
     * Abstract base class for classes that may process multidimensional
     * arrays types recursively.
     */
    abstract static class ArrayProcessor
    {
        /**
         * Processes the given array object recursively, and calls
         * doProcess with the final array
         * 
         * @param arrayObject The array object to process
         * @param buffer The buffer
         */
        public void process(Object arrayObject, ByteBuffer buffer)
        {
            if (arrayObject.getClass().getComponentType().isArray())
            {
                int length = Array.getLength(arrayObject);
                for (int i=0; i<length; i++)
                {
                    Object element = Array.get(arrayObject, i);
                    process(element, buffer);
                }
            }
            else
            {
                doProcess(arrayObject, buffer);
            }
        }
        
        /**
         * Processes the given array object. This method will be
         * called for arrays that do not contain further array 
         * objects, i.e. for the recursion base case.
         * 
         * @param arrayObject The array object
         * @param buffer The buffer
         */
        abstract void doProcess(Object arrayObject, ByteBuffer buffer);
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_byte extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            byte array[] = (byte[])arrayObject;
            targetBuffer.put(array);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_short extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            short array[] = (short[])arrayObject;
            targetBuffer.asShortBuffer().put(array);
            targetBuffer.position(
                targetBuffer.position()+array.length*Sizeof.cl_short);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_char extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            char array[] = (char[])arrayObject;
            targetBuffer.asCharBuffer().put(array);
            targetBuffer.position(
                targetBuffer.position()+array.length*Sizeof.cl_short);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_int extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            int array[] = (int[])arrayObject;
            targetBuffer.asIntBuffer().put(array);
            targetBuffer.position(
                targetBuffer.position()+array.length*Sizeof.cl_int);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_long extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            long array[] = (long[])arrayObject;
            targetBuffer.asLongBuffer().put(array);
            targetBuffer.position(
                targetBuffer.position()+array.length*Sizeof.cl_long);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_float extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            float array[] = (float[])arrayObject;
            targetBuffer.asFloatBuffer().put(array);
            targetBuffer.position(
                targetBuffer.position()+array.length*Sizeof.cl_float);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_double extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            double array[] = (double[])arrayObject;
            targetBuffer.asDoubleBuffer().put(array);
            targetBuffer.position(
                targetBuffer.position()+array.length*Sizeof.cl_double);
        }
    }
    
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_byte extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            byte array[] = (byte[])arrayObject;
            sourceBuffer.get(array);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_short extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            short array[] = (short[])arrayObject;
            sourceBuffer.asShortBuffer().get(array);
            sourceBuffer.position(
                sourceBuffer.position()+array.length*Sizeof.cl_short);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_char extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            char array[] = (char[])arrayObject;
            sourceBuffer.asCharBuffer().get(array);
            sourceBuffer.position(
                sourceBuffer.position()+array.length*Sizeof.cl_short);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_int extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            int array[] = (int[])arrayObject;
            sourceBuffer.asIntBuffer().get(array);
            sourceBuffer.position(
                sourceBuffer.position()+array.length*Sizeof.cl_int);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_long extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            long array[] = (long[])arrayObject;
            sourceBuffer.asLongBuffer().get(array);
            sourceBuffer.position(
                sourceBuffer.position()+array.length*Sizeof.cl_long);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_float extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            float array[] = (float[])arrayObject;
            sourceBuffer.asFloatBuffer().get(array);
            sourceBuffer.position(
                sourceBuffer.position()+array.length*Sizeof.cl_float);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_double extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            double array[] = (double[])arrayObject;
            sourceBuffer.asDoubleBuffer().get(array);
            sourceBuffer.position(
                sourceBuffer.position()+array.length*Sizeof.cl_double);
        }
    }
    

    
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_cl_vector_type extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            cl_vector_type array[] = (cl_vector_type[])arrayObject;
            Buffers.writeToBuffer(targetBuffer, array);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_cl_vector_type extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            cl_vector_type array[] = (cl_vector_type[])arrayObject;
            Buffers.readFromBuffer(sourceBuffer, array);
        }
    }
    

    
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayWriter_Struct extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer targetBuffer)
        {
            Struct array[] = (Struct[])arrayObject;
            Buffers.writeToBuffer(targetBuffer, array);
        }
    }
    
    /**
     * Implementation of an ArrayProcessor
     */
    static class ArrayReader_Struct extends ArrayProcessor
    {
        @Override
        void doProcess(Object arrayObject, ByteBuffer sourceBuffer)
        {
            Struct array[] = (Struct[])arrayObject;
            Buffers.readFromBuffer(sourceBuffer, array);
        }
    }
    
    
}
