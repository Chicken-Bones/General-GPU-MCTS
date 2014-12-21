package gpuproj.srctree;

public class Method
{
    public int modifiers;
    public ClassType owner;
    public String name;
    public String returnType;
    public String[] paramTypes;
    public String[] paramNames;

    public Method(int modifiers, ClassType owner, String name, String returnType, String[] paramTypes) {
        this.modifiers = modifiers;
        this.owner = owner;
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }
}
