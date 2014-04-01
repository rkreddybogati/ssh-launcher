import com.scalr.ssh.SSHLauncherManager;
import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.launcher.SSHLauncherInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SSHLauncherManagerTestCase {
    private SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");

    @Test
    public void testPreferredLauncher() throws InvalidEnvironmentException {
        SSHLauncherManager sshLauncherManager = new SSHLauncherManager("mac os x");
        ArrayList<SSHLauncherInterface> sshLaunchers;

        sshLaunchers = sshLauncherManager.getOrderedSSHLaunchers(sshConfiguration, null);
        assertEquals(2, sshLaunchers.size());
        assertEquals("com.scalr.ssh.launcher.MacNativeSSHLauncher", sshLaunchers.get(0).getClass().getCanonicalName());

        sshLaunchers = sshLauncherManager.getOrderedSSHLaunchers(sshConfiguration, "com.scalr.ssh.launcher.MacSSHLauncher");
        assertEquals(2, sshLaunchers.size());
        assertEquals("com.scalr.ssh.launcher.MacSSHLauncher", sshLaunchers.get(0).getClass().getCanonicalName());

    }
}
