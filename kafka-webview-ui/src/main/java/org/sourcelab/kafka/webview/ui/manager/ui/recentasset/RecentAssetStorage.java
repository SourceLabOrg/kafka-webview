package org.sourcelab.kafka.webview.ui.manager.ui.recentasset;

import com.google.common.base.Charsets;
import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Used to remember a user's most recent assets using cookies.
 */
@NotThreadSafe
public class RecentAssetStorage {
    private static final Logger logger = LoggerFactory.getLogger(RecentAssetStorage.class);

    private static final int MAX_PER_ASSET = 15;
    private static final Pattern FILTER_PATTERN = Pattern.compile("[^0-9, ]");

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    // Cache current cookie value between request and response.
    private final Map<RecentAssetType, List<Long>> currentCookieValues = new HashMap<>();

    /**
     * Constructor.
     * @param request Request instance.
     * @param response Response instance.
     */
    public RecentAssetStorage(final HttpServletRequest request, final HttpServletResponse response) {
        this.request = Objects.requireNonNull(request);
        this.response = Objects.requireNonNull(response);
    }

    /**
     * Get the most recent asset ids for the given type.
     * @param type Asset type.
     * @return List of Ids for that asset, ordered by most recently accessed.
     */
    public List<Long> getMostRecentAssetIds(final RecentAssetType type) {
        Objects.requireNonNull(type);

        // Get value if available.
        if (currentCookieValues.containsKey(type)) {
            return currentCookieValues.get(type);
        }

        // Get from cookie
        final String cookieName = getCookieName(type);
        final Cookie cookie = WebUtils.getCookie(request, cookieName);

        // If no cookie exists
        if (cookie == null) {
            // Default to new list
            currentCookieValues.put(type, new ArrayList<>());
            return currentCookieValues.get(type);
        }

        // Pull string value out.
        final String cookieValueStrEncoded = cookie.getValue();

        // Base64 decode
        String cookieValueStr = "";
        try {
            cookieValueStr = new String(
                Base64.getDecoder().decode(cookieValueStrEncoded),
                Charsets.UTF_8
            );
        } catch (final IllegalArgumentException exception) {
            logger.error("Invalid value in cookie {} : {}", type, exception.getMessage(), exception);
            cookieValueStr = "";
        }

        // Poor mans parsing
        final Matcher matcher = FILTER_PATTERN.matcher(cookieValueStr);
        final String cleanedStr = matcher.replaceAll("").trim();
        final List<Long> values = Arrays.stream(cleanedStr.split(","))
            .map(String::trim)
            .map((val) -> {
                try {
                    return Long.parseLong(val);
                } catch (NumberFormatException exception) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // Save into map, trimming max number of entries.
        currentCookieValues.put(type, new ArrayList<>(
            values.subList(0, Math.min(MAX_PER_ASSET, values.size()))
        ));
        return currentCookieValues.get(type);
    }

    /**
     * Add an asset to the most recently used assets.
     * @param type Type of asset.
     * @param assetId Id of the asset.
     * @return Updated list of most recently used assets.
     */
    public List<Long> addMostRecentAssetId(final RecentAssetType type, final long assetId) {
        Objects.requireNonNull(type);

        // Get the most recent list.
        final List<Long> current = getMostRecentAssetIds(type);

        // Remove this asset from the list
        current.remove(assetId);

        // Add this asset to the head
        current.add(0, assetId);

        // Trim the list to a max value.
        final List<Long> updated = current.subList(0, Math.min(MAX_PER_ASSET, current.size()));

        // Update cached value.
        currentCookieValues.put(type, updated);

        // Persist as a cookie.
        final Cookie cookie = createCookie(type, updated);
        response.addCookie(cookie);

        return updated;
    }

    private String getCookieName(final RecentAssetType type) {
        Objects.requireNonNull(type);
        return "recent_" + type.name();
    }

    private Cookie createCookie(final RecentAssetType type, final List<Long> ids) {
        final String cookieName = getCookieName(type);
        final String cookieValue = ids.toString();
        final String cookieValueEncoded = Base64.getEncoder().encodeToString(cookieValue.getBytes(Charsets.UTF_8));

        // create a cookie
        final Cookie cookie = new Cookie(cookieName, cookieValueEncoded);
        cookie.setMaxAge(180 * 24 * 60 * 60); // expires in 180 days
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }
}
