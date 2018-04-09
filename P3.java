
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

public class P3 {
     
  private static final String EXCHANGE_NAME = "topic_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("192.168.102.128");
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
        
        BufferedImage buffer = base64StringToImg(message);
        buffer = resizeImage(buffer, 100, 100);
        String base64Result = imgToBase64String(buffer, "png"); // acroteau: il faut png ou jpg et non BufferedImage, c'était le bug qui empêchait p3 de marcher
        
        System.out.println("\n\n\n"+base64Result);
        
        // TODO: créer une classe pour envoyer les resultats en json a P4
        
        /*Channel senderChannel = connection.createChannel();
        senderChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
        String routingKey = "tp2.save";
        senderChannel.basicPublish(EXCHANGE_NAME, routingKey, null, jsonString.getBytes());
        System.out.println(" [x] Sent '" + routingKey + "':'" + translatedText + "'");*/
        
      }
    };
    channel.basicConsume(queueName, true, consumer);  
  }
  
  //https://stackoverflow.com/questions/12879540/image-resizing-in-java-to-reduce-image-size#12879764
  //https://www.htmlgoodies.com/beyond/java/create-high-quality-thumbnails-using-the-imgscalr-library.html
    public static BufferedImage resizeImage(BufferedImage img, int targetWidth, int targetHeight)
    {
        return Scalr.resize(  img, 
                        Scalr.Method.SPEED,
                        Mode.FIT_TO_WIDTH,
                        targetWidth,
                        targetHeight,
                        Scalr.OP_ANTIALIAS );
    } 
    
    public static BufferedImage base64StringToImg(final String base64String) 
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
    
    public static String imgToBase64String(final RenderedImage img, final String formatName) 
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
