/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.systests.jms_1_1.extensions.tls;

import static org.apache.qpid.test.utils.TestSSLConstants.BROKER_KEYSTORE_PASSWORD;
import static org.apache.qpid.test.utils.TestSSLConstants.BROKER_TRUSTSTORE_PASSWORD;
import static org.apache.qpid.test.utils.TestSSLConstants.KEYSTORE_PASSWORD;
import static org.apache.qpid.test.utils.TestSSLConstants.TRUSTSTORE_PASSWORD;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.xml.bind.DatatypeConverter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.qpid.server.model.Port;
import org.apache.qpid.server.model.Protocol;
import org.apache.qpid.server.security.FileKeyStore;
import org.apache.qpid.server.security.FileTrustStore;
import org.apache.qpid.systests.JmsTestBase;
import org.apache.qpid.test.utils.TestSSLConstants;
import org.apache.qpid.tests.utils.BrokerAdmin;

public class TlsTest extends JmsTestBase
{
    private static final String TEST_PROFILE_RESOURCE_BASE = System.getProperty("java.io.tmpdir") + "/";
    private static final String BROKER_KEYSTORE =
            TEST_PROFILE_RESOURCE_BASE + org.apache.qpid.test.utils.TestSSLConstants.BROKER_KEYSTORE;
    private static final String BROKER_TRUSTSTORE =
            TEST_PROFILE_RESOURCE_BASE + org.apache.qpid.test.utils.TestSSLConstants.BROKER_TRUSTSTORE;
    private static final String KEYSTORE =
            TEST_PROFILE_RESOURCE_BASE + org.apache.qpid.test.utils.TestSSLConstants.KEYSTORE;
    private static final String TRUSTSTORE =
            TEST_PROFILE_RESOURCE_BASE + org.apache.qpid.test.utils.TestSSLConstants.TRUSTSTORE;

    @BeforeClass
    public static void setUp() throws Exception
    {
        System.setProperty("javax.net.debug", "ssl");

        // workaround for QPID-8069
        if (getProtocol() != Protocol.AMQP_1_0 && getProtocol() != Protocol.AMQP_0_10)
        {
            System.setProperty("amqj.MaximumStateWait", "4000");
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        System.clearProperty("javax.net.debug");
        if (getProtocol() != Protocol.AMQP_1_0)
        {
            System.clearProperty("amqj.MaximumStateWait");
        }
    }

    @Test
    public void testCreateSSLConnectionUsingConnectionURLParams() throws Exception
    {
        //Start the broker (NEEDing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), true, false, false);

        InetSocketAddress brokerAddress = getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP);
        Connection connection = getConnectionBuilder().setSslPort(port)
                                                      .setHost(brokerAddress.getHostName())
                                                      .setTls(true)
                                                      .setKeyStoreLocation(KEYSTORE)
                                                      .setKeyStorePassword(KEYSTORE_PASSWORD)
                                                      .setTrustStoreLocation(TRUSTSTORE)
                                                      .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                                      .build();
        try
        {
            assertConnection(connection);
        }
        finally
        {
            connection.close();
        }
    }

    @Test
    public void testCreateSSLConnectionWithCertificateTrust() throws Exception
    {
        assumeThat("Qpid JMS Client does not support trusting of a certificate",
                   getProtocol(),
                   is(not(equalTo(Protocol.AMQP_1_0))));

        int port = configureTlsPort(getTestPortName(), false, false, false);
        File trustCertFile = extractCertFileFromTestTrustStore();

        InetSocketAddress brokerAddress = getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP);
        Connection connection = getConnectionBuilder().setSslPort(port)
                                                      .setHost(brokerAddress.getHostName())
                                                      .setTls(true)
                                                      .setOptions(Collections.singletonMap("trusted_certs_path",
                                                                                           trustCertFile.getCanonicalPath()))
                                                      .build();
        try
        {
            assertConnection(connection);
        }
        finally
        {
            connection.close();
        }
    }

    @Test
    public void testSSLConnectionToPlainPortRejected() throws Exception
    {
        assumeThat("QPID-8069", getProtocol(), is(anyOf(equalTo(Protocol.AMQP_1_0), equalTo(Protocol.AMQP_0_10))));

        setSslStoreSystemProperties();
        try
        {
            InetSocketAddress brokerAddress = getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP);
            getConnectionBuilder().setSslPort(brokerAddress.getPort())
                                  .setHost(brokerAddress.getHostName())
                                  .setTls(true)
                                  .build();

            fail("Exception not thrown");
        }
        catch (JMSException e)
        {
            // PASS
        }
        finally
        {
            clearSslStoreSystemProperties();
        }
    }

    @Test
    public void testHostVerificationIsOnByDefault() throws Exception
    {
        assumeThat("QPID-8069", getProtocol(), is(anyOf(equalTo(Protocol.AMQP_1_0), equalTo(Protocol.AMQP_0_10))));

        //Start the broker (NEEDing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), true, false, false);

        try
        {
            getConnectionBuilder().setSslPort(port)
                                  .setHost("127.0.0.1")
                                  .setTls(true)
                                  .setKeyStoreLocation(KEYSTORE)
                                  .setKeyStorePassword(KEYSTORE_PASSWORD)
                                  .setTrustStoreLocation(TRUSTSTORE)
                                  .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                  .build();
            fail("Exception not thrown");
        }
        catch (JMSException e)
        {
            // PASS
        }

        Connection connection = getConnectionBuilder().setSslPort(port)
                                                      .setHost("127.0.0.1")
                                                      .setTls(true)
                                                      .setKeyStoreLocation(KEYSTORE)
                                                      .setKeyStorePassword(KEYSTORE_PASSWORD)
                                                      .setTrustStoreLocation(TRUSTSTORE)
                                                      .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                                      .setVerifyHostName(false)
                                                      .build();
        try
        {
            assertConnection(connection);
        }
        finally
        {
            connection.close();
        }
    }

    @Test
    public void testCreateSslConnectionUsingJVMSettings() throws Exception
    {
        //Start the broker (NEEDing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), true, false, false);
        setSslStoreSystemProperties();
        try
        {
            Connection connection = getConnectionBuilder().setSslPort(port)
                                                          .setTls(true)
                                                          .build();
            try
            {
                assertConnection(connection);
            }
            finally
            {
                connection.close();
            }
        }
        finally
        {
            clearSslStoreSystemProperties();
        }
    }

    @Test
    public void testMultipleCertsInSingleStore() throws Exception
    {
        //Start the broker (NEEDing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), true, false, false);
        setSslStoreSystemProperties();
        try
        {
            Connection connection = getConnectionBuilder().setClientId(getTestName())
                                                          .setSslPort(port)
                                                          .setTls(true)
                                                          .setKeyAlias(TestSSLConstants.CERT_ALIAS_APP1)
                                                          .build();
            try
            {
                assertConnection(connection);
            }
            finally
            {
                connection.close();
            }

            Connection connection2 = getConnectionBuilder().setSslPort(port)
                                                           .setTls(true)
                                                           .setKeyAlias(TestSSLConstants.CERT_ALIAS_APP2)
                                                           .build();
            try
            {
                assertConnection(connection2);
            }
            finally
            {
                connection2.close();
            }
        }
        finally
        {
            clearSslStoreSystemProperties();
        }
    }

    @Test
    public void testVerifyHostNameWithIncorrectHostname() throws Exception
    {
        assumeThat("QPID-8069", getProtocol(), is(anyOf(equalTo(Protocol.AMQP_1_0), equalTo(Protocol.AMQP_0_10))));

        //Start the broker (WANTing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), false, true, false);

        setSslStoreSystemProperties();
        try
        {
            getConnectionBuilder().setSslPort(port)
                                  .setHost("127.0.0.1")
                                  .setTls(true)
                                  .setVerifyHostName(true)
                                  .build();
            fail("Exception not thrown");
        }
        catch (JMSException e)
        {
            // PASS
        }
        finally
        {
            clearSslStoreSystemProperties();
        }
    }

    @Test
    public void testVerifyLocalHost() throws Exception
    {
        //Start the broker (WANTing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), false, true, false);

        setSslStoreSystemProperties();
        try
        {
            Connection connection = getConnectionBuilder().setSslPort(port)
                                                          .setHost("localhost")
                                                          .setTls(true)
                                                          .build();
            try
            {
                assertConnection(connection);
            }
            finally
            {
                connection.close();
            }
        }
        finally
        {
            clearSslStoreSystemProperties();
        }
    }

    @Test
    public void testCreateSSLConnectionUsingConnectionURLParamsTrustStoreOnly() throws Exception
    {
        //Start the broker (WANTing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), false, true, false);

        InetSocketAddress brokerAddress = getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP);
        Connection connection = getConnectionBuilder().setSslPort(port)
                                                      .setHost(brokerAddress.getHostName())
                                                      .setTls(true)
                                                      .setTrustStoreLocation(TRUSTSTORE)
                                                      .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                                      .build();
        try
        {
            assertConnection(connection);
        }
        finally
        {
            connection.close();
        }
    }

    @Test
    public void testClientCertificateMissingWhilstNeeding() throws Exception
    {
        assumeThat("QPID-8069", getProtocol(), is(anyOf(equalTo(Protocol.AMQP_1_0), equalTo(Protocol.AMQP_0_10))));

        //Start the broker (NEEDing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), true, false, false);

        try
        {
            getConnectionBuilder().setSslPort(port)
                                  .setHost(getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP).getHostName())
                                  .setTls(true)
                                  .setTrustStoreLocation(TRUSTSTORE)
                                  .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                  .build();
            fail("Connection was established successfully");
        }
        catch (JMSException e)
        {
            // PASS
        }
    }

    @Test
    public void testClientCertificateMissingWhilstWanting() throws Exception
    {
        //Start the broker (WANTing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), false, true, false);

        InetSocketAddress brokerAddress = getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP);
        Connection connection = getConnectionBuilder().setSslPort(port)
                                                      .setHost(brokerAddress.getHostName())
                                                      .setTls(true)
                                                      .setTrustStoreLocation(TRUSTSTORE)
                                                      .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                                      .build();
        try
        {
            assertConnection(connection);
        }
        finally
        {
            connection.close();
        }
    }

    @Test
    public void testClientCertMissingWhilstWantingAndNeeding() throws Exception
    {
        assumeThat("QPID-8069", getProtocol(), is(anyOf(equalTo(Protocol.AMQP_1_0), equalTo(Protocol.AMQP_0_10))));
        //Start the broker (NEEDing and WANTing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), true, true, false);

        try
        {
            getConnectionBuilder().setSslPort(port)
                                  .setHost(getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP).getHostName())
                                  .setTls(true)
                                  .setTrustStoreLocation(TRUSTSTORE)
                                  .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                  .build();
            fail("Connection was established successfully");
        }
        catch (JMSException e)
        {
            // PASS
        }
    }

    @Test
    public void testCreateSSLandTCPonSamePort() throws Exception
    {

        //Start the broker (WANTing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), false, true, true);

        InetSocketAddress brokerAddress = getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP);
        Connection connection = getConnectionBuilder().setSslPort(port)
                                                      .setHost(brokerAddress.getHostName())
                                                      .setTls(true)
                                                      .setKeyStoreLocation(KEYSTORE)
                                                      .setKeyStorePassword(KEYSTORE_PASSWORD)
                                                      .setTrustStoreLocation(TRUSTSTORE)
                                                      .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                                      .build();
        try
        {
            assertConnection(connection);
        }
        finally
        {
            connection.close();
        }

        Connection connection2 = getConnectionBuilder().setPort(port)
                                                       .setHost(brokerAddress.getHostName())
                                                       .build();
        try
        {
            assertConnection(connection2);
        }
        finally
        {
            connection2.close();
        }
    }

    @Test
    public void testCreateSSLWithCertFileAndPrivateKey() throws Exception
    {
        assumeThat("Qpid JMS Client does not support trusting of a certificate",
                   getProtocol(),
                   is(not(equalTo(Protocol.AMQP_1_0))));

        //Start the broker (NEEDing client certificate authentication)
        int port = configureTlsPort(getTestPortName(), true, false, false);

        clearSslStoreSystemProperties();
        File[] certAndKeyFiles = extractResourcesFromTestKeyStore();
        final Map<String, String> options = new HashMap<>();
        options.put("client_cert_path", certAndKeyFiles[1].getCanonicalPath());
        options.put("client_cert_priv_key_path", certAndKeyFiles[0].getCanonicalPath());
        InetSocketAddress brokerAddress = getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP);
        Connection connection = getConnectionBuilder().setSslPort(port)
                                                      .setHost(brokerAddress.getHostName())
                                                      .setTls(true)
                                                      .setTrustStoreLocation(TRUSTSTORE)
                                                      .setTrustStorePassword(TRUSTSTORE_PASSWORD)
                                                      .setVerifyHostName(false)
                                                      .setOptions(options)
                                                      .build();
        try
        {
            assertConnection(connection);
        }
        finally
        {
            connection.close();
        }
    }


    private int configureTlsPort(final String portName,
                                 final boolean needClientAuth,
                                 final boolean wantClientAuth,
                                 final boolean samePort) throws Exception
    {

        Connection connection = getConnectionBuilder().setVirtualHost("$management").build();
        try
        {
            connection.start();
            return createPort(portName, needClientAuth, wantClientAuth, samePort, connection);
        }
        finally
        {
            connection.close();
        }
    }

    private int createPort(final String portName,
                           final boolean needClientAuth,
                           final boolean wantClientAuth,
                           final boolean plainAndSsl,
                           Connection connection) throws Exception
    {
        String keyStoreName = portName + "KeyStore";
        String trustStoreName = portName + "TrustStore";
        String authenticationProvider = null;

        List<Map<String, Object>> ports = queryEntitiesUsingAmqpManagement("org.apache.qpid.AmqpPort", connection);
        for (Map<String, Object> port : ports)
        {
            String name = String.valueOf(port.get(Port.NAME));

            Map<String, Object> attributes =
                    readEntityUsingAmqpManagement(name, "org.apache.qpid.AmqpPort", false, connection);
            if (attributes.get("boundPort")
                          .equals(getBrokerAdmin().getBrokerAddress(BrokerAdmin.PortType.AMQP).getPort()))
            {
                authenticationProvider = String.valueOf(attributes.get(Port.AUTHENTICATION_PROVIDER));
                break;
            }
        }

        final Map<String, Object> keyStoreAttributes = new HashMap<>();
        keyStoreAttributes.put("storeUrl", BROKER_KEYSTORE);
        keyStoreAttributes.put("password", BROKER_KEYSTORE_PASSWORD);
        createEntity(keyStoreName, FileKeyStore.class.getName(), keyStoreAttributes, connection);

        final Map<String, Object> trustStoreAttributes = new HashMap<>();
        trustStoreAttributes.put("storeUrl", BROKER_TRUSTSTORE);
        trustStoreAttributes.put("password", BROKER_TRUSTSTORE_PASSWORD);
        createEntity(trustStoreName, FileTrustStore.class.getName(), trustStoreAttributes, connection);

        Map<String, Object> sslPortAttributes = new HashMap<>();
        sslPortAttributes.put(Port.TRANSPORTS, plainAndSsl ? "[\"SSL\",\"TCP\"]" : "[\"SSL\"]");
        sslPortAttributes.put(Port.PORT, 0);
        sslPortAttributes.put(Port.AUTHENTICATION_PROVIDER, authenticationProvider);
        sslPortAttributes.put(Port.NEED_CLIENT_AUTH, needClientAuth);
        sslPortAttributes.put(Port.WANT_CLIENT_AUTH, wantClientAuth);
        sslPortAttributes.put(Port.NAME, portName);
        sslPortAttributes.put(Port.KEY_STORE, keyStoreName);
        sslPortAttributes.put(Port.TRUST_STORES, "[\"" + trustStoreName + "\"]");
        createEntity(portName, "org.apache.qpid.AmqpPort", sslPortAttributes, connection);

        Map<String, Object> portEffectiveAttributes =
                readEntityUsingAmqpManagement(portName, "org.apache.qpid.AmqpPort", false, connection);
        if (portEffectiveAttributes.containsKey("boundPort"))
        {
            return (int) portEffectiveAttributes.get("boundPort");
        }
        throw new RuntimeException("Bound port is not found");
    }

    private void setSslStoreSystemProperties()
    {
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);
    }

    private void clearSslStoreSystemProperties()
    {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }

    private File[] extractResourcesFromTestKeyStore() throws Exception
    {
        java.security.KeyStore ks = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
        try (InputStream is = new FileInputStream(KEYSTORE))
        {
            ks.load(is, KEYSTORE_PASSWORD.toCharArray());
        }

        File privateKeyFile = Files.createTempFile(getTestName(), ".private-key.der").toFile();
        try (FileOutputStream kos = new FileOutputStream(privateKeyFile))
        {
            Key pvt = ks.getKey(TestSSLConstants.CERT_ALIAS_APP1, KEYSTORE_PASSWORD.toCharArray());
            kos.write("-----BEGIN PRIVATE KEY-----\n".getBytes());
            String base64encoded = DatatypeConverter.printBase64Binary(pvt.getEncoded());
            while (base64encoded.length() > 76)
            {
                kos.write(base64encoded.substring(0, 76).getBytes());
                kos.write("\n".getBytes());
                base64encoded = base64encoded.substring(76);
            }

            kos.write(base64encoded.getBytes());
            kos.write("\n-----END PRIVATE KEY-----".getBytes());
            kos.flush();
        }

        File certificateFile = Files.createTempFile(getTestName(), ".certificate.der").toFile();
        try (FileOutputStream cos = new FileOutputStream(certificateFile))
        {
            Certificate[] chain = ks.getCertificateChain(TestSSLConstants.CERT_ALIAS_APP1);
            for (Certificate pub : chain)
            {
                cos.write("-----BEGIN CERTIFICATE-----\n".getBytes());
                String base64encoded = DatatypeConverter.printBase64Binary(pub.getEncoded());
                while (base64encoded.length() > 76)
                {
                    cos.write(base64encoded.substring(0, 76).getBytes());
                    cos.write("\n".getBytes());
                    base64encoded = base64encoded.substring(76);
                }
                cos.write(base64encoded.getBytes());

                cos.write("\n-----END CERTIFICATE-----\n".getBytes());
            }
            cos.flush();
        }

        return new File[]{privateKeyFile, certificateFile};
    }

    private File extractCertFileFromTestTrustStore() throws Exception
    {
        java.security.KeyStore ks = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
        try (InputStream is = new FileInputStream(TRUSTSTORE))
        {
            ks.load(is, TRUSTSTORE_PASSWORD.toCharArray());
        }

        File certificateFile = Files.createTempFile(getTestName(), ".crt").toFile();

        try (FileOutputStream cos = new FileOutputStream(certificateFile))
        {

            for (String alias : Collections.list(ks.aliases()))
            {
                Certificate pub = ks.getCertificate(alias);
                cos.write("-----BEGIN CERTIFICATE-----\n".getBytes());
                String base64encoded = DatatypeConverter.printBase64Binary(pub.getEncoded());
                while (base64encoded.length() > 76)
                {
                    cos.write(base64encoded.substring(0, 76).getBytes());
                    cos.write("\n".getBytes());
                    base64encoded = base64encoded.substring(76);
                }
                cos.write(base64encoded.getBytes());

                cos.write("\n-----END CERTIFICATE-----\n".getBytes());
            }
            cos.flush();
        }

        return certificateFile;
    }

    private String getTestPortName()
    {
        return getTestName() + "TlsPort";
    }

    private void assertConnection(final Connection connection) throws JMSException
    {
        assertNotNull("connection should be successful", connection);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        assertNotNull("create session should be successful", session);
    }
}
