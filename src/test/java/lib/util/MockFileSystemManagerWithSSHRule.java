package lib.util;

public class MockFileSystemManagerWithSSHRule extends MockFileSystemManagerRule {
    protected String[] getDefaultPaths () {
        return new String[] {"/usr/bin/ssh"};
    }
}
