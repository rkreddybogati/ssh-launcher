package controller;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.OpenSSHController;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.controller.PuTTYController;
import com.scalr.ssh.controller.SSHController;
import lib.util.MockFileSystemManagerRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class SSHControllerFindExecutableTestCase {

    @Rule
    public MockFileSystemManagerRule fsRule = new MockFileSystemManagerRule();

    @Test(expected=InvalidEnvironmentException.class)
    public void testSSHNotFound () throws LauncherException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHController sshController = new OpenSSHController(sshConfiguration, fsRule.getFileSystemManager());
        sshController.getSSHCommandLineBits();
    }

    @Test
    public void testFindPuTTY () throws LauncherException {
        fsRule.addExistingPath("C:/Program Files (x86)/PuTTY/putty.exe");

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setDisableKeyAuth(true);
        SSHController sshController = new PuTTYController(sshConfiguration, fsRule.getFileSystemManager());

        String[] sshCommandLineBits = sshController.getSSHCommandLineBits();

        assertEquals(3, sshCommandLineBits.length);

        assertEquals("-ssh", sshCommandLineBits[1]);
        assertEquals("example.com", sshCommandLineBits[2]);

        String puttyExecutable = sshCommandLineBits[0];
        assertThat(puttyExecutable, containsString("PuTTY"));
        assertThat(puttyExecutable, containsString("putty.exe"));
    }

    @Test(expected=InvalidEnvironmentException.class)
    public void testPuTTYNotFound () throws LauncherException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        SSHController sshController = new PuTTYController(sshConfiguration, fsRule.getFileSystemManager());
        sshController.getSSHCommandLineBits();
    }
}
