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



/**
 * This class defines the built-in OpenCL vector types for the
 * platform that uses OpenCL.
 */
public final class CLTypes
{
    /*
     * Implementation note: Different implementations for the
     * vector types have been tested. Using a backing array
     * seems to be the best tradeoff between the time 
     * required for creating instances of these types,
     * filling them, and reading/writing them from and to
     * buffers.
     */
    
    /**
     * The base type for all OpenCL vector types
     */
    static abstract class cl_vector_type extends Bufferable
    {
    }


    /**
     * Base class for all cl_char&lt;n&gt; types
     */
    private static abstract class cl_char_type extends cl_vector_type
    {
        /**
         * The array that backs this cl_vector_type
         */
        private byte array[];

        /**
         * Creates a new vector type with the given array
         * 
         * @param array The backing array
         */
        protected cl_char_type(byte array[])
        {
            this.array = array;
        }

        /**
         * Returns the element at the given index of this
         * cl_vector type.
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @return The element at the specified index.
         */
        public byte get(int index)
        {
            return array[index];
        }

        /**
         * Sets the given value at the given index of this
         * cl_vector_type
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @param value The value to set at the specified index.
         */
        public void set(int index, byte value)
        {
            array[index] = value;
        }

        @Override
        int getSize()
        {
            return array.length;
        }

        @Override
        void readThisFromBuffer(ByteBuffer sourceBuffer)
        {
            sourceBuffer.get(array);
        }

        @Override
        void writeThisToBuffer(ByteBuffer targetBuffer)
        {
            targetBuffer.put(array);
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i=0; i<array.length; i++)
            {
                sb.append(String.valueOf(array[i]));
                if (i < array.length-1)
                {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static final class cl_char   extends cl_char_type { public cl_char   () { super(new byte[1  ]); } public byte get() { return get(0); }  public void set(byte value) { set(0, value); } }
    public static final class cl_char2  extends cl_char_type { public cl_char2  () { super(new byte[2  ]); } }
    public static final class cl_char4  extends cl_char_type { public cl_char4  () { super(new byte[4  ]); } }
    public static final class cl_char8  extends cl_char_type { public cl_char8  () { super(new byte[8  ]); } }
    public static final class cl_char16 extends cl_char_type { public cl_char16 () { super(new byte[16 ]); } }

    private static abstract class cl_uchar_type extends cl_char_type { cl_uchar_type(byte array[]) { super(array); } }

    public static final class cl_uchar   extends cl_uchar_type { public cl_uchar   () { super(new byte[1  ]); } public byte get() { return get(0); }  public void set(byte value) { set(0, value); } }
    public static final class cl_uchar2  extends cl_uchar_type { public cl_uchar2  () { super(new byte[2  ]); } }
    public static final class cl_uchar4  extends cl_uchar_type { public cl_uchar4  () { super(new byte[4  ]); } }
    public static final class cl_uchar8  extends cl_uchar_type { public cl_uchar8  () { super(new byte[8  ]); } }
    public static final class cl_uchar16 extends cl_uchar_type { public cl_uchar16 () { super(new byte[16 ]); } }




    /**
     * Base class for all cl_short&lt;n&gt; types
     */
    private static abstract class cl_short_type extends cl_vector_type
    {
        /**
         * The array that backs this cl_vector_type
         */
        private short array[];

        /**
         * Creates a new vector type with the given array
         * 
         * @param array The backing array
         */
        protected cl_short_type(short array[])
        {
            this.array = array;
        }

        /**
         * Returns the element at the given index of this
         * cl_vector type.
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @return The element at the specified index.
         */
        public short get(int index)
        {
            return array[index];
        }

        /**
         * Sets the given value at the given index of this
         * cl_vector_type
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @param value The value to set at the specified index.
         */
        public void set(int index, short value)
        {
            array[index] = value;
        }

        @Override
        int getSize()
        {
            return array.length << 1;
        }

        @Override
        void readThisFromBuffer(ByteBuffer sourceBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                array[i] = sourceBuffer.getShort();
            }
        }

        @Override
        void writeThisToBuffer(ByteBuffer targetBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                targetBuffer.putShort(array[i]);
            }
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i=0; i<array.length; i++)
            {
                sb.append(String.valueOf(array[i]));
                if (i < array.length-1)
                {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static final class cl_short   extends cl_short_type { public cl_short   () { super(new short[1  ]); } public short get() { return get(0); }  public void set(short value) { set(0, value); } }
    public static final class cl_short2  extends cl_short_type { public cl_short2  () { super(new short[2  ]); } }
    public static final class cl_short4  extends cl_short_type { public cl_short4  () { super(new short[4  ]); } }
    public static final class cl_short8  extends cl_short_type { public cl_short8  () { super(new short[8  ]); } }
    public static final class cl_short16 extends cl_short_type { public cl_short16 () { super(new short[16 ]); } }

    private static abstract class cl_ushort_type extends cl_short_type { cl_ushort_type(short array[]) { super(array); } }

    public static final class cl_ushort   extends cl_ushort_type { public cl_ushort   () { super(new short[1  ]); } public short get() { return get(0); }  public void set(short value) { set(0, value); } }
    public static final class cl_ushort2  extends cl_ushort_type { public cl_ushort2  () { super(new short[2  ]); } }
    public static final class cl_ushort4  extends cl_ushort_type { public cl_ushort4  () { super(new short[4  ]); } }
    public static final class cl_ushort8  extends cl_ushort_type { public cl_ushort8  () { super(new short[8  ]); } }
    public static final class cl_ushort16 extends cl_ushort_type { public cl_ushort16 () { super(new short[16 ]); } }




    /**
     * Base class for all cl_int&lt;n&gt; types
     */
    private static abstract class cl_int_type extends cl_vector_type
    {
        /**
         * The array that backs this cl_vector_type
         */
        private int array[];

        /**
         * Creates a new vector type with the given array
         * 
         * @param array The backing array
         */
        protected cl_int_type(int array[])
        {
            this.array = array;
        }

        /**
         * Returns the element at the given index of this
         * cl_vector type.
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @return The element at the specified index.
         */
        public int get(int index)
        {
            return array[index];
        }

        /**
         * Sets the given value at the given index of this
         * cl_vector_type
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @param value The value to set at the specified index.
         */
        public void set(int index, int value)
        {
            array[index] = value;
        }

        @Override
        int getSize()
        {
            return array.length << 2;
        }

        @Override
        void readThisFromBuffer(ByteBuffer sourceBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                array[i] = sourceBuffer.getInt();
            }
        }

        @Override
        void writeThisToBuffer(ByteBuffer targetBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                targetBuffer.putInt(array[i]);
            }
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i=0; i<array.length; i++)
            {
                sb.append(String.valueOf(array[i]));
                if (i < array.length-1)
                {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static final class cl_int   extends cl_int_type { public cl_int   () { super(new int[1  ]); } public int get() { return get(0); }  public void set(int value) { set(0, value); } }
    public static final class cl_int2  extends cl_int_type { public cl_int2  () { super(new int[2  ]); } }
    public static final class cl_int4  extends cl_int_type { public cl_int4  () { super(new int[4  ]); } }
    public static final class cl_int8  extends cl_int_type { public cl_int8  () { super(new int[8  ]); } }
    public static final class cl_int16 extends cl_int_type { public cl_int16 () { super(new int[16 ]); } }

    private static abstract class cl_uint_type extends cl_int_type { cl_uint_type(int array[]) { super(array); } }

    public static final class cl_uint   extends cl_uint_type { public cl_uint   () { super(new int[1  ]); } public int get() { return get(0); }  public void set(int value) { set(0, value); } }
    public static final class cl_uint2  extends cl_uint_type { public cl_uint2  () { super(new int[2  ]); } }
    public static final class cl_uint4  extends cl_uint_type { public cl_uint4  () { super(new int[4  ]); } }
    public static final class cl_uint8  extends cl_uint_type { public cl_uint8  () { super(new int[8  ]); } }
    public static final class cl_uint16 extends cl_uint_type { public cl_uint16 () { super(new int[16 ]); } }





    /**
     * Base class for all cl_long&lt;n&gt; types
     */
    private static abstract class cl_long_type extends cl_vector_type
    {
        /**
         * The array that backs this cl_vector_type
         */
        private long array[];

        protected cl_long_type(long array[])
        {
            this.array = array;
        }

        /**
         * Creates a new vector type with the given array
         * 
         * @param array The backing array
         */
        /**
         * Returns the element at the given index of this
         * cl_vector type.
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @return The element at the specified index.
         */
        public long get(int index)
        {
            return array[index];
        }

        /**
         * Sets the given value at the given index of this
         * cl_vector_type
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @param value The value to set at the specified index.
         */
        public void set(int index, long value)
        {
            array[index] = value;
        }

        @Override
        int getSize()
        {
            return array.length << 3;
        }

        @Override
        void readThisFromBuffer(ByteBuffer sourceBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                array[i] = sourceBuffer.getLong();
            }
        }

        @Override
        void writeThisToBuffer(ByteBuffer targetBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                targetBuffer.putLong(array[i]);
            }
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i=0; i<array.length; i++)
            {
                sb.append(String.valueOf(array[i]));
                if (i < array.length-1)
                {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static final class cl_long   extends cl_long_type { public cl_long   () { super(new long[1  ]); } public long get() { return get(0); }  public void set(long value) { set(0, value); } }
    public static final class cl_long2  extends cl_long_type { public cl_long2  () { super(new long[2  ]); } }
    public static final class cl_long4  extends cl_long_type { public cl_long4  () { super(new long[4  ]); } }
    public static final class cl_long8  extends cl_long_type { public cl_long8  () { super(new long[8  ]); } }
    public static final class cl_long16 extends cl_long_type { public cl_long16 () { super(new long[16 ]); } }

    private static abstract class cl_ulong_type extends cl_long_type { cl_ulong_type(long array[]) { super(array); } }

    public static final class cl_ulong   extends cl_ulong_type { public cl_ulong   () { super(new long[1  ]); } public long get() { return get(0); }  public void set(long value) { set(0, value); } }
    public static final class cl_ulong2  extends cl_ulong_type { public cl_ulong2  () { super(new long[2  ]); } }
    public static final class cl_ulong4  extends cl_ulong_type { public cl_ulong4  () { super(new long[4  ]); } }
    public static final class cl_ulong8  extends cl_ulong_type { public cl_ulong8  () { super(new long[8  ]); } }
    public static final class cl_ulong16 extends cl_ulong_type { public cl_ulong16 () { super(new long[16 ]); } }





    /**
     * Base class for all cl_float&lt;n&gt; types
     */
    private static abstract class cl_float_type extends cl_vector_type
    {
        /**
         * The array that backs this cl_vector_type
         */
        private float array[];

        /**
         * Creates a new vector type with the given array
         * 
         * @param array The backing array
         */
        protected cl_float_type(float array[])
        {
            this.array = array;
        }

        /**
         * Returns the element at the given index of this
         * cl_vector type.
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @return The element at the specified index.
         */
        public float get(int index)
        {
            return array[index];
        }

        /**
         * Sets the given value at the given index of this
         * cl_vector_type
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @param value The value to set at the specified index.
         */
        public void set(int index, float value)
        {
            array[index] = value;
        }

        @Override
        int getSize()
        {
            return array.length << 2;
        }

        @Override
        void readThisFromBuffer(ByteBuffer sourceBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                array[i] = sourceBuffer.getFloat();
            }
        }

        @Override
        void writeThisToBuffer(ByteBuffer targetBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                targetBuffer.putFloat(array[i]);
            }
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i=0; i<array.length; i++)
            {
                sb.append(String.valueOf(array[i]));
                if (i < array.length-1)
                {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static final class cl_float   extends cl_float_type { public cl_float   () { super(new float[1  ]); } public float get() { return get(0); }  public void set(float value) { set(0, value); } }
    public static final class cl_float2  extends cl_float_type { public cl_float2  () { super(new float[2  ]); } }
    public static final class cl_float4  extends cl_float_type { public cl_float4  () { super(new float[4  ]); } }
    public static final class cl_float8  extends cl_float_type { public cl_float8  () { super(new float[8  ]); } }
    public static final class cl_float16 extends cl_float_type { public cl_float16 () { super(new float[16 ]); } }





    /**
     * Base class for all cl_double&lt;n&gt; types
     */
    private static abstract class cl_double_type extends cl_vector_type
    {
        /**
         * The array that backs this cl_vector_type
         */
        private double array[];

        /**
         * Creates a new vector type with the given array
         * 
         * @param array The backing array
         */
        protected cl_double_type(double array[])
        {
            this.array = array;
        }

        /**
         * Returns the element at the given index of this
         * cl_vector type.
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @return The element at the specified index.
         */
        public double get(int index)
        {
            return array[index];
        }

        /**
         * Sets the given value at the given index of this
         * cl_vector_type
         * 
         * @param index The index. Must be in a valid range
         * for this vector type, otherwise the behavior
         * of this method is undefined.
         * @param value The value to set at the specified index.
         */
        public void set(int index, double value)
        {
            array[index] = value;
        }

        @Override
        int getSize()
        {
            return array.length << 3;
        }

        @Override
        void readThisFromBuffer(ByteBuffer sourceBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                array[i] = sourceBuffer.getDouble();
            }
        }

        @Override
        void writeThisToBuffer(ByteBuffer targetBuffer)
        {
            for (int i=0; i<array.length; i++)
            {
                targetBuffer.putDouble(array[i]);
            }
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i=0; i<array.length; i++)
            {
                sb.append(String.valueOf(array[i]));
                if (i < array.length-1)
                {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static final class cl_double   extends cl_double_type { public cl_double   () { super(new double[1  ]); } public double get() { return get(0); }  public void set(double value) { set(0, value); } }
    public static final class cl_double2  extends cl_double_type { public cl_double2  () { super(new double[2  ]); } }
    public static final class cl_double4  extends cl_double_type { public cl_double4  () { super(new double[4  ]); } }
    public static final class cl_double8  extends cl_double_type { public cl_double8  () { super(new double[8  ]); } }
    public static final class cl_double16 extends cl_double_type { public cl_double16 () { super(new double[16 ]); } }



    /**
     * Private constructor to prevent instantiation
     */
    private CLTypes()
    {}

}



