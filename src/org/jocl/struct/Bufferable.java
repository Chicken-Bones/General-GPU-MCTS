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

import java.nio.*;

/**
 * Package-private abstract base class for Structs and the
 * cl_vector_types. This class summarizes the methods that
 * are required for reading or writing an object from or
 * to a byte buffer.<br />
 * <br />
 * This should be considered as an implementation detail,
 * and thus is NOT visible through a public interface.
 */
abstract class Bufferable
{
    /**
     * Writes this object to the given target buffer.
     * 
     * @param targetBuffer The target buffer
     */
    abstract void writeThisToBuffer(ByteBuffer targetBuffer);

    /**
     * Reads the contents of this object from the given buffer.
     * 
     * @param sourceBuffer The source buffer.
     */
    abstract void readThisFromBuffer(ByteBuffer sourceBuffer);
    
    /**
     * Returns the size of this object, in bytes
     * 
     * @return The size of this object, in bytes
     */
    abstract int getSize();
}
