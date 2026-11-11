package diff.strazzere.anti.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UtilitiesTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void anyPathExistsFindsAFileAmongMissingOnes() throws IOException {
        File existing = folder.newFile("existing");

        assertTrue(Utilities.anyPathExists(new String[]{"/definitely/not/here", existing.getPath(), "/nor/here"}));
    }

    @Test
    public void anyPathExistsFindsADirectory() {
        assertTrue(Utilities.anyPathExists(new String[]{folder.getRoot().getPath()}));
    }

    @Test
    public void anyPathExistsIsFalseWhenNothingExists() {
        assertFalse(Utilities.anyPathExists(new String[]{"/definitely/not/here", "/nor/here"}));
        assertFalse(Utilities.anyPathExists(new String[0]));
    }

    @Test
    public void readFileReadsTheContents() throws IOException {
        File file = folder.newFile("status");
        Files.write(file.toPath(), "Name:\tapp\nTracerPid:\t0\n".getBytes(StandardCharsets.UTF_8));

        assertEquals("Name:\tapp\nTracerPid:\t0\n", Utilities.readFile(file.getPath(), 1024));
    }

    @Test
    public void readFileOnlyReadsTheStartOfALargeFile() throws IOException {
        File file = folder.newFile("large");
        Files.write(file.toPath(), new byte[10000]);

        assertEquals(100, Utilities.readFile(file.getPath(), 100).length());
    }

    @Test
    public void readFileIsNullWhenTheFileCannotBeRead() {
        assertNull(Utilities.readFile("/definitely/not/here", 1024));
        // A directory can't be read as a file either
        assertNull(Utilities.readFile(folder.getRoot().getPath(), 1024));
    }
}
