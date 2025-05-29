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
    // Login və authentication üçün - USER və ADMIN
    Optional<User> findByEmail(String email);

    // Email mövcudluğunu yoxlamaq üçün - PUBLIC
    boolean existsByEmail(String email);

    // Google istifadəçilərini tapmaq üçün - USER və ADMIN
    List<User> findByIsGoogleUserTrue();

    // Rola görə istifadəçiləri tapmaq - ADMIN
    List<User> findByRole(UserRole role);

    // Telefon nömrəsi ilə axtarış - ADMIN
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Admin üçün istifadəçi axtarışı (ADMIN)
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByNameOrEmailContaining(@Param("name") String name, @Param("email") String email);

    // Son qeydiyyat olanlar - ADMIN
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegistered();

    // Aktiv sifarişi olan istifadəçilər - ADMIN
    @Query("SELECT DISTINCT u FROM User u JOIN u.orders o WHERE o.status != 'CANCELLED'")
    List<User> findUsersWithActiveOrders();

}
