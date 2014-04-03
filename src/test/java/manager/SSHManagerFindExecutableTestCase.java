package manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.manager.OpenSSHManager;
import com.scalr.ssh.manager.PuTTYManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import lib.util.MockFileSystemManagerRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class SSHManagerFindExecutableTestCase {

    @Rule
    public MockFileSystemManagerRule fsRule = new MockFileSystemManagerRule();

    @Test(expected=InvalidEnvironmentException.class)
    public void testSSHNotFound () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration, fsRule.getFileSystemManager());
        sshManager.getSSHCommandLineBits();
    }

    @Test
    public void testFindPuTTY () throws InvalidEnvironmentException {
        fsRule.addExistingPath("C:/Program Files (x86)/PuTTY/putty.exe");

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHManagerInterface sshManager = new PuTTYManager(sshConfiguration, fsRule.getFileSystemManager());

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
        SSHManagerInterface sshManager = new PuTTYManager(sshConfiguration, fsRule.getFileSystemManager());
        sshManager.getSSHCommandLineBits();
    }
}
