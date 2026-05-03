package org.marllon.caip.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
            throw new IllegalStateException(
                    "FATAL: A propriedade 'security.jwt.secret-base64' não foi encontrada. " +
                            "Verifique o seu arquivo .env ou as variáveis de ambiente do sistema. " +
                            "O servidor não pode iniciar sem uma chave criptográfica."
            );
        }
        byte[] keyBytes = tryDecodeSecret(secretBase64.trim());
        if (keyBytes.length < 32) { // Garantir 256 bits para HMAC-SHA256
            keyBytes = derive256Bits(secretBase64.trim());
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
        this.audience = audience;
    }

    private byte[] tryDecodeSecret(String raw) {
        try {
            return Decoders.BASE64.decode(raw);
        } catch (Exception e1) {
            try {
                String normalized = normalizeBase64(raw);
                return Decoders.BASE64.decode(normalized);
            } catch (Exception e2) {
                return raw.getBytes(StandardCharsets.UTF_8);
            }
        }
    }

    private String normalizeBase64(String s) {
        String n = s.replace('-', '+').replace('_', '/');
        int pad = n.length() % 4;
        if (pad == 2) n = n + "==";
        else if (pad == 3) n = n + "=";
        else if (pad == 1) {
            throw new IllegalArgumentException(
                    "FATAL: A chave JWT fornecida não é um Base64 válido. Comprimento incorreto após normalização."
            );
        }
        return n;
    }

    private byte[] derive256Bits(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "FATAL: O algoritmo SHA-256 não está disponível na JVM atual. Não é possível derivar a chave JWT.", e
            );
        }
    }

    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("typ", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpirationSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpirationSeconds)))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return username.equals(tokenUsername) && isAccessToken(token);
    }

    public boolean isRefreshTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return username.equals(tokenUsername) && isRefreshToken(token);
    }

    public boolean isAccessToken(String token) {
        String type = extractClaim(token, claims -> claims.get("typ", String.class));
        return "access".equals(type);
    }

    public boolean isRefreshToken(String token) {
        String type = extractClaim(token, claims -> claims.get("typ", String.class));
        return "refresh".equals(type);
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }
}