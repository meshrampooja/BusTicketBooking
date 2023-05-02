package com.booking.service.Impl;


import com.booking.exception.UserNotFoundException;
import com.booking.util.ExcelGenerator;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import com.booking.entities.User;
import com.booking.payload.UserDTO;
import com.booking.repository.UserRepository;
import com.booking.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    //private final PasswordEncoder passwordEncoder;
    private final String uploadDirectory = "src/main/resources/static/user_profile_image";


    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
       // this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = DTOToUser(userDTO);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        // Encode the password
       // user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));

        MultipartFile profileImage = userDTO.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            String fileName = saveProfileImage(profileImage);
            user.setProfilePicture(fileName);
        }

        User savedUser = userRepository.save(user);
        return userToDTO(savedUser);
    }
    @Override
    public Resource loadUserProfileImage(String fileName) throws IOException {
        Path file = Paths.get(uploadDirectory).resolve(fileName);
        Resource resource = new UrlResource(file.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Failed to load user profile image");
        }
    }
    private String saveProfileImage(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            String baseFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
            String uniqueFileName = baseFileName + "_" + System.currentTimeMillis() + fileExtension;
            Path path = Paths.get(uploadDirectory + uniqueFileName);
            Files.write(path, bytes);

            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile image", e);
        }

    }
    @Override
    public InputStreamResource getUsersAsExcel() throws IOException {
        List<UserDTO> userDTOs = userRepository.findAll().stream()
                .map(this::userToDTO)
                .collect(Collectors.toList());
        ByteArrayInputStream excelInputStream = ExcelGenerator.usersToExcel(userDTOs);

        return new InputStreamResource(excelInputStream);
    }


    @Override
    public InputStreamResource getUserAsExcel(UserDTO user) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("User Data");

        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("id");
        row.createCell(1).setCellValue("firstName");
        row.createCell(2).setCellValue("lastName");
        row.createCell(3).setCellValue("email");
        row.createCell(4).setCellValue("password");
        row.createCell(5).setCellValue("phoneNumber");
        row.createCell(6).setCellValue("profilePicture");


        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(user.getId());
        row.createCell(1).setCellValue(user.getFirstName());
        row.createCell(2).setCellValue(user.getLastName());
        row.createCell(3).setCellValue(user.getEmail());
        row.createCell(4).setCellValue(user.getPassword());
        row.createCell(5).setCellValue(user.getPhoneNumber());
        row.createCell(6).setCellValue(user.getProfilePicture());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        return new InputStreamResource(inputStream);
    }



        private User DTOToUser(UserDTO userDTO) {
            User user = new User();
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setPasswordHash(userDTO.getPassword());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            return user;
        }

        private UserDTO userToDTO(User user) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setLastName(user.getLastName());
            userDTO.setEmail(user.getEmail());
            userDTO.setPhoneNumber(user.getPhoneNumber());
            userDTO.setProfilePicture(user.getProfilePicture());
            userDTO.setCreatedAt(user.getCreatedAt());
            userDTO.setUpdatedAt(user.getUpdatedAt());
            return userDTO;
        }

        @Override
        public UserDTO getUserById(Long id) {
            Optional<User> user = userRepository.findById(id);
            return user.map(this::userToDTO)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        }

//    @Override
//    public ResponseEntity<byte[]> downloadUserDataAsExcel(Long id) throws IOException {
//        return null;
//    }


}


