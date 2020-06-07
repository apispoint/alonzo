/*
 * Copyright APIS Point, LLC or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.apispoint.service;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class EccBC {

    public static void main(String[] args) throws Exception {
        Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);

        /*
        Curve   Size (in bits)
        P-224   224
        P-256   256
        P-384   384
        P-521   521
        */

        long stime = System.currentTimeMillis();
        KeyPairGenerator keyPair = KeyPairGenerator.getInstance("EC"/*, "BCFIPS"*/);
        keyPair.initialize(521);
        KeyPair pair = keyPair.generateKeyPair();
        long etime = System.currentTimeMillis();
        System.out.println(etime - stime + "ms");
        System.out.println("Default CryptoProvider = " + Security.getProviders()[0]);
        System.out.println("CryptoProvider = " + keyPair.getProvider());

        System.out.println(pair.getPublic().toString());
        System.out.println("----------");
        System.out.println(pair.getPrivate().toString());

        /*
         * CERTIFICATE
         */
        Calendar startDate = Calendar.getInstance();  // time from which certificate is valid
        Calendar expiryDate = Calendar.getInstance(); // time after which certificate is not valid
        expiryDate.set(Calendar.YEAR, expiryDate.get(Calendar.YEAR) + 1);

        X500NameBuilder issuer = new X500NameBuilder(BCStyle.INSTANCE);
        issuer.addRDN(BCStyle.C, "C");
        issuer.addRDN(BCStyle.O, "O");
        issuer.addRDN(BCStyle.ST, "ST");
        issuer.addRDN(BCStyle.L, "L");
        issuer.addRDN(BCStyle.OU, "OU");
        issuer.addRDN(BCStyle.EmailAddress, "EmailAddress");
        issuer.addRDN(BCStyle.CN, "CN");

        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                issuer.build(),
                new BigInteger("1"),
                startDate.getTime(),
                expiryDate.getTime(),
                issuer.build(),
                pair.getPublic());

        ContentSigner sigGen =
                new JcaContentSignerBuilder("SHA384withECDSA")
                .setProvider("BCFIPS")
                .build(pair.getPrivate());

        X509Certificate cert =
                new JcaX509CertificateConverter().setProvider("BCFIPS")
                .getCertificate(certGen.build(sigGen));

        cert.checkValidity(new Date());
        cert.verify(cert.getPublicKey());
        System.out.println(cert.toString());

        /*
         * SIGNATURE VERIFY
         */
        Signature dsa = Signature.getInstance("SHA384withECDSA");

        dsa.initSign(pair.getPrivate());

        String str = "This is string to sign";
        byte[] strByte = str.getBytes("UTF-8");
        dsa.update(strByte);

        /*
         * Now that all the data to be signed has been read in, generate a
         * signature for it
         */

        byte[] realSig = dsa.sign();
        System.out.println("Signature: " + new BigInteger(1, realSig).toString(16));

        dsa.initVerify(pair.getPublic());
        dsa.update(str.getBytes("UTF-8"));
        System.out.println(" true = " + dsa.verify(realSig)); // True

        KeyPairGenerator fakekeypair = KeyPairGenerator.getInstance("ECDSA", "BCFIPS");
        fakekeypair.initialize(521, new SecureRandom());
        KeyPair fakepair = fakekeypair.generateKeyPair();

        dsa.initVerify(fakepair.getPublic());
        dsa.update(str.getBytes("UTF-8"));
        System.out.println("false = " + dsa.verify(realSig)); // False
    }

}
