package Tools;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class OutlookInterface {

    public final String username;
    public final String password;

    public static OutlookInterface outlookInterface;


    public static void initOutlookInterface(final String user, final String password) {
        outlookInterface = new OutlookInterface(user, password);
    }

    private OutlookInterface(final String user, final String password) {
        this.username = user;
        this.password = password;
    }

    public void sendMail(final String to, final String object, final String content) {

        try {

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp-mail.outlook.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable","true");
            props.put("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                            return new javax.mail.PasswordAuthentication(username,password);
                        }
                    });

            String msgBody = "Sending email using JavaMail API...";

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(username, "Centrale Fitness"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to, object));
            msg.setSubject(content);
            msg.setText(msgBody);
            Transport.send(msg);
            System.out.println("Email sent successfully...");
        } catch (AddressException e) {
        } catch (MessagingException e) {
        } catch (UnsupportedEncodingException e) {
        }
    }


}
