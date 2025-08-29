package phuctnh;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class sql_function {
    private static final String jdbcURL = "jdbc:mysql://sql12.freesqldatabase.com/sql12796558";
    private static final String username = "sql12796558";
    private static final String password = "wfy1yg9E9a";
    public function function = new function();
    public sql_function() {
        initDatabase();
    }
    public String toJson(Object obj) {
        return new Gson().toJson(obj);
    }
    public void initDatabase() {
        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             Statement stmt = conn.createStatement()) {

            // Tạo bảng daily_user nếu chưa có
            String createUserTable = "CREATE TABLE IF NOT EXISTS daily_user (" +
                    "username VARCHAR(50) PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "role VARCHAR(50) NOT NULL," +
                    "password VARCHAR(255) NOT NULL" +
                    ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.execute(createUserTable);

            // Tạo bảng daily_attendance nếu chưa có
            String createAttendanceTable = "CREATE TABLE IF NOT EXISTS daily_attendance (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL," +
                    "checkin DATETIME," +
                    "checkout DATETIME," +
                    "time TIME," +
                    "todolist TEXT," +
                    "status VARCHAR(50)," +
                    "FOREIGN KEY (username) REFERENCES daily_user(username) ON DELETE CASCADE" +
                    ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.execute(createAttendanceTable);

            function.log("Database check/initialize done!");

        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
        }
    }

    public boolean registerUser(String user, String name, String role, String pass) {
        String sql = "INSERT INTO daily_user (username, name, role, password) VALUES (?, ?, ?, ?)";
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(pass);
        try
             {
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user);
            statement.setString(2, name);
            statement.setString(3, role);
            statement.setString(4, hashedPassword);
            boolean status =  statement.executeUpdate() > 0;
            statement.close();
            connection.close();
            return status;

        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505",e);
            return false;
        }
    }

    public String loginUser(String user, String pass, HttpSession session) {
        String sql = "SELECT name, password, role FROM daily_user WHERE username = ?";
        try {
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                String role = resultSet.getString("role");
                String name = resultSet.getString("name");
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                if (passwordEncoder.matches(pass, hashedPassword)) {
                    // Lưu thông tin vào session
                    session.setAttribute("username", user);
                    session.setAttribute("name", name);
                    session.setAttribute("role", role);
                    session.setMaxInactiveInterval(300);
                    return role;
                }
                //"{\"name\":\"" + name + "\",\"role\":\"" + role + "\"}"
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            function.log("LoginUser: "+ e.getMessage());
            function.logExceptionWithCode("001_01_505",e);
        }
        return null;
    }

    public boolean deleteUser(String user) {
        String sql = "DELETE FROM daily_user WHERE username = ?";
        try {
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            PreparedStatement statement = connection.prepareStatement(sql);
             statement.setString(1, user);
            boolean status =  statement.executeUpdate() > 0;
            statement.close();
            connection.close();
            return status;
        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505",e);
            return false;
        }
    }

    public ArrayList<User_Info> getAllUsersInfo() {
        ArrayList<User_Info> list = new ArrayList<>();
        String sql = "SELECT u.username, u.name, u.role, a.checkin, a.checkout, a.status, a.todolist " +
                "FROM daily_user u " +
                "LEFT JOIN daily_attendance a ON u.username = a.username";
        try
        {
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String userName = rs.getString("username");
                String name = rs.getString("name");
                String role = rs.getString("role");
                String checkin = rs.getString("checkin");
                String checkout = rs.getString("checkout");
                String status = rs.getString("status");
                String todolist = rs.getString("todolist");
                User_Info userInfo = new User_Info(userName, name, role, checkin, checkout, status, todolist);
                list.add(userInfo);
            }
                  rs.close();
                  pstmt.close();
                  connection.close();
        } catch (SQLException e) {
            function.log(e.getMessage());
        }
        return list;
    }
    public ArrayList<AttendanceStats> getAttendanceStats() {
        ArrayList<AttendanceStats> statsList = new ArrayList<>();

        String sql = "SELECT u.name, " +
                "SUM(TIME_TO_SEC(a.time)) / 3600 AS totalHours, " +
                "SUM(CASE WHEN a.status = 'Đi trễ' THEN 1 ELSE 0 END) AS lateDays, " +
                "GROUP_CONCAT(a.todolist SEPARATOR '||') AS all_todolists " +
                "FROM daily_attendance a " +
                "JOIN daily_user u ON a.username = u.username " +
                "GROUP BY u.name";

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                double totalHours = rs.getDouble("totalHours");
                int lateDays = rs.getInt("lateDays");
                String allTodos = rs.getString("all_todolists");

                int totalPending = 0;
                int totalCompleted = 0;

                if (allTodos != null && !allTodos.isEmpty()) {
                    String[] todosPerDay = allTodos.split("\\|\\|"); // Mỗi ngày một todoList
                    for (String todo : todosPerDay) {
                        if (todo != null && !todo.isEmpty()) {
                            String res = AttendanceStats.analyzetodo(todo);
                            totalPending += Integer.parseInt(res.substring(0, res.indexOf(',')));
                            totalCompleted += Integer.parseInt(res.substring(res.indexOf(',') + 1));
                        }
                    }
                }

                statsList.add(new AttendanceStats(name, totalHours, lateDays, totalCompleted, totalPending));
            }
        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
        }
        return statsList;
    }


    public String checkIn(String userId) {
        String checkQuery = "SELECT COUNT(*) FROM daily_attendance WHERE username = ? AND DATE(checkin) = ?";
        String insertQuery = "INSERT INTO daily_attendance (username, checkin) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            // Lấy thời gian hiện tại theo giờ VN
            LocalDateTime nowVN = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Date today = Date.valueOf(nowVN.toLocalDate());

            // Kiểm tra đã checkin trong ngày chưa
            checkStmt.setString(1, userId);
            checkStmt.setDate(2, today);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    function.log_info("{\"status\":\"001_01_500\", \"message\":\"checkIn: Bạn đã check-in trong ngày hôm nay\"}");
                    return "{\"status\":\"001_01_500\", \"message\":\"Bạn đã check-in trong ngày hôm nay.\"}";
                }
            }

            // Thực hiện check-in
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, userId);
                insertStmt.setTimestamp(2, Timestamp.valueOf(nowVN));

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    return "{\"status\":\"001_01_200\", \"message\":\"Checkin thành công\"}";
                } else {
                    function.log("{\"status\":\"001_01_500\", \"message\":\"checkIn: Checkin thất bại\"}");
                    return "{\"status\":\"001_01_500\", \"message\":\"Checkin thất bại\"}";
                }
            }

        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
            return "{\"status\":\"001_01_505\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }



    public String addToDoList(String userId, String todoList) {
        String updateQuery = "UPDATE daily_attendance SET todolist = ? WHERE username = ? AND checkout IS NULL";

        try (
                Connection conn = DriverManager.getConnection(jdbcURL, username, password);
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery)
        ) {
            updateStmt.setString(1, todoList);
            updateStmt.setString(2, userId);

            // Thực hiện cập nhật vào cơ sở dữ liệu
            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected > 0) {
                return "{\"status\":\"001_01_200\", \"message\":\"Viết TodoList thành công\"}"; // Cập nhật to-do list thành công
            } else {
                function.log("{\"status\":\"001_01_500\", \"message\":\"addToDoList: Bạn chưa viết TodoList\"}");
                return "{\"status\":\"001_01_500\", \"message\":\"Viết TodoList thất bại\"}"; // Cập nhật to-do list thất bại
            }

        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
            return "{\"status\":\"001_01_505\", \"message\":\"ERROR\"}"; // Trả về mã lỗi trong trường hợp xảy ra ngoại lệ
        }
    }

    public String checkOut(String userId) {
        String checkQuery = "SELECT checkin FROM daily_attendance WHERE username = ? AND checkin IS NOT NULL AND checkout IS NULL";
        String todoCheckQuery = "SELECT COUNT(*) FROM daily_attendance WHERE username = ? AND todolist IS NOT NULL AND todolist != '' AND checkout IS NULL";
        String updateQuery = "UPDATE daily_attendance SET checkout = ?, time = TIMEDIFF(?, ?), status = ? WHERE username = ? AND checkout IS NULL";

        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setString(1, userId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp checkInTime = rs.getTimestamp("checkin");

                    // Kiểm tra TodoList
                    try (PreparedStatement todoCheckStmt = conn.prepareStatement(todoCheckQuery)) {
                        todoCheckStmt.setString(1, userId);
                        try (ResultSet todoRs = todoCheckStmt.executeQuery()) {
                            if (todoRs.next() && todoRs.getInt(1) == 0) {
                                function.log("{\"status\":\"001_01_400\", \"message\":\"checkOut: Bạn chưa viết TodoList\"}");
                                return "{\"status\":\"001_01_400\", \"message\":\"Bạn chưa viết TodoList\"}";
                            }
                        }
                    }

                    // Lấy giờ checkout theo VN
                    LocalDateTime nowVN = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                    Timestamp checkoutTime = Timestamp.valueOf(nowVN);

                    // Tính status
                    LocalDate checkInDate = checkInTime.toLocalDateTime().toLocalDate();
                    Timestamp eightAM = Timestamp.valueOf(checkInDate.atTime(8, 0));
                    String status = (checkInTime.before(eightAM) || checkInTime.equals(eightAM))
                            ? "Đúng giờ" : "Đi trễ";

                    // Update checkout
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setTimestamp(1, checkoutTime);
                        updateStmt.setTimestamp(2, checkoutTime);
                        updateStmt.setTimestamp(3, checkInTime);
                        updateStmt.setString(4, status);
                        updateStmt.setString(5, userId);

                        int rowsAffected = updateStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            return "{\"status\":\"001_01_200\", \"message\":\"Checkout thành công\"}";
                        } else {
                            function.log("{\"status\":\"001_01_500\", \"message\":\"checkOut: Checkout thất bại\"}");
                            return "{\"status\":\"001_01_500\", \"message\":\"Checkout thất bại\"}";
                        }
                    }
                } else {
                    function.log("{\"status\":\"001_01_400\", \"message\":\"checkOut: Bạn chưa checkin trước đó\"}");
                    return "{\"status\":\"001_01_400\", \"message\":\"Bạn chưa checkin trước đó\"}";
                }
            }

        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
            return "{\"status\":\"001_01_505\", \"message\":\"" + e + "\"}";
        }
    }

    public String getUserInfo(String userId) {
        String selectQuery = "SELECT username, checkin, checkout, time, todolist, status " +
                "FROM daily_attendance WHERE username = ?";

        try (
                Connection conn = DriverManager.getConnection(jdbcURL, username, password);
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery)
        ) {
            selectStmt.setString(1, userId);

            try (ResultSet rs = selectStmt.executeQuery()) {
                JSONArray jsonArray = new JSONArray();

                while (rs.next()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("username", rs.getString("username"));
                    jsonObject.put("checkin", rs.getString("checkin"));
                    jsonObject.put("checkout", rs.getString("checkout"));
                    jsonObject.put("time", rs.getString("time"));
                    jsonObject.put("todolist", rs.getString("todolist"));
                    jsonObject.put("status", rs.getString("status"));
                    jsonArray.put(jsonObject);
                }

                if (jsonArray.isEmpty()) {
                    function.log("{\"status\":\"404\", \"message\":\"getUserInfo: Dữ liệu trống\"}");
                    return "{\"status\":\"404\", \"message\":\"Dữ liệu trống\"}";
                } else {
                    return jsonArray.toString();
                }
            }

        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
            return "{\"status\":\"001_01_505\", \"message\":\"" + e + "\"}";
        }
    }

    public String getUserName(String data) {
        String selectQuery = "SELECT name FROM daily_user WHERE username = ?";

        try (
                Connection conn = DriverManager.getConnection(jdbcURL, username, password);
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery)
        ) {
            selectStmt.setString(1, data);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                } else {
                    return "404";
                }
            }
        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
            return "001_01_505";
        }
    }

    public String getTodayTodoList(String userId) {
        String selectQuery = "SELECT todolist FROM daily_attendance " +
                "WHERE username = ? AND DATE(checkin) = ?";

        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE);

        try (
                Connection conn = DriverManager.getConnection(jdbcURL, username, password);
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery)
        ) {
            selectStmt.setString(1, userId);
            selectStmt.setString(2, todayStr);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    String todolist = rs.getString("todolist");
                    if (todolist == null || todolist.isEmpty()) {
                        return "{\"status\":\"404\", \"message\":\"Không có todolist cho ngày hôm nay\"}";
                    } else {
                        return todolist;
                    }
                } else {
                    return "{\"status\":\"404\", \"message\":\"Không có dữ liệu cho ngày hôm nay\"}";
                }
            }

        } catch (SQLException e) {
            function.logExceptionWithCode("001_01_505", e);
            return "{\"status\":\"500\", \"message\":\"Lỗi khi truy vấn cơ sở dữ liệu\"}";
        }
    }

}
