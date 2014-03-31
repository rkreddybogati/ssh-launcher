import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.manager.OpenSSHManager;
import com.scalr.ssh.manager.PuTTYManager;
import com.scalr.ssh.manager.SSHManagerInterface;
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
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SSHManagerTestCase {

    // Test local interfaces (those that do not hit the filesystem)

    @Test
    public void testUnixSSHManager () throws InvalidEnvironmentException {
        fsManager.existingPaths.add("/usr/bin/ssh");

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithUser () throws InvalidEnvironmentException {
        fsManager.existingPaths.add("/usr/bin/ssh");

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setUsername("user");
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "user@example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithPort () throws InvalidEnvironmentException {
        fsManager.existingPaths.add("/usr/bin/ssh");

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setPort(2222);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "-p", "2222", "example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithKey () throws InvalidEnvironmentException {
        fsManager.existingPaths.add("/usr/bin/ssh");

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setOpenSSHPrivateKey("My Private Key");
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);

        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();

        assertEquals(4, sshCommandLineBits.length);

        assertEquals("/usr/bin/ssh", sshCommandLineBits[0]);
        assertEquals("-i", sshCommandLineBits[1]);
        assertEquals("example.com", sshCommandLineBits[3]);

        String privateKeyPath = sshCommandLineBits[2];
        assertThat(privateKeyPath, containsString(".ssh"));
        assertThat(privateKeyPath, containsString("scalr"));
        assertThat(privateKeyPath, containsString(".pem"));
    }

    @Test
    public void testSSHKeyPathDependsOnKey () throws InvalidEnvironmentException {
        fsManager.existingPaths.add("/usr/bin/ssh");

        SSHConfiguration sshConfiguration1 = new SSHConfiguration("example.com");
        sshConfiguration1.setOpenSSHPrivateKey("Private Key 1");
        SSHManagerInterface sshManager1 = new OpenSSHManager(sshConfiguration1);

        SSHConfiguration sshConfiguration2 = new SSHConfiguration("example.com");
        sshConfiguration2.setOpenSSHPrivateKey("Private Key 2");
        SSHManagerInterface sshManager2 = new OpenSSHManager(sshConfiguration2);

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
        public ArrayList<String> existingPaths = new ArrayList<String>();

        @Override
        public String getUserHome() {
            return userHome;
        }

        public boolean fileExists (File file) {
            return existingPaths.contains(file.getPath());
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
        fsManager.existingPaths.add("/usr/bin/ssh");

        String privateKey = "Private\nKey\nContents";

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setOpenSSHPrivateKey(privateKey);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration, fsManager);

        // Check a key is created
        assertEquals(0, testDirectory.toFile().listFiles().length);
        sshManager.setUpSSHEnvironment();
        assertEquals(1, testDirectory.toFile().listFiles().length);

        // Check the file is in the right location
        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();
        String sshPrivateKeyPath = sshCommandLineBits[2];

        // Check the contents
        assertEquals(privateKey, FileUtils.readFileToString(new File(sshPrivateKeyPath)));
    }

    @Test(expected=InvalidEnvironmentException.class)
    public void testSSHNotFound () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration, fsManager);
        sshManager.getSSHCommandLineBits();
    }

    @Test
    public void testFindPuTTY () throws InvalidEnvironmentException {
        fsManager.existingPaths.add("C:/Program Files (x86)/PuTTY/putty.exe");

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHManagerInterface sshManager = new PuTTYManager(sshConfiguration, fsManager);

        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();

        assertEquals(3, sshCommandLineBits.length);

        assertEquals("-ssh", sshCommandLineBits[1]);
        assertEquals("example.com", sshCommandLineBits[2]);

        String puttyExecutable = sshCommandLineBits[0];
        assertThat(puttyExecutable, containsString("PuTTY"));
        assertThat(puttyExecutable, containsString("putty.exe"));
    }

    @Test(expected=InvalidEnvironmentException.class)
    public void testPuTTYNotFound () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHManagerInterface sshManager = new PuTTYManager(sshConfiguration, fsManager);
        sshManager.getSSHCommandLineBits();
    }
}
