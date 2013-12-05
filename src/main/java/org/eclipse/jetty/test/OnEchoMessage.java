package org.eclipse.jetty.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class OnEchoMessage extends WebSocketAdapter
{
    private static final Logger LOG = Log.getLogger(OnEchoMessage.class);
    private final int currentCaseId;
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
        // Echo the data back.
        getRemote().sendBytesByFuture(ByteBuffer.wrap(payload,offset,len));
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode,reason);
        LOG.debug("onClose({}, \"{}\")",statusCode,reason);
        latch.countDown();
    }

    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        LOG.info("Executing test case {}",currentCaseId);
        LOG.debug("onOpen({})",session);
    }

    @Override
    public void onWebSocketText(String message) {
        // Echo the data back.
        getRemote().sendStringByFuture(message);
    }
}
