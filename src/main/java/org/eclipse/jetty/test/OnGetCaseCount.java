package org.eclipse.jetty.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.WebSocket;

public class OnGetCaseCount implements WebSocket.OnTextMessage
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
    public void onClose(int closeCode, String message)
    {
        // do nothing
        LOG.debug("onClose({}, \"{}\")",closeCode,message);
    }

    @Override
    public void onMessage(String data)
    {
        LOG.debug("onMessage(\"{}\")",data);
        casecount = Integer.decode(data);
        latch.countDown();
    }

    @Override
    public void onOpen(Connection connection)
    {
        // do nothing
        LOG.debug("onOpen({})",connection);
    }
}
