package fr.louison.gestionsallespringapi.demo.controller;

import fr.louison.gestionsallespringapi.demo.dto.DemoAccountDto;
import fr.louison.gestionsallespringapi.demo.service.DemoAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Under /api/auth so that it is reachable before login, like the login endpoint itself.
@RestController
@RequestMapping("/api/auth/demo-accounts")
@RequiredArgsConstructor
public class DemoController {

    private final DemoAccountService demoAccountService;

    /** Empty unless the demo mode is enabled. */
    @GetMapping
    public ResponseEntity<List<DemoAccountDto>> getDemoAccounts() {
        return ResponseEntity.ok(demoAccountService.getAccounts());
    }
}
