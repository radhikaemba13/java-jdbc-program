import java.sql.*;
import java.util.Scanner;

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
                System.out.println("\n--- HOTEL MANAGEMENT SYSTEM ---");
                System.out.println("1. Add Booking");
                System.out.println("2. View Bookings");
                System.out.println("3. Search Booking");
                System.out.println("4. Update Booking");
                System.out.println("5. Delete Booking");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        addBooking();
                        break;
                    case 2:
                        viewBookings();
                        break;
                    case 3:
                        searchBooking();
                        break;
                    case 4:
                        updateBooking();
                        break;
                    case 5:
                        deleteBooking();
                        break;
                    case 6:
                        System.out.println("Thank you!");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

            con.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void addBooking() throws SQLException {
        sc.nextLine();

        System.out.print("Enter customer name: ");
        String name = sc.nextLine();

        System.out.print("Enter room number: ");
        int room = sc.nextInt();

        System.out.print("Enter number of days: ");
        int days = sc.nextInt();

        System.out.print("Enter amount: ");
        double amount = sc.nextDouble();

        String sql = "INSERT INTO bookings "
                   + "(customer_name, room_no, days, amount) "
                   + "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, name);
        ps.setInt(2, room);
        ps.setInt(3, days);
        ps.setDouble(4, amount);

        ps.executeUpdate();

        System.out.println("Booking added successfully!");
        ps.close();
    }

    static void viewBookings() throws SQLException {
        String sql = "SELECT * FROM bookings";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        System.out.println("\nID\tName\tRoom\tDays\tAmount");

        while (rs.next()) {
            System.out.println(
                rs.getInt("id") + "\t" +
                rs.getString("customer_name") + "\t" +
                rs.getInt("room_no") + "\t" +
                rs.getInt("days") + "\t" +
                rs.getDouble("amount")
            );
        }

        rs.close();
        st.close();
    }

    static void searchBooking() throws SQLException {
        System.out.print("Enter booking ID: ");
        int id = sc.nextInt();

        String sql = "SELECT * FROM bookings WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("Customer: "
                + rs.getString("customer_name"));
            System.out.println("Room: "
                + rs.getInt("room_no"));
            System.out.println("Days: "
                + rs.getInt("days"));
            System.out.println("Amount: "
                + rs.getDouble("amount"));
        } else {
            System.out.println("Booking not found!");
        }

        rs.close();
        ps.close();
    }

    static void updateBooking() throws SQLException {
        System.out.print("Enter booking ID: ");
        int id = sc.nextInt();

        System.out.print("Enter new room number: ");
        int room = sc.nextInt();

        System.out.print("Enter new number of days: ");
        int days = sc.nextInt();

        System.out.print("Enter new amount: ");
        double amount = sc.nextDouble();

        String sql = "UPDATE bookings SET room_no=?, "
                   + "days=?, amount=? WHERE id=?";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, room);
        ps.setInt(2, days);
        ps.setDouble(3, amount);
        ps.setInt(4, id);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Booking updated successfully!");
        else
            System.out.println("Booking not found!");

        ps.close();
    }

    static void deleteBooking() throws SQLException {
        System.out.print("Enter booking ID: ");
        int id = sc.nextInt();

        String sql = "DELETE FROM bookings WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Booking deleted successfully!");
        else
            System.out.println("Booking not found!");

        ps.close();
    }
}