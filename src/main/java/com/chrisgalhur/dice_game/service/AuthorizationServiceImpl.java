package com.chrisgalhur.dice_game.service;

import com.chrisgalhur.dice_game.exception.InvalidCredentialsException;
import com.chrisgalhur.dice_game.response.AuthResponse;
import com.chrisgalhur.dice_game.dto.SessionPlayerDTO;
import com.chrisgalhur.dice_game.security.CustomAuthenticationManager;
import com.chrisgalhur.dice_game.security.JWTGenerator;
import io.micrometer.common.util.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementation of the authorization service.
 * @see AuthorizationService for the interface.
 *
 * @version 1.0
 * @author ChrisGalHur
 */
@Service
public class AuthorizationServiceImpl implements AuthorizationService{

    //region ATTRIBUTES
    private static final String INVALID_REQUEST_BODY = "Invalid request body";
    //endregion ATTRIBUTES

    //region INJECTIONS
    private final SessionPlayerServiceImpl sessionPlayerService;
    private final CustomAuthenticationManager customAuthenticationManager;
    private final JWTGenerator jwtGenerator;

    /**
     * Constructor of the class.
     *
     * @param sessionPlayerService Player service.
     * @param customAuthenticationManager Custom authentication manager.
     * @param jwtGenerator JWT generator.
     */
    public AuthorizationServiceImpl(SessionPlayerServiceImpl sessionPlayerService, CustomAuthenticationManager customAuthenticationManager, JWTGenerator jwtGenerator) {
        this.sessionPlayerService = sessionPlayerService;
        this.customAuthenticationManager = customAuthenticationManager;
        this.jwtGenerator = jwtGenerator;
    }
    //endregion INJECTIONS

    //region AUTHENTICATE REGISTER PLAYER
    /**
     * Method to authenticate a player.
     *
     * @param sessionPlayerDTO SessionPlayerDTO with the player information.
     * @return AuthResponseDTO With the authentication response.
     * @throws InvalidCredentialsException If the request body is invalid or the player already exists.
     */
    @Override
    public AuthResponse authenticateRegisterPlayer(SessionPlayerDTO sessionPlayerDTO) {
        AuthResponse authValidated = validateRegistrationRequest(sessionPlayerDTO);

        SessionPlayerDTO playerRegistered = sessionPlayerService.registerNewUser(sessionPlayerDTO);

        Authentication authentication = customAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(playerRegistered.getName(), sessionPlayerDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(playerRegistered.getId());

        if(sessionPlayerDTO.getName().equals(playerRegistered.getName())){
            authValidated.setAccessToken(token);
            return authValidated;
        }else throw new InvalidCredentialsException("Something went wrong. Please try again.");
    }
    //endregion AUTHENTICATE REGISTER PLAYER

    //region VALIDATE REGISTRATION REQUEST
    /**
     * Method to validate the registration request.
     * If the request is valid, the player is registered.
     * If the player already exists, an exception is thrown.
     *
     * @param sessionPlayerDTO SessionPlayerDTO with the player information.
     * @return AuthResponseDTO With the authentication response.
     * @throws InvalidCredentialsException If the request body is invalid or the player already exists.
     */
    private AuthResponse validateRegistrationRequest(SessionPlayerDTO sessionPlayerDTO) {

            if (sessionPlayerDTO == null) {
                throw new InvalidCredentialsException(INVALID_REQUEST_BODY);
            }

            //validate if name of user is null or empty
            if (StringUtils.isEmpty(sessionPlayerDTO.getName())) {
                sessionPlayerDTO.setName("UNKNOWN");
                SessionPlayerDTO playerRegistered = sessionPlayerService.registerNewUser(sessionPlayerDTO);

                Authentication authentication = customAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(String.valueOf(playerRegistered.getId()), playerRegistered.getName()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtGenerator.generateToken(sessionPlayerDTO.getId());
                return new AuthResponse(token, "User registered with default name: " + playerRegistered.getName());
            }

            //validate if user already exists
            if (sessionPlayerService.existsByName(sessionPlayerDTO.getName())) {
                throw new InvalidCredentialsException("User by name " + sessionPlayerDTO.getName() + " already exists.\n" +
                        "Please select another name.");
            }

            return new AuthResponse(null, "User registered with name: " + sessionPlayerDTO.getName());
    }
    //endregion VALIDATE REGISTRATION REQUEST

    //region AUTHENTICATE LOGIN PLAYER
    /**
     * Method to authenticate a player when logging in.
     *
     * @param sessionPlayerDTO SessionPlayerDTO with the player information.
     * @throws InvalidCredentialsException If the request body is invalid or the player does not exist.
     * @return AuthResponseDTO With the authentication response.
     */
    @Override
    public AuthResponse authenticateLoginPlayer(SessionPlayerDTO sessionPlayerDTO) {
        try {
            //validate if PlayerDTO is null or empty
            if (sessionPlayerDTO == null || StringUtils.isBlank(sessionPlayerDTO.getName()) || StringUtils.isEmpty(sessionPlayerDTO.getPassword())) {
                throw new InvalidCredentialsException(INVALID_REQUEST_BODY);
            }

            SessionPlayerDTO sessionPlayerLogged = sessionPlayerService.loginUser(sessionPlayerDTO);

            if (sessionPlayerLogged == null) {
                throw new InvalidCredentialsException("User " + sessionPlayerDTO.getName() + " does not exist or password is incorrect.");
            } else {

                Authentication authentication = customAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(sessionPlayerDTO.getName(), sessionPlayerDTO.getPassword()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtGenerator.generateToken(sessionPlayerLogged.getId());
                return new AuthResponse(token, "User " + sessionPlayerDTO.getName() + " logged in successfully.");
            }
        } catch (InvalidCredentialsException e) {
            return new AuthResponse(null, e.getMessage());
        }
    }
    //endregion AUTHENTICATE LOGIN PLAYER
}
