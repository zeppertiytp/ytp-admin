package com.example.adminpanel.infrastructure.menu;

import com.example.adminpanel.application.menu.NavigationMenuService;
import com.example.adminpanel.application.security.UserScopeService;
import com.example.adminpanel.domain.menu.MenuItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.icon.VaadinIcon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link NavigationMenuService} implementation that sources the navigation structure from
 * a JSON configuration file located on the classpath.  The file is parsed once at startup and
 * cached as a set of menu item definitions.  On each request the menu is filtered according to
 * the scopes returned by {@link UserScopeService}, enabling future integration with OAuth/OIDC
 * providers without changing the UI layer.
 */
@Slf4j
@Service
public class JsonNavigationMenuService implements NavigationMenuService {

    private final List<MenuItemDefinition> menuDefinitions;
    private final UserScopeService userScopeService;

    public JsonNavigationMenuService(ObjectMapper objectMapper,
                                     @Value("classpath:menu/navigation-menu.json") Resource menuResource,
                                     UserScopeService userScopeService) {
        this.userScopeService = Objects.requireNonNull(userScopeService, "userScopeService must not be null");
        this.menuDefinitions = loadDefinitions(objectMapper, menuResource);
        log.info("Loaded {} navigation menu definition(s) from {}", menuDefinitions.size(), menuResource.getDescription());
    }

    @Override
    public List<MenuItem> getMenuItemsForCurrentUser() {
        Set<String> scopes = safeCopyScopes(userScopeService.getCurrentUserScopes());
        List<MenuItem> items = menuDefinitions.stream()
                .map(definition -> toMenuItem(definition, scopes))
                .flatMap(Optional::stream)
                .toList();
        log.debug("Resolved {} navigation menu item(s) for current user with scopes {}", items.size(), scopes);
        return items;
    }

    private static Set<String> safeCopyScopes(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return Set.of();
        }
        return scopes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(scope -> !scope.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private Optional<MenuItem> toMenuItem(MenuItemDefinition definition, Set<String> scopes) {
        if (!definition.isAccessibleFor(scopes)) {
            return Optional.empty();
        }

        List<MenuItem> children = definition.childrenOrEmpty().stream()
                .map(child -> toMenuItem(child, scopes))
                .flatMap(Optional::stream)
                .toList();

        if (!definition.childrenOrEmpty().isEmpty() && children.isEmpty()) {
            return Optional.empty();
        }

        VaadinIcon icon = definition.iconName()
                .map(this::resolveIcon)
                .orElse(null);

        MenuItem menuItem = children.isEmpty()
                ? new MenuItem(definition.group(), definition.labelKey(), icon, definition.navigationTarget().orElse(null))
                : new MenuItem(definition.group(), definition.labelKey(), icon, definition.navigationTarget().orElse(null), children);

        return Optional.of(menuItem);
    }

    private VaadinIcon resolveIcon(String iconName) {
        String normalized = iconName.trim().replace('-', '_').toUpperCase(Locale.ROOT);
        try {
            return VaadinIcon.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unknown Vaadin icon name '" + iconName + "' in navigation menu configuration", ex);
        }
    }

    private static List<MenuItemDefinition> loadDefinitions(ObjectMapper objectMapper, Resource resource) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(resource, "resource must not be null");
        try (InputStream inputStream = resource.getInputStream()) {
            MenuDefinition menuDefinition = objectMapper.readValue(inputStream, MenuDefinition.class);
            List<MenuItemDefinition> items = menuDefinition.itemsOrEmpty();
            return List.copyOf(items);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read navigation menu configuration from " + resource.getDescription(), ex);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MenuDefinition(List<MenuItemDefinition> items) {
        List<MenuItemDefinition> itemsOrEmpty() {
            return items == null ? List.of() : items;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MenuItemDefinition(
            String group,
            String labelKey,
            String icon,
            String navigationTarget,
            List<MenuItemDefinition> children,
            List<String> requiredScopes,
            String requiredScopesLogic
    ) {
        List<MenuItemDefinition> childrenOrEmpty() {
            return children == null ? List.of() : children;
        }

        List<String> requiredScopesOrEmpty() {
            return requiredScopes == null ? List.of() : requiredScopes;
        }

        Optional<String> iconName() {
            return Optional.ofNullable(icon)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty());
        }

        Optional<String> navigationTarget() {
            return Optional.ofNullable(navigationTarget);
        }

        boolean isAccessibleFor(Set<String> scopes) {
            List<String> required = normalizedRequiredScopes();
            if (required.isEmpty()) {
                return true;
            }
            return scopeLogic().isSatisfied(scopes, required);
        }

        private List<String> normalizedRequiredScopes() {
            return requiredScopesOrEmpty().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(scope -> !scope.isEmpty())
                    .toList();
        }

        private ScopeLogic scopeLogic() {
            if (requiredScopesLogic == null || requiredScopesLogic.isBlank()) {
                return ScopeLogic.AND;
            }
            String normalized = requiredScopesLogic.trim().toUpperCase(Locale.ROOT);
            try {
                return ScopeLogic.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("Unknown requiredScopesLogic '" + requiredScopesLogic +
                        "' configured for navigation item '" + labelKey + "'", ex);
            }
        }

        private enum ScopeLogic {
            AND {
                @Override
                boolean isSatisfied(Set<String> scopes, List<String> requiredScopes) {
                    return scopes.containsAll(requiredScopes);
                }
            },
            OR {
                @Override
                boolean isSatisfied(Set<String> scopes, List<String> requiredScopes) {
                    return requiredScopes.stream().anyMatch(scopes::contains);
                }
            };

            abstract boolean isSatisfied(Set<String> scopes, List<String> requiredScopes);
        }
    }
}
