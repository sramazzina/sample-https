/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.serasoft;

import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class SimpleHTTPServer {

  public static class MyHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
      String response = "This is the response";
      HttpsExchange httpsExchange = (HttpsExchange) t;
      t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      t.sendResponseHeaders(200, response.getBytes().length);

      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    SimpleHTTPServer srv = new SimpleHTTPServer();
    String pathToCert = args[0];
    String keyStoreType = args[1];
    int port = Integer.parseInt(args[2]);

    srv.runHttpServer(pathToCert, keyStoreType, port);
  }

  public void runHttpServer(String pathToCert, String keyStoreType, int port) {
    try {
      // setup the socket address
      System.out.println("Opening port: " + port);
      InetSocketAddress address = new InetSocketAddress(port);

      // initialise the HTTPS server
      HttpsServer httpsServer = HttpsServer.create(address, 0);
      SSLContext sslContext = SSLContext.getInstance("TLS");

      // initialise the keystore
      char[] password = "changeit".toCharArray();
      System.out.println("Keystore Type: " + keyStoreType);
      KeyStore ks = KeyStore.getInstance(keyStoreType);

      FileInputStream certFile = new FileInputStream(pathToCert);
      ks.load(certFile, password);

      Enumeration<String> aliases = ks.aliases();
      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        Certificate cert = ks.getCertificate(alias);
        if (cert instanceof X509Certificate)
          System.out.println(
              "Loaded Certificate: "
                  + ((X509Certificate) cert).getSubjectDN().getName()
                  + " - Type: "
                  + cert.getType());
      }


      // setup the key manager factory
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, password);

      // setup the trust manager factory
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      tmf.init(ks);

      // setup the HTTPS context and parameters
      sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
      httpsServer.setHttpsConfigurator(
          new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
              try {
                // initialise the SSL context
                SSLContext context = getSSLContext();
                SSLEngine engine = context.createSSLEngine();
                params.setNeedClientAuth(false);
                params.setCipherSuites(engine.getEnabledCipherSuites());
                params.setProtocols(engine.getEnabledProtocols());

                // Set the SSL parameters
                SSLParameters sslParameters = context.getSupportedSSLParameters();
                params.setSSLParameters(sslParameters);

              } catch (Exception ex) {
                System.out.println("Failed to create HTTPS port");
              }
            }
          });

      httpsServer.createContext("/test", new MyHandler());
      httpsServer.setExecutor(null); // creates a default executor
      httpsServer.start();

    } catch (Exception exception) {
      System.out.println("Failed to create HTTPS server on port " + port + " of localhost");
      exception.printStackTrace();
    }
  }
}
