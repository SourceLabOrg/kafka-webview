package org.sourcelab.kafka.webview.ui.manager.ui.recentasset;

import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class RecentAssetManager {
    private final ClusterRepository clusterRepository;
    private final ViewRepository viewRepository;

    @Autowired
    public RecentAssetManager(final ClusterRepository clusterRepository, final ViewRepository viewRepository) {
        this.clusterRepository = Objects.requireNonNull(clusterRepository);
        this.viewRepository = Objects.requireNonNull(viewRepository);
    }

    public List<RecentAsset> getRecentAssets(final RecentAssetType type, final List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        switch (type) {
            case CLUSTER:
                return retrieveClusters(ids);
            case VIEW:
                return retrieveViews(ids);
            default:
                return Collections.emptyList();
        }
    }

    private List<RecentAsset> retrieveViews(final List<Long> ids) {
        final List<RecentAsset> recentAssets = new ArrayList<>();

        for (final long id : ids) {
            viewRepository.findById(id).ifPresent((view) -> {
                recentAssets.add(new RecentAsset(
                    view.getName(),
                    view.getId(),
                    "/view/read/" + view.getId()
                ));
            });
        }
        return Collections.unmodifiableList(recentAssets);
    }

    private List<RecentAsset> retrieveClusters(final List<Long> ids) {
        final List<RecentAsset> recentAssets = new ArrayList<>();

        for (final long id : ids) {
            clusterRepository.findById(id).ifPresent((cluster) -> {
                recentAssets.add(new RecentAsset(
                    cluster.getName(),
                    cluster.getId(),
                    "/cluster/read/" + cluster.getId()
                ));
            });
        }
        return Collections.unmodifiableList(recentAssets);
    }


}
