package com.example.lunchapp.repository;

import com.example.lunchapp.model.entity.Order;
import com.example.lunchapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user u LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.foodItem fi WHERE o.user = :user ORDER BY o.orderDate DESC")
    List<Order> findByUserOrderByOrderDateDescFetchingAll(@Param("user") User user);

    List<Order> findByOrderDate(LocalDateTime orderDate);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user u LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.foodItem fi WHERE o.orderDate >= :startDate AND o.orderDate < :endDate ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetweenFetchingAll(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startOfDay AND o.orderDate < :endOfDay")
    List<Order> findByOrderDateOn(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.orderDate >= :startOfDay AND o.orderDate < :endOfDay")
    List<Order> findByUserAndOrderDateOn(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    boolean existsByUser_IdAndOrderDateBetween(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    Optional<Order> findFirstByUser_IdAndOrderDateBetweenOrderByOrderDateDesc(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}