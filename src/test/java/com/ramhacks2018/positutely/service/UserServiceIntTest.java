package com.ramhacks2018.positutely.service;

import com.ramhacks2018.positutely.PositutelyApp;
import com.ramhacks2018.positutely.config.Constants;
import com.ramhacks2018.positutely.domain.User;
import com.ramhacks2018.positutely.repository.UserRepository;
import com.ramhacks2018.positutely.service.dto.UserDTO;
import com.ramhacks2018.positutely.security.AuthoritiesConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PositutelyApp.class)
public class UserServiceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User user;

    @Before
    public void init() {
        userRepository.deleteAll();
        user = new User();
        user.setLogin("johndoe");
        user.setActivated(true);
        user.setEmail("johndoe@localhost");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");
    }

    @Test
    public void assertThatAnonymousUserIsNotGet() {
        user.setId(Constants.ANONYMOUS_USER);
        user.setLogin(Constants.ANONYMOUS_USER);
        if (!userRepository.findOneByLogin(Constants.ANONYMOUS_USER).isPresent()) {
            userRepository.save(user);
        }
        final PageRequest pageable = PageRequest.of(0, (int) userRepository.count());
        final Page<UserDTO> allManagedUsers = userService.getAllManagedUsers(pageable);
        assertThat(allManagedUsers.getContent().stream()
            .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
            .isTrue();
    }


    @Test
    public void assertThatUserLocaleIsCorrectlySetFromAuthenticationDetails() {
        user.setId(Constants.ANONYMOUS_USER);
        user.setLogin(Constants.ANONYMOUS_USER);
        if (!userRepository.findOneByLogin(Constants.ANONYMOUS_USER).isPresent()) {
            userRepository.save(user);
        }

        Map<String, Object> userDetails = new HashMap<String, Object>();
        userDetails.put("preferred_username", user.getLogin());
    	userDetails.put("given_name", user.getFirstName());
    	userDetails.put("family_name", user.getLastName());
    	userDetails.put("email", user.getEmail());
    	userDetails.put("picture", user.getImageUrl());
    	userDetails.put("locale", "en_US");

    	OAuth2Authentication authentication = createMockOAuth2AuthenticationWithDetails(userDetails);

    	UserDTO userDTO = userService.getUserFromAuthentication(authentication);

    	assertEquals("User langkey is not corretly set.", userDTO.getLangKey(), "en");

    	userDetails.put("locale", "it-IT");
    	authentication = createMockOAuth2AuthenticationWithDetails(userDetails);

    	userDTO = userService.getUserFromAuthentication(authentication);

    	assertEquals("User langkey is not corretly set.", userDTO.getLangKey(), "it");
    }

    private OAuth2Authentication createMockOAuth2AuthenticationWithDetails(Map<String, Object> userDetails) {
    	Set<String> scopes = new HashSet<String>();

    	Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(Constants.ANONYMOUS_USER, Constants.ANONYMOUS_USER, authorities);
        usernamePasswordAuthenticationToken.setDetails(userDetails);

    	OAuth2Request authRequest = new OAuth2Request(null, "testClient", null, true, scopes, null, null, null, null);

    	return new OAuth2Authentication(authRequest, ((Authentication)usernamePasswordAuthenticationToken));
    }
}
