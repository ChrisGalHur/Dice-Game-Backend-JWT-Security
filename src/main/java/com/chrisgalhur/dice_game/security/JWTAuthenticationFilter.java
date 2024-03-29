package com.chrisgalhur.dice_game.security;

import com.chrisgalhur.dice_game.repository.PlayerRepository;
import com.chrisgalhur.dice_game.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

/**
 * Handles JWT-based authentication.
 *
 * @version 1.0
 * @author ChrisGalHur
 */
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    //region DEPENDENCY INJECTION
    @Autowired
    private JWTGenerator tokenGenerator;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private PlayerRepository playerRepository;
    //endregion DEPENDENCY INJECTION

    //region DO FILTER INTERNAL
    /**
     * Filters the request and validates the token JWT.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param filterChain Filter chain to process the request.
     * @throws ServletException If occurs an error during the filter process.
     * @throws IOException If I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //Extract and validate the token from the request.
        String token = getJWTFromRequest(request);

        if(StringUtils.hasText(token) && tokenGenerator.validateToken(token)) {
            //Extract the user id from the token and get the user name from the database.
            String id = tokenGenerator.getUserNameFromJWT(token);
            String name = playerRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Player not found")).getName();

            //Create the authentication token and set the authentication details.
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(name);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //Set the authentication token in the security context.
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        //Continue the filter chain.
        filterChain.doFilter(request, response);
    }
    //endregion DO FILTER INTERNAL

    //region GET JWT FROM REQUEST
    /**
     * Extract the JWT token from the request.
     * If the token is not found, it returns null.
     *
     * @param request HTTP request.
     * @return Extracted JWT token or null if not found.
     */
    private String getJWTFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")){
            return bearerToken.substring(7);
        }
        return null;
    }
    //endregion GET JWT FROM REQUEST
}
