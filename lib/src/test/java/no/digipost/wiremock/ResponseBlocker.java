package no.digipost.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ResponseBlocker extends ResponseTransformer implements BeforeEachCallback {

    private final Duration blockTimeout;
    private volatile CountDownLatch latch = new CountDownLatch(0);
    private volatile CountDownLatch reachedBlocking = new CountDownLatch(0);

    public ResponseBlocker() {
        this(Duration.ofSeconds(20));
    }

    public ResponseBlocker(Duration blockTimeout) {
        this.blockTimeout = blockTimeout;
        this.release();
    }

    public void block() {
        latch = new CountDownLatch(1);
        reachedBlocking = new CountDownLatch(1);
    }

    public void awaitUntilBlocking() {
        try {
            reachedBlocking.await(blockTimeout.toMillis(), MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public void release() {
        latch.countDown();
        reachedBlocking.countDown();
    }

    @Override
    public String getName() {
        return "response-blocker";
    }

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        try {
            reachedBlocking.countDown();
            latch.await(blockTimeout.toMillis(), MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return response;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        this.release();
    }

}
