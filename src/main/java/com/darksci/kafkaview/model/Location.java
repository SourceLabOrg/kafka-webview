package com.darksci.kafkaview.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity(name = "location")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, name = "user_id")
    private long userId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "lat")
    private double lat;

    @Column(name = "lng")
    private double lng;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(final double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(final double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Location{" +
            "id=" + id +
            ", userId=" + userId +
            ", createdAt=" + createdAt +
            ", lat=" + lat +
            ", lng=" + lng +
            '}';
    }
}
