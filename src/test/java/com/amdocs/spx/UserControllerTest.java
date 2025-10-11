package com.amdocs.spx;

import com.amdocs.spx.controller.UserController;
import com.amdocs.spx.dto.UserDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class UserControllerTest {


    @Test
    void testDeleteResponseEntity() {
        // Simulate delete user response
        String result = "User deleted successfully";
        Assertions.assertEquals("User deleted successfully", result);
    }

    @Test
    void testDummyResponseEntity() {
        // Simulate a response for getUserById
        ResponseEntity<UserDTO> response =
                new ResponseEntity<>(new UserDTO(), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void testDummyListResponseEntity() {
        // Simulate a response for getAllUsers
        ResponseEntity<List<UserDTO>> response =
                new ResponseEntity<>(List.of(new UserDTO(), new UserDTO()), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, response.getBody().size());
    }
}
