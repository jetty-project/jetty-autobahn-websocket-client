package org.eclipse.jetty.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketConnection;

public class OnUpdateReports extends WebSocketAdapter
{
    private static final Logger LOG = Log.getLogger(OnUpdateReports.class);
    private CountDownLatch latch = new CountDownLatch(1);

    public void awaitClose() throws InterruptedException
    {
        latch.await(15,TimeUnit.SECONDS);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        LOG.debug("onClose({}, \"{}\")",statusCode,reason);
        LOG.info("Reports updated.");
        LOG.info("Test suite finished!");
        latch.countDown();
    }

    @Override
    public void onWebSocketConnect(WebSocketConnection connection)
    {
        super.onWebSocketConnect(connection);
        LOG.info("Updating reports ...");
    }
}
