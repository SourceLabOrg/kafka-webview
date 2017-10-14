package com.darksci.kafkaview.manager.ui;

import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

public class BreadCrumbManager {
    private List<Crumb> crumbs = new ArrayList<>();

    public BreadCrumbManager(final Model model) {
        model.addAttribute("BreadCrumbs", this);

        // Add home by default
        addCrumb("Home", "/");
    }

    public BreadCrumbManager addCrumb(final String name, final String url) {
        crumbs.add(new Crumb(name, url));
        return this;
    }

    public List<Crumb> getCrumbs() {
        return crumbs;
    }

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
