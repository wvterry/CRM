package com.example.demo.Controller;

import com.example.demo.DTO.UpdateUserDTO;
import com.example.demo.DTO.UpdateUserRoleDTO;
import com.example.demo.DTO.UpdatedUserDTO;
import com.example.demo.DTO.UserInfoDTO;
import com.example.demo.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/update/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdatedUserDTO> updateUser(@PathVariable Long id,
                                                     @RequestBody UpdateUserDTO updateUserDTO){
        return ResponseEntity.ok(adminService.updateUser(id, updateUserDTO));
    }

    @GetMapping("/get/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserInfoDTO>> getAllUserInfo(){
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/update/userrole/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id,@RequestBody UpdateUserRoleDTO updateUserRoleDTO){
        adminService.updateUserRole(id, updateUserRoleDTO);
        return ResponseEntity.ok().build();
    }

}
