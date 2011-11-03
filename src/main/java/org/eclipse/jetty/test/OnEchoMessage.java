package org.eclipse.jetty.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.WebSocket;

public class OnEchoMessage implements WebSocket.OnTextMessage, WebSocket.OnBinaryMessage
{
    private static final Logger LOG = Log.getLogger(OnEchoMessage.class);
    private static final int KBYTE = 1024;
    private static final int MBYTE = KBYTE * KBYTE;
    private final int currentCaseId;
    private Connection connection;
    private CountDownLatch latch = new CountDownLatch(1);

    public OnEchoMessage(int currentCaseId)
    {
        this.currentCaseId = currentCaseId;
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
    public void onMessage(byte[] data, int offset, int length)
    {
        try
        {
            // Echo the data back.
            connection.sendMessage(data,offset,length);
        }
        catch (IOException e)
        {
            LOG.warn("Unable to echo binary message back",e);
        }
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
            LOG.warn("Unable to echo text message back",e);
        }
    }

    @Override
    public void onOpen(Connection connection)
    {
        this.connection = connection;
        LOG.info("Executing test case {}",currentCaseId);
        LOG.debug("onOpen({})",connection);
        connection.setMaxBinaryMessageSize(20 * MBYTE);
        connection.setMaxTextMessageSize(20 * MBYTE);
    }
}
