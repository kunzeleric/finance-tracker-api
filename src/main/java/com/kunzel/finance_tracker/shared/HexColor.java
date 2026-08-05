package com.kunzel.finance_tracker.shared;

import java.util.regex.Pattern;

import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

public final class HexColor {
  public static final String DEFAULT = "#FFFFFF";
  public static final String PATTERN = "^#[0-9A-Fa-f]{6}$";

  private static final Pattern COMPILED_PATTERN = Pattern.compile(PATTERN);

  private HexColor() {
  }

  public static String normalize(String color) {
    if (color == null || color.isBlank()) {
      return DEFAULT;
    }

    String trimmed = color.trim();

    if (!COMPILED_PATTERN.matcher(trimmed).matches()) {
      throw new ValidationException("Cor inválida: '" + color + "'. Formato esperado: #RRGGBB");
    }

    return trimmed.toUpperCase();
  }
}
