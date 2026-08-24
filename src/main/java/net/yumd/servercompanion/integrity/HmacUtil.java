package net.yumd.servercompanion.integrity;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.yumd.servercompanion.network.ModEntry;

/**
 * Lightweight integrity check for client reports: HMAC-SHA256 over a canonical string built
 * from the nonce, timestamp, and report contents.
 *
 * Threat model this covers: accidental corruption in transit, replay of a stale/old report, and
 * a stray/malformed packet that isn't actually from this mod. It does NOT protect against a
 * player who decompiles and patches the mod itself -- the secret ships inside the client jar and
 * is therefore extractable by a sufficiently motivated person. That's an inherent limit of any
 * client-self-report scheme, not something a signature can fix.
 */
public final class HmacUtil {
    private static final String ALGORITHM = "HmacSHA256";

    private HmacUtil() {
    }

    public static String canonicalPayload(UUID nonce, long timestamp, List<ModEntry> mods,
            List<String> localResourcePacks, String selfJarHash) {
        StringBuilder sb = new StringBuilder();
        sb.append(nonce).append('|').append(timestamp).append('|');
        for (ModEntry mod : mods) {
            sb.append(mod.id()).append(':').append(mod.version()).append(';');
        }
        sb.append('|');
        for (String pack : localResourcePacks) {
            sb.append(pack).append(';');
        }
        sb.append('|').append(selfJarHash);
        return sb.toString();
    }

    public static String sign(String secret, String canonicalPayload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] digest = mac.doFinal(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC signature", e);
        }
    }

    public static boolean verify(String secret, String canonicalPayload, String signatureHex) {
        String expected = sign(secret, canonicalPayload);
        return constantTimeEquals(expected, signatureHex);
    }

    // Avoids leaking timing information about how many leading hex chars matched.
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
