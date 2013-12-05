package org.eclipse.jetty.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class OnGetCaseCount extends WebSocketAdapter
{
    private static final Logger LOG = Log.getLogger(OnGetCaseCount.class);
    private Integer casecount = null;
    private CountDownLatch latch = new CountDownLatch(1);

    public void awaitMessage() throws InterruptedException
    {
        latch.await(1,TimeUnit.SECONDS);
    }

    public int getCaseCount()
    {
        return casecount.intValue();
    }

    public boolean hasCaseCount()
    {
        return (casecount != null);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode,reason);
        LOG.debug("onWebSocketClose({}, \"{}\")",statusCode,reason);
    }

    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        LOG.debug("onOpen({})",session);
    }

    @Override
    public void onWebSocketText(String message)
    {
        LOG.debug("onWebSocketText(\"{}\")",message);
        casecount = Integer.decode(message);
        latch.countDown();
    }
}
