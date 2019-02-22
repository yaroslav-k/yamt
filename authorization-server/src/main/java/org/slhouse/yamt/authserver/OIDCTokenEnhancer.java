/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.authserver;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Yaroslav V. Khazanov
 * Make our Authorization Server partially compatible with OpenID Connect standard. Actually, just adds id_token.
 * taken here:
 * https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/blob/master/openid-connect-server/src/main/java/org/mitre/openid/connect/token/ConnectTokenEnhancer.java
 **/
@Slf4j
@Component
class OIDCTokenEnhancer implements TokenEnhancer {
    private String issuer;

    private final KeyPair keyPair;

    @Autowired
    public OIDCTokenEnhancer(KeyPair keyPair) {
        this.keyPair = keyPair;
        try {
            issuer = "http://" + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Error in InetAddress.getLocalHost().getHostName()", e);
            issuer = "http://localhost";
        }
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        OAuth2Request originalAuthRequest = authentication.getOAuth2Request();
        String clientId = originalAuthRequest.getClientId();

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .claim("azp", clientId)
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(accessToken.getExpiration())
                .subject(authentication.getName())
                .jwtID(UUID.randomUUID().toString()) // set a random NONCE in the middle of it
                // and some custom information
                .claim("authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .claim("scopes", accessToken.getScope())
        ;


        String audience = (String) authentication.getOAuth2Request().getExtensions().get("aud");
        if (audience != null && !audience.isEmpty()) {
            builder.audience(Collections.singletonList(audience));
        } else
            builder.audience(clientId);

        final JWTClaimsSet claims = builder.build();
        final JWSHeader.Builder jwsBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256);
        final JWSHeader header = jwsBuilder.build();
        SignedJWT signed = new SignedJWT(header, claims);
        try {
            signed.sign(new RSASSASigner(keyPair.getPrivate()));
        } catch (JOSEException e) {
            log.error("Error signing jwt: ", e);
        }
        accessToken.getAdditionalInformation().put(OidcParameterNames.ID_TOKEN, signed.serialize());

        return accessToken;
    }
}
