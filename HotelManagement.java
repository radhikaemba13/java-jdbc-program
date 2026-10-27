import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HotelManagement {

    static final String URL =
        "jdbc:mysql://localhost:3306/hotel_db";
    static final String USER = "root";
    static final String PASSWORD = "Shanks@2007";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                URL, USER, PASSWORD);

            System.out.println("Database Connected Successfully!");

            int choice;

            do {
                System.out.println("\n===== HOTEL MANAGEMENT SYSTEM =====");
                System.out.println("1. Add Room");
                System.out.println("2. View Rooms");
                System.out.println("3. Check Room Availability");
                System.out.println("4. Book Room");
                System.out.println("5. View Bookings");
                System.out.println("6. Cancel Booking");
                System.out.println("7. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        addRoom();
                        break;

                    case 2:
                        viewRooms();
                        break;

                    case 3:
                        checkAvailability();
                        break;

                    case 4:
                        bookRoom();
                        break;

                    case 5:
                        viewBookings();
                        break;

                    case 6:
                        cancelBooking();
                        break;

                    case 7:
                        System.out.println("Thank you!");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 7);

            con.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    
    static void addRoom() throws SQLException {

        System.out.print("Enter room number: ");
        int roomNo = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter room type: ");
        String type = sc.nextLine();

        System.out.print("Enter tariff per night: ");
        double tariff = sc.nextDouble();

        if (tariff <= 0) {
            System.out.println("Tariff must be greater than zero.");
            return;
        }

        String sql =
            "INSERT INTO rooms (room_no, room_type, tariff) VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomNo);
            ps.setString(2, type);
            ps.setDouble(3, tariff);

            ps.executeUpdate();

            System.out.println("Room added successfully!");
        }
    }

    
    static void viewRooms() throws SQLException {

        String sql = "SELECT * FROM rooms ORDER BY room_no";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\nROOM DETAILS");
            System.out.println("Room No\tType\t\tTariff");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("room_no") + "\t" +
                    rs.getString("room_type") + "\t\t" +
                    rs.getDouble("tariff"));
            }
        }
    }

    
    static LocalDate[] getDates() {

        sc.nextLine();

        try {
            System.out.print("Enter check-in date (YYYY-MM-DD): ");
            LocalDate checkIn = LocalDate.parse(sc.nextLine());

            System.out.print("Enter check-out date (YYYY-MM-DD): ");
            LocalDate checkOut = LocalDate.parse(sc.nextLine());

            if (!checkOut.isAfter(checkIn)) {
                System.out.println(
                    "Check-out must be after check-in.");
                return null;
            }

            if (checkIn.isBefore(LocalDate.now())) {
                System.out.println(
                    "Check-in date cannot be in the past.");
                return null;
            }

            return new LocalDate[]{checkIn, checkOut};

        } catch (Exception e) {
            System.out.println("Invalid date format!");
            return null;
        }
    }

    
    static boolean isAvailable(
        int roomNo, LocalDate checkIn, LocalDate checkOut)
        throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM bookings " +
            "WHERE room_no = ? AND check_in < ? AND check_out > ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomNo);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    
    static void checkAvailability() throws SQLException {

        LocalDate[] dates = getDates();

        if (dates == null) {
            return;
        }

        String sql = "SELECT * FROM rooms ORDER BY room_no";
        boolean found = false;

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\nAVAILABLE ROOMS");

            while (rs.next()) {

                int roomNo = rs.getInt("room_no");

                if (isAvailable(roomNo, dates[0], dates[1])) {

                    System.out.println(
                        "Room: " + roomNo +
                        ", Type: " + rs.getString("room_type") +
                        ", Tariff: Rs. " + rs.getDouble("tariff"));

                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("No rooms available for these dates.");
        }
    }

    
    static void bookRoom() throws SQLException {

        LocalDate[] dates = getDates();

        if (dates == null) {
            return;
        }

        LocalDate checkIn = dates[0];
        LocalDate checkOut = dates[1];

        System.out.print("Enter room number: ");
        int roomNo = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter customer name: ");
        String name = sc.nextLine();

        // Retrieve room tariff
        String roomSql =
            "SELECT tariff FROM rooms WHERE room_no = ?";

        double tariff;

        try (PreparedStatement ps = con.prepareStatement(roomSql)) {

            ps.setInt(1, roomNo);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    System.out.println("Room not found!");
                    return;
                }

                tariff = rs.getDouble("tariff");
            }
        }

        // Check availability
        if (!isAvailable(roomNo, checkIn, checkOut)) {
            System.out.println("Room is already booked for these dates!");
            return;
        }

        
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double amount = nights * tariff;

        String sql =
            "INSERT INTO bookings " +
            "(customer_name, room_no, check_in, check_out, nights, amount) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, roomNo);
            ps.setDate(3, Date.valueOf(checkIn));
            ps.setDate(4, Date.valueOf(checkOut));
            ps.setLong(5, nights);
            ps.setDouble(6, amount);

            ps.executeUpdate();

            System.out.println("\nBOOKING SUCCESSFUL!");
            System.out.println("Customer: " + name);
            System.out.println("Room Number: " + roomNo);
            System.out.println("Check-in: " + checkIn);
            System.out.println("Check-out: " + checkOut);
            System.out.println("Number of nights: " + nights);
            System.out.println("Tariff per night: Rs. " + tariff);
            System.out.println("Total cost: Rs. " + amount);
        }
    }

    
    static void viewBookings() throws SQLException {

        String sql = "SELECT * FROM bookings ORDER BY id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\nBOOKING DETAILS");
            System.out.println(
                "ID\tCustomer\tRoom\tCheck-in\tCheck-out\tNights\tAmount");

            while (rs.next()) {

                System.out.println(
                    rs.getInt("id") + "\t" +
                    rs.getString("customer_name") + "\t" +
                    rs.getInt("room_no") + "\t" +
                    rs.getDate("check_in") + "\t" +
                    rs.getDate("check_out") + "\t" +
                    rs.getInt("nights") + "\t" +
                    rs.getDouble("amount"));
            }
        }
    }

    
    static void cancelBooking() throws SQLException {

        System.out.print("Enter booking ID to cancel: ");
        int id = sc.nextInt();

        String sql = "DELETE FROM bookings WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Booking cancelled successfully!");
            } else {
                System.out.println("Booking not found!");
            }
        }
    }
}