package com.darksci.kafkaview.manager.ui;

import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick n' Dirty manager for managing breadcrumbs in the UI.
 */
public class BreadCrumbManager {
    /**
     * Contains our bread crumbs.
     */
    private List<Crumb> crumbs = new ArrayList<>();

    /**
     * Constructor.
     * @param model view Model we can add ourself onto.
     */
    public BreadCrumbManager(final Model model) {
        model.addAttribute("BreadCrumbs", this);

        // Add home by default
        addCrumb("Home", "/");
    }

    /**
     * Add new crumb to the UI linked to given URL.
     * @param name Name to display for the bread crumb.
     * @param url Url to link to
     * @return self
     */
    public BreadCrumbManager addCrumb(final String name, final String url) {
        crumbs.add(new Crumb(name, url));
        return this;
    }

    /**
     * Add new crumb to the UI without a link.
     * @param name Name to display for the bread crumb.
     * @return self
     */
    public BreadCrumbManager addCrumb(final String name) {
        crumbs.add(new Crumb(name, null));
        return this;
    }

    /**
     * Get all defined crumbs.
     */
    public List<Crumb> getCrumbs() {
        return crumbs;
    }

    /**
     * Internal class to keep track of the crumbs.
     */
    private static class Crumb {
        private final String name;
        private final String url;

        private Crumb(final String name, final String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
