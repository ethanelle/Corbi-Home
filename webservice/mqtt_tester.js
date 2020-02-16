'use strict'
const mqtt = require('mqtt');

const client = mqtt.connect('http://ec2-18-206-127-80.compute-1.amazonaws.com:1883');

client.on('connect', function () {
    client.subscribe('presence', function (err) {
      if (!err) {
        client.publish('motion', 'Hello this is the motion topic!')
        client.end();
      }
    })
  })