package com.roommatematch.util;

import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    public String sanitize(String input) {
        if (input == null) {
            return "";
        }

        // Strip HTML tags
        String result = input.replaceAll("<[^>]*>", "");

        // Strip script content
        result = result.replaceAll("(?i)<script.*?>.*?</script>", "");

        // Trim whitespace
        result = result.trim();

        return result;
    }

    public String sanitizeOrNull(String input) {
        if (input == null) {
            return null;
        }
        return sanitize(input);
    }
}
