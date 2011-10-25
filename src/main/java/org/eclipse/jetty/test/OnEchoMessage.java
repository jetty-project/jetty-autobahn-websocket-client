package org.eclipse.jetty.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.WebSocket;

public class OnEchoMessage implements WebSocket.OnTextMessage
{
    private static final Logger LOG = Log.getLogger(OnEchoMessage.class);
    private final int currentCaseId;
    private final int totalCaseCount;
    private Connection connection;
    private CountDownLatch latch = new CountDownLatch(1);

    public OnEchoMessage(int currentCaseId, int totalCaseCount)
    {
        this.currentCaseId = currentCaseId;
        this.totalCaseCount = totalCaseCount;
    }

    public void awaitClose() throws InterruptedException
    {
        latch.await(5,TimeUnit.SECONDS);
    }

    @Override
    public void onClose(int closeCode, String message)
    {
        LOG.debug("onClose({}, \"{}\")",closeCode,message);
        latch.countDown();
    }

    @Override
    public void onMessage(String data)
    {
        try
        {
            // Echo the data back.
            connection.sendMessage(data);
        }
        catch (IOException e)
        {
            LOG.warn("Unable to echo data back",e);
        }
    }

    @Override
    public void onOpen(Connection connection)
    {
        this.connection = connection;
        LOG.info("Executing test case {}/{}",currentCaseId,totalCaseCount);
        LOG.debug("onOpen({})",connection);
    }
}
