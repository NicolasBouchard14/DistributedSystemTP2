
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.net.*;
import com.rabbitmq.client.*;
import java.util.concurrent.atomic.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.*;

//https://stackoverflow.com/questions/26811924/spring-amqp-rabbitmq-3-3-5-access-refused-login-was-refused-using-authentica
public class P2 {

  public static void main(String[] argv) throws Exception {
    String EXCHANGE_NAME = "topic_logs";
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("192.168.102.128");
    /*factory.setUsername("guest");
    factory.setPassword("guest");
    factory.setPort(5672);
    factory.setVirtualHost("/");*/
		
    Connection connection = factory.newConnection();
    Channel receiverChannel = connection.createChannel();

    receiverChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
    String queueName = receiverChannel.queueDeclare().getQueue();
    
    receiverChannel.queueBind(queueName, EXCHANGE_NAME, "tp2.texte");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    
    AtomicReference<String> translatedText = new AtomicReference<String>();
    
    Consumer consumer = new DefaultConsumer(receiverChannel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
        
        ObjectMapper objMapper = new ObjectMapper();
        try
        {
            TranslationResponse transResponse = objMapper.readValue(translateToFrench(message), TranslationResponse.class);
            transResponse.setOrigText(message);
            
            translatedText.set(transResponse.getText()[0]);
            
            String jsonString = objMapper.writeValueAsString(transResponse); // permet de passer tout l'objet en json à P4
            
            if(translatedText.get() != null)
            {
                Channel senderChannel = connection.createChannel();
                senderChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
                String routingKey = "tp2.save";
                senderChannel.basicPublish(EXCHANGE_NAME, routingKey, null, jsonString.getBytes());
                System.out.println(" [x] Sent '" + routingKey + "':'" + translatedText + "'");
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
