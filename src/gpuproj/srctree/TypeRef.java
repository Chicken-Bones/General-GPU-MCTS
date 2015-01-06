package gpuproj.srctree;

import java.util.*;

/**
 * A reference to a TypeSymbol, specifies values for parameters
 * Can be modified to represent a pointer, and corresponding C class
 */
public class TypeRef
{
    /**
     * If this flag is set, TypeRefs, statements and expressions will print openCL code instead of Java code with toString methods
     */
    public static boolean printCL = false;

    //OpenCL reference modifiers
    public static final int GLOBAL = 1;
    public static final int LOCAL = 2;
    public static final int CONSTANT = 4;
    public static final int UNSIGNED = 8;

    /**
     * The type being referenced
     */
    public final TypeSymbol type;
    /**
     * Values for the parameters of type. May be empty for unspecified parameters
     */
    public List<TypeRef> params = new LinkedList<>();
    /**
     * Pointer level. Number of asterisks before this
     */
    public int pointer;
    /**
     * OpenCL modifiers
     */
    public int modifiers;

    public TypeRef(TypeSymbol type) {
        if(type == null) throw new IllegalArgumentException("Null type");
        this.type = type;
    }

    /**
     * Chaining setter for pointer field
     */
    public TypeRef point(int i) {
        pointer = i;
        return this;
    }

    /**
     * Chaining setter for modifiers field
     */
    public TypeRef modify(int i) {
        modifiers = i;
        return this;
    }

    /**
     * @return type casted to ReferenceSymbol
     */
    public ReferenceSymbol refType() {
        return (ReferenceSymbol)type;
    }

    /**
     * @return type casted to ClassSymbol
     */
    public ClassSymbol classType() {
        return (ClassSymbol) type;
    }

    /**
     * @return A TypeRef for the component type of this, assuming this references an ArraySymbol
     */
    public TypeRef componentRef() {
        return new TypeRef(((ArraySymbol)type).type);
    }

    /**
     * @return A TypeRef to type.array()
     */
    public TypeRef arrayRef() {
        return new TypeRef(type.array());
    }

    /**
     * @return type.concrete(). The type this reference would be compiled to
     */
    public TypeSymbol concrete() {
        return type.concrete();
    }

    /**
     * Finds the reference in this type tree that specifies the parameters to type
     * Eg. specify(Set) on theoretical IntegerSet extends Set<Integer> would return Set<Integer>
     * If this.type == type, returns this
     */
    public TypeRef specifier(TypeSymbol type) {
        if(this.type == type)
            return this;

        return classType().parentRef(type);
    }

    /**
     * Recursively calculates the set of all TypeParams referenced by this and it's params
     * Eg. Map<A, Pair<A, B>> would return (A, B)
     */
    public Collection<TypeParam> getParams() {
        if(type instanceof TypeParam)
            return Arrays.asList((TypeParam) type);

        if(params.isEmpty())
            return Collections.emptyList();

        Set<TypeParam> set = new TreeSet<>();
        for(TypeRef param : params)
            set.addAll(param.getParams());

        return set;
    }

    public TypeRef mapParams(Map<TypeParam, TypeSymbol> typeMap) {
        if(typeMap.isEmpty())
            return this;

        TypeSymbol mapped = typeMap.get(type);
        TypeRef r = new TypeRef(mapped == null ? type : mapped);
        for(TypeRef ref : params)
            r.params.add(ref.mapParams(typeMap));

        return r;
    }

    /**
     * The compiled signature of this reference
     */
    public String signature() {
        return type.signature();
    }

    public TypeRef copy() {
        TypeRef copy = new TypeRef(type);
        for(TypeRef p : params)
            copy.params.add(p.copy());

        return copy.point(pointer).modify(modifiers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if(printCL) {
            if((modifiers & GLOBAL) != 0) sb.append("global ");
            else if((modifiers & LOCAL) != 0) sb.append("local ");
            else if((modifiers & CONSTANT) != 0) sb.append("constant ");
            if((modifiers & UNSIGNED) != 0) sb.append("unsigned ");

            int ptr = pointer;
            TypeSymbol baseType = type;
            while(baseType instanceof ArraySymbol) {
                baseType = ((ArraySymbol) baseType).type;
                ptr++;
            }
            String name;
            if(type == PrimitiveSymbol.BYTE || type == PrimitiveSymbol.BOOLEAN)
                name = "char";
            else
                name = type.getName();

            sb.append(name);
            for(int i = 0; i < ptr; i++)
                sb.append('*');
        } else {
            sb.append(type.fullname);
            if (!params.isEmpty())
                sb.append('<').append(SourceUtil.listString(params)).append('>');
        }

        return sb.toString();
    }

    /**
     * Creates or extracts a TypeRef based on the type of o
     * If o is a TypeRef, returns o
     * If o is a TypeSymbol, returns new TypeRef(o)
     * If o is an Expression, returns o.returnType()
     * If o is a Variable, returns o.getType()
     * If o is a String, returns new TypeRef(TypeIndex.resolveType(o))
     * Otherwise returns null
     */
    public static TypeRef get(Object o) {
        if(o instanceof TypeRef)
            return (TypeRef) o;
        if(o instanceof TypeSymbol)
            return new TypeRef((TypeSymbol) o);
        if(o instanceof Expression)
            return ((Expression) o).returnType();
        if(o instanceof Variable)
            return ((Variable) o).getType();
        if(o instanceof String)
            return new TypeRef(TypeIndex.resolveType((String) o));

        return null;
    }

    public static TypeSymbol specify(TypeParam param, TypeRef pattern, TypeRef specific) {
        if(pattern.type == param)
            return specific.type;

        if(pattern.type instanceof TypeParam || pattern.params.isEmpty())//pattern does not contain param
            return param;

        TypeRef match = specific.specifier(pattern.type);
        if(match.params.isEmpty())//match does not specify any information about pattern
            return param;

        for(int i = 0; i < pattern.params.size(); i++) {
            TypeSymbol t = specify(param, pattern.params.get(i), match.params.get(i));
            if(t == param)
                continue;//nothing new here

            if (!(t instanceof TypeParam))
                return t;//resolved to a concrete type, hooray

            //resolve the new type param
            param = (TypeParam) t;
            return specify(param, ((ClassSymbol)param.owner).parameterPattern(), specific);
        }

        return param;
    }

    public static Map<TypeParam, TypeSymbol> specify(Collection<TypeParam> params, List patterns, List specifiers) {
        if(params.isEmpty())
            return Collections.emptyMap();

        Map<TypeParam, TypeSymbol> typeMap = new TreeMap<>();
        List<TypeParam> remaining = new LinkedList<>(params);
        for(int i = 0; i < specifiers.size() && !remaining.isEmpty(); i++) {
            TypeRef pattern = get(patterns.get(i));
            TypeRef specifier = get(specifiers.get(i));
            for(Iterator<TypeParam> it = remaining.iterator(); it.hasNext();) {
                TypeParam p = it.next();
                TypeSymbol t = specify(p, pattern, specifier);
                if(t != p) {
                    typeMap.put(p, t);
                    it.remove();
                }
            }
        }

        return typeMap;
    }
}
