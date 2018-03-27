package topic;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public class P4 {

	public static void main(String[] argv) throws Exception {

		String EXCHANGE_NAME = "echangeur_topic02";
		String NOM_FILE_DATTENTE = "file_d_attente02";
		String nomUtilisateur = "guest"; // par defaut
		String motDePasse = "guest"; // par defaut
		int numeroPort = 5672; // par defaut
		String virtualHostName = "/"; // par defaut
		String hostName = "localhost";
		//String hostName = "192.168.183.129";
		boolean autoAck = false;
		boolean durable = true;
		boolean passive = true; // a true, on suppose que l'echangeur existe deja
		boolean autoDelete = false; // ne pas supprimer l'echangeur lorsqu'aucun client n'est connecte
		boolean exclusive = false;

		// se connecter au broker RabbitMQ
		ConnectionFactory factory = new ConnectionFactory();

		// indiquer les parametres de la connexion
		factory.setUsername(nomUtilisateur);
		factory.setPassword(motDePasse);
		factory.setPort(numeroPort);
		factory.setVirtualHost(virtualHostName);
		factory.setHost(hostName);

		// creer une nouvelle connexion
		Connection connexion = factory.newConnection();

		// ouvrir un canal de communication avec le Broker pour l'envoi et la
		// reception de messages
		Channel canalDeCommunication = connexion.createChannel();
		canalDeCommunication.exchangeDeclare(EXCHANGE_NAME, "topic", passive, durable, autoDelete, null);
		
		// recuperer le nom d file d'attente associee a  
		//String nomFileDAttente = canalDeCommunication.queueDeclare().getQueue();
		canalDeCommunication.queueDeclare(NOM_FILE_DATTENTE , durable, exclusive, autoDelete, null);

		
		//String cleDeLiaison = "log.message";
		String cleDeLiaison = "tp2.resultat";
		
		// lier la file d'attente a l'echangeur
		canalDeCommunication.queueBind(NOM_FILE_DATTENTE, EXCHANGE_NAME, cleDeLiaison);
		
		// Ne pas delivrer a un consommateur plus qu'un message a la fois: Fair dispatch
		canalDeCommunication.basicQos(1);
		
		System.out.println(" -* En attente de messages ... pour arreter pressez CTRL+C");

		Consumer consumer = new DefaultConsumer(canalDeCommunication) {
		      @Override
		      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		          throws IOException {
		        String message = new String(body, "UTF-8");
		        System.out.println(" - Message recu: '" + message + "', cle de routage: '"+envelope.getRoutingKey()+" '");
		        
		        canalDeCommunication.basicAck(envelope.getDeliveryTag(), false);
		      }
		    };
		canalDeCommunication.basicConsume(NOM_FILE_DATTENTE, autoAck, consumer);		

	}
}
