package com.mfood.bot.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Goal {
    LOSE_WEIGHT(-500),
    MAINTAIN(0),
    GAIN_WEIGHT(500);

    private final int calorieAdjustment;
}
