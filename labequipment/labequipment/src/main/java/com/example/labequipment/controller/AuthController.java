package com.example.labequipment.controller;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.LoginDTO;
import com.example.labequipment.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }
}
