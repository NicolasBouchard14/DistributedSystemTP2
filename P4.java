import com.rabbitmq.client.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class P4 {

	public static void main(String[] argv) throws Exception {

		String EXCHANGE_NAME = "topic_logs";
		String NOM_FILE_DATTENTE = "file_d_attente02";
		/*String nomUtilisateur = "guest"; // par defaut
		String motDePasse = "guest"; // par defaut
		int numeroPort = 5672; // par defaut
		String virtualHostName = "/"; */// par defaut
		String hostName = "192.168.102.128";

		// se connecter au broker RabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
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

			try {
				// Convert JSON string to Object
				TranslationResponse responseText = mapper.readValue(message, TranslationResponse.class);
				
				if(DatabaseHelper.InsertText(responseText.getOrigText(), responseText.getText()[0]))
				{
					System.out.println("ok");
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
