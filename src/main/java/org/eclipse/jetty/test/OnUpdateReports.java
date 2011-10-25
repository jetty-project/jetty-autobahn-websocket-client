package org.eclipse.jetty.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.WebSocket;

public class OnUpdateReports implements WebSocket
{
    private static final Logger LOG = Log.getLogger(OnUpdateReports.class);
    private CountDownLatch latch = new CountDownLatch(1);

    public void awaitClose() throws InterruptedException
    {
        latch.await(15,TimeUnit.SECONDS);
    }

    @Override
    public void onClose(int closeCode, String message)
    {
        LOG.debug("onClose({}, \"{}\")",closeCode,message);
        LOG.info("Reports updated.");
        LOG.info("Test suite finished!");
        latch.countDown();
    }

    @Override
    public void onOpen(Connection connection)
    {
        LOG.info("Updating reports ...");
    }
}
