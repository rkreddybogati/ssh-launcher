package manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;
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
    public void testUnixSSHManager () throws LauncherException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setDisableKeyAuth(true);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithUser () throws LauncherException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setUsername("user");
        sshConfiguration.setDisableKeyAuth(true);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "user@example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithPort () throws LauncherException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setPort(2222);
        sshConfiguration.setDisableKeyAuth(true);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "-p", "2222", "example.com"}, sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithNoHostKeyChecking () throws LauncherException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setIgnoreHostKeys(true);
        sshConfiguration.setDisableKeyAuth(true);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        assertArrayEquals(new String[]{"/usr/bin/ssh", "-o", "UserKnownHostsFile=/dev/null", "-o", "CheckHostIP=no", "-o", "StrictHostKeyChecking=no", "example.com"},
                sshManager.getSSHCommandLineBits());
    }

    @Test
    public void testUnixSSHManagerWithKey () throws LauncherException {
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

    @Test(expected=InvalidConfigurationException.class)
    public void testEmptySSHKeyWithAutoKeyName () throws LauncherException {
        // In this case, we'll attempt to create the key name from the key contents, but they are null.
        // This should error out
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");

        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        sshManager.getSSHCommandLineBits();
    }

    @Test(expected=InvalidConfigurationException.class)
    public void testEmptySSHKeyPath () throws LauncherException {
        // In this case, we'll attempt to write to a key file with no name
        // This should error out
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setSSHKeyName("");

        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        sshManager.getSSHCommandLineBits();
    }
}
