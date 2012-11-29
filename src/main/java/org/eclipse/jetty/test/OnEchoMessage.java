package org.eclipse.jetty.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketConnection;

public class OnEchoMessage extends WebSocketAdapter
{
    private static final Logger LOG = Log.getLogger(OnEchoMessage.class);
    private static final int KBYTE = 1024;
    private static final int MBYTE = KBYTE * KBYTE;
    private final int currentCaseId;
    private WebSocketConnection connection;
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
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        try
        {
            // Echo the data back.
            connection.write(payload,offset,len);
        }
        catch (IOException e)
        {
            LOG.warn("Unable to echo binary message back",e);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode,reason);
        LOG.debug("onClose({}, \"{}\")",statusCode,reason);
        latch.countDown();
    }

    @Override
    public void onWebSocketConnect(WebSocketConnection connection)
    {
        super.onWebSocketConnect(connection);
        this.connection = connection;
        LOG.info("Executing test case {}",currentCaseId);
        LOG.debug("onOpen({})",connection);
//        connection.setMaxBinaryMessageSize(20 * MBYTE);
//        connection.setMaxTextMessageSize(20 * MBYTE);
    }

    @Override
    public void onWebSocketText(String message) {
        try
        {
            // Echo the data back.
            connection.write(message);
        }
        catch (IOException e)
        {
            LOG.warn("Unable to echo text message back",e);
        }
    }
}
