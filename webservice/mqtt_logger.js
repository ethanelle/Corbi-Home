'use strict'
const mqtt = require('mqtt');
const fs = require('fs');

process.title = "mosquittoLogger";

const client = mqtt.connect('http://ec2-18-206-127-80.compute-1.amazonaws.com:1883');

client.on('connect', function() {
    client.subscribe('motion', function(err) {
        if(!err) {
            console.log("Connected... listening on topic 'motion'");
        } else {
            console.log("There was an error connecting, exiting.")
            client.end();
        }
    })
})

client.on('message', function (topic, message) {
    // message is Buffer
    if (topic === 'motion') {
        let detection = message.toString() + '\n';
        fs.appendFile('motion_list.txt', detection, function(err) {
            if(err) {
                console.log("There was an error writing the message to motion_list.txt");
            }
        });
    }
})
