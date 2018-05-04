package com.ecit.common.utils.salt;

import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.PRNG;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class DefaultHashStrategy
        implements ShopHashStrategy {
    private final PRNG random;
    private List<String> nonces;
    private final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public DefaultHashStrategy(Vertx vertx) {
        this.random = new PRNG(vertx);
    }

    public String generateSalt() {
        byte[] salt = new byte[32];
        this.random.nextBytes(salt);
        return bytesToHex(salt);
    }

    public String computeHash(String password, String salt, int version) {
        try {
            String concat = (salt == null ? "" : salt) + password;
            if (version >= 0) {
                if (this.nonces == null) {
                    throw new VertxException("nonces are not available");
                }
                if (version < this.nonces.size()) {
                    concat = concat + (String) this.nonces.get(version);
                }
            }
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bHash = md.digest(concat.getBytes(StandardCharsets.UTF_8));
            return version >= 0 ? bytesToHex(bHash) + '$' + version : bytesToHex(bHash);
        } catch (NoSuchAlgorithmException var7) {
            throw new VertxException(var7);
        }
    }

    public String getHashedStoredPwd(JsonArray row) {
        return row.getString(2);
    }

    public String getSalt(JsonArray row) {
        return row.getString(3);
    }

    public Long getUserId(JsonArray row) {
        return Long.parseLong(row.getString(0));
    }

    public String getLoginName(JsonArray row) {
        return row.getString(1);
    }

    public void setNonces(List<String> nonces) {
        this.nonces = Collections.unmodifiableList(nonces);
    }

    private String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int x = 0xFF & bytes[i];
            chars[(i * 2)] = this.HEX_CHARS[(x >>> 4)];
            chars[(1 + i * 2)] = this.HEX_CHARS[(0xF & x)];
        }
        return new String(chars);
    }
}
