package com.dsc.wai.manager.email;

import com.darksci.pardot.api.Configuration;

/**
 * Configuration for PardotMailSender.
 */
public class PardotMailSenderConfig {
    private final Configuration pardotConfig;
    private final long campaignId;

    public PardotMailSenderConfig(final Configuration pardotConfig, final long campaignId) {
        this.pardotConfig = pardotConfig;
        this.campaignId = campaignId;
    }

    public Configuration getPardotConfig() {
        return pardotConfig;
    }

    public long getCampaignId() {
        return campaignId;
    }
}
