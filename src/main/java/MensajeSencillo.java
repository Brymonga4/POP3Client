import javax.mail.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MensajeSencillo {
    private String asunto;
    private String remitente;
    private Date fechaDeEnvio;
    private String cuerpo;

    private String contenidoCompleto;

    public MensajeSencillo(Message message) {
        try {
            this.asunto = message.getSubject();
            this.remitente = remitentesDeMensaje(message.getFrom());
            this.fechaDeEnvio = message.getSentDate();
            this.cuerpo = decodificarContenido(message.getContent());
            this.contenidoCompleto = construirContenidoCompleto();


        } catch (MessagingException|IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String decodificarContenido(Object content){

        StringBuilder contenidoMensaje= new StringBuilder();

        try {
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                // Iterar sobre todas las partes del mensaje
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);

                    // Si la parte es texto, imprimirlo
                    if (bodyPart.isMimeType("text/plain")) {
                        contenidoMensaje.append("Contenido: ").append(bodyPart.getContent()).append("\n");
                    }
                    //se podría añadir manejar más tipos de mimetype
                }
            } else {
                // Si el contenido no es multipart, simplemente imprimir el contenido
                contenidoMensaje.append("Contenido: ").append(content.toString());
            }
        } catch (IOException|MessagingException e) {
            throw new RuntimeException(e);
        }
        return contenidoMensaje.toString();
    }

    public String remitentesDeMensaje(Address[] remitentes){
        String remitente ="";
        if (remitentes.length > 0) {
            remitente = remitentes[0].toString();
        } else {
            remitente = "Remitente desconocido";
        }
        return remitente;
    }

    public String getContenidoCompleto() {
        return contenidoCompleto;
    }

    public String construirContenidoCompleto(){
        StringBuilder contenidoCompleto = new StringBuilder();

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
        String fechaFormateada = formatoFecha.format(this.fechaDeEnvio);

        contenidoCompleto.append(fechaFormateada).append("\n")
                         .append(this.asunto).append("\n")
                         .append(this.remitente).append("\n")
                         .append(this.cuerpo).append("\n");


        return contenidoCompleto.toString();
    }


}
