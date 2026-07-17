package com.enterprise.apimonetization.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
}
