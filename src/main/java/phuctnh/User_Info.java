package phuctnh;

public class User_Info {
    private String username;
    private String name;
    private String role;
    private String checkin;
    private String checkout;
    private String status;
    private String todolist;

    public User_Info(String username, String name, String role, String checkin, String checkout, String status, String todolist) {
        this.username = username;
        this.name = name;
        this.role = role;
        this.checkin = checkin;
        this.checkout = checkout;
        this.status = status;
        this.todolist = todolist;
    }

    public User_Info(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTodolist() {
        return todolist;
    }

    public void setTodolist(String todolist) {
        this.todolist = todolist;
    }
}
