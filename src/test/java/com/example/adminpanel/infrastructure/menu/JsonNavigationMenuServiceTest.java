package com.example.adminpanel.infrastructure.menu;

import com.example.adminpanel.application.security.UserScopeService;
import com.example.adminpanel.domain.menu.MenuItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonNavigationMenuServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void orLogicAllowsAccessWhenAnyScopeMatches() {
        String json = """
                {
                  \"items\": [
                    {
                      \"group\": \"general\",
                      \"labelKey\": \"menu.or\",
                      \"requiredScopes\": [\"scope:a\", \"scope:b\"],
                      \"requiredScopesLogic\": \"OR\"
                    }
                  ]
                }
                """;

        JsonNavigationMenuService service = createService(json, Set.of("scope:b"));

        List<MenuItem> items = service.getMenuItemsForCurrentUser();

        assertThat(items)
                .extracting(MenuItem::getLabelKey)
                .containsExactly("menu.or");
    }

    @Test
    void andLogicRequiresAllScopesToBePresent() {
        String json = """
                {
                  \"items\": [
                    {
                      \"group\": \"general\",
                      \"labelKey\": \"menu.and\",
                      \"requiredScopes\": [\"scope:a\", \"scope:b\"],
                      \"requiredScopesLogic\": \"AND\"
                    }
                  ]
                }
                """;

        JsonNavigationMenuService missingScopeService = createService(json, Set.of("scope:a"));
        JsonNavigationMenuService completeScopeService = createService(json, Set.of("scope:a", "scope:b"));

        assertThat(missingScopeService.getMenuItemsForCurrentUser()).isEmpty();
        assertThat(completeScopeService.getMenuItemsForCurrentUser())
                .extracting(MenuItem::getLabelKey)
                .containsExactly("menu.and");
    }

    @Test
    void unknownLogicValueResultsInIllegalState() {
        String json = """
                {
                  \"items\": [
                    {
                      \"group\": \"general\",
                      \"labelKey\": \"menu.invalid\",
                      \"requiredScopes\": [\"scope:a\"],
                      \"requiredScopesLogic\": \"XOR\"
                    }
                  ]
                }
                """;

        JsonNavigationMenuService service = createService(json, Set.of("scope:a"));

        assertThatThrownBy(service::getMenuItemsForCurrentUser)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requiredScopesLogic");
    }

    private JsonNavigationMenuService createService(String json, Set<String> scopes) {
        Resource resource = new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
        UserScopeService scopeService = () -> scopes;
        return new JsonNavigationMenuService(OBJECT_MAPPER, resource, scopeService);
    }
}
