package com.example.Variety.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    // SECRET KEY DEL JWT
    // Esta clave se usa para firmar y validar el token.
    @Value("${jwt.secret:EstaEsMiClaveSecretaSuperSeguraParaFirmarElJWTMinimalistaDeSpring123456}")
    private String SECRET;

    // Convierte el SECRET (String) a una clave criptográfica válida para el algoritmo HS256
    private Key getSignInKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extrae todas las claims del token
    // Si el token fue modificado, expiró o no coincide la firma esta función lanzará una excepción
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // clave para validar firma
                .build()
                .parseClaimsJws(token) // valida y abre el JWT
                .getBody();  // devuelve contenido (claims)
    }

    // Obtiene un claim específico usando una función resolver
    
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // Extract the username (SUBJECT) inside the JWT
    // El "subject" es el campo principal que identifica al usuario
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Obtiene la fecha de expiración del token
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Verifica si el token está vencido
    // Devuelve true si la fecha de expiración está en el pasado
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Genera un token nuevo para un usuario en login
    // Por defecto no se agregan claims extra, solo:
    //  - subject (username)
    //  - issuedAt
    //  - expiration
    
    public String generateToken(UserDetails userDetails) {
        Map<String,Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    // CREA EL JWT FIRMADO
    
    private String createToken(Map<String,Object> claims, String subject) {

        long expirationTime = 1000 * 60 * 2; // 2 minutos

        return Jwts.builder()
                .setClaims(claims) // datos adicionales (vacíos)
                .setSubject(subject) // nombre del usuario
                .setIssuedAt(new Date(System.currentTimeMillis())) // fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // expiración
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // firma con clave secreta
                .compact();  // generar token final (String)
    }

    // VALIDACIÓN COMPLETA DEL TOKEN
    // 1. El usuario del token debe coincidir con el usuario cargado.
    // 2. El token NO debe estar expirado.
    // 3. Si la firma no coincide, extractUsername() lanzará excepción
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        return (username.equals(userDetails.getUsername()) // identidad válida
                && !isTokenExpired(token));  // no expirado
    }
}
