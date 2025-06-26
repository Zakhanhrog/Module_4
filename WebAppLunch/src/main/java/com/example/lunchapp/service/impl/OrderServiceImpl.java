package com.example.lunchapp.service.impl;

import com.example.lunchapp.model.dto.OrderRequestDto;
import com.example.lunchapp.model.dto.SelectedFoodItemDto;
import com.example.lunchapp.model.entity.FoodItem;
import com.example.lunchapp.model.entity.Order;
import com.example.lunchapp.model.entity.OrderItem;
import com.example.lunchapp.model.entity.User;
import com.example.lunchapp.repository.FoodItemRepository;
import com.example.lunchapp.repository.OrderRepository;
import com.example.lunchapp.repository.UserRepository;
import com.example.lunchapp.service.AppSettingService;
import com.example.lunchapp.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodItemRepository foodItemRepository;
    private final AppSettingService appSettingService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            FoodItemRepository foodItemRepository,
                            AppSettingService appSettingService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodItemRepository = foodItemRepository;
        this.appSettingService = appSettingService;
    }

    @Override
    @Transactional
    public Order placeOrder(Long userId, OrderRequestDto orderRequestDto, boolean bypassTimeCheck) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        if (!bypassTimeCheck) {
            LocalTime cutoffTime = appSettingService.getOrderCutoffTime();
            if (LocalDateTime.now().toLocalTime().isAfter(cutoffTime)) {
                logger.warn("Thao tác đặt món sau thời gian chốt đơn {} bởi người dùng {}", cutoffTime, userId);
                throw new RuntimeException("Đã hết giờ đặt món cho hôm nay. Vui lòng đặt trước " + cutoffTime.toString());
            }

            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
            if (orderRepository.existsByUser_IdAndOrderDateBetween(userId, startOfDay, endOfDay)) {
                logger.warn("Người dùng {} đã đặt hàng hôm nay.", userId);
                throw new RuntimeException("Bạn đã đặt hàng cho hôm nay rồi. Mỗi người chỉ được đặt 1 lần/ngày. Nếu muốn thay đổi, vui lòng hủy đơn hàng hiện tại và đặt lại.");
            }
        } else {
            logger.info("Admin is placing order for user {}, bypassing time check.", userId);
        }


        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        if (orderRequestDto.getNote() != null && !orderRequestDto.getNote().trim().isEmpty()) {
            order.setNote(orderRequestDto.getNote().trim());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        if (orderRequestDto.getSelectedItems() == null || orderRequestDto.getSelectedItems().isEmpty()) {
            throw new IllegalArgumentException("Không thể đặt đơn hàng trống. Vui lòng chọn món.");
        }

        for (SelectedFoodItemDto selectedItemDto : orderRequestDto.getSelectedItems()) {
            if (selectedItemDto.getQuantity() == null || selectedItemDto.getQuantity() <= 0) {
                continue;
            }

            FoodItem foodItem = foodItemRepository.findById(selectedItemDto.getFoodItemId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với ID: " + selectedItemDto.getFoodItemId()));

            if (!foodItem.isAvailableToday()) {
                throw new RuntimeException("Món ăn '" + foodItem.getName() + "' không có trong thực đơn hôm nay.");
            }
            if (foodItem.getDailyQuantity() == null || foodItem.getDailyQuantity() < selectedItemDto.getQuantity()) {
                throw new RuntimeException("Không đủ số lượng cho món '" + foodItem.getName() + "'. Hiện có: " + (foodItem.getDailyQuantity() != null ? foodItem.getDailyQuantity() : 0));
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setFoodItem(foodItem);
            orderItem.setQuantity(selectedItemDto.getQuantity());
            orderItem.setPrice(foodItem.getPrice());

            order.addOrderItem(orderItem);

            foodItem.setDailyQuantity(foodItem.getDailyQuantity() - selectedItemDto.getQuantity());
            foodItemRepository.save(foodItem);
        }
        totalAmount = order.getTotalAmount();

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tổng tiền đơn hàng phải lớn hơn 0. Vui lòng chọn món ăn hợp lệ.");
        }

        if (user.getBalance().compareTo(totalAmount) < 0) {
            logger.warn("Người dùng {} không đủ số dư. Cần: {}, Hiện có: {}", userId, totalAmount, user.getBalance());
            throw new RuntimeException("Không đủ số dư. Vui lòng nạp thêm tiền.");
        }

        user.setBalance(user.getBalance().subtract(totalAmount));
        userRepository.save(user);

        logger.info("Đặt món thành công cho người dùng {}. Tổng tiền: {}. Ghi chú: {}", userId, totalAmount, order.getNote());
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return orderRepository.findByUserOrderByOrderDateDescFetchingAll(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return orderRepository.findByOrderDateBetweenFetchingAll(startOfDay, endOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));


        User user = order.getUser();
        user.setBalance(user.getBalance().add(order.getTotalAmount()));
        userRepository.save(user);


        for (OrderItem item : order.getOrderItems()) {
            FoodItem foodItem = item.getFoodItem();
            if (foodItem != null) {
                foodItem.setDailyQuantity(foodItem.getDailyQuantity() + item.getQuantity());
                foodItemRepository.save(foodItem);
            }
        }
        orderRepository.delete(order);
        logger.info("Đơn hàng ID {} đã được xóa (có thể bởi admin hoặc hủy bởi user) và tiền/số lượng đã hoàn lại.", orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserOrderedToday(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return orderRepository.existsByUser_IdAndOrderDateBetween(userId, startOfDay, endOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getTodaysOrderByUser(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return orderRepository.findFirstByUser_IdAndOrderDateBetweenOrderByOrderDateDesc(userId, startOfDay, endOfDay);
    }

    @Override
    @Transactional
    public void cancelTodaysOrderAndRefund(Long userId) {
        if (LocalDateTime.now().toLocalTime().isAfter(appSettingService.getOrderCutoffTime())) {
            throw new RuntimeException("Đã quá thời gian cho phép hủy đơn hàng hôm nay.");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Optional<Order> todaysOrderOpt = orderRepository.findFirstByUser_IdAndOrderDateBetweenOrderByOrderDateDesc(userId, startOfDay, endOfDay);

        if (todaysOrderOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn hàng nào của bạn trong ngày hôm nay để hủy.");
        }

        Order orderToCancel = todaysOrderOpt.get();


        User user = orderToCancel.getUser();
        user.setBalance(user.getBalance().add(orderToCancel.getTotalAmount()));
        userRepository.save(user);
        logger.info("Hoàn tiền {} cho user {} khi hủy đơn hàng ID {}.", orderToCancel.getTotalAmount(), userId, orderToCancel.getId());


        for (OrderItem item : orderToCancel.getOrderItems()) {
            FoodItem foodItem = item.getFoodItem();
            if (foodItem != null) {
                foodItem.setDailyQuantity(foodItem.getDailyQuantity() + item.getQuantity());
                foodItemRepository.save(foodItem);
                logger.info("Hoàn lại {} cho món {} (ID: {})", item.getQuantity(), foodItem.getName(), foodItem.getId());
            }
        }


        orderRepository.delete(orderToCancel);
        logger.info("Người dùng {} đã hủy thành công đơn hàng ID {} của ngày hôm nay.", userId, orderToCancel.getId());
    }
}