package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 獲取當前登入用戶的個人資料
     * @param userDetails Spring Security 會自動注入當前登入的用戶資訊
     * @return User 物件 (或是一個 DTO)
     */
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        // 從 UserDetails 中獲取用戶名稱
        String username = userDetails.getUsername();
        
        // 透過 UserService 查找用戶
        User user = userService.findByUsername(username);

        // 為了安全起見，不應該回傳整個 User 物件 (包含密碼)
        // 這裡我們先回傳，但最佳實踐是建立一個 UserProfileResponse DTO
        // 這裡我們先手動清除密碼
        user.setPassword(null); 
        
        return ResponseEntity.ok(user);
    }

    @PostMapping("/updateEmail")
    public User emailUpdater(@RequestBody String newemail ,@AuthenticationPrincipal UserDetails userDetail) {
        
        String tmp = "";
        for (int i=0;i<newemail.length()-1;i++){
            if(newemail.charAt(i) == '%'){
                tmp = tmp + '@';
                i+=3;
            }
            tmp  = tmp + newemail.charAt(i);
        }
        newemail = tmp;
        System.out.println(newemail);
        User user = userService.updateEmail(newemail,userDetail.getUsername());
        user.setPassword(null);
        return user;
    }
}