package com.example.adminpanel.infrastructure.form;

import com.example.adminpanel.application.form.FormValidationService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * A mock implementation of {@link FormValidationService} used for
 * demonstrations. This class simulates server‑side validation by
 * performing simple checks on known form identifiers. In a real
 * application, you might call a backend service or database to
 * validate the form data.
 */
@Service
public class InMemoryFormValidationService implements FormValidationService {

    @Override
    public Map<String, String> validate(String formId, Map<String, Object> fields) {
        Map<String, String> errors = new HashMap<>();
        // Log submitted values to the server console.  This helps developers
        // verify that the dynamic form is collecting values from all field
        // types, including custom uploads, map selectors, groups, etc.
        // In a production system you might inject a logger instead of using
        // System.out.println().
        System.out.println("Form submission (" + formId + "): " + fields);
        // Example validation: for the user form, disallow specific email
        if ("user-form-v1".equals(formId)) {
            Object email = fields.get("email");
            if (email != null) {
                String emailStr = email.toString().trim().toLowerCase();
                // Simulate server‑side uniqueness check
                if ("admin@example.com".equals(emailStr) || "test@invalid.com".equals(emailStr)) {
                    errors.put("email", "email.taken");
                }
            }
        }
        return errors;
    }
}