package com.changelens.app.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/** Pure validation logic for the project form (unit-testable, no Android deps). */
public final class ProjectValidator {

    public static final int MAX_TITLE = 80;
    public static final int MAX_DESCRIPTION = 500;

    public static final class Result {
        public final boolean valid;
        @Nullable
        public final String titleError;
        @Nullable
        public final String descriptionError;
        @Nullable
        public final String dateError;

        Result(boolean valid, @Nullable String titleError,
               @Nullable String descriptionError, @Nullable String dateError) {
            this.valid = valid;
            this.titleError = titleError;
            this.descriptionError = descriptionError;
            this.dateError = dateError;
        }
    }

    /** Error keys resolved to localized strings at the call site. */
    public static final String ERR_TITLE_REQUIRED = "title_required";
    public static final String ERR_TITLE_LONG = "title_long";
    public static final String ERR_DESC_LONG = "description_long";
    public static final String ERR_DATE_ORDER = "date_order";

    private ProjectValidator() {
    }

    @NonNull
    public static Result validate(@NonNull String title,
                                  @Nullable String description,
                                  long startDate,
                                  @Nullable Long targetDate) {
        String titleError = null;
        String descriptionError = null;
        String dateError = null;

        String trimmedTitle = title == null ? "" : title.trim();
        if (trimmedTitle.isEmpty()) {
            titleError = ERR_TITLE_REQUIRED;
        } else if (trimmedTitle.length() > MAX_TITLE) {
            titleError = ERR_TITLE_LONG;
        }
        if (description != null && description.length() > MAX_DESCRIPTION) {
            descriptionError = ERR_DESC_LONG;
        }
        if (targetDate != null && targetDate < startDate) {
            dateError = ERR_DATE_ORDER;
        }
        boolean valid = titleError == null && descriptionError == null && dateError == null;
        return new Result(valid, titleError, descriptionError, dateError);
    }

    @NonNull
    public static String pickCategory(@NonNull List<String> categories, @Nullable String current) {
        if (current == null || current.isEmpty()) {
            return categories.isEmpty() ? "" : categories.get(0);
        }
        return current;
    }

    public static int indexOfCategory(@NonNull List<String> categories, @Nullable String current) {
        if (current == null) {
            return 0;
        }
        int index = categories.indexOf(current);
        return Math.max(0, index);
    }
}
