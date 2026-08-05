package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;

public record AccountSignedSum(Long accountId, BigDecimal signedSum) {
}
