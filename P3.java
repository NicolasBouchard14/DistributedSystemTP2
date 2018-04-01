
import java.io.*;
import static org.imgscalr.Scalr.*;
import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import com.rabbitmq.client.*;
import java.io.IOException;

//https://stackoverflow.com/questions/26811924/spring-amqp-rabbitmq-3-3-5-access-refused-login-was-refused-using-authentica
public class P3 {
    
    private static String base64String = "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";
  
  private static final String EXCHANGE_NAME = "topic_logs";

  public static void main(String[] argv) throws Exception {
		String EXCHANGE_NAME = "echangeur_topic02";
		String NOM_FILE_DATTENTE = "file_d_attente02";
		//String hostName = "192.168.183.129";
		boolean autoAck = false;
		boolean durable = true;
		boolean passive = true; // a true, on suppose que l'echangeur existe deja
		boolean autoDelete = false; // ne pas supprimer l'echangeur lorsqu'aucun client n'est connecte
		boolean exclusive = false;

		// se connecter au broker RabbitMQ
		ConnectionFactory factory = new ConnectionFactory();

		// indiquer les parametres de la connexion
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setPort(5672);
		factory.setVirtualHost("/");
		factory.setHost("localhost");

		// creer une nouvelle connexion
		Connection connexion = factory.newConnection();

		// ouvrir un canal de communication avec le Broker pour l'envoi et la
		// reception de messages
		Channel canalDeCommunication = connexion.createChannel();
		canalDeCommunication.exchangeDeclare(EXCHANGE_NAME, "topic", passive, durable, autoDelete, null);
        
        //Clé de liaison et message
		String cleDeLiaison = "tp2.resultat";	
        String message = base64String;
		
		System.out.println(" -* En attente de messages ... pour arreter pressez CTRL+C");
        canalDeCommunication.basicPublish();
        
        channel.close();
        connection.close();
  }
  
  //https://stackoverflow.com/questions/12879540/image-resizing-in-java-to-reduce-image-size#12879764
  //https://www.htmlgoodies.com/beyond/java/create-high-quality-thumbnails-using-the-imgscalr-library.html
    public BufferedImage resizeImage(BufferedImage img, int targetWidth, int targetHeight)
    {
        return resize(  img, 
                        Method.SPEED,
                        Mode.FIT_TO_WIDTH,
                        150,
                        100,
                        OP_ANTIALIAS );
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
    
    /*Pour tester*/
    /*public static void main(String [] args)
    {
    
      String base64 = base64String;
      BufferedImage image = base64StringToImg(base64);
      
    }*/
}