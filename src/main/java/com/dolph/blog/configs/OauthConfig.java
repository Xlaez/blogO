package com.dolph.blog.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

//@Configuration
public class OauthConfig {

  @Value("${spring.github.clientId}")
  private String clientId;

  @Value("${spring.github.clientSecret}")
  private String clientSecret;

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(){
    return new InMemoryClientRegistrationRepository(this.githubClientRegistry());
  }

  private ClientRegistration githubClientRegistry(){
    return ClientRegistration.withRegistrationId("github")
        .clientId(clientId)
        .clientSecret(clientSecret)
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("https://github.com/login/oauth/access_token")
        .scope("user:email", "read:user")
        .tokenUri("https://github.com/login/oauth/access_token")
        .userInfoUri("https://api.github.com/user")
        .userNameAttributeName(IdTokenClaimNames.SUB)
//        .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
        .clientName("Github")
        .build();
  }
}
