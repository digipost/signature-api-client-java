package no.digipost.signature.client.asice.archive;

import no.digipost.signature.client.asice.ASiCEAttachable;
import no.digipost.signature.client.core.DocumentType;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class CreateZipTest {

    @Test
    public void test_create_zip_file_readable_by_java() throws IOException {
        CreateZip createZip = new CreateZip();

        List<ASiCEAttachable> asicEAttachables = asList(
                file("file.txt", "test"),
                file("file2.txt", "test2")
        );

        byte[] archive = createZip.zipIt(asicEAttachables);

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(archive))) {
            verifyZipFile(zipInputStream, "file.txt", "test");
            verifyZipFile(zipInputStream, "file2.txt", "test2");
        }

    }

    private static void verifyZipFile(ZipInputStream zipInputStream, String fileName, String contents) throws IOException {
        ZipEntry firstZipFile = zipInputStream.getNextEntry();
        assertThat(firstZipFile.getName(), containsString(fileName));
        assertArrayEquals(IOUtils.toByteArray(zipInputStream), contents.getBytes());
    }

    private ASiCEAttachable file(String fileName, String contents) {
        return new ASiCEAttachable() {
            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public byte[] getContent() {
                return contents.getBytes();
            }

            @Override
            public String getMediaType() {
                return DocumentType.TXT.getMediaType();
            }
        };
    }

}
