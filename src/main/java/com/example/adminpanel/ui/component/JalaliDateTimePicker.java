package com.example.adminpanel.ui.component;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Tag("jalali-date-time-picker")
@JsModule("./components/jalali-date-time-picker.ts")
@NpmPackage(value = "jalaali-js", version = "1.2.8")
public class JalaliDateTimePicker extends AbstractField<JalaliDateTimePicker, LocalDateTime>
        implements HasSize, HasValidation, HasHelper, HasLabel, LocaleChangeObserver,
        Focusable<JalaliDateTimePicker> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final List<String> PERSIAN_MONTHS = List.of(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند");
    private static final List<String> ENGLISH_MONTHS = List.of(
            "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
            "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand");
    private static final List<String> PERSIAN_WEEKDAYS = List.of(
            "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه");
    private static final List<String> ENGLISH_WEEKDAYS = List.of(
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

    public enum PickerVariant {
        DATE_TIME,
        DATE
    }

    private LocalDateTime min;
    private LocalDateTime max;
    private boolean invalid;
    private String errorMessage = "";
    private PickerVariant pickerVariant = PickerVariant.DATE_TIME;
    private String customOpenButtonLabel;

    public JalaliDateTimePicker() {
        this(null);
    }

    public JalaliDateTimePicker(LocalDateTime initialValue) {
        super(null);
        getElement().setProperty("value", "");
        getElement().setProperty("min", "");
        getElement().setProperty("max", "");
        getElement().setProperty("minuteStep", 1);
        getElement().setProperty("mode", "date-time");

        getElement().addPropertyChangeListener("value", "value-changed", event -> {
            if (isReadOnly()) {
                setPresentationValue(getValue());
                return;
            }
            LocalDateTime parsed = parseClientValue(event.getValue());
            if (parsed == null && event.getValue() != null && !Objects.toString(event.getValue(), "").isBlank()) {
                setPresentationValue(getValue());
                return;
            }
            setModelValue(clampToRange(parsed), event.isUserOriginated());
        });

        if (initialValue != null) {
            setValue(initialValue);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        applyTranslations();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        applyTranslations();
    }

    @Override
    protected void setPresentationValue(LocalDateTime newPresentationValue) {
        if (newPresentationValue == null) {
            getElement().setProperty("value", "");
        } else {
            getElement().setProperty("value", formatValue(newPresentationValue));
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        getElement().setProperty("readOnly", readOnly);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        getElement().setProperty("required", requiredIndicatorVisible);
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
        getElement().setProperty("invalid", invalid);
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? "" : errorMessage;
        getElement().setProperty("errorMessage", this.errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        super.onEnabledStateChanged(enabled);
        getElement().setProperty("disabled", !enabled);
    }

    public Optional<LocalDateTime> getMin() {
        return Optional.ofNullable(min);
    }

    public void setMin(LocalDateTime min) {
        this.min = min;
        ensureRangeIsSane();
        getElement().setProperty("min", min == null ? "" : formatValue(min));
    }

    public Optional<LocalDateTime> getMax() {
        return Optional.ofNullable(max);
    }

    public void setMax(LocalDateTime max) {
        this.max = max;
        ensureRangeIsSane();
        getElement().setProperty("max", max == null ? "" : formatValue(max));
    }

    public void setMinuteStep(int minuteStep) {
        if (minuteStep <= 0 || minuteStep > 60) {
            throw new IllegalArgumentException("Minute step must be between 1 and 60");
        }
        getElement().setProperty("minuteStep", minuteStep);
    }

    public int getMinuteStep() {
        return getElement().getProperty("minuteStep", 1);
    }

    public PickerVariant getPickerVariant() {
        return pickerVariant;
    }

    public void setPickerVariant(PickerVariant pickerVariant) {
        PickerVariant resolved = pickerVariant == null ? PickerVariant.DATE_TIME : pickerVariant;
        this.pickerVariant = resolved;
        getElement().setProperty("mode", resolved == PickerVariant.DATE ? "date" : "date-time");
        applyTranslations();
    }

    public void setOpenButtonLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            customOpenButtonLabel = null;
            applyTranslations();
        } else {
            customOpenButtonLabel = label;
            getElement().setProperty("openButtonLabel", label);
        }
    }

    public void openPicker() {
        getElement().callJsFunction("openPicker");
    }

    public void closePicker() {
        getElement().callJsFunction("closePicker");
    }

    private void applyTranslations() {
        Locale locale = getLocale();
        String language = locale != null ? locale.getLanguage() : "";
        boolean persian = "fa".equalsIgnoreCase(language) || "fa_IR".equalsIgnoreCase(language);

        getElement().setPropertyList("monthNames", persian ? PERSIAN_MONTHS : ENGLISH_MONTHS);
        getElement().setPropertyList("weekdayNames", persian ? PERSIAN_WEEKDAYS : ENGLISH_WEEKDAYS);
        getElement().setProperty("calendarHeading",
                getTranslation("component.jalaliDateTime.calendarHeading"));
        getElement().setProperty("timeHeading",
                getTranslation("component.jalaliDateTime.timeHeading"));
        getElement().setProperty("todayLabel",
                getTranslation("component.jalaliDateTime.today"));
        getElement().setProperty("nowLabel",
                getTranslation("component.jalaliDateTime.now"));
        getElement().setProperty("clearLabel",
                getTranslation("component.jalaliDateTime.clear"));
        getElement().setProperty("hourLabel",
                getTranslation("component.jalaliDateTime.hour"));
        getElement().setProperty("minuteLabel",
                getTranslation("component.jalaliDateTime.minute"));
        getElement().setProperty("previousMonthLabel",
                getTranslation("component.jalaliDateTime.previousMonth"));
        getElement().setProperty("nextMonthLabel",
                getTranslation("component.jalaliDateTime.nextMonth"));
        getElement().setProperty("previousYearLabel",
                getTranslation("component.jalaliDateTime.previousYear"));
        getElement().setProperty("nextYearLabel",
                getTranslation("component.jalaliDateTime.nextYear"));
        getElement().setProperty("yearLabel",
                getTranslation("component.jalaliDateTime.year"));
        getElement().setProperty("noValueLabel",
                getTranslation("component.jalaliDateTime.previewEmpty"));
        getElement().setProperty("ariaSelectedLabel",
                getTranslation("component.jalaliDateTime.ariaSelected"));
        getElement().setProperty("ariaTodayLabel",
                getTranslation("component.jalaliDateTime.ariaToday"));
        getElement().setProperty("firstDayOfWeek", 6);
        getElement().setProperty("usePersianDigits", persian);
        if (customOpenButtonLabel == null || customOpenButtonLabel.isBlank()) {
            getElement().setProperty("openButtonLabel",
                    getTranslation(pickerVariant == PickerVariant.DATE
                            ? "component.jalaliDateTime.openDate"
                            : "component.jalaliDateTime.openDateTime"));
        }
        getElement().setProperty("mode", pickerVariant == PickerVariant.DATE ? "date" : "date-time");
        if (persian) {
            getElement().setAttribute("dir", "rtl");
        } else {
            getElement().setAttribute("dir", "ltr");
        }
    }

    private void ensureRangeIsSane() {
        if (min != null && max != null && min.isAfter(max)) {
            throw new IllegalArgumentException("Minimum value must not be after maximum value");
        }
    }

    private LocalDateTime clampToRange(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        LocalDateTime result = value;
        if (min != null && result.isBefore(min)) {
            result = min;
        }
        if (max != null && result.isAfter(max)) {
            result = max;
        }
        return result;
    }

    private static LocalDateTime parseClientValue(Serializable rawValue) {
        if (rawValue == null) {
            return null;
        }
        String value = Objects.toString(rawValue, "").trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, ISO_FORMATTER);
        } catch (DateTimeParseException ex) {
            if (value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(value + ":00", ISO_FORMATTER);
            }
            return null;
        }
    }

    private static String formatValue(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.SECONDS).format(ISO_FORMATTER);
    }
}
