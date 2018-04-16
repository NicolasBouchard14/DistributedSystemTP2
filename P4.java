import com.rabbitmq.client.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class P4 {
	public static void main(String[] argv) throws Exception {
		String EXCHANGE_NAME = "topic_logs";
		String NOM_FILE_DATTENTE = "file_d_attente02";
		String hostName = "192.168.102.128";

		// se connecter au broker RabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(hostName);
                factory.setUsername("mqadmin");
                factory.setPassword("mqadmin");
		
		Connection connexion = factory.newConnection();

		// ouvrir un canal de communication avec le Broker pour l'envoi et la
		// reception de messages
		Channel receiverChannel = connexion.createChannel();
		receiverChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
		
		// recuperer le nom d file d'attente associee a la clé de liaison 
		String queueName = receiverChannel.queueDeclare().getQueue();
		receiverChannel.queueBind(queueName, EXCHANGE_NAME, "tp2.save");

		System.out.println(" [*] En attente de messages ... pour arreter pressez CTRL+C");

		Consumer consumer = new DefaultConsumer(receiverChannel) {
		      @Override
		      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		          throws IOException {
		        String message = new String(body, "UTF-8");
		        
		        System.out.println(" [x] Message recu: '" + message + "', cle de routage: '"+envelope.getRoutingKey()+" '");
		        
		        //https://www.mkyong.com/java/how-to-convert-java-object-to-from-json-jackson/
		        ObjectMapper mapper = new ObjectMapper();
		        SimpleModule module = new SimpleModule();
    			module.addDeserializer(Response.class, new ResponseDeserializer());
    			mapper.registerModule(module);

			try {
				// Convert JSON string to Object
				Response responseText = mapper.readValue(message, Response.class);
				
				if(responseText instanceof TranslationResponse)
				{
					if(DatabaseHelper.InsertText(((TranslationResponse)responseText).getOrig(), ((TranslationResponse)responseText).getText()[0]))
					{
						System.out.println("ok");
					}
				}
				else if(responseText instanceof ResizeResponse)
				{
					String[] saveImgs = {((ResizeResponse)responseText).getOrig(), ((ResizeResponse)responseText).getImg1(), ((ResizeResponse)responseText).getImg2()};
				
					if(DatabaseHelper.InsertImage(saveImgs))
					{
						System.out.println("ok");
					}
				}

			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			}
		        
		    }
		};
		receiverChannel.basicConsume(queueName, true, consumer);
	}
}
