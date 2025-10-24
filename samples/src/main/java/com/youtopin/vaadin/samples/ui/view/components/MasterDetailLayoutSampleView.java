package com.youtopin.vaadin.samples.ui.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.youtopin.vaadin.samples.ui.layout.AppPageLayout;
import com.youtopin.vaadin.samples.ui.layout.MainLayout;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Samples that demonstrate building master-detail experiences using SplitLayout
 * and tabbed navigation patterns.
 */
@Route(value = "components/master-detail-layout", layout = MainLayout.class)
public class MasterDetailLayoutSampleView extends AppPageLayout implements LocaleChangeObserver {

    private final H1 pageTitle;

    private final H2 splitSectionTitle;
    private final Span splitSectionDescription;
    private final SplitLayout splitLayout;
    private final VerticalLayout splitMasterContainer;
    private final H3 splitMasterTitle;
    private final Span splitMasterSubtitle;
    private final ListBox<String> splitMasterList;
    private final VerticalLayout splitDetailContainer;
    private final H3 splitDetailTitle;
    private final Paragraph splitDetailBody;
    private final Span splitDetailMeta;
    private final Span splitDetailHint;

    private final List<String> splitItemKeys = Arrays.asList("launch", "support", "retention");
    private final Map<String, String> splitLabelToKey = new LinkedHashMap<>();
    private String splitSelectedKey = splitItemKeys.get(0);

    private final H2 tabsSectionTitle;
    private final Span tabsSectionDescription;
    private final Tabs tabs;
    private final Map<Tab, String> tabToKey = new LinkedHashMap<>();
    private final VerticalLayout tabsDetailContainer;
    private final H3 tabsDetailTitle;
    private final Paragraph tabsDetailBody;
    private final Span tabsDetailHint;
    private String tabsSelectedKey;

    public MasterDetailLayoutSampleView() {
        pageTitle = createPageTitle(getTranslation("components.masterDetail.title"));

        splitSectionTitle = new H2();
        splitSectionDescription = new Span();
        splitSectionDescription.addClassName("page-subtitle");

        splitMasterTitle = new H3();
        splitMasterSubtitle = new Span();
        splitMasterSubtitle.addClassName("page-subtitle");
        splitMasterList = new ListBox<>();
        splitMasterList.setWidthFull();
        splitMasterList.addValueChangeListener(event -> {
            String label = event.getValue();
            if (label != null) {
                splitSelectedKey = splitLabelToKey.getOrDefault(label, splitItemKeys.get(0));
                updateSplitDetailContent();
            }
        });

        splitMasterContainer = new VerticalLayout(splitMasterTitle, splitMasterSubtitle, splitMasterList);
        splitMasterContainer.setPadding(false);
        splitMasterContainer.setSpacing(false);
        splitMasterContainer.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        splitMasterContainer.addClassName("stack-md");
        splitMasterContainer.setHeightFull();
        splitMasterList.setHeightFull();

        splitDetailTitle = new H3();
        splitDetailBody = new Paragraph();
        splitDetailMeta = new Span();
        splitDetailMeta.addClassName("page-subtitle");
        splitDetailHint = new Span();
        splitDetailHint.addClassName("page-subtitle");

        splitDetailContainer = new VerticalLayout(splitDetailTitle, splitDetailBody, splitDetailMeta, splitDetailHint);
        splitDetailContainer.setPadding(false);
        splitDetailContainer.setSpacing(false);
        splitDetailContainer.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        splitDetailContainer.addClassName("stack-md");
        splitDetailContainer.setHeightFull();

        splitLayout = new SplitLayout(splitMasterContainer, splitDetailContainer);
        splitLayout.setWidthFull();
        splitLayout.setHeight("360px");
        splitLayout.setSplitterPosition(38);
        splitLayout.addClassName("app-master-detail-split");

        VerticalLayout splitCard = createCard(splitSectionTitle, splitSectionDescription, splitLayout);
        splitCard.addClassName("stack-lg");
        add(splitCard);

        tabsSectionTitle = new H2();
        tabsSectionDescription = new Span();
        tabsSectionDescription.addClassName("page-subtitle");

        tabs = new Tabs();
        tabs.setWidthFull();
        Tab roadmapTab = new Tab();
        Tab activityTab = new Tab();
        Tab filesTab = new Tab();
        tabToKey.put(roadmapTab, "roadmap");
        tabToKey.put(activityTab, "activity");
        tabToKey.put(filesTab, "files");
        tabs.add(roadmapTab, activityTab, filesTab);
        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            tabsSelectedKey = tabToKey.getOrDefault(selectedTab, "roadmap");
            updateTabsDetailContent();
        });

        tabsDetailTitle = new H3();
        tabsDetailBody = new Paragraph();
        tabsDetailHint = new Span();
        tabsDetailHint.addClassName("page-subtitle");

        tabsDetailContainer = new VerticalLayout(tabsDetailTitle, tabsDetailBody, tabsDetailHint);
        tabsDetailContainer.setPadding(false);
        tabsDetailContainer.setSpacing(false);
        tabsDetailContainer.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        tabsDetailContainer.addClassName("stack-md");

        VerticalLayout tabsCard = createCard(tabsSectionTitle, tabsSectionDescription, tabs, tabsDetailContainer);
        tabsCard.addClassName("stack-lg");
        add(tabsCard);

        tabsSelectedKey = "roadmap";
        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        pageTitle.setText(getTranslation("components.masterDetail.title"));

        splitSectionTitle.setText(getTranslation("components.masterDetail.split.title"));
        splitSectionDescription.setText(getTranslation("components.masterDetail.split.description"));
        splitMasterTitle.setText(getTranslation("components.masterDetail.split.master.title"));
        splitMasterSubtitle.setText(getTranslation("components.masterDetail.split.master.subtitle"));
        splitDetailHint.setText(getTranslation("components.masterDetail.split.detail.hint"));

        splitLabelToKey.clear();
        List<String> labels = new java.util.ArrayList<>(splitItemKeys.size());
        for (String key : splitItemKeys) {
            String label = getTranslation("components.masterDetail.split.items." + key + ".label");
            labels.add(label);
            splitLabelToKey.put(label, key);
        }
        splitMasterList.setItems(labels);

        if (splitSelectedKey == null) {
            splitSelectedKey = splitItemKeys.get(0);
        }
        String selectedLabel = getTranslation("components.masterDetail.split.items." + splitSelectedKey + ".label");
        if (labels.contains(selectedLabel)) {
            splitMasterList.setValue(selectedLabel);
        } else if (!labels.isEmpty()) {
            splitSelectedKey = splitItemKeys.get(0);
            splitMasterList.setValue(labels.get(0));
        }
        updateSplitDetailContent();

        tabsSectionTitle.setText(getTranslation("components.masterDetail.tabs.title"));
        tabsSectionDescription.setText(getTranslation("components.masterDetail.tabs.description"));
        tabToKey.forEach((tab, key) -> tab.setLabel(getTranslation("components.masterDetail.tabs.tab." + key)));
        tabsDetailHint.setText(getTranslation("components.masterDetail.tabs.hint"));

        if (tabsSelectedKey == null) {
            Tab selectedTab = tabs.getSelectedTab();
            tabsSelectedKey = tabToKey.getOrDefault(selectedTab, "roadmap");
        }
        updateTabsDetailContent();

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().setTitle(getTranslation("components.masterDetail.title"));
        }
    }

    private void updateSplitDetailContent() {
        if (splitSelectedKey == null) {
            splitDetailTitle.setText("");
            splitDetailBody.setText("");
            splitDetailMeta.setText("");
            return;
        }

        splitDetailTitle.setText(getTranslation("components.masterDetail.split.items." + splitSelectedKey + ".detail.title"));
        splitDetailBody.setText(getTranslation("components.masterDetail.split.items." + splitSelectedKey + ".detail.body"));
        splitDetailMeta.setText(getTranslation("components.masterDetail.split.items." + splitSelectedKey + ".detail.meta"));
    }

    private void updateTabsDetailContent() {
        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) {
            return;
        }

        tabsSelectedKey = tabToKey.getOrDefault(selectedTab, tabsSelectedKey != null ? tabsSelectedKey : "roadmap");
        tabsDetailTitle.setText(getTranslation("components.masterDetail.tabs.content." + tabsSelectedKey + ".title"));
        tabsDetailBody.setText(getTranslation("components.masterDetail.tabs.content." + tabsSelectedKey + ".body"));
    }
}
