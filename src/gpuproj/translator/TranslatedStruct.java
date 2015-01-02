package gpuproj.translator;

import gpuproj.srctree.ClassSymbol;
import gpuproj.srctree.FieldSymbol;
import org.jocl.CLException;
import org.jocl.struct.StructAccess.StructAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Parallel to StructInfo, but build from any ClassSymbol, doesn't have to extend Struct
 */
public class TranslatedStruct
{
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

    public static void createAccessors(ClassSymbol type, List<StructAccessor> accessors) {
        List<FieldSymbol> fields = new LinkedList<>();
        getStructFields(type, fields);
        for (FieldSymbol field : fields) {

        }
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
