package com.example.lunchapp.controller;

import com.example.lunchapp.model.dto.OrderRequestDto;
import com.example.lunchapp.model.dto.SelectedFoodItemDto;
import com.example.lunchapp.model.dto.UserDto;
import com.example.lunchapp.model.entity.*;
import com.example.lunchapp.repository.RoleRepository;
import com.example.lunchapp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final String LOGGED_IN_USER_SESSION_KEY = "loggedInUser";

    private final UserService userService;
    private final FoodItemService foodItemService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final ServletContext servletContext;
    private final AppSettingService appSettingService;
    private final RoleRepository roleRepository;
    private final Validator validator;

    @Value("${app.upload.food-image-dir}")
    private String foodImagePhysicalDir;

    @Autowired
    public AdminController(UserService userService, FoodItemService foodItemService, OrderService orderService,
                           CategoryService categoryService, ServletContext servletContext,
                           AppSettingService appSettingService, RoleRepository roleRepository,
                           Validator validator) {
        this.userService = userService;
        this.foodItemService = foodItemService;
        this.orderService = orderService;
        this.categoryService = categoryService;
        this.servletContext = servletContext;
        this.appSettingService = appSettingService;
        this.roleRepository = roleRepository;
        this.validator = validator;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, HttpSession session) {
        UserDto adminUser = (UserDto) session.getAttribute(LOGGED_IN_USER_SESSION_KEY);
        model.addAttribute("adminUser", adminUser);
        return "admin/dashboard";
    }

    @GetMapping("/food/all")
    public String showAllFoodItems(Model model) {
        List<FoodItem> foodItems = foodItemService.getAllFoodItems();
        model.addAttribute("foodItems", foodItems);
        return "admin/food-list-all";
    }

    @GetMapping("/food/add")
    public String showAddFoodItemForm(Model model) {
        model.addAttribute("foodItem", new FoodItem());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/food-form";
    }

    @GetMapping("/food/edit/{id}")
    public String showEditFoodItemForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FoodItem> foodItemOpt = foodItemService.findById(id);
        if (foodItemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy món ăn với ID: " + id);
            return "redirect:/admin/food/all";
        }
        model.addAttribute("foodItem", foodItemOpt.get());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/food-form";
    }

    @PostMapping("/food/save")
    public String saveFoodItem(@Valid @ModelAttribute("foodItem") FoodItem foodItem,
                               BindingResult bindingResult,
                               @RequestParam("imageFile") MultipartFile imageFile,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            logger.warn("Lỗi validation form món ăn: {}", bindingResult.getAllErrors());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/food-form";
        }

        if (!imageFile.isEmpty()) {
            String originalFilename = imageFile.getOriginalFilename();
            if (originalFilename != null && (originalFilename.toLowerCase().endsWith(".png") || originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg"))) {
                try {
                    String fileName = "food_" + System.currentTimeMillis() + "_" + originalFilename.replaceAll("\\s+", "_");
                    Path uploadPathDir = Paths.get(foodImagePhysicalDir);
                    if (!Files.exists(uploadPathDir)) {
                        Files.createDirectories(uploadPathDir);
                    }
                    Path filePath = uploadPathDir.resolve(fileName);
                    try (InputStream inputStream = imageFile.getInputStream()) {
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    foodItem.setImageUrl("/uploaded-images/food/" + fileName);
                } catch (IOException e) {
                    logger.error("Không thể lưu file ảnh: " + originalFilename, e);
                    bindingResult.rejectValue("imageUrl", "error.foodItem", "Không thể tải lên ảnh: " + e.getMessage());
                    model.addAttribute("categories", categoryService.getAllCategories());
                    return "admin/food-form";
                }
            } else if (originalFilename != null && !originalFilename.isEmpty()){
                bindingResult.rejectValue("imageUrl", "error.foodItem", "Loại file ảnh không hợp lệ. Chỉ chấp nhận PNG, JPG, JPEG.");
                model.addAttribute("categories", categoryService.getAllCategories());
                return "admin/food-form";
            }
        } else if (foodItem.getId() != null) {
            foodItemService.findById(foodItem.getId()).ifPresent(existingFood -> {
                if (foodItem.getImageUrl() == null || foodItem.getImageUrl().isEmpty()) {
                    foodItem.setImageUrl(existingFood.getImageUrl());
                }
            });
        }

        try {
            foodItemService.saveFoodItem(foodItem);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu món ăn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu món ăn: " + e.getMessage());
        }
        return "redirect:/admin/food/all";
    }

    @GetMapping("/food/delete/{id}")
    public String deleteFoodItem(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            foodItemService.deleteFoodItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa món ăn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa món ăn: " + e.getMessage() + ". Món ăn có thể đã nằm trong đơn hàng.");
        }
        return "redirect:/admin/food/all";
    }

    @GetMapping("/food/daily-menu")
    public String showDailyMenuSetupForm(Model model) {
        List<FoodItem> allFoodItems = foodItemService.getAllFoodItems();
        model.addAttribute("allFoodItems", allFoodItems);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/daily-menu-setup";
    }

    @PostMapping("/food/daily-menu/save")
    public String saveDailyMenuSetup(@RequestParam(value = "selectedFoodItemIds", required = false) List<Long> selectedFoodItemIds,
                                     @RequestParam Map<String, String> allRequestParams,
                                     RedirectAttributes redirectAttributes) {
        Map<Long, Integer> dailyQuantities = new HashMap<>();
        if (selectedFoodItemIds != null) {
            for (Long itemId : selectedFoodItemIds) {
                String quantityParam = "dailyQuantity_" + itemId;
                if (allRequestParams.containsKey(quantityParam)) {
                    try {
                        int quantity = Integer.parseInt(allRequestParams.get(quantityParam));
                        dailyQuantities.put(itemId, Math.max(0, quantity));
                    } catch (NumberFormatException e) {
                        dailyQuantities.put(itemId, 0);
                    }
                } else {
                    dailyQuantities.put(itemId, 0);
                }
            }
        }
        try {
            foodItemService.setFoodItemsForToday(selectedFoodItemIds, dailyQuantities);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thực đơn hàng ngày thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật thực đơn hàng ngày: " + e.getMessage());
        }
        return "redirect:/admin/food/daily-menu";
    }

    @GetMapping("/config/order-time")
    public String showOrderTimeConfigForm(Model model) {
        model.addAttribute("currentCutoffTime", appSettingService.getOrderCutoffTime());
        return "admin/order-time-config";
    }

    @PostMapping("/config/order-time/save")
    public String saveOrderTimeConfig(@RequestParam("cutoffTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime cutoffTime,
                                      RedirectAttributes redirectAttributes) {
        if (cutoffTime == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Định dạng thời gian không hợp lệ.");
            return "redirect:/admin/config/order-time";
        }
        try {
            appSettingService.setOrderCutoffTime(cutoffTime);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thời gian chốt đơn thành công: " + cutoffTime.toString() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật thời gian chốt đơn: " + e.getMessage());
        }
        return "redirect:/admin/config/order-time";
    }

    @GetMapping("/users/list")
    public String showUserList(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng với ID: " + id);
            return "redirect:/admin/users/list";
        }
        model.addAttribute("userToEdit", userOpt.get());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/user-form-edit";
    }

    @PostMapping("/users/update")
    public String updateUserByAdmin(@Valid @ModelAttribute("userToEdit") User userToEdit,
                                    BindingResult bindingResult,
                                    @RequestParam(value = "newPassword", required = false) String newPassword,
                                    @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasFieldErrors("username") || bindingResult.hasFieldErrors("department") || bindingResult.hasFieldErrors("balance")) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/user-form-edit";
        }
        try {
            userService.updateUserByAdmin(userToEdit.getId(), userToEdit, newPassword, roleIds);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công!");
        } catch (RuntimeException e) {
            model.addAttribute("userToEdit", userToEdit);
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("errorMessage", "Lỗi cập nhật người dùng: " + e.getMessage());
            return "admin/user-form-edit";
        }
        return "redirect:/admin/users/list";
    }

    @GetMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái người dùng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật trạng thái người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users/list";
    }

    @GetMapping("/orders/create-for-user/{userId}")
    public String showOrderFormForUser(@PathVariable("userId") Long userId, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        UserDto adminUser = (UserDto) session.getAttribute(LOGGED_IN_USER_SESSION_KEY);
        if (adminUser == null) {
            return "redirect:/auth/login";
        }
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng với ID: " + userId);
            return "redirect:/admin/users/list";
        }
        Map<Category, List<FoodItem>> groupedFoodItems = foodItemService.getGroupedAvailableFoodItemsForToday();
        model.addAttribute("groupedFoodItems", groupedFoodItems);
        model.addAttribute("orderRequestDto", new OrderRequestDto());
        model.addAttribute("targetUser", userOpt.get());
        return "admin/order-form-for-user";
    }

    @PostMapping("/orders/place-for-user/{userId}")
    public String placeOrderForUser(@PathVariable("userId") Long userId,
                                    @RequestParam Map<String, String> allParams,
                                    @RequestParam(name = "note", required = false) String note,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes, Model model) {

        UserDto adminUser = (UserDto) session.getAttribute(LOGGED_IN_USER_SESSION_KEY);
        if (adminUser == null) {
            return "redirect:/auth/login";
        }

        Optional<User> targetUserOpt = userService.findById(userId);
        if (targetUserOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng mục tiêu với ID: " + userId);
            return "redirect:/admin/users/list";
        }

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        List<SelectedFoodItemDto> selectedItemsList = new ArrayList<>();
        Map<String, SelectedFoodItemDto> itemsBeingBuilt = new HashMap<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("selectedItems[") && key.endsWith("].foodItemId")) {
                String index = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
                itemsBeingBuilt.putIfAbsent(index, new SelectedFoodItemDto());
                if (value != null && !value.isEmpty()) {
                    try {
                        itemsBeingBuilt.get(index).setFoodItemId(Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        logger.warn("Giá trị foodItemId không hợp lệ: {}", value);
                    }
                }
            } else if (key.startsWith("selectedItems[") && key.endsWith("].quantity")) {
                String index = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
                itemsBeingBuilt.putIfAbsent(index, new SelectedFoodItemDto());
                if (value != null && !value.isEmpty()) {
                    try {
                        itemsBeingBuilt.get(index).setQuantity(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        logger.warn("Giá trị số lượng không hợp lệ: {}", value);
                        itemsBeingBuilt.get(index).setQuantity(0);
                    }
                } else {
                    itemsBeingBuilt.get(index).setQuantity(0);
                }
            }
        }

        for (SelectedFoodItemDto itemDto : itemsBeingBuilt.values()) {
            if (itemDto.getFoodItemId() != null && itemDto.getQuantity() != null && itemDto.getQuantity() > 0) {
                selectedItemsList.add(itemDto);
            }
        }
        orderRequestDto.setSelectedItems(selectedItemsList);
        orderRequestDto.setNote(note);

        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(orderRequestDto, "orderRequestDto");
        validator.validate(orderRequestDto, bindingResult);

        if (bindingResult.hasErrors()) {
            logger.warn("Lỗi validation sau khi xây dựng OrderRequestDto thủ công (Admin đặt hộ): {}", bindingResult.getAllErrors());
            model.addAttribute("groupedFoodItems", foodItemService.getGroupedAvailableFoodItemsForToday());
            model.addAttribute("targetUser", targetUserOpt.get());
            model.addAttribute("org.springframework.validation.BindingResult.orderRequestDto", bindingResult);
            model.addAttribute("orderRequestDto", orderRequestDto);
            return "admin/order-form-for-user";
        }

        if (orderRequestDto.getSelectedItems().isEmpty()) {
            logger.warn("Admin {} không chọn món nào hợp lệ cho user {}", adminUser.getUsername(), targetUserOpt.get().getUsername());
            model.addAttribute("errorMessage", "Vui lòng chọn ít nhất một món ăn với số lượng hợp lệ (lớn hơn 0).");
            model.addAttribute("groupedFoodItems", foodItemService.getGroupedAvailableFoodItemsForToday());
            model.addAttribute("targetUser", targetUserOpt.get());
            model.addAttribute("orderRequestDto", new OrderRequestDto()); // Reset DTO
            return "admin/order-form-for-user";
        }

        try {
            Order placedOrder = orderService.placeOrder(userId, orderRequestDto, true);
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Đặt món thành công cho người dùng %s (ID: %d)! Mã đơn hàng: %d. Tổng tiền: %.2f VND.",
                            targetUserOpt.get().getUsername(), userId,
                            placedOrder.getId(),
                            placedOrder.getTotalAmount()
                    ));
        } catch (Exception e) {
            logger.error("Lỗi khi Admin {} đặt món cho người dùng {}: {}", adminUser.getUsername(), targetUserOpt.get().getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đặt món cho người dùng: " + e.getMessage());
            return "redirect:/admin/orders/create-for-user/" + userId;
        }
        return "redirect:/admin/orders/list";
    }

    @GetMapping("/orders/list")
    public String showOrderListByDate(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<Order> ordersForDate = orderService.getOrdersByDate(date);

        // Tính tổng doanh thu
        BigDecimal totalRevenueForDate = ordersForDate.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính thống kê tổng hợp món ăn
        Map<String, Integer> foodItemSummary = new HashMap<>();
        if (ordersForDate != null) {
            for (Order order : ordersForDate) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getFoodItem() != null) {
                        String foodName = item.getFoodItem().getName();
                        int quantity = item.getQuantity();
                        foodItemSummary.merge(foodName, quantity, Integer::sum);
                    }
                }
            }
        }

        // Thống kê theo user (nếu cần dùng)
        Map<User, Order> ordersByUserForDate = new LinkedHashMap<>();
        for (Order order : ordersForDate) {
            ordersByUserForDate.put(order.getUser(), order);
        }

        // Gửi tất cả dữ liệu sang View
        model.addAttribute("ordersForDate", ordersForDate);
        model.addAttribute("selectedDate", date);
        model.addAttribute("totalRevenueForDate", totalRevenueForDate);
        model.addAttribute("ordersByUserForDate", ordersByUserForDate);
        model.addAttribute("foodItemSummary", foodItemSummary); // <-- GỬI DỮ LIỆU THỐNG KÊ

        return "admin/order-list-by-date";
    }
    @GetMapping("/orders/delete-by-date")
    public String deleteOrdersByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                     RedirectAttributes redirectAttributes) {
        if (date == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cần chọn ngày.");
            return "redirect:/admin/orders/list";
        }
        try {
            List<Order> ordersToDelete = orderService.getOrdersByDate(date);
            if (ordersToDelete.isEmpty()) {
                redirectAttributes.addFlashAttribute("infoMessage", "Không có đơn hàng nào cho ngày " + date.toString() + " để xóa.");
            } else {
                for (Order order : ordersToDelete) {
                    orderService.deleteOrderById(order.getId());
                }
                redirectAttributes.addFlashAttribute("successMessage", "Tất cả đơn hàng cho ngày " + date.toString() + " đã được xóa.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/orders/list?date=" + date.toString();
    }
}