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
 * This annotation is an emulation of the __attribute__ 
 * annotation of OpenCL.
 * 
 * TODO: This is highly experimental, and not yet made 
 * public
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@interface __attribute__
{
    int align() default 0; 
    boolean packed() default false;
}
