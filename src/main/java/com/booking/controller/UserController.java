package com.booking.controller;

import com.booking.entities.User;
import com.booking.payload.UserDTO;
import com.booking.repository.UserRepository;
import com.booking.service.UserService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;


import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

//http://localhost:8080/api/users/create
    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(firstName);
        userDTO.setLastName(lastName);
        userDTO.setEmail(email);
        userDTO.setPassword(password);
        userDTO.setPhoneNumber(phoneNumber);
        userDTO.setProfileImage(profileImage);

        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    //http://localhost:8080/api/users/user_profile_image/{fileName}
    @GetMapping("/user_profile_image/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) throws IOException {
        try {
            Resource resource = userService.loadUserProfileImage(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while loading the file", e);
        }
    }

   // http://localhost:8080/api/download/excel
    @GetMapping(value="/download/excel",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadExcel() {
        InputStreamResource file = null;
        try {
            file = userService.getUsersAsExcel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String fileName = "users_" + System.currentTimeMillis() + ".xlsx";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }
    //http://localhost:8080/api/users/{id}/download/excel


    //http://localhost:8080/api/users/{id}/excel
    @GetMapping("/{id}/excel")
    public ResponseEntity<InputStreamResource> downloadUserAsExcel(@PathVariable Long id) throws IOException {
        UserDTO userDTO = userService.getUserById(id);
        InputStreamResource inputStreamResource = userService.getUserAsExcel(userDTO);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=User_Data.xlsx");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(inputStreamResource);
    }

       //http://localhost:8080/api/users/2/pdf
        @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
        public void getUserPdf(@PathVariable("id") Long id, HttpServletResponse response) throws IOException, DocumentException {

            Optional<User> userOptional = userRepository.findById(id);

            if (!userOptional.isPresent()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            User user = userOptional.get();

            // Create a PDF document with user data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("User ID: " + user.getId()));
            document.add(new Paragraph("First name: " + user.getFirstName()));
            document.add(new Paragraph("Last name: " + user.getLastName()));
            document.add(new Paragraph("Email: " + user.getEmail()));
            document.add(new Paragraph("Phone number: " + user.getPhoneNumber()));
            document.close();

            // Set response headers
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"user_" + user.getId() + ".pdf\"");

            // Write the PDF document to response output stream
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            StreamUtils.copy(bais, response.getOutputStream());
        }

    }





