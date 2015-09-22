/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signering.client.asice.archive;

import no.digipost.signering.client.asice.ASiCEAttachable;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class CreateZipTest {

    @Test
    public void test_create_zip_file_readable_by_java() throws IOException {
        CreateZip createZip = new CreateZip();

        List<ASiCEAttachable> asicEAttachables = asList(
                file("file.txt", "test"),
                file("file2.txt", "test2")
        );

        Archive archive = createZip.zipIt(asicEAttachables);

        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(archive.getBytes()));

        verifyZipFile(zipInputStream, "file.txt", "test");
        verifyZipFile(zipInputStream, "file2.txt", "test2");
    }

    private void verifyZipFile(ZipInputStream zipInputStream, String fileName, String contents) throws IOException {
        ZipEntry firstZipFile = zipInputStream.getNextEntry();
        assertThat(firstZipFile.getName(), containsString(fileName));
        assertArrayEquals(IOUtils.toByteArray(zipInputStream), contents.getBytes());
    }

    private ASiCEAttachable file(final String fileName, final String contents) {
        return new ASiCEAttachable() {
            public String getFileName() {
                return fileName;
            }

            public byte[] getBytes() {
                return contents.getBytes();
            }
        };
    }

}
