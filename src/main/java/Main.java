import javax.mail.MessagingException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            Connection connection = new Connection();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
