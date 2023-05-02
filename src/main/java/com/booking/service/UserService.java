package com.booking.service;


import com.booking.payload.UserDTO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;


import java.io.IOException;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);


    Resource loadUserProfileImage(String fileName) throws IOException;

    InputStreamResource getUsersAsExcel() throws IOException;

    //InputStreamResource getUserAsExcel(UserDTO user) throws IOException;


    InputStreamResource getUserAsExcel(UserDTO user) throws IOException;

   UserDTO getUserById(Long id);


    //ResponseEntity<byte[]> downloadUserDataAsExcel(Long id) throws IOException;
}
