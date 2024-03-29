package com.chrisgalhur.dice_game.controller;

import com.chrisgalhur.dice_game.exception.InvalidCredentialsException;
import com.chrisgalhur.dice_game.response.AuthResponse;
import com.chrisgalhur.dice_game.dto.SessionPlayerDTO;
import com.chrisgalhur.dice_game.service.AuthorizationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RESTFUL Controller class for handling only authentication operations.
 * This controller manages operations related to player authentication:
 * - POST /register: Register a new player.
 * - POST /login: Login an existing player.
 *
 * @version 1.0
 * @author ChrisGalHur
 */
@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthorizationController {

    //region INJECTIONS
    private final AuthorizationServiceImpl authorizationService;

    /**
     * Injects dependencies into the controller.
     *
     * @param authorizationService Authorization service implementation.
     *  */
    @Autowired
    public AuthorizationController(AuthorizationServiceImpl authorizationService) {
        this.authorizationService = authorizationService;
    }
    //endregion INJECTIONS

    //region REGISTER
    /**
     * Registers a new player using a POST request.
     * Validates the player information to ensure it is not null and meets specific conditions.
     *
     * @param sessionPlayerDTO The player to register
     * @return ResponseEntity indicating the result of the registration operation.
     *        Possible HTTP status codes:
     *        - 201 Created: Player registered successfully.
     *        - 400 Bad Request: Invalid request body or player already exists.
     *        - 401 Unauthorized: Player is not authorized to perform the operation.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody SessionPlayerDTO sessionPlayerDTO) {
        log.info("endpoint /api/auth/register called");

        try{
            AuthResponse response = authorizationService.authenticateRegisterPlayer(sessionPlayerDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (InvalidCredentialsException e){
            return ResponseEntity.badRequest().body(new AuthResponse(null, e.getMessage()));
        }
    }
    //endregion REGISTER

    //region LOGIN
    /**
     * Logs in a player using a POST request.
     * Validates the player information to ensure it is not null and meets specific conditions.
     *
     * @param sessionPlayerDTO The player to login
     * @return ResponseEntity indicating the result of the login operation.
     *        Possible HTTP status codes:
     *        - 200 OK: Player logged in successfully.
     *        - 400 Bad Request: Invalid request body or player does not exist.
     *        - 401 Unauthorized: Player is not authorized to perform the operation.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody SessionPlayerDTO sessionPlayerDTO) {
        log.info("endpoint /api/auth/login called");

        try{
            AuthResponse response = authorizationService.authenticateLoginPlayer(sessionPlayerDTO);
            return ResponseEntity.ok().body(response);
        }catch (InvalidCredentialsException e){
            return ResponseEntity.badRequest().body(new AuthResponse(null, e.getMessage()));
        }
    }
    //endregion LOGIN
}
