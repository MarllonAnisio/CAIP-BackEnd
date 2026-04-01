package org.marllon.caip.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final long accessExpirationSeconds = 60 * 15; // 15 minutos
    private final long refreshExpirationSeconds = 60L * 60 * 24 * 7; // 7 dias
    private final String issuer;
    private final String audience;

    public JwtTokenService(
            @Value("${security.jwt.secret-base64}") String secretBase64,
            @Value("${security.jwt.issuer:caip-service}") String issuer,
            @Value("${security.jwt.audience:caip-clients}") String audience
    ) {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException("Propriedade 'security.jwt.secret-base64' não configurada");
        }
        byte[] keyBytes = tryDecodeSecret(secretBase64.trim());
        if (keyBytes.length < 32) { // garantir 256 bits para HS256
            keyBytes = derive256Bits(secretBase64.trim());
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
        this.audience = audience;
    }

    private byte[] tryDecodeSecret(String raw) {
        try {
            // Tenta Base64 padrão
            return Decoders.BASE64.decode(raw);
        } catch (Exception e1) {
            try {
                // Normaliza URL-safe Base64 (substitui '-' e '_' e adiciona padding)
                String normalized = normalizeBase64(raw);
                return Decoders.BASE64.decode(normalized);
            } catch (Exception e2) {
                // Fallback: retorna bytes do texto (será derivado depois)
                return raw.getBytes(StandardCharsets.UTF_8);
            }
        }
    }

    private String normalizeBase64(String s) {
        String n = s.replace('-', '+').replace('_', '/');
        int pad = n.length() % 4;
        if (pad == 2) n = n + "==";
        else if (pad == 3) n = n + "=";
        else if (pad == 1) throw new IllegalArgumentException("Base64 inválido (comprimento inválido)");
        return n;
    }

    private byte[] derive256Bits(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao derivar chave de 256 bits para JWT", e);
        }
    }

    public String generateToken(String username) {
        return generateAccessToken(username);
    }

    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setAudience(audience)
                .claim("typ", "access")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpirationSeconds)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setAudience(audience)
                .claim("typ", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpirationSeconds)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (username.equals(tokenUsername)) && !isTokenExpired(token) && isAccessToken(token) && validateStandardClaims(token);
    }

    public boolean isRefreshTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        if (!username.equals(tokenUsername) || isTokenExpired(token)) return false;
        return isRefreshToken(token) && validateStandardClaims(token);
    }

    public boolean isAccessToken(String token) {
        String type = extractClaim(token, claims -> claims.get("typ", String.class));
        return "access".equals(type);
    }

    public boolean isRefreshToken(String token) {
        String type = extractClaim(token, claims -> claims.get("typ", String.class));
        return "refresh".equals(type);
    }

    private boolean validateStandardClaims(String token) {

        String tokenIssuer = extractClaim(token, Claims::getIssuer);
        java.util.Set<String> tokenAudience = extractClaim(token, Claims::getAudience);

        return issuer.equals(tokenIssuer) && audience.equals(tokenAudience);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public long getExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }
}
