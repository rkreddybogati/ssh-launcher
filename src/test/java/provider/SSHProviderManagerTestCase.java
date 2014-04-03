package provider;

import com.scalr.ssh.SSHProviderManager;
import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.provider.SSHProviderInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SSHProviderManagerTestCase {
    private SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");

    @Test
    public void testPreferredProvider () throws InvalidEnvironmentException {
        SSHProviderManager sshProviderManager = new SSHProviderManager("mac os x");
        ArrayList<SSHProviderInterface> sshProviders;

        sshProviders = sshProviderManager.getOrderedSSHProviders(sshConfiguration, null);
        assertEquals(2, sshProviders.size());
        assertEquals("com.scalr.ssh.provider.MacNativeSSHProvider", sshProviders.get(0).getClass().getCanonicalName());

        sshProviders = sshProviderManager.getOrderedSSHProviders(sshConfiguration, "com.scalr.ssh.provider.MacSSHProvider");
        assertEquals(2, sshProviders.size());
        assertEquals("com.scalr.ssh.provider.MacSSHProvider", sshProviders.get(0).getClass().getCanonicalName());

    }
}
