import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import javax.mail.*;

public class Connection {

    private Message[] messages;
    public Connection() throws MessagingException {

        Properties properties = new Properties();
        properties.put("mail.store.protocol", "pop3s");
        properties.put("mail.pop3s.host", "pop.gmail.com");
        properties.put("mail.pop3s.port", "995");
        properties.put("mail.pop3s.ssl.trust", "*"); // Confía en todos los certificados
        //Si se pone a "*", confío en todos los hosts.

        //create a session object that contains the connection properties for the mail server.
        Session session = Session.getDefaultInstance(properties);
        //session.setDebug(true);

        //we are creating a session object for a POP3 server
        // Once we have the session object, we can create a store object that represents the connection to the mail server.

        // Connect to the POP3 server

        Store store = session.getStore("pop3s");
        store.connect("hitohitotadano7@gmail.com","wctztpcbrptsdjmn");

        //we are connecting to the mail server using the username and password of the email account that we want to read emails from.
        //Once we have connected to the mail server, we can create a folder object that represents the folder containing the emails that we want to read.
        //POP3 supports only a single folder named "INBOX".
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // Get the messages from the inbox folder
        messages = inbox.getMessages();
        ordenarMensajesPorFecha(messages);

        if ( messages.length == 0)
            System.out.println("nada?");

        try {
            //imprimirMensajes();
            imprimirListadoMensajesDeInbox(messages);
            Scanner sc= new Scanner (System.in);
            System.out.println("Elija un correo que quiere descargar:");
            int n= sc.nextInt();
            imprimirContenidoMensaje(inbox, n);

            int n2= sc.nextInt(); //Bloqueo para que no termine

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Finally, don’t forget to close the folder and store objects when you are done reading the emails
        inbox.close(false);
        store.close();
    }

    public void imprimirMensajes() throws MessagingException, IOException {
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            // Print message subject
            System.out.println("Subject: " + message.getSubject());
            System.out.println("------------------------------------------");
            //FROM
            System.out.println("From: " + message.getFrom()[0]);
            System.out.println("------------------------------------------");
            // Print message subject
            System.out.println("Flags: " + message.getFlags());
            System.out.println("------------------------------------------");
            // Print message number
            System.out.println("Msg number: " + message.getMessageNumber());
            System.out.println("------------------------------------------");
            // Print message number
            System.out.println("Date sent: " + message.getSentDate());
            System.out.println("------------------------------------------");
            // Check if the content is a multipart message
            if (message.getContent() instanceof Multipart) {
                Multipart multipart = (Multipart) message.getContent();

                // Iterate over all parts of the message
                for (int j = 0; j < multipart.getCount(); j++) {
                    BodyPart bodyPart = multipart.getBodyPart(j);

                    // Get the content type of the part
                    String contentType = bodyPart.getContentType();
                    System.out.println("Content Type: " + contentType);

                    // Check if the part is plain text
                    if (contentType.toLowerCase().contains("text/plain")) {
                        System.out.println("Text: " + (String) bodyPart.getContent());
                    } else if (contentType.toLowerCase().contains("text/html")) {
                        // The part is HTML, handle accordingly
                        // For example, you can use Jsoup to parse the HTML
                        String html = (String) bodyPart.getContent();
                        // Extract text from HTML if necessary
                        System.out.println("HTML: " + html);
                    }
                    // Handle other content types (attachments, etc.) as necessary
                }
            } else {
                // If the content is plain text or HTML, print it directly
                System.out.println("Text: " + message.getContent().toString());
            }

            System.out.println();
        }
    }

    public void ordenarMensajesPorFecha(Message[] messages){

        System.out.println("Ordenando Mensajes...");
        // Comparator<Message>
        Arrays.sort(messages, (m1, m2) -> {
            try {
                if (m1.getSentDate() == null || m2.getSentDate() == null) {
                    return 0; //Si alguna de las fechas es nula
                }
                return m2.getSentDate().compareTo(m1.getSentDate());
            } catch (MessagingException e) {
                e.printStackTrace();
                return 0;
            }
        });

    }

    public void imprimirListadoMensajesDeInbox(Message[] messages)  throws MessagingException, IOException {

        for (Message message : messages) {
            try {
                String asunto = message.getSubject(); // El asunto
                Address[] remitentes = message.getFrom(); // Los remitentes
                int messageIdNumber = message.getMessageNumber(); // Identificador de mensaje que podrá variar según sesiones
                Date fechaDeEnvio = message.getSentDate();


                System.out.println("Mensaje #" + messageIdNumber);
                System.out.println("Fecha: "+ fechaDeEnvio);
                System.out.println("Asunto: " + asunto);
                if (remitentes.length > 0) {
                    System.out.println("Remitente: " + remitentes[0].toString());
                } else {
                    System.out.println("Remitente: Desconocido");
                }
                System.out.println("------------------------------------------");

            } catch (MessagingException e) {
                System.err.println("Error al obtener información del mensaje: " + e.getMessage());
            }
        }
    }

    public void imprimirContenidoMensaje(Folder folder, int idMensaje) throws MessagingException, IOException {


        Message message = folder.getMessage(idMensaje);
        MensajeSencillo mensajeSencillo = new MensajeSencillo(message);

        String asunto = message.getSubject(); // El asunto
        Address[] remitentes = message.getFrom();

        System.out.println("Asunto: " + asunto);
        if (remitentes.length > 0) {
            System.out.println("Remitente: " + remitentes[0].toString());
        } else {
            System.out.println("Remitente: Desconocido");
        }

        //Si el contenido del mensaje, está compuesto de multipartes
        if (message.getContent() instanceof Multipart) {
            Multipart multipart = (Multipart) message.getContent();

            // Iterar sobre todas las partes del mensaje
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                // Si la parte es texto, imprimirlo
                if (bodyPart.isMimeType("text/plain")) {
                    System.out.println("Contenido: " + bodyPart.getContent());
                }
                // Puedes agregar más condiciones para manejar otros tipos de contenido
            }
        } else {
            // Si el contenido no es multipart, simplemente imprimir el contenido
            System.out.println("Contenido: " + message.getContent().toString());
        }

        Scanner sc= new Scanner (System.in);
        System.out.println("¿Descargar Mensaje? Y/N");
        String s= sc.nextLine();
        if(s.equalsIgnoreCase("y")) {
            System.out.println("Por favor escriba el directorio donde quiere guardarlo");
            String directorio = sc.nextLine();
            saveTextContent(mensajeSencillo, directorio);
        }else if (s.equalsIgnoreCase("n")) {
            System.out.println("Nada");
        }

    }

    public  void saveTextContent(MensajeSencillo message, String rutaUsuario)  {

        String dirPordefecto = System.getProperty("user.home") + "\\Downloads\\";

        String[] charInvalidos = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
        String fechaFormateada = formatoFecha.format(message.getFechaDeEnvio());

        String asunto = message.getAsunto().replace(" ", "_").toLowerCase();
        for ( String charInvalido: charInvalidos){
            asunto = asunto.replace(charInvalido, "");
        }

        String nombreDeArchivo = asunto+"_"+fechaFormateada+".txt";

        System.out.println(nombreDeArchivo);

        String rutaArchivo = dirPordefecto + nombreDeArchivo;
        File dir = new File(rutaArchivo);

        System.out.println("------ Contenido completo del mensaje --------");
        System.out.println(message.getContenidoCompleto());

        try (FileWriter writer = new FileWriter(rutaArchivo, true)) {
            writer.append(message.getContenidoCompleto());
        } catch (IOException e) {
            System.out.println("Error en el formato");
        }
    }


}
