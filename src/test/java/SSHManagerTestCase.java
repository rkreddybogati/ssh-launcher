import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import com.scalr.ssh.manager.UnixSSHManager;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SSHManagerTestCase {

    // Test local interfaces (those that do not hit the filesystem)

    @Test
    public void testUnixSSHManager () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("root", "example.com");
        SSHManagerInterface sshManager = new UnixSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"ssh", "root@example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithPort () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("root", "example.com");
        sshConfiguration.setPort(2222);
        SSHManagerInterface sshManager = new UnixSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"ssh", "-p", "2222", "root@example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithKey () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("root", "example.com");
        sshConfiguration.setPrivateKey("My Private Key");
        SSHManagerInterface sshManager = new UnixSSHManager(sshConfiguration);

        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();

        assertEquals(4, sshCommandLineBits.length);

        assertEquals("ssh", sshCommandLineBits[0]);
        assertEquals("-i", sshCommandLineBits[1]);
        assertEquals("root@example.com", sshCommandLineBits[3]);

        String privateKeyPath = sshCommandLineBits[2];
        assertThat(privateKeyPath, containsString(".ssh"));
        assertThat(privateKeyPath, containsString("scalr"));
        assertThat(privateKeyPath, containsString(".pem"));
    }

    @Test
    public void testSSHKeyPathDependsOnKey () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration1 = new SSHConfiguration("root", "example.com");
        sshConfiguration1.setPrivateKey("Private Key 1");
        SSHManagerInterface sshManager1 = new UnixSSHManager(sshConfiguration1);

        SSHConfiguration sshConfiguration2 = new SSHConfiguration("root", "example.com");
        sshConfiguration2.setPrivateKey("Private Key 2");
        SSHManagerInterface sshManager2 = new UnixSSHManager(sshConfiguration2);

        String[] sshCommandLineBits1 = sshManager1.getSSHCommandLineBits();
        String[] sshCommandLineBits2 = sshManager2.getSSHCommandLineBits();

        for (Integer i : new Integer[] {0, 1, 3}) {
            assertEquals(sshCommandLineBits1[i], sshCommandLineBits2[i]);
        }

        assertNotEquals(sshCommandLineBits1[2], sshCommandLineBits2[2]);
    }

    // Test interfaces that hit the filesystem

    private class TestFileSystemManager extends FileSystemManager {
        public String userHome = "";

        @Override
        public String getUserHome() {
            return userHome;
        }
    }

    TestFileSystemManager fsManager;
    Path testDirectory;

    @Rule
    public ExternalResource fsManagerResource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            testDirectory = Files.createTempDirectory("test");
            fsManager = new TestFileSystemManager();
            fsManager.userHome = testDirectory.toFile().getCanonicalPath();
        }

        @Override
        protected void after() {
            try {
                FileUtils.deleteDirectory(testDirectory.toFile());
            } catch (IOException e) {
                // This is OK
            }
        }
    };

    @Test
    public void testSSHKeyCreation () throws IOException, LauncherException {
        //TODO --> Use ExternalResource
        String privateKey = "Private\nKey\nContents";
        TestFileSystemManager fsManager = new TestFileSystemManager();
        Path testDirectory = Files.createTempDirectory("test");
        fsManager.userHome = testDirectory.toFile().getCanonicalPath();

        SSHConfiguration sshConfiguration = new SSHConfiguration("root", "example.com");
        sshConfiguration.setPrivateKey(privateKey);

        SSHManagerInterface sshManager = new UnixSSHManager(sshConfiguration, fsManager);

        // Check a key is created
        assertEquals(0, testDirectory.toFile().listFiles().length);
        sshManager.setUpSSHEnvironment();
        assertEquals(1, testDirectory.toFile().listFiles().length);

        // Check the file is in the right location
        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();
        String sshPrivateKeyPath = sshCommandLineBits[2];

        // Check the contnets
        assertEquals(privateKey, FileUtils.readFileToString(new File(sshPrivateKeyPath)));
    }
}
