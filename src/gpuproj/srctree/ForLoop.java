package gpuproj.srctree;

import gpuproj.srctree.Scope.ScopeProvider;

public class ForLoop extends Statement
{
    public Statement init;
    public Statement test;
    public Statement update;
    public Statement body;
}
