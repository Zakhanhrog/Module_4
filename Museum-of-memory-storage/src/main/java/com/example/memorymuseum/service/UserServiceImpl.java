package com.example.memorymuseum.service;

import com.example.memorymuseum.dto.UserPasswordChangeDto;
import com.example.memorymuseum.dto.UserProfileUpdateDto;
import com.example.memorymuseum.dto.UserRegistrationDto;
import com.example.memorymuseum.dto.UserSearchDto;
import com.example.memorymuseum.model.Role;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService; // Thêm
    private static final String AVATAR_SUBFOLDER = "avatars";

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public User registerNewUserAccount(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + registrationDto.getUsername());
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registrationDto.getEmail());
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setRole(Role.USER);
        user.setPreferredLanguage("vi"); // Default
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(String currentUsername, UserProfileUpdateDto profileUpdateDto) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUsername));

        // Kiểm tra email mới có bị trùng với user khác không (nếu email thay đổi)
        if (!user.getEmail().equalsIgnoreCase(profileUpdateDto.getEmail()) &&
                userRepository.findByEmail(profileUpdateDto.getEmail()).filter(otherUser -> !otherUser.getId().equals(user.getId())).isPresent()) {
            throw new IllegalArgumentException("Email already in use by another account."); // I18N sau
        }

        user.setFullName(profileUpdateDto.getFullName());
        user.setEmail(profileUpdateDto.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String currentUsername, UserPasswordChangeDto passwordChangeDto) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUsername));

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect current password."); // I18N sau
        }
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match."); // I18N sau
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String updateUserAvatar(String currentUsername, MultipartFile avatarFile) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUsername));

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Avatar file cannot be empty.");
        }

        // Xóa avatar cũ nếu có
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            try {
                String oldAvatarFileName = Paths.get(user.getAvatarUrl()).getFileName().toString();
                fileStorageService.delete(oldAvatarFileName, AVATAR_SUBFOLDER + "/" + user.getId());
            } catch (Exception e) {
                // Log lỗi xóa file cũ nhưng vẫn tiếp tục upload file mới
                System.err.println("Error deleting old avatar: " + e.getMessage());
            }
        }

        // Lưu avatar mới, tạo thư mục con theo user ID để tránh trùng tên file
        String storedFileName = fileStorageService.store(avatarFile, AVATAR_SUBFOLDER + "/" + user.getId());
        user.setAvatarUrl(storedFileName); // Lưu đường dẫn tương đối
        userRepository.save(user);
        return storedFileName; // Trả về đường dẫn đã lưu
    }
    @Override
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public Optional<User> findById(Long userId) { // Implement phương thức mới
        return userRepository.findById(userId);
    }

    @Override
    @Transactional
    public User changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User toggleUserEnabledStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserByAdmin(Long userId, UserProfileUpdateDto profileUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Kiểm tra email mới có bị trùng với user khác không (nếu email thay đổi)
        if (!user.getEmail().equalsIgnoreCase(profileUpdateDto.getEmail()) &&
                userRepository.findByEmail(profileUpdateDto.getEmail())
                        .filter(otherUser -> !otherUser.getId().equals(user.getId()))
                        .isPresent()) {
            throw new IllegalArgumentException("Email already in use by another account.");
        }

        user.setFullName(profileUpdateDto.getFullName());
        user.setEmail(profileUpdateDto.getEmail());
        
        // Có thể thêm các trường khác cần cập nhật tại đây
        if (profileUpdateDto.getPreferredLanguage() != null) {
            user.setPreferredLanguage(profileUpdateDto.getPreferredLanguage());
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Kiểm tra không cho phép xóa admin duy nhất
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the only admin account.");
            }
        }
        
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false); // Disable tài khoản khi xóa
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setEnabled(true); // Mở lại tài khoản khi restore
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findActiveUsers(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findDeletedUsers(Pageable pageable) {
        return userRepository.findByDeletedTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(UserSearchDto searchDto, Pageable pageable) {
        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (searchDto.getUsername() != null && !searchDto.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")), 
                    "%" + searchDto.getUsername().toLowerCase() + "%")
                );
            }
            
            if (searchDto.getEmail() != null && !searchDto.getEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), 
                    "%" + searchDto.getEmail().toLowerCase() + "%")
                );
            }
            
            if (searchDto.getFullName() != null && !searchDto.getFullName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")), 
                    "%" + searchDto.getFullName().toLowerCase() + "%")
                );
            }
            
            if (searchDto.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), searchDto.getRole()));
            }
            
            if (searchDto.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), searchDto.getEnabled()));
            }
            
            if (searchDto.getDeleted() != null) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), searchDto.getDeleted()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return userRepository.findAll(specification, pageable);
    }
}