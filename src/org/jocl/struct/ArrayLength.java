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

import java.lang.annotation.*;


/**
 * This annotation may be used to specify the length of arrays inside
 * a {@link Struct}. <br />
 * <br />
 * Example:
 * <code><pre>
 * public class Data extends Struct
 * {
 *     // A 3D position
 *     &#064;ArrayLength(3)
 *     public float position[];
 *     
 *     // A 3x3 matrix
 *     &#064;ArrayLength({3,3})
 *     public float matrix[][];
 * }
 * </pre></code>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ArrayLength 
{
    /**
     * The values given as the array dimensions
     * 
     * @return The array dimensions
     */
    int[] value(); 
}
