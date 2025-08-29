package phuctnh;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class DailyController {
    private String success = "200";
    private String fail = "400";
    private final sql_function sqlFunctions = new sql_function();
    public function function = new function();

    // <-------------------- AUTH -------------------->
    // Đăng xuất
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return success;
    }

    // Xác thực đăng ký
    @PostMapping("/auth/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String name,
                               @RequestParam String role,
                               @RequestParam String password) {
        boolean isRegistered = sqlFunctions.registerUser(username, name, role, password);
        if (isRegistered) {
            return success;
        } else {
            function.log("auth/register: 001_02_400");
            return fail;
        }
    }

    // Xác thực đăng nhập
    @PostMapping("/auth/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session) {
        String data = sqlFunctions.loginUser(username, password, session);
        if (data != null) {
            return data;   // Trả về "admin" hoặc "user"
        }
        function.log("auth/login: 001_02_400");
        return fail;
    }

    // <-------------------- ADMIN -------------------->
    // Xóa nhân viên
    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam String username){
        boolean isDelete = sqlFunctions.deleteUser(username);
        if (isDelete) {
            return success;
        } else {
            function.log("admin/delete: 001_02_400");
            return fail;
        }
    }

    // Hiển thị danh sách toàn bộ nhân viên
    @GetMapping("/admin/show")
    public String getAllEmployees() {
        ArrayList<User_Info> list = sqlFunctions.getAllUsersInfo();
        if (!list.isEmpty()) {
            return sqlFunctions.toJson(list);
        } else {
            function.log("admin/show: 001_02_400");
            return "[]";
        }
    }

    // Thống kê
    @GetMapping("/admin/stats")
    public String getAttendanceStats() {
        ArrayList<AttendanceStats> list = sqlFunctions.getAttendanceStats();
        if (!list.isEmpty()) {
            return sqlFunctions.toJson(list);
        } else {
            function.log("admin/stats: 001_02_400");
            return "[]";
        }
    }

    // <-------------------- USER -------------------->
    @GetMapping("/checkin")
    public String checkin(@RequestParam String username) {
        return sqlFunctions.checkIn(username);
    }
    //Viết todolist
    @GetMapping("/todolist")
    public String todo(@RequestParam String username, @RequestParam String todolist) {
        return sqlFunctions.addToDoList(username,todolist);
    }
    // Endpoint cho check-out, trả về trực tiếp chuỗi kết quả
    @GetMapping("/checkout")
    public String checkout(@RequestParam String username) {
        return sqlFunctions.checkOut(username);
    }

    // Endpoint để lấy thông tin người dùng, trả về trực tiếp chuỗi JSON kết quả
    @GetMapping("/info")
    public String getinfo(@RequestParam String username) {
        return sqlFunctions.getUserInfo(username);
    }

    @GetMapping("/getname")
    public String name(@RequestParam String username) {
        return sqlFunctions.getUserName(username);
    }
    @GetMapping("/gettodolist")
    public String getdo(@RequestParam String username) {
        return sqlFunctions.getTodayTodoList(username);
    }



}
