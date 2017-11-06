package com.darksci.kafka.webview.ui.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Represents a Many-to-Many join table between View.id and Filter.id.
 */
@Entity
public class ViewToFilterEnforced {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "filter_id", nullable = false)
    private Long filterId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private View view;

    @Column(nullable = false)
    private Long sortOrder;

    @Column(nullable = false)
    private String optionParameters = "";

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

    public View getView() {
        return view;
    }

    public void setView(final View view) {
        this.view = view;
    }

    public Long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final Long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getOptionParameters() {
        return optionParameters;
    }

    public void setOptionParameters(final String optionParameters) {
        this.optionParameters = optionParameters;
    }

    @Override
    public String toString() {
        return "ViewToFilterEnforced{"
            + "id=" + id
            + ", filterId=" + filterId
            + ", sortOrder=" + sortOrder
            + ", optionParameters='" + optionParameters + '\''
            + '}';
    }
}
