package com.example.masker;

import com.example.masker.annotation.Mask;

/**
 * Record для параметров маскировки
 */
public record MaskingParams(
        int visibleStart,
        int visibleEnd,
        String maskChars,
        char maskChar,
        int startIndex,
        int length,
        int maskLength,
        int nameMaskLength
) {
    /**
     * Создает MaskingParams из аннотации @Mask
     */
    public static MaskingParams fromAnnotation(Mask annotation) {
        return new MaskingParams(
                annotation.visibleStart(),
                annotation.visibleEnd(),
                annotation.maskChars(),
                annotation.maskChar(),
                annotation.startIndex(),
                annotation.length(),
                annotation.maskLength(),
                annotation.nameMaskLength()
        );
    }
}


