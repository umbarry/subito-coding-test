package com.subito.subitocodingtest.service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    Claims parseToken(String token);
}
