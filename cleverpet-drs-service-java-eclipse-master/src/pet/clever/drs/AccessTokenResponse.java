package pet.clever.drs;

/**
 * lwa
 */
public class AccessTokenResponse {
    public String access_token;
    public String token_type; // e.g., "bearer"
    public int expires_in; // e.g., 3600
    public String refresh_token;
    public String scope; // e.g., "profile"
}
