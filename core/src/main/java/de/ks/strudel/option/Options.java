/**
 * Copyright [2016] [Christian Loehnert]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.strudel.option;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;

@Singleton
public class Options {
  private int port = 4567;
  private String host = "0.0.0.0";
  private SSLContext sslContext;

  /**
   * Injection point for own ssl context
   *
   * @param sslContext created by you guice module
   * @return self for chaining
   */
  @Inject(optional = true)
  public Options setSslContext(SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  /**
   * @return the configured host adress
   */
  public String host() {
    return host;
  }

  /**
   * Assigns the bind adress for starting the host on. Default is 0.0.0.0 which means all adresses.
   * This is important on a clustered environment when a server switch happens.
   *
   * @param host the adress
   * @return self
   */
  public Options host(String host) {
    this.host = host;
    return this;
  }

  public Options port(int port) {
    this.port = port;
    return this;
  }

  public int port() {
    return port;
  }

  /**
   * Constructs a new ssl context for starting undertow https server.
   * You can also provide the ssl context yourself in a guice module which is injected via {@link #setSslContext(SSLContext)}
   *
   * @param keystoreFile     nullable file, either classpath or absolute
   * @param keystorePassword password for the keystore
   * @return self for chaining
   * @throws IOException               e
   * @throws KeyStoreException         e
   * @throws NoSuchAlgorithmException  e
   * @throws CertificateException      e
   * @throws UnrecoverableKeyException e
   * @throws KeyManagementException    e
   */

  public Options secure(String keystoreFile, String keystorePassword) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException {
    return secure(keystoreFile, keystorePassword, null, null);
  }

  /**
   * Constructs a new ssl context for starting undertow https server.
   * You can also provide the ssl context yourself in a guice module which is injected via {@link #setSslContext(SSLContext)}
   *
   * @param keystoreFile       nullable file, either classpath or absolute
   * @param keystorePassword   password for the keystore
   * @param truststoreFile     nullable file, either classpath or absolute
   * @param truststorePassword password for the keystore
   * @return self for chaining
   * @throws IOException               e
   * @throws KeyStoreException         e
   * @throws NoSuchAlgorithmException  e
   * @throws CertificateException      e
   * @throws UnrecoverableKeyException e
   * @throws KeyManagementException    e
   */
  public Options secure(String keystoreFile, String keystorePassword, @Nullable String truststoreFile, @Nullable String truststorePassword) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(loadKeyStore(keystoreFile, keystorePassword), keystorePassword.toCharArray());
    KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

    TrustManager[] trustManagers = new TrustManager[0];
    if (truststoreFile != null && truststorePassword != null) {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(loadKeyStore(truststoreFile, truststorePassword));
      trustManagers = trustManagerFactory.getTrustManagers();
    }

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyManagers, trustManagers, new SecureRandom());
    this.sslContext = sslContext;
    return this;
  }

  private KeyStore loadKeyStore(String keystoreFile, String keystorePassword) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
    InputStream keyStoreStream = null;
    try {
      URL resource = getClass().getResource(keystoreFile);
      if (resource != null) {
        keyStoreStream = resource.openStream();
      } else {
        keyStoreStream = new FileInputStream(keystoreFile);
      }
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(keyStoreStream, keystorePassword.toCharArray());
      return keyStore;
    } finally {
      if (keyStoreStream != null) {
        keyStoreStream.close();
      }
    }
  }

  public boolean isSecure() {
    return sslContext != null;
  }

  public SSLContext getSslContext() {
    return sslContext;
  }
}
