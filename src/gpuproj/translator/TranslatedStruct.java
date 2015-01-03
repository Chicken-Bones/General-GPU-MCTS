package gpuproj.translator;

import gpuproj.srctree.*;
import org.jocl.CLException;
import org.jocl.struct.SizeofStruct;
import org.jocl.struct.StructAccess;
import org.jocl.struct.StructAccess.StructAccessor;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Parallel to StructInfo, but build from any ClassSymbol, doesn't have to extend Struct
 */
public class TranslatedStruct
{
    public static class TranslatedStructAccessor extends StructAccessor
    {
        public final TranslatedStruct struct;

        public TranslatedStructAccessor(TranslatedStruct struct, Field field, int currentOffset, int size, int fieldAlignment, boolean isPackedField) {
            super(field, currentOffset, size, fieldAlignment, isPackedField);
            this.struct = struct;
        }

        @Override
        public void writeToBuffer(Object object, ByteBuffer targetBuffer) throws IllegalArgumentException, IllegalAccessException {
            struct.write(object, targetBuffer);
        }

        @Override
        public void readFromBuffer(Object object, ByteBuffer sourceBuffer) throws IllegalArgumentException, IllegalAccessException {
            struct.read(object, sourceBuffer);
        }
    }

    private static Map<ClassSymbol, TranslatedStruct> cache = new HashMap<>();
    public static TranslatedStruct translate(ClassSymbol type) {
        TranslatedStruct struct = cache.get(type);
        if(struct == null)
            cache.put(type, struct = new TranslatedStruct(type));

        return struct;
    }

    public final ClassSymbol type;
    public List<StructAccessor> accessors = new LinkedList<>();
    public int size;

    public TranslatedStruct(ClassSymbol type) {
        this.type = type;
        createAccessors(type, accessors);
    }

    public static void getStructFields(ClassSymbol type, List<FieldSymbol> fields) {
        if(type.parent != null)
            getStructFields(type.parent.classType(), fields);

        for(FieldSymbol field : type.fields)
            if(!field.isStatic())
                fields.add(field);
    }

    private static Stack<ClassSymbol> stack = new Stack<>();
    public static void createAccessors(ClassSymbol classSym, List<StructAccessor> accessors) {
        if(stack.contains(classSym))
            throw new IllegalArgumentException("Structs cannot contain recursive members");

        stack.push(classSym);

        List<FieldSymbol> fields = new LinkedList<>();
        getStructFields(classSym, fields);

        boolean isPackedStruct = StructAccess.isPacked(classSym.runtimeClass());
        int currentOffset = 0;

        for (FieldSymbol sym : fields) {
            Field field = sym.runtimeField();
            int fieldAlignment = StructAccess.obtainAlignment(field);

            // According to the OpenCL spec, declaring a struct
            // as 'packed' is equivalent to declaring each field
            // of the struct as 'packed'
            boolean isPackedField = isPackedStruct;
            if (!isPackedField)
                isPackedField = StructAccess.isPacked(field);

            // Create a StructAccessor depending on the
            // type of the field.
            StructAccessor structAccessor = null;
            TypeSymbol type = sym.type.concrete();
            if (type instanceof PrimitiveSymbol)
                structAccessor = StructAccess.createPrimitiveStructAccessor(
                                field, currentOffset, fieldAlignment,
                                isPackedField);
            else if (type instanceof ArraySymbol) {
                int[] dimensions = Translator.getArrayDimensions(sym);

                TypeSymbol baseType = type;
                while(baseType instanceof ArraySymbol)
                    baseType = ((ArraySymbol) baseType).type;
                structAccessor = StructAccess.createArrayStructAccessor(
                                field, baseType.runtimeClass(), currentOffset,
                                dimensions, fieldAlignment, isPackedField);
            }
            else if (type.isAssignableTo(TypeIndex.resolveType("org.jocl.struct.CLTypes.cl_vector_type")))
                structAccessor = StructAccess.createVectorTypeAccessor(
                        field, currentOffset, SizeofStruct.sizeof(field.getType()),
                        fieldAlignment, isPackedField);
            else {
                TranslatedStruct struct = translate((ClassSymbol) type);
                if (fieldAlignment == 0)
                    for(StructAccessor accessor : accessors)
                        fieldAlignment = Math.max(fieldAlignment, accessor.getAlignment());

                structAccessor = new TranslatedStructAccessor(
                                struct,
                                field, currentOffset,
                                struct.size,
                                fieldAlignment, isPackedField);
            }
            currentOffset = structAccessor.getOffset() + structAccessor.getSize();
            accessors.add(structAccessor);
        }
        stack.pop();
    }

    public void write(Object obj, ByteBuffer buf) {
        int basepos = buf.position();
        for (StructAccessor field : accessors) {
            try {
                buf.position(basepos + field.getOffset());
                field.writeToBuffer(obj, buf);
            } catch (Exception e) {
                throw new CLException("Could not write field " + field.getField() + " of structure " + this, e);
            }
        }
        buf.position(basepos + size);
    }

    public void read(Object obj, ByteBuffer buf) {
        int basepos = buf.position();
        for (StructAccessor field : accessors) {
            try {
                buf.position(basepos + field.getOffset());
                field.readFromBuffer(obj, buf);
            } catch (Exception e) {
                throw new CLException("Could not write field " + field.getField() + " of structure " + this, e);
            }
        }
        buf.position(basepos + size);
    }
}
