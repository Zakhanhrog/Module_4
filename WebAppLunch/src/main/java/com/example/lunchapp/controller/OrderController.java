package com.example.lunchapp.controller;

import com.example.lunchapp.model.dto.OrderRequestDto;
import com.example.lunchapp.model.dto.SelectedFoodItemDto;
import com.example.lunchapp.model.dto.UserDto;
import com.example.lunchapp.model.entity.Category;
import com.example.lunchapp.model.entity.FoodItem;
import com.example.lunchapp.model.entity.Order;
import com.example.lunchapp.repository.UserRepository;
import com.example.lunchapp.service.AppSettingService;
import com.example.lunchapp.service.FoodItemService;
import com.example.lunchapp.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final String LOGGED_IN_USER_SESSION_KEY = "loggedInUser";

    private final FoodItemService foodItemService;
    private final OrderService orderService;
    private final AppSettingService appSettingService;
    private final UserRepository userRepository;
    private final Validator validator;

    @Autowired
    public OrderController(FoodItemService foodItemService,
                           OrderService orderService,
                           AppSettingService appSettingService,
                           UserRepository userRepository,
                           Validator validator) {
        this.foodItemService = foodItemService;
        this.orderService = orderService;
        this.appSettingService = appSettingService;
        this.userRepository = userRepository;
        this.validator = validator;
    }

    private boolean isOrderTimeAllowed() {
        return LocalDateTime.now().toLocalTime().isBefore(appSettingService.getOrderCutoffTime());
    }

    @GetMapping("/menu")
    public String showMenu(Model model, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute(LOGGED_IN_USER_SESSION_KEY);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        Map<Category, List<FoodItem>> groupedFoodItems = foodItemService.getGroupedAvailableFoodItemsForToday();
        model.addAttribute("groupedFoodItems", groupedFoodItems);
        if (!model.containsAttribute("orderRequestDto")) {
            model.addAttribute("orderRequestDto", new OrderRequestDto());
        }

        LocalTime cutoffTime = appSettingService.getOrderCutoffTime();
        boolean orderTimeAllowed = isOrderTimeAllowed();
        model.addAttribute("orderTimeAllowed", orderTimeAllowed);
        model.addAttribute("cutoffTime", cutoffTime);

        boolean hasOrderedToday = false;
        Optional<Order> todaysOrderOpt = Optional.empty();

        if (orderTimeAllowed) {
            long secondsRemaining = java.time.Duration.between(LocalDateTime.now(), cutoffTime.atDate(LocalDate.now())).getSeconds();
            model.addAttribute("countdownSeconds", Math.max(0, secondsRemaining));

            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
            hasOrderedToday = orderService.hasUserOrderedToday(currentUser.getId(), startOfDay, endOfDay);
            if(hasOrderedToday){
                todaysOrderOpt = orderService.getTodaysOrderByUser(currentUser.getId(), startOfDay, endOfDay);
            }

            Object countdownSecondsAttr = model.getAttribute("countdownSeconds");
            logger.info("OrderController - Current Time: {}", LocalDateTime.now().toLocalTime());
            logger.info("OrderController - Cutoff Time from Service: {}", cutoffTime);
            logger.info("OrderController - orderTimeAllowed: {}", orderTimeAllowed);
            logger.info("OrderController - countdownSeconds from model: {} (Type: {})", countdownSecondsAttr, (countdownSecondsAttr != null ? countdownSecondsAttr.getClass().getName() : "null"));
            logger.info("OrderController - User {} hasOrderedToday: {}", currentUser.getUsername(), hasOrderedToday);
        } else {
            model.addAttribute("countdownSeconds", 0L);
            // Nếu đã hết giờ, cũng kiểm tra xem có đơn hàng hôm nay không để hiển thị thông tin
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
            if(orderService.hasUserOrderedToday(currentUser.getId(), startOfDay, endOfDay)){
                hasOrderedToday = true; // User might have ordered before cutoff
                todaysOrderOpt = orderService.getTodaysOrderByUser(currentUser.getId(), startOfDay, endOfDay);
            }
        }
        model.addAttribute("hasOrderedToday", hasOrderedToday);
        todaysOrderOpt.ifPresent(order -> model.addAttribute("todaysOrderId", order.getId()));

        return "order/menu";
    }

    @PostMapping("/place")
    public String placeOrder(@RequestParam(name = "selectedItemCheck", required = false) List<Long> selectedFoodItemIds,
                             @RequestParam(name = "note", required = false) String note,
                             HttpSession session,
                             RedirectAttributes redirectAttributes, Model model) {

        UserDto currentUser = (UserDto) session.getAttribute(LOGGED_IN_USER_SESSION_KEY);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (selectedFoodItemIds == null || selectedFoodItemIds.isEmpty()) {
            logger.warn("Người dùng {} không chọn món nào.", currentUser.getUsername());
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một món ăn.");
            return "redirect:/order/menu";
        }

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        List<SelectedFoodItemDto> selectedItemsList = new ArrayList<>();
        for (Long foodId : selectedFoodItemIds) {
            SelectedFoodItemDto itemDto = new SelectedFoodItemDto();
            itemDto.setFoodItemId(foodId);
            itemDto.setQuantity(1);
            selectedItemsList.add(itemDto);
        }
        orderRequestDto.setSelectedItems(selectedItemsList);
        orderRequestDto.setNote(note);

        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(orderRequestDto, "orderRequestDto");
        validator.validate(orderRequestDto, bindingResult);

        if (bindingResult.hasErrors()) {
            logger.warn("Lỗi validation khi người dùng {} đặt món: {}", currentUser.getUsername(), bindingResult.getAllErrors());
            // Logic để render lại trang menu với lỗi và dữ liệu đã nhập
            Map<Category, List<FoodItem>> groupedFoodItems = foodItemService.getGroupedAvailableFoodItemsForToday();
            model.addAttribute("groupedFoodItems", groupedFoodItems);
            model.addAttribute("cutoffTime", appSettingService.getOrderCutoffTime());
            boolean currentOrderTimeAllowed = isOrderTimeAllowed();
            model.addAttribute("orderTimeAllowed", currentOrderTimeAllowed);

            boolean currentHasOrderedToday = false;
            Optional<Order> currentTodaysOrderOpt = Optional.empty();
            if (currentOrderTimeAllowed) {
                long secondsRemaining = java.time.Duration.between(LocalDateTime.now(), appSettingService.getOrderCutoffTime().atDate(LocalDate.now())).getSeconds();
                model.addAttribute("countdownSeconds", Math.max(0, secondsRemaining));
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
                currentHasOrderedToday = orderService.hasUserOrderedToday(currentUser.getId(), startOfDay, endOfDay);
                if(currentHasOrderedToday){
                    currentTodaysOrderOpt = orderService.getTodaysOrderByUser(currentUser.getId(), startOfDay, endOfDay);
                }
            } else {
                model.addAttribute("countdownSeconds", 0L);
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
                if(orderService.hasUserOrderedToday(currentUser.getId(), startOfDay, endOfDay)){
                    currentHasOrderedToday = true;
                    currentTodaysOrderOpt = orderService.getTodaysOrderByUser(currentUser.getId(), startOfDay, endOfDay);
                }
            }
            model.addAttribute("hasOrderedToday", currentHasOrderedToday);
            currentTodaysOrderOpt.ifPresent(o -> model.addAttribute("todaysOrderId", o.getId()));

            model.addAttribute("org.springframework.validation.BindingResult.orderRequestDto", bindingResult);
            // orderRequestDto đã chứa note và selectedItems nên không cần set lại tường minh trừ khi muốn reset
            model.addAttribute("orderRequestDto", orderRequestDto);
            return "order/menu";
        }

        try {
            Order placedOrder = orderService.placeOrder(currentUser.getId(), orderRequestDto, false);

            UserDto userAfterOrder = new UserDto(userRepository.findById(currentUser.getId()).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng sau khi đặt hàng")));
            session.setAttribute(LOGGED_IN_USER_SESSION_KEY, userAfterOrder);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Đặt món thành công! Mã đơn hàng: %d. Tổng tiền: %.2f VND. Số dư mới: %.2f VND",
                            placedOrder.getId(),
                            placedOrder.getTotalAmount(),
                            userAfterOrder.getBalance()
                    ));
            return "redirect:/user/history";
        } catch (Exception e) {
            logger.error("Lỗi khi người dùng {} đặt món: {}", currentUser.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đặt món: " + e.getMessage());
            return "redirect:/order/menu";
        }
    }

    @PostMapping("/cancel-and-reorder")
    public String cancelAndReorder(HttpSession session, RedirectAttributes redirectAttributes) {
        UserDto currentUser = (UserDto) session.getAttribute(LOGGED_IN_USER_SESSION_KEY);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (!isOrderTimeAllowed()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã hết thời gian cho phép hủy và đặt lại đơn hàng.");
            return "redirect:/order/menu";
        }

        try {
            orderService.cancelTodaysOrderAndRefund(currentUser.getId());
            // Cập nhật lại thông tin user trong session (ví dụ: số dư)
            UserDto userAfterCancel = new UserDto(userRepository.findById(currentUser.getId()).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng sau khi hủy đơn")));
            session.setAttribute(LOGGED_IN_USER_SESSION_KEY, userAfterCancel);

            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng hôm nay. Bạn có thể đặt lại. Số dư mới: " + String.format("%.2f VND", userAfterCancel.getBalance()));
        } catch (Exception e) {
            logger.error("Lỗi khi người dùng {} hủy đơn hàng để đặt lại: {}", currentUser.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy đơn hàng: " + e.getMessage());
        }
        return "redirect:/order/menu";
    }
}