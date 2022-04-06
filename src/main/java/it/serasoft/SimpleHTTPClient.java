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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SimpleHTTPClient {

  public static void main(String[] args) {
    SimpleHTTPClient httpClient = new SimpleHTTPClient();
    String url = args[0];
    String keystore = args[1];
    httpClient.connect(url, keystore);
  }

  public void connect(String url, String keystore) {

    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    KeyStore keyStore = null;
    try {
      keyStore = KeyStore.getInstance("JKS");
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }
    try {
      FileInputStream fis1 = new FileInputStream(keystore);
      keyStore.load(fis1, "changeit".toCharArray());
    } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
      e.printStackTrace();
    }

    SSLContext sslContext = null;
    try {
      sslContext = SSLContexts.custom()
              .loadKeyMaterial(keyStore, "changeit".toCharArray())
              .loadTrustMaterial(keyStore, (X509Certificate[] arg0, String arg1) -> true)
              .build();
    } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
      e.printStackTrace();
    }

    CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
    try {
      HttpResponse response = httpClient.execute(new HttpGet(url));
      System.out.println("Print response output: " + response.toString());
      HttpEntity entity = response.getEntity();
      String entityContent = EntityUtils.toString(entity);
      System.out.println("Received output: " + entityContent);
    } catch (IOException e) {
      System.out.println("Error while calling test URL: " + e.getMessage());
    }
  }
}
