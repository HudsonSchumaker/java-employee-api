package com.schumaker.api.security.view;

import com.schumaker.api.security.model.entity.AuthUser;
import com.schumaker.api.security.view.dto.LoginForm;
import com.schumaker.api.security.view.dto.TokenDTO;
import com.schumaker.api.security.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/token")
public class AuthController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<TokenDTO> doLogin(@RequestBody @Valid LoginForm form, UriComponentsBuilder uriBuilder) {
        var token = new UsernamePasswordAuthenticationToken(form.getEmail(), form.getPassword());
        var authentication = manager.authenticate(token);
        var TokenDTO = new TokenDTO(tokenService.generateToken((AuthUser) authentication.getPrincipal()));
        var address = uriBuilder.path("/token").build().toUri();

        return ResponseEntity.created(address).body(TokenDTO);
    }
}
