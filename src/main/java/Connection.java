import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import javax.mail.*;

public class Connection {
    private Message[] messages;
    public Connection() {
        try {
            Scanner sc= new Scanner (System.in);
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "pop3s");
            properties.put("mail.pop3s.host", "pop.gmail.com");
            properties.put("mail.pop3s.port", "995");
            properties.put("mail.pop3s.ssl.trust", "*"); // Confía en todos los certificados
            //Si se pone a "*", confío en todos los hosts.
            //Se crea un objeto session que contenga las propiedades para la conexión con el servidor mail
            Session session = Session.getDefaultInstance(properties);
            //Una vez tengamos la session, podemos guardar un objeto que represente la conexión con el servidor mail
            // Nos conectamos al servidor POP3s
            Store store = session.getStore("pop3s");
            store.connect("hitohitotadano7@gmail.com","wctztpcbrptsdjmn");
            //Importante ir a tu cuenta Gmail y activar la verificación 2FA. Ya que google quitó hace tiempo lo del acceso a aplicaciones poco seguras
            //Ahora hay que crear una password específica para la aplicación en la que lo vayas a usar
            //Una vez conectados al servidor mail, creamos un objeto carpeta, que representa la carpeta que contiene los emails que queremos leer
            //POP3 solo permite una única carpeta llamada "INBOX"
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            //Recuperamos los mensajes de la carpeta inbox
            messages = inbox.getMessages();
            //Los vamos a ordenar manualmente por fecha de enviado
            ordenarMensajesPorFecha(messages);
            if ( messages.length == 0)
                System.out.println("No hay mensajes, vaya... =(");
            else {
                int salir = -1;
                while (salir != 0) {
                    System.out.println("¿Qué opción desea?" + "\n" + "1 - Listado de mensajes" + "\n" + "2 - Visualización de mensajes" + "\n" + "0 - Salir/Cerrar Sesión");
                    salir = sc.nextInt();
                    switch (salir) {
                        case 1: //Listado de los mensajes ya ordenados
                            ordenarMensajesPorFecha(messages);
                            imprimirListadoMensajesDeInbox(messages);
                            break;
                        case 2:
                            System.out.println("¿Qué mensaje quiere visualizar?"); //Pedimos que nos de el "id" de mensaje que quiere descargar
                            int mensaje = sc.nextInt();
                            //Tuve un problema con gmail, y pasaba que cuando consultaba los mensajes del buzón una vez
                            // cuando lo ejecutaba una segunda vez, tenía que volver a activar un setting en la cuenta (POP is enables for all mail) porque por alguna razón se desmarcaba y dejaba de funcionar
                            imprimirContenidoMensaje(inbox, mensaje);
                            break;
                        case 0:
                            System.out.println("Que tenga un buen día =)");
                            break;
                        default:
                            System.out.println("Elija una opción válida");
                            break;
                    }
                }
            }
            //No hay que olvidar cerrar la carpeta y el objeto store cuando terminemos de leer los emails
            inbox.close(false);
            store.close();
        } catch (IOException | MessagingException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void ordenarMensajesPorFecha(Message[] messages){
        System.out.println("Ordenando Mensajes...");
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
        //Construyo el mensaje en el constructor de este objeto
        MensajeSencillo mensajeSencillo = new MensajeSencillo(message);
        System.out.println(mensajeSencillo.getContenidoCompleto());
        Scanner sc= new Scanner (System.in);
        System.out.println("¿Descargar Mensaje? Y/N");
        String s= sc.nextLine();
        if(s.equalsIgnoreCase("y")) {
            System.out.println("Por favor escriba el directorio donde quiere guardarlo");
            String directorio = sc.nextLine();
            saveTextContent(mensajeSencillo, directorio);
        }else if (s.equalsIgnoreCase("n")) {
            System.out.println("No ha querido descargar en mensaje =(");
        }
    }

    public  void saveTextContent(MensajeSencillo message, String rutaUsuario)  {
        File dir = new File(rutaUsuario);
        String rutaArchivo="";
        //solo si el directorio existe, lo usa, si no, no lo crea y usa Descargas del usuario
        if(!dir.exists()){
            System.out.println("Ese directorio no existe, se guadará en su carpeta de Descargas.");
            rutaArchivo = generaRutaLimpia(message, rutaUsuario);
            dir = new File(rutaArchivo);
        }
        System.out.println("------ Contenido completo del mensaje --------");
        System.out.println(message.getContenidoCompleto());
        try (FileWriter writer = new FileWriter(rutaArchivo, true)) {
            writer.append(message.getContenidoCompleto());
        } catch (IOException e) {
            System.out.println("Error en el formato");
        }
    }

    public String generaRutaLimpia(MensajeSencillo message, String rutaUsuario){
        File dir = new File(rutaUsuario);
        //Por defecto si el directorio no existe
        String dirPordefecto = System.getProperty("user.home") + "\\Downloads\\";
        String[] charInvalidos = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
        String fechaFormateada = formatoFecha.format(message.getFechaDeEnvio());
        String asunto = message.getAsunto().replace(" ", "_").toLowerCase();
        for ( String charInvalido: charInvalidos){
            asunto = asunto.replace(charInvalido, "");
        }
        String nombreDeArchivo = asunto+"_"+fechaFormateada+".txt";
        return dirPordefecto + nombreDeArchivo;
    }
}
