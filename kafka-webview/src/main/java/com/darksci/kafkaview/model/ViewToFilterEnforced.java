package com.darksci.kafkaview.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ViewToFilterEnforced {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "filter_id", nullable = false)
    private Long filterId;

    @Column(name = "view_id", nullable = false)
    private Long viewId;

    @Column(nullable = false)
    private Long sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getFilterId() {
        return filterId;
    }

    public void setFilterId(final Long filterId) {
        this.filterId = filterId;
    }

    public Long getViewId() {
        return viewId;
    }

    public void setViewId(final Long viewId) {
        this.viewId = viewId;
    }

    public Long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final Long sortOrder) {
        this.sortOrder = sortOrder;
    }
}
