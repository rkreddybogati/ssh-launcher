package lib.util;

import com.scalr.ssh.filesystem.FileSystemManager;
import lib.mock.MockFileSystemManager;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MockFileSystemManagerRule extends ExternalResource {
    private MockFileSystemManager fsManager;
    private Path testDirectory;

    public MockFileSystemManagerRule() {
        fsManager = new MockFileSystemManager();
    }

    @Override
    protected void before() throws Throwable {
        testDirectory = Files.createTempDirectory("test");
        fsManager.userHome = testDirectory.toFile().getCanonicalPath();
        for (String path : getDefaultPaths()) {
            fsManager.existingPaths.add(path);
        }
    }

    @Override
    protected void after() {
        try {
            FileUtils.deleteDirectory(testDirectory.toFile());
        } catch (IOException e) {
            // This is OK
        }
    }

    protected String[] getDefaultPaths () {
        return new String[] {};
    }

    public void addExistingPath(String path) {
        fsManager.existingPaths.add(path);
    }

    public FileSystemManager getFileSystemManager() {
        return fsManager;
    }

    public Path getTestDirectory () {
        return testDirectory;
    }
}
