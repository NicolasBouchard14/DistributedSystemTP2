
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.net.*;

import java.io.*;

//https://stackoverflow.com/questions/26811924/spring-amqp-rabbitmq-3-3-5-access-refused-login-was-refused-using-authentica
public class P2 {
  /*private static final String EXCHANGE_NAME = "topic_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("192.168.142.131");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    String queueName = channel.queueDeclare().getQueue();

    if (argv.length < 1) {
      System.err.println("Usage: ReceiveLogsTopic [binding_key]...");
      System.exit(1);
    }

    for (String bindingKey : argv) {
      channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
    }

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
      }
    };
    channel.basicConsume(queueName, true, consumer);
  }*/
  
  //https://tech.yandex.com/translate/doc/dg/reference/translate-docpage/
  //https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
  public static String translateToFrench (String text)
  {
      String frenchText = "";
      
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
            frenchText = s.hasNext() ? s.next() : "";
            
            
      }
      catch (Exception e)
      {
          System.out.println("Exception: " + e.getMessage());
      }
      return frenchText; 
  }
  
  public static void main(String [] args)
  {

      String text = "Where is Charlie?";
      System.out.println(text);
      String frenchText = translateToFrench(text);
      System.out.println(frenchText);
  }
}