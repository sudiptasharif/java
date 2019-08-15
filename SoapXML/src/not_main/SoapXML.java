package not_main;


/* Call SOAP URL and send the Request XML and Get Response XML back */
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;


public class SoapXML {

	public static void sendSoapRequest() throws Exception {
		//Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("", 80));
		
		// Original URL stripped away for security reasons. Substitute with your own target URL
		String SOAPUrl = "";
		String xmlFile2Send = "request.xml";
		String responseFileName = "response.xml";
		String SOAPAction = "";

		// Create the connection where we're going to send the file.
		URL url = new URL(SOAPUrl);
		//URLConnection connection = url.openConnection(proxy);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection) connection;
		
		FileInputStream fin = new FileInputStream(xmlFile2Send);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		// Copy SOAP file to the open connection.
		copy(fin, bout);
		fin.close();

		byte[] b = bout.toByteArray();
		StringBuffer buf=new StringBuffer();
		String s=new String(b);
		//replacing value in XML
		//s=s.replaceAll("CISID", cisId);
		//System.out.println(s); //print all xml
		b=s.getBytes();
		// Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", SOAPAction);
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		//httpConn.setDoInput(true);

		// send the XML that was read in to b.
		OutputStream out = httpConn.getOutputStream();
		out.write(b);
		out.close();
		
		// Read the response.
		httpConn.connect();
		System.out.println("http connection status :"+ httpConn.getResponseMessage());
		InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
		BufferedReader in = new BufferedReader(isr);

		/*while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);*/
		FileOutputStream fos=new FileOutputStream(responseFileName);
		copy(httpConn.getInputStream(),fos);
		in.close();
	}

	public static void copy(InputStream in, OutputStream out)
			throws IOException {

		synchronized (in) {
			synchronized (out) {
				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = in.read(buffer);
					if (bytesRead == -1)
						break;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
	}
}