
import java.io.*;
//import static org.imgscalr.Scalr.*;
//import org.imgscalr.Scalr.*;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.atomic.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class P3 {
     
  private static final String EXCHANGE_NAME = "topic_logs";

  public static void main(String[] argv) throws Exception {
    
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("192.168.102.130");
    factory.setUsername("mqadmin");
    factory.setPassword("mqadmin");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    String queueName = channel.queueDeclare().getQueue();

    channel.queueBind(queueName, EXCHANGE_NAME, "tp2.images");
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
        
        //La chaine de caractère Base64 est transformée en objet de type BufferedImage
        BufferedImage buffer = base64StringToImg(message);
        //Ajustement du format de l'image pour l'agrandir et conversion du résultat en base64
        buffer = resizeImage(buffer, 100, 100);
        String base64Result = imgToBase64String(buffer, "png");
        //Ajustement du format de l'image pour la rapetisser et conversion du résultat en base64
        BufferedImage buffer2 = resizeImage(buffer, 50, 50);
        String base64Result2 = imgToBase64String(buffer2, "png");
        
        //Construire l'objet ResizeResponse et le sérializer en JSON avant de le passer à P4
        ObjectMapper objMapper = new ObjectMapper();
        ResizeResponse retour = new ResizeResponse();
        retour.setOrig(message);
        retour.setImg1(base64Result);
        retour.setImg2(base64Result);
        String jsonString = objMapper.writeValueAsString(retour);
        
        //On peut utiliser la même connexion, mais il faut créer un nouveau canal pour l'envoi
        Channel senderChannel = connection.createChannel();
        senderChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
        String routingKey = "tp2.save";
        //Envoi du message ainsi que la clé de routage à l'échangeur
        senderChannel.basicPublish(EXCHANGE_NAME, routingKey, null, jsonString.getBytes());
        System.out.println(" [x] Sent '" + routingKey + "':'" + base64Result + "'");
      }
    };
    channel.basicConsume(queueName, true, consumer);  
  }
  
    //https://stackoverflow.com/questions/12879540/image-resizing-in-java-to-reduce-image-size#12879764
    //https://www.htmlgoodies.com/beyond/java/create-high-quality-thumbnails-using-the-imgscalr-library.html
    //Méthode principale qui sert à modifier le format d'un objet de type BufferedImage
    private static BufferedImage resizeImage(BufferedImage img, int targetWidth, int targetHeight)
    {
        return Scalr.resize(  img, 
                        Scalr.Method.SPEED,
                        Mode.FIT_TO_WIDTH,
                        targetWidth,
                        targetHeight,
                        Scalr.OP_ANTIALIAS );
    } 
    
    //Transforme une chaine de caractère base64 en objet de type BufferedImage
    private static BufferedImage base64StringToImg(final String base64String) 
    {
        try 
        {
            return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
        } 
        catch (final IOException ioe) 
        {
            throw new UncheckedIOException(ioe);
        }
    }
    
    //Transforme un objet de type BufferedImage en une chaine de caractère base64
    private static String imgToBase64String(final RenderedImage img, final String formatName) 
    {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try 
        {
            ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));
            return os.toString(StandardCharsets.ISO_8859_1.name());
        } 
        catch (final IOException ioe) 
        {
            throw new UncheckedIOException(ioe);
        }
    }
}
