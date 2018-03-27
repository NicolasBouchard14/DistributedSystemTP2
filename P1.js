#!/usr/bin/env node

var amqp = require('amqplib/callback_api');

// lire le fichier json pour obtenir les donnees a envoyer a P2
var fs = require("fs");
var content = fs.readFileSync("tp2_input.json");
var jsonContent = JSON.parse(contents);

// parcourir l'objet json contenant un tableau de textes avec les images
for (var i=0; i<jsonContent.length; i++)
{
	// parcourir les paragraphes
	for (var j=0; j<jsonContent[i]['paragraphes'].length; j++)
	{
		amqp.connect('amqps://localhost', function(err, conn) {
		  conn.createChannel(function(err, ch) {
		    var ex = 'topic_logs';
		    var key = 'tp2.texte';
		    var msg = jsonContent[i]['paragraphes'][j];

		    ch.assertExchange(ex, 'topic', {durable: false});
		    ch.publish(ex, key, new Buffer(msg));
		    console.log(" [x] Sent %s:'%s'", key, msg);
		  });

		  setTimeout(function() { conn.close(); process.exit(0) }, 500);
		}); 
	}
	
	// parcourir les images (elles sont sous forme base64 string, p3 va les convertir en images pour faire le traitement)
	for (var k=0; k<jsonContent[i]['images'].length; k++)
	{
		amqp.connect('amqps://localhost', function(err, conn) {
		  conn.createChannel(function(err, ch) {
		    var ex = 'topic_logs';
		    var key = 'tp2.images';
		    var msg = jsonContent[i]['images'][k];

		    ch.assertExchange(ex, 'topic', {durable: false});
		    ch.publish(ex, key, new Buffer(msg));
		    console.log(" [x] Sent %s:'%s'", key, msg);
		  });

		  setTimeout(function() { conn.close(); process.exit(0) }, 500);
		}); 
	}   
}
