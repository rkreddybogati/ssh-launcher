package manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.manager.OpenSSHManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import lib.util.MockFileSystemManagerWithSSHRule;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SSHManagerSSHKeysTestCase {
    @Rule
    public MockFileSystemManagerWithSSHRule fsRule = new MockFileSystemManagerWithSSHRule();

    @Test
    public void testSSHKeyCreation () throws IOException, LauncherException {
        String privateKey = "Private\nKey\nContents";

        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setOpenSSHPrivateKey(privateKey);
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration, fsRule.getFileSystemManager());

        Path homeDirectoryPath = fsRule.getTestDirectory();
        Path sshDirectoryPath = homeDirectoryPath.resolve(".ssh");
        Path keyFilePath;

        DirectoryStream<Path> paths;
        Iterator<Path> dirIter;

        // Check no key exists before creation
        // Check no SSH folder exists either
        paths = Files.newDirectoryStream(homeDirectoryPath);
        assertFalse(paths.iterator().hasNext());

        sshManager.setUpSSHEnvironment();

        // Check a folder is created to hold the key
        paths = Files.newDirectoryStream(sshDirectoryPath);
        dirIter = paths.iterator();

        assertEquals(Paths.get("scalr-ssh-keys"), dirIter.next().getFileName());
        assertFalse(dirIter.hasNext());

        // Check the folder contains the key
        paths = Files.newDirectoryStream(sshDirectoryPath.resolve("scalr-ssh-keys"));
        dirIter = paths.iterator();

        keyFilePath = dirIter.next();
        //assertTrue(keyFilePath.getFileName().startsWith("scalr-key"));
        assertFalse(dirIter.hasNext());

        // Check the file is in the right location
        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();
        String sshPrivateKeyPath = sshCommandLineBits[2];

        assertEquals(Paths.get(sshPrivateKeyPath).toFile().getCanonicalPath(), keyFilePath.toFile().getCanonicalPath());

        // Check the contents
        assertEquals(privateKey, FileUtils.readFileToString(new File(sshPrivateKeyPath)));
    }

    @Test
    public void testSSHKeyPathDependsOnKey () throws InvalidEnvironmentException {
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

    @Test(expected=SecurityException.class)
    public void testInvalidSSHKeyName () throws EnvironmentSetupException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setOpenSSHPrivateKey("test");
        sshConfiguration.setSSHKeyName("../not-valid-key");

        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration, fsRule.getFileSystemManager());
        sshManager.setUpSSHEnvironment();
    }

    public void testSSHKeyName () throws InvalidEnvironmentException {
        SSHConfiguration sshConfiguration = new SSHConfiguration("example.com");
        sshConfiguration.setOpenSSHPrivateKey("test");
        sshConfiguration.setSSHKeyName("valid-key-name");

        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        String[] sshCommandLineBits = sshManager.getSSHCommandLineBits();
        String sshPrivateKeyPath = sshCommandLineBits[2];

        // It's OK to only check the name passed in SSH configuration, as we check above that this matches
        // What's on the filesystem
        assertEquals("valid-key-name.pem", Paths.get(sshPrivateKeyPath).getFileName().toString());
    }
}
