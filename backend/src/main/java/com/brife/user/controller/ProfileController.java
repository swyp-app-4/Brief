package com.brife.user.controller;

import com.brife.user.dto.InterestRequest;
import com.brife.user.dto.UserProfileResponse;
import com.brife.user.dto.UserProfileUpdate;
import com.brife.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal Long userId, @RequestBody UserProfileUpdate request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Long userId) {
        profileService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/interests")
    public ResponseEntity<Void> saveInterests(@AuthenticationPrincipal Long userId, @RequestBody InterestRequest request) {
        profileService.saveInterests(userId, request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/me/interests")
    public ResponseEntity<Void> resetInterests(@AuthenticationPrincipal Long userId, @RequestBody InterestRequest request) {
        profileService.resetInterests(userId, request);
        return ResponseEntity.ok().build();
    }
}
