package com.youtopin.vaadin.samples.ui.formengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data model backing the repeatable duplication sample.
 */
public class RepeatableDuplicationFormData {

    private final PageLayout layout = new PageLayout();

    public PageLayout getLayout() {
        return layout;
    }

    public static class PageLayout {
        private final List<ContentBlock> blocks = new ArrayList<>();

        public PageLayout() {
            ContentBlock hero = new ContentBlock();
            hero.setTitle("Hero banner");
            hero.setSummary("Large introductory block with highlighted call to action.");
            blocks.add(hero);

            ContentBlock highlights = new ContentBlock();
            highlights.setTitle("Highlights");
            highlights.setSummary("Three key messages with supporting icons.");
            blocks.add(highlights);
        }

        public List<ContentBlock> getBlocks() {
            return blocks;
        }

        public void setBlocks(List<ContentBlock> entries) {
            blocks.clear();
            if (entries == null) {
                return;
            }
            entries.stream()
                    .filter(Objects::nonNull)
                    .map(ContentBlock::copyOf)
                    .forEach(blocks::add);
        }
    }

    public static class ContentBlock {
        private String title = "";
        private String summary = "";

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title == null ? "" : title;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary == null ? "" : summary;
        }

        private static ContentBlock copyOf(ContentBlock source) {
            ContentBlock copy = new ContentBlock();
            if (source == null) {
                return copy;
            }
            copy.setTitle(source.getTitle());
            copy.setSummary(source.getSummary());
            return copy;
        }
    }
}
