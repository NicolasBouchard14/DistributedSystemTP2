
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.net.*;
import com.rabbitmq.client.*;
import java.util.concurrent.atomic.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.*;

public class P2 {

  public static void main(String[] argv) throws Exception {
      
    String EXCHANGE_NAME = "topic_logs";
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("192.168.102.129");
    factory.setUsername("mqadmin");
    factory.setPassword("mqadmin");
	
    //Connection au Broker RabbitMQ et création du canal de communication qui servira à la réception
    Connection connection = factory.newConnection();
    Channel receiverChannel = connection.createChannel();

    //Faire le lien avec l'échangeur topic, puis association à la bonne file grâce à la clé "tp2.texte"
    receiverChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
    String queueName = receiverChannel.queueDeclare().getQueue();
    receiverChannel.queueBind(queueName, EXCHANGE_NAME, "tp2.texte");
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        
    Consumer consumer = new DefaultConsumer(receiverChannel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
        
        ObjectMapper objMapper = new ObjectMapper();
        
        try
        {
            //Appelle la méthode pour la traduction, et désérialise l'objet retourné par cette méthode en un objet "TranslationResponse"
            TranslationResponse transResponse = objMapper.readValue(translateToFrench(message), TranslationResponse.class);
            //Ajout du message original à l'objet
            transResponse.setOrig(message);            
            //Sérialization de l'objet avant de la retourner à P4
            String jsonString = objMapper.writeValueAsString(transResponse); 
            
            //Envoi vers P4 si le texte traduit n'est pas null
            if(transResponse.getText()[0] != null || transResponse.getText()[0] != "")
            {
                //On peut utiliser la même connexion, mais il faut créer un nouveau canal pour l'envoi
                Channel senderChannel = connection.createChannel();
                senderChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
                String routingKey = "tp2.save";
                //Envoi du message ainsi que la clé de routage à l'échangeur
                senderChannel.basicPublish(EXCHANGE_NAME, routingKey, null, jsonString.getBytes());
                System.out.println(" [x] Sent '" + routingKey + "':'" + transResponse.getText()[0] + "'");
            }
        }
        catch (JsonParseException e) { e.printStackTrace();}
        catch (JsonMappingException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
      }
    };
    receiverChannel.basicConsume(queueName, true, consumer);  
  }
  
  
  
  //https://tech.yandex.com/translate/doc/dg/reference/translate-docpage/
  //https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
  private static String translateToFrench (String text)
  {
      String jsonResponse = "";
      
      try
      {
            String charset = "UTF-8";
            String param1 = "trnsl.1.1.20180320T020156Z.58154e111535edb1.fee24c55f8c8c4bbe7392d8aa9f89fd807f2322e";
            String param2 = text;
            String param3 = "fr";
            String query = String.format("key=%s&text=%s&lang=%s", 
                URLEncoder.encode(param1, charset), 
                URLEncoder.encode(param2, charset),
                URLEncoder.encode(param3, charset));
            URLConnection connection = new URL("https://translate.yandex.net/api/v1.5/tr.json/translate").openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            try (OutputStream output = connection.getOutputStream()) 
            {
               output.write(query.getBytes(charset));
            }
            catch (IOException e)
            {
                System.out.println("Exception: " + e.getMessage());
            }
            
            InputStream response = connection.getInputStream();
            
            java.util.Scanner s = new java.util.Scanner(response).useDelimiter("\\A");
            jsonResponse = s.hasNext() ? s.next() : "";
            
            
      }
      catch (Exception e)
      {
          System.out.println("Exception: " + e.getMessage());
      }
      return jsonResponse; 
  }
}
