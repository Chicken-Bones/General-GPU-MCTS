package gpuproj.translator;

import java.util.List;

public class OCLProgramBuilder
{
    public static interface Constant
    {
        public String getName();
        public String print();
        public List<Constant> getDependencies();
    }

    public static interface GlobalVar
    {
        public String getName();
        public void setName(String name);
        public void print();
    }



    //constants
    //variables
    //functions (name, global vars, deps)

    //initialisers
    //main



}
