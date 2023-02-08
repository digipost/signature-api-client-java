package no.digipost.signature.client.core.internal.http;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UriHelperTest {

    @Test
    void appendsQueryParam() {
        var uri = URI.create("https://test.example.com");
        var withQuery = UriHelper.addQuery(uri, "key=value");
        assertThat(withQuery, is(URI.create("https://test.example.com?key=value")));
    }

    @Test
    void appendsQueryOnUriWithQuery() {
        var uri = URI.create("https://test.example.com?key1=value1");
        var withQuery = UriHelper.addQuery(uri, "key2=value2");
        assertThat(withQuery, is(URI.create("https://test.example.com?key1=value1&key2=value2")));
    }
}
