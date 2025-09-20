package com.example.adminpanel.infrastructure.form;

import com.example.adminpanel.application.form.FormValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * A mock implementation of {@link FormValidationService} used for
 * demonstrations. This class simulates server-side validation by
 * performing simple checks on known form identifiers. In a real
 * application, you might call a backend service or database to
 * validate the form data.
 */
@Slf4j
@Service
public class InMemoryFormValidationService implements FormValidationService {
    @Override
    public Map<String, String> validate(String formId, Map<String, Object> fields) {
        Map<String, String> errors = new HashMap<>();
        Map<String, Object> safeFields = fields != null ? fields : Map.of();
        int fieldCount = safeFields.size();
        log.debug("Validating form '{}' with {} field(s)", formId, fieldCount);

        // Example validation: for the user form, disallow specific email
        if ("user-form-v1".equals(formId)) {
            Object email = safeFields.get("email");
            if (email != null) {
                String emailStr = email.toString().trim().toLowerCase();
                // Simulate server-side uniqueness check
                if ("admin@example.com".equals(emailStr) || "test@invalid.com".equals(emailStr)) {
                    errors.put("email", "email.taken");
                    log.info("Form '{}' rejected reserved email '{}'", formId, emailStr);
                }
            }
        }

        if (errors.isEmpty()) {
            log.debug("Form '{}' passed validation", formId);
        } else {
            log.warn("Form '{}' failed validation with {} issue(s)", formId, errors.size());
        }
        return errors;
    }
}
