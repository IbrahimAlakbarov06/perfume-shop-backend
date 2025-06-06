package org.perfume.domain.repo;

import org.perfume.domain.entity.User;
import org.perfume.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsGoogleUserTrue();

    List<User> findByRole(UserRole role);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("select u from User u where lower(u.name) LIKE lower(concat('%', :name, '%') ) or lower(u.email) like lower(concat('%', :email, '%') ) ")
    List<User> findByNameOrEmailContaining(@Param("name") String name, @Param("email") String email);

    @Query("select u from User u order by u.createdAt desc ")
    List<User> findRecentlyRegistered();

    @Query("select distinct u from User u join u.orders o where o.status != 'CANCELLED'")
    List<User> findUsersWithActiveOrders();
}
