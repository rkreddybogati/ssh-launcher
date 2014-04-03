package lib.mock;

import com.scalr.ssh.fs.FileSystemManager;

import java.io.File;
import java.util.ArrayList;

public class MockFileSystemManager extends FileSystemManager {
    public String userHome = "";
    public ArrayList<String> existingPaths = new ArrayList<String>();

    @Override
    public String getUserHome() {
        return userHome;
    }

    public boolean fileExists (File file) {
        return existingPaths.contains(file.getPath());
    }
}

