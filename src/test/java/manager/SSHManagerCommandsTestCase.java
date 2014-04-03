package manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.manager.OpenSSHManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import lib.util.MockFileSystemManagerWithSSHRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SSHManagerCommandsTestCase {
    @Rule
    public MockFileSystemManagerWithSSHRule fsRule = new MockFileSystemManagerWithSSHRule();

    @Test
    public void testUnixSSHManager () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithUser () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setUsername("user");
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "user@example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithPort () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setPort(2222);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "-p", "2222", "example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithKey () throws InvalidEnvironmentException {
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
}
