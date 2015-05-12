package org.eclipse.jetty.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.extensions.compress.PerMessageDeflateExtension;

public class TestClient
{
    private static final int MBYTE = 1024 * 1024;

    private static String getJettyVersion() throws IOException
    {
        String resource = "META-INF/maven/org.eclipse.jetty/jetty-websocket/pom.properties";
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null)
        {
            if (WebSocketClient.class.getPackage() != null)
            {
                Package pkg = WebSocketClient.class.getPackage();
                if (pkg.getImplementationVersion() != null)
                {
                    return pkg.getImplementationVersion();
                }
            }
            return "GitMaster";
        }

        InputStream in = null;
        try
        {
            in = url.openStream();
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version");
        }
        finally
        {
            IO.close(in);
        }
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            System.err.println("ERROR: Hostname and port not specified");
            System.err.printf("USAGE: java -cp jetty-autobahn-websocket-client.jar %s [hostname] [port]%n",TestClient.class.getName());
            System.exit(-1);
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        int caseNumbers[] = null;

        // Optional case numbers
        // NOTE: these are url query parameter case numbers (whole integers, eg "6"), not the case ids (eg "7.3.1")
        if (args.length > 2)
        {
            int offset = 2;
            caseNumbers = new int[args.length - offset];
            for (int i = offset; i < args.length; i++)
            {
                caseNumbers[i - offset] = Integer.parseInt(args[i]);
            }
        }

        TestClient client = null;
        try
        {
            String userAgent = "JettyWebsocketClient/" + getJettyVersion();
            client = new TestClient(hostname,port,userAgent);

            client.updateStatus("Running test suite...");
            client.updateStatus("Using Fuzzing Server: %s:%d",hostname,port);
            client.updateStatus("User Agent: %s",userAgent);

            if (caseNumbers == null)
            {
                int caseCount = client.getCaseCount();
                client.updateStatus("Will run all %d cases ...",caseCount);
                for (int caseNum = 1; caseNum <= caseCount; caseNum++)
                {
                    client.updateStatus("Running case %d (of %d) ...",caseNum,caseCount);
                    client.runCaseByNumber(caseNum);
                }
            }
            else
            {
                client.updateStatus("Will run %d cases ...",caseNumbers.length);
                for (int caseNum : caseNumbers)
                {
                    client.runCaseByNumber(caseNum);
                }
            }
            client.updateStatus("All test cases executed.");
            client.updateReports();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
        finally
        {
            if (client != null)
            {
                client.shutdown();
            }
        }
        System.exit(0);
    }

    private Logger log;
    private URI baseWebsocketUri;
    private WebSocketClient client;
    @SuppressWarnings("unused")
    private String hostname;
    @SuppressWarnings("unused")
    private int port;
    private String userAgent;

    public TestClient(String hostname, int port, String userAgent) throws Exception
    {
        this.log = Log.getLogger(this.getClass());
        this.hostname = hostname;
        this.port = port;
        this.userAgent = userAgent;
        this.baseWebsocketUri = new URI("ws://" + hostname + ":" + port);
        this.client = new WebSocketClient();
        this.client.getPolicy().setMaxBinaryMessageSize(20 * MBYTE);
        this.client.getPolicy().setMaxTextMessageSize(20 * MBYTE);
        this.client.getExtensionFactory().register("permessage-deflate",PerMessageDeflateExtension.class);
        this.client.start();
    }

    public int getCaseCount() throws IOException, InterruptedException, ExecutionException, TimeoutException
    {
        URI wsUri = baseWebsocketUri.resolve("/getCaseCount");
        OnGetCaseCount onCaseCount = new OnGetCaseCount();
        Future<Session> response = client.connect(onCaseCount,wsUri);

        if (waitForUpgrade(wsUri,response))
        {
            onCaseCount.awaitMessage();
            if (onCaseCount.hasCaseCount())
            {
                return onCaseCount.getCaseCount();
            }
        }
        throw new IllegalStateException("Unable to get Case Count");
    }

    public void runCaseByNumber(int caseNumber) throws IOException, InterruptedException
    {
        URI wsUri = baseWebsocketUri.resolve("/runCase?case=" + caseNumber + "&agent=" + UrlEncoded.encodeString(userAgent));
        log.debug("next uri - {}",wsUri);
        OnEchoMessage onEchoMessage = new OnEchoMessage(caseNumber);

        Future<Session> response = client.connect(onEchoMessage,wsUri);

        if (waitForUpgrade(wsUri,response))
        {
            onEchoMessage.awaitClose();
        }
    }

    public void shutdown()
    {
        try
        {
            this.client.stop();
        }
        catch (Exception e)
        {
            log.warn("Unable to stop WebSocketClient",e);
        }
    }

    public void updateReports() throws IOException, InterruptedException, ExecutionException, TimeoutException
    {
        URI wsUri = baseWebsocketUri.resolve("/updateReports?agent=" + UrlEncoded.encodeString(userAgent));
        OnUpdateReports onUpdateReports = new OnUpdateReports();
        Future<Session> response = client.connect(onUpdateReports,wsUri);
        response.get(5,TimeUnit.SECONDS);
        onUpdateReports.awaitClose();
    }

    public void updateStatus(String format, Object... args)
    {
        log.info(String.format(format,args));
    }

    private boolean waitForUpgrade(URI wsUri, Future<Session> response) throws InterruptedException
    {
        try
        {
            response.get(1,TimeUnit.SECONDS);
            return true;
        }
        catch (ExecutionException e)
        {
            log.warn("Unable to connect to: " + wsUri,e);
            return false;
        }
        catch (TimeoutException e)
        {
            log.warn("Unable to connect to: " + wsUri,e);
            return false;
        }
    }
}
