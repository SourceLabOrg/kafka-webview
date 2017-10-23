package com.darksci.kafka.webview.ui.repository;

import com.darksci.kafka.webview.ui.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For interacting w/ the User database table.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Find all users, ordered by email, where isActive = parameter
     * @param status Is Active status flag.
     * @return Collection of users.
     */
    Iterable<User> findAllByIsActiveOrderByEmailAsc(final boolean status);

    /**
     * Find user by email address.
     */
    User findByEmail(String email);
}
