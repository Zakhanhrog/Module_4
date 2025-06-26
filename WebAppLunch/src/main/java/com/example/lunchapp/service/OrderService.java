package com.example.lunchapp.service;

import com.example.lunchapp.model.dto.OrderRequestDto;
import com.example.lunchapp.model.entity.Order;
import com.example.lunchapp.model.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order placeOrder(Long userId, OrderRequestDto orderRequestDto, boolean bypassTimeCheck);
    List<Order> getOrderHistory(Long userId);
    List<Order> getOrdersByDate(LocalDate date);
    List<Order> getAllOrders();
    void deleteOrderById(Long orderId); // Phương thức này có thể được dùng bởi admin
    boolean hasUserOrderedToday(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);
    Optional<Order> getTodaysOrderByUser(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);
    void cancelTodaysOrderAndRefund(Long userId); // Phương thức mới cho người dùng hủy đơn hôm nay
}