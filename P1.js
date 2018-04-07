#!/usr/bin/env node

var amqp = require('amqplib/callback_api');

// lire le fichier json pour obtenir les donnees a envoyer a P2
var fs = require("fs");
var content = fs.readFileSync("tp2_input.json");
var jsonContent = JSON.parse(content);

amqp.connect('amqp://localhost', function(err, conn) {
  conn.createChannel(function(err, ch) {
    var ex = 'topic_logs';
    var key = 'tp2.images';
    var key1 = 'tp2.texte';
		    
    // parcourir l'objet json contenant un tableau de textes avec les images
    for (var i=0; i<jsonContent.length; i++)
    {
        // parcourir les paragraphes
        for (var j=0; j<jsonContent[i].paragraphes.length; j++)
        {
            ch.assertExchange(ex, 'topic', {durable: false});
            ch.publish(ex, key1, new Buffer(jsonContent[i].paragraphes[j]));
            console.log(" [x] Sent %s:'%s'", key1, jsonContent[i].paragraphes[j]);
        }
	            
        // parcourir les images (elles sont sous forme base64 string, p3 va les convertir en images pour faire le traitement)
        for (var k=0; k<jsonContent[i].images.length; k++)
        {
            ch.assertExchange(ex, 'topic', {durable: false});
            ch.publish(ex, key, new Buffer(jsonContent[i].images[k]));
            console.log(" [x] Sent %s:'%s'", key, jsonContent[i].images[k]);
        }
    }
	    
 });
 setTimeout(function() { conn.close(); process.exit(0) }, 500);
}); 
